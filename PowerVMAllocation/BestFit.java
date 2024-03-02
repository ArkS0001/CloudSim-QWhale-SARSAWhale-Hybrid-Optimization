package org.cloudbus.cloudsim.examples.power.planetlab;

import java.util.Random;

class VirtualMachine {
    private int vmId;
    private double cpuUtilization;

    public VirtualMachine(int vmId, double cpuUtilization) {
        this.vmId = vmId;
        this.cpuUtilization = cpuUtilization;
    }

    public int getVmId() {
        return vmId;
    }

    public double getCpuUtilization() {
        return cpuUtilization;
    }

    @Override
    public String toString() {
        return "VM " + vmId + " (CPU Utilization: " + cpuUtilization + ")";
    }
}

class CloudSimulator {
    private VirtualMachine[] vms;
    private double threshold;

    public CloudSimulator(int numVms, double threshold) {
        vms = new VirtualMachine[numVms];
        Random rand = new Random();
        for (int i = 0; i < numVms; i++) {
            vms[i] = new VirtualMachine(i, rand.nextDouble() * 100); // Random CPU utilization
        }
        this.threshold = threshold;
    }

    public void allocateResources() {
        VirtualMachine bestFitVM = null;
        double minDifference = Double.MAX_VALUE;

        for (VirtualMachine vm : vms) {
            if (vm.getCpuUtilization() > threshold) {
                double difference = vm.getCpuUtilization() - threshold;
                if (difference < minDifference) {
                    minDifference = difference;
                    bestFitVM = vm;
                }
            }
        }

        if (bestFitVM != null) {
            System.out.println("Allocating more resources to " + bestFitVM);
            // Add logic here to allocate more resources to the best fit VM
        } else {
            System.out.println("No VMs found with CPU utilization exceeding the threshold.");
        }
    }
}

public class BestFit {
    public static void main(String[] args) {
        int numVms = 50;
        double threshold = 90.0;

        CloudSimulator cloudSim = new CloudSimulator(numVms, threshold);
        cloudSim.allocateResources();
    }
}
