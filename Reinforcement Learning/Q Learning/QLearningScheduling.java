package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.*;

public class QLearningScheduling {
    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;

    private static Datacenter createDatacenter(String name, List<Host> hostList) {
        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this resource
        double costPerBw = 0.0; // the cost of using bw in this resource

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone,
                cost, costPerMem, costPerStorage, costPerBw
        );

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics,
                    new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return broker;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;
        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time"
                + indent + "Start Time" + indent + "Finish Time");

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

    public static void main(String[] args) {
        Log.printLine("Starting Q Learning...");

        try {
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            CloudSim.init(num_user, calendar, trace_flag);

            DatacenterBroker broker = createBroker();

            // Create VMs
            vmList = new ArrayList<>();
            int vmId = 0;
            int pesNumber = 1; // number of cpus
            long bw = 1000;
            String vmm = "Xen"; // VMM name
            for (int i = 0; i < 5; i++) {
                Vm vm = new Vm(vmId++, broker.getId(), 1000, pesNumber, 512, bw, 10000, vmm,
                        new CloudletSchedulerTimeShared());
                vmList.add(vm);
            }
            broker.submitVmList(vmList);

            // Create Cloudlets
            cloudletList = new ArrayList<>();
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();
            for (int i = 0; i < 5; i++) {
                Cloudlet cloudlet = new Cloudlet(i, 40000, pesNumber, fileSize, outputSize, utilizationModel,
                        utilizationModel, utilizationModel);
                cloudlet.setUserId(broker.getId());
                cloudletList.add(cloudlet);
            }
            broker.submitCloudletList(cloudletList);

            // Bind Cloudlets to VMs
            for (int i = 0; i < 5; i++) {
                broker.bindCloudletToVm(cloudletList.get(i).getCloudletId(), vmList.get(i).getId());
            }

            QLearningProcessor qScheduler = new QLearningProcessor(500, 0.9, 1, 0.5, 0.1, 3, vmList);
            qScheduler.train(vmList);

            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();
            printCloudletList(newList);

            Log.printLine("Q Learning Scheduling Test Finished.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happened.");
        }
    }
}
