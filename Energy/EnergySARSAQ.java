package org.cloudbus.cloudsim.examples;
//@Author:Aakarshit Srivastava,Bhaskar Bannerjee,Ayush Verma;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import datacenter.Constants;
import datacenter.Type;

// Define a class to represent a state in the SARSA algorithm
class State4 {
    private int cloudletId;
    private int vmId;

    public State4(int cloudletId, int vmId) {
        this.cloudletId = cloudletId;
        this.vmId = vmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State4 state = (State4) o;
        return cloudletId == state.cloudletId && vmId == state.vmId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cloudletId, vmId);
    }
    
    public int getVmId() {
        return vmId;
    }
}

// Define a class to represent an action in the SARSA algorithm
class Action4 {
    private int vmId;

    public Action4(int vmId) {
        this.vmId = vmId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action4 action = (Action4) o;
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

// Define SARSA processor class
class Energy {
    private double alpha; // Learning rate
    private double gamma; // Discount factor
    private double epsilon; // Epsilon-greedy exploration factor
    private Map<State4, Map<Action4, Double>> qTable;

    public Energy(double alpha, double gamma, double epsilon) {
        this.alpha = alpha;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.qTable = new HashMap<>();
    }

    // Method to select action using epsilon-greedy policy
    public Action4 selectAction(State4 state) {
        if (Math.random() < epsilon) {
            // Explore: Choose a random action
            // For simplicity, we'll choose a random VM as action
            int randomVmId = (int) (Math.random() * 10);
            return new Action4(randomVmId);
        } else {
            // Exploit: Choose the action with the highest Q-value
            Map<Action4, Double> actionValues = qTable.get(state);
            if (actionValues == null || actionValues.isEmpty()) {
                // If no Q-values for the state, explore
                return selectAction(state);
            }
            Action4 maxAction = null;
            double maxQValue = Double.NEGATIVE_INFINITY;
            for (Action4 action : actionValues.keySet()) {
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
    public void updateQValue(State4 currentState, Action4 selectedAction, State4 newState, Action4 newAction, double reward) {
        double oldQValue = qTable.getOrDefault(currentState, new HashMap<>()).getOrDefault(selectedAction, 0.0);

        // Get the Q-value of the new action in the new state
        double newQValue = qTable.getOrDefault(newState, new HashMap<>()).getOrDefault(newAction, 0.0);

        // Calculate Q-value based on SARSA update rule
        double updatedQValue = oldQValue + alpha * (reward + gamma * newQValue - oldQValue);

        // Update Q-value in Q-table
        qTable.computeIfAbsent(currentState, k -> new HashMap<>()).put(selectedAction, updatedQValue);
    }

    // Method to get the maximum Q-value for a state
    private double getMaxQValue(State4 state) {
        Map<Action4, Double> actionValues = qTable.getOrDefault(state, new HashMap<>());
        if (actionValues.isEmpty()) {
            return 0.0; // If no Q-values for the state, return 0
        }
        return actionValues.values().stream().max(Double::compare).orElse(0.0);
    }

    public Map<State4, Map<Action4, Double>> getQTable() {
        return qTable;
    }
}

public class EnergySARSAQ {
    private static final int CLOUDLET_N = 100;
    private static final Random R = new Random(0);
    private static final int NUM_USER = 1;
    private static List<Vm> vmList;
    public final static PowerModel HOST_POWER = new PowerModelSpecPowerHpProLiantMl110G4Xeon3040();

    public static void main(String[] args) throws Exception {
        Log.printLine("Starting...");

        CloudSim.init(NUM_USER, Calendar.getInstance(), false);

        Datacenter datacenter0 = createDatacenter("Datacenter0", Type.LOW);
        Datacenter datacenter1 = createDatacenter("Datacenter1", Type.MEDIUM);
        Datacenter datacenter2 = createDatacenter("Datacenter2", Type.HIGH);

        DatacenterBroker broker = new DatacenterBroker("Broker");
        int brokerId = broker.getId();

        vmList = createVms(brokerId);
        broker.submitVmList(vmList);

        List<Cloudlet> cloudletList = createCloudlets(brokerId);
        broker.submitCloudletList(cloudletList);

        for (int i = 0; i < cloudletList.size(); i++) {
            broker.bindCloudletToVm(i, i % vmList.size());
        }

        CloudSim.startSimulation();

        List<Cloudlet> newList = broker.getCloudletReceivedList();
        CloudSim.stopSimulation();

        printCloudletList(newList);

        Log.printLine("EnergySARSAQ finished!");
    }

    private static List<Cloudlet> createCloudlets(int userId) {
        List<Cloudlet> list = new ArrayList<>();
        long length = 1000;
        for (int i = 0; i < CLOUDLET_N; i++) {
            Cloudlet cloudlet = new Cloudlet(i, length, 1, 300, 300, new UtilizationModelFull(), new UtilizationModelFull(),
                    new UtilizationModelFull());
            cloudlet.setUserId(userId);
            list.add(cloudlet);
        }
        return list;
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


    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;
        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent
                + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent
                        + dft.format(cloudlet.getExecStartTime()) + indent + indent
                        + dft.format(cloudlet.getFinishTime()));
            }
        }
    }
}
