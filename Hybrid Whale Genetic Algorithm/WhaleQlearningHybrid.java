package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;

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

import datacenter.Constants;
import datacenter.Scheduler;
import datacenter.Type;
import woa.WOAScheduler;

// Define a class to represent a state in the Q-learning algorithm
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

// Define a class to represent an action in the Q-learning algorithm
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
    
    public int getVmId() {
        return vmId;
    }
}

// Define Q-learning processor class
class QLearningProcessor {
    private double alpha; // Learning rate
    private double gamma; // Discount factor
    private double epsilon; // Epsilon-greedy exploration factor
    private Map<State, Map<Action, Double>> qTable;

    public QLearningProcessor(double alpha, double gamma, double epsilon) {
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

    // Method to update Q-value based on Q-learning update rule
    public void updateQValue(State currentState, Action selectedAction, State newState, double reward) {
        double oldQValue = qTable.getOrDefault(currentState, new HashMap<>()).getOrDefault(selectedAction, 0.0);

        // Calculate Q-value based on Q-learning update rule
        double newQValue = oldQValue + alpha * (reward + gamma * getMaxQValue(newState) - oldQValue);

        // Update Q-value in Q-table
        qTable.computeIfAbsent(currentState, k -> new HashMap<>()).put(selectedAction, newQValue);
    }

    // Method to get the maximum Q-value for a state
    private double getMaxQValue(State state) {
        Map<Action, Double> actionValues = qTable.getOrDefault(state, new HashMap<>());
        if (actionValues.isEmpty()) {
            return 0.0; // If no Q-values for the state, return 0
        }
        return actionValues.values().stream().max(Double::compare).orElse(0.0);
    }
}

public class WhaleQLearningHybrid {

    private static final int CLOUDLET_N = 500;
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

        QLearningProcessor qLearningProcessor = new QLearningProcessor(0.1, 0.9, 0.1);

        for (int i = 0; i < 1000; i++) {
        	for (Cloudlet cloudlet : cloudletList) {
        	    State currentState = getStateRepresentation(cloudlet, vmList);
        	    Action selectedAction = qLearningProcessor.selectAction(currentState);
        	    executeAction(selectedAction, cloudlet, vmList);
        	    State newState = getStateRepresentation(cloudlet, vmList);
        	    double reward = calculateReward(currentState, newState);
        	    qLearningProcessor.updateQValue(currentState, selectedAction, newState, reward);
        	}

        }

        Scheduler scheduler = new WOAScheduler(cloudletList, vmList);
		
		scheduler.schedule();

        Log.printLine("========== START ==========");
        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        List<Cloudlet> newList = broker.getCloudletReceivedList();
        printCloudletList(newList);
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
        int vmId = action.getVmId();
        Vm selectedVm = vmList.get(vmId);
        cloudlet.setVmId(selectedVm.getId());
    }

