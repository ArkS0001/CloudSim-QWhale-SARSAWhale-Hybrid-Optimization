import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Define a class to represent a state in the SARSA algorithm
class State {
    private int cloudletId;
    private int vmId;

    public State(int cloudletId, int vmId) {
        this.cloudletId = cloudletId;
        this.vmId = vmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return cloudletId == state.cloudletId && vmId == state.vmId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cloudletId, vmId);
    }
}

// Define a class to represent an action in the SARSA algorithm
class Action {
    private int vmId;

    public Action(int vmId) {
        this.vmId = vmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        return vmId == action.vmId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vmId);
    }
}

// Define SARSA processor class
class SARSAProcessor {
    private double alpha; // Learning rate
    private double gamma; // Discount factor
    private double epsilon; // Epsilon-greedy exploration factor
    private Map<State, Map<Action, Double>> qTable;

    public SARSAProcessor(double alpha, double gamma, double epsilon) {
        this.alpha = alpha;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.qTable = new HashMap<>();
    }

    // Method to select action using epsilon-greedy policy
    public Action selectAction(State state) {
        if (Math.random() < epsilon) {
            // Explore: Choose a random action
            // For simplicity, we'll choose a random VM as action
            int randomVmId = (int) (Math.random() * 10);
            return new Action(randomVmId);
        } else {
            // Exploit: Choose the action with the highest Q-value
            Map<Action, Double> actionValues = qTable.get(state);
            if (actionValues == null || actionValues.isEmpty()) {
                // If no Q-values for the state, explore
                return selectAction(state);
            }
            Action maxAction = null;
            double maxQValue = Double.NEGATIVE_INFINITY;
            for (Action action : actionValues.keySet()) {
                double qValue = actionValues.get(action);
                if (qValue > maxQValue) {
                    maxQValue = qValue;
                    maxAction = action;
                }
            }
            return maxAction;
        }
    }

    // Method to update Q-value based on SARSA update rule
    public void updateQValue(State currentState, Action selectedAction, State newState, Action newAction, double reward) {
        double oldQValue = qTable.getOrDefault(currentState, new HashMap<>()).getOrDefault(selectedAction, 0.0);

        // Get the Q-value of the new action in the new state
        double newQValue = qTable.getOrDefault(newState, new HashMap<>()).getOrDefault(newAction, 0.0);

        // Calculate Q-value based on SARSA update rule
        double updatedQValue = oldQValue + alpha * (reward + gamma * newQValue - oldQValue);

        // Update Q-value in Q-table
        qTable.computeIfAbsent(currentState, k -> new HashMap<>()).put(selectedAction, updatedQValue);
    }

    // Method to get the maximum Q-value for a state
    private double getMaxQValue(State state) {
        Map<Action, Double> actionValues = qTable.getOrDefault(state, new HashMap<>());
        if (actionValues.isEmpty()) {
            return 0.0; // If no Q-values for the state, return 0
        }
        return actionValues.values().stream().max(Double::compare).orElse(0.0);
    }

    public Map<State, Map<Action, Double>> getQTable() {
        return qTable;
    }
}

public class SARSA_CloudletScheduling {
    private static final int CLOUDLET_N = 1000;
    private static final Random R = new Random(0);
    private static final int NUM_USER = 1;

    public static void main(String[] args) throws Exception {
        Log.printLine("Starting...");

        CloudSim.init(NUM_USER, Calendar.getInstance(), false);

        Datacenter datacenter0 = createDatacenter("Datacenter0", Type.LOW);
        Datacenter datacenter1 = createDatacenter("Datacenter1", Type.MEDIUM);
        Datacenter datacenter2 = createDatacenter("Datacenter2", Type.HIGH);

        DatacenterBroker broker = new DatacenterBroker("Broker");
        int brokerId = broker.getId();

                List<Vm> vmList = createVms(brokerId);
        broker.submitVmList(vmList);

        List<Cloudlet> cloudletList = createCloudlets(brokerId);
        broker.submitCloudletList(cloudletList);

        SARSAProcessor sarsaProcessor = new SARSAProcessor(0.1, 0.9, 0.1);

        for (int i = 0; i < 3000; i++) {
            for (Cloudlet cloudlet : cloudletList) {
                State currentState = getStateRepresentation(cloudlet, vmList);
                Action selectedAction = sarsaProcessor.selectAction(currentState);
                executeAction(selectedAction, cloudlet, vmList);
                State newState = getStateRepresentation(cloudlet, vmList);
                Action newAction = sarsaProcessor.selectAction(newState);
                double reward = calculateReward(currentState, newState);
                sarsaProcessor.updateQValue(currentState, selectedAction, newState, newAction, reward);
            }
        }

        printQTable(sarsaProcessor);

        Log.printLine("========== START ==========");
        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        List<Cloudlet> newList = broker.getCloudletReceivedList();
        printCloudletList(newList);
    }

    private static void printQTable(SARSAProcessor sarsaProcessor) {
        Map<State, Map<Action, Double>> qTable = sarsaProcessor.getQTable();
        System.out.println("Q-table:");
        for (State state : qTable.keySet()) {
            System.out.println("State: " + state);
            Map<Action, Double> actionValues = qTable.get(state);
            for (Action action : actionValues.keySet()) {
                double qValue = actionValues.get(action);
                System.out.println("  Action: " + action.vmId + ", Q-value: " + qValue);
            }
        }
    }

    private static State getStateRepresentation(Cloudlet cloudlet, List<Vm> vmList) {
        int cloudletId = cloudlet.getCloudletId();
        int vmId = cloudlet.getVmId();
        return new State(cloudletId, vmId);
    }

    private static double calculateReward(State currentState, State newState) {
        // Placeholder: Assume completion time is a random value between 0 and 1000
        double completionTimeOldState = getCompletionTimeOfState(currentState);
        double completionTimeNewState = getCompletionTimeOfState(newState);

        // If completion time improves, provide positive reward; otherwise, negative reward
        if (completionTimeNewState < completionTimeOldState) {
            return 1.0; // Positive reward for improvement
        } else if (completionTimeNewState > completionTimeOldState) {
            return -1.0; // Negative reward for degradation
        } else {
            return 0.0; // No change in completion time, neutral reward
        }
    }

    private static double getCompletionTimeOfState(State state) {
        // Placeholder: Assume completion time is a random value between 0 and 1000
        return Math.random() * 1000;
    }

    private static void executeAction(Action action, Cloudlet cloudlet, List<Vm> vmList) {
        // Assign cloudlet to the VM corresponding to the selected action
        int vmId = action.vmId;
        Vm selectedVm = vmList.get(vmId);
        cloudlet.setVmId(selectedVm.getId());
    }

    // The remaining methods for creating datacenter, VMs, and cloudlets remain unchanged...
}