    private static Datacenter createDatacenter(String name, Type type) throws Exception {
		int ram, bw, mips;
		long storage;
		double costPerSec;

		switch (type) {
			case LOW:
				ram = Constants.RAM * Constants.L_VM_N;
				bw = Constants.BW * Constants.L_VM_N;
				mips = Constants.L_MIPS * Constants.L_VM_N;
				storage = Constants.STORAGE * Constants.L_VM_N;
				costPerSec = Constants.L_PRICE;
				break;
			case MEDIUM:
				ram = Constants.RAM * Constants.M_VM_N;
				bw = Constants.BW * Constants.M_VM_N;
				mips = Constants.M_MIPS * Constants.M_VM_N;
				storage = Constants.STORAGE * Constants.M_VM_N;
				costPerSec = Constants.M_PRICE;
				break;
			case HIGH:
				ram = Constants.RAM * Constants.H_VM_N;
				bw = Constants.BW * Constants.H_VM_N;
				mips = Constants.H_MIPS * Constants.H_VM_N;
				storage = Constants.STORAGE * Constants.H_VM_N;
				costPerSec = Constants.H_PRICE;
				break;
			default:
				throw new Exception("Invalid datacenter type");
		}


		// 1. We need to create a list to store our machine
		List<Host> hostList = new ArrayList<>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		List<Pe> peList = new ArrayList<>();

		// 3. Create PEs and add these into a list.
		// To simplify the model, it will only have one core.
		peList.add(new Pe(0, new PeProvisionerSimple(mips)));

		// physical machine
		hostList.add(
				new Host(
						0,
						new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw),
						storage,
						peList,
						new VmSchedulerTimeShared(peList)
				)
		);

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this resource
		double costPerGB = 0.1; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<>(); // we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, costPerSec, costPerMem,
				costPerStorage, costPerGB);

		// 6. Finally, we need to create a PowerDatacenter object.
		return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
	}

	private static List<Vm> createVms(int userId) {
		List<Vm> vmList = new ArrayList<>();

		int vmId = 0;
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name

		for (int i = 0; i < Constants.L_VM_N; i++) {
			vmList.add(new Vm(vmId++, userId, Constants.L_MIPS, pesNumber, Constants.RAM, Constants.BW, Constants.IMAGE_SIZE, vmm, new CloudletSchedulerSpaceShared()));
		}

		for (int i = 0; i < Constants.M_VM_N; i++) {
			vmList.add(new Vm(vmId++, userId, Constants.M_MIPS, pesNumber, Constants.RAM, Constants.BW, Constants.IMAGE_SIZE, vmm, new CloudletSchedulerSpaceShared()));
		}

		for (int i = 0; i < Constants.H_VM_N; i++) {
			vmList.add(new Vm(vmId++, userId, Constants.H_MIPS, pesNumber, Constants.RAM, Constants.BW, Constants.IMAGE_SIZE, vmm, new CloudletSchedulerSpaceShared()));
		}
		return vmList;
	}

	private static List<Cloudlet> createCloudlets(int userId) {
		List<Cloudlet> cloudletList = new ArrayList<>();
		int id = 0;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		for (int i = 0; i < CLOUDLET_N; i++) {
			long length = R.nextInt(40000) + 10000;
			long fileSize = R.nextInt(190) + 10;
			long outputSize = R.nextInt(190) + 10;
			Cloudlet cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet.setUserId(userId);
			cloudletList.add(cloudlet);
			id++;
		}
		return cloudletList;
	}

	private static void printCloudletList(List<Cloudlet> cloudletList) {
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time" + indent + "BWCost" + indent + "CPUCost");

		DecimalFormat dft = new DecimalFormat("###.##");
		double makespan = 0;
		int vmNum = Constants.L_VM_N + Constants.M_VM_N + Constants.H_VM_N;
		double[] executeTimeOfVM = new double[vmNum];
		double cost = 0;
		double LB = 0;

		for (Cloudlet cloudlet : cloudletList) {
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {

				double finishTime = cloudlet.getFinishTime();
				if (finishTime > makespan) {
					makespan = finishTime;
				}
				int vmId = cloudlet.getVmId();
				double actualCPUTime = cloudlet.getActualCPUTime();
				executeTimeOfVM[vmId] += actualCPUTime;
				cost += actualCPUTime * cloudlet.getCostPerSec();

				Log.print("SUCCESS");
				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getSubmissionTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(finishTime)
						+ indent + indent + indent + dft.format(cloudlet.getProcessingCost())
						+ indent + indent + indent + dft.format(actualCPUTime * cloudlet.getCostPerSec()));

			}
			
		}
		double avgExecuteTime = Arrays.stream(executeTimeOfVM).average().getAsDouble();
		for (int i = 0; i < vmNum; i++) {
			// System.out.print(executeTimeOfVM[i] + " ");
			LB += Math.pow(executeTimeOfVM[i] - avgExecuteTime, 2);
		}
		LB = Math.sqrt(LB / vmNum);
		double finalMakespan = makespan;
		Log.printLine("makespan: " + finalMakespan);
		Log.printLine("LB: " + LB);
		Log.printLine("cost: " + cost);

		Log.printLine();
		Log.printLine("Whale Optimization Algorithm Example");
	}
}
