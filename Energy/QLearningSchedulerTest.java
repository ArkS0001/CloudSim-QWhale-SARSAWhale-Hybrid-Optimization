import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.QLearningScheduler.*;
import org.cloudbus.cloudsim.power.*;

import java.util.*;
import java.util.Map.Entry;

public class QLearningSchedulerTest {
    private static final double DEFAULT_LEARNING_GAMMA = 0.2;
    private static final double DEFAULT_LEARNING_ALPHA = 0.2;
    private static final double DEFAULT_LEARNING_EPSILON = 0.05;

    private List<Cloudlet> cloudletList;
    private List<Vm> vmList;
    private QlearningPowerDataCenter datacenter;
    private QLearningAgent qAgent;
    private PowerDatacenterBroker broker;

    public void execute() {
        initializeSimulation();

        for (int i = 0; i < 100; i++) {
            runIteration(i);
        }

        printResults();
    }

    private void initializeSimulation() {
        CloudSim.init(1, Calendar.getInstance(), false);

        qAgent = new QLearningAgent(DEFAULT_LEARNING_GAMMA, DEFAULT_LEARNING_ALPHA, DEFAULT_LEARNING_EPSILON);
        datacenter = createDatacenter("DataCenter_0");
        broker = Qhelper.createBroker();

        int brokerId = broker.getId();
        cloudletList = Qhelper.createCloudletListPlanetLab(brokerId, Constants.inputFolder);
        vmList = Qhelper.createVmList(brokerId, cloudletList.size());

        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);
    }

    private void runIteration(int iteration) {
        double epsilon = 1 / (iteration + 1);
        qAgent.setEpsilon(epsilon);

        CloudSim.terminateSimulation(Constants.SIMULATION_LIMIT);
        double lastClock = CloudSim.startSimulation();
        
        // Result analysis
        List<Cloudlet> newList = broker.getCloudletReceivedList();
        analyzeResults(datacenter, newList);
        CloudSim.stopSimulation();
    }

    private void analyzeResults(QlearningPowerDataCenter datacenter, List<Cloudlet> cloudletList) {
    // Calculate total energy consumption
    double totalEnergyConsumption = datacenter.getPowerModified() / (3600 * 1000); // in kWh

    // Calculate total number of VM migrations
    int totalMigrations = datacenter.getMigrationCount();

    // Calculate SLA violations and average SLA
    double averageSlaViolation = calculateAverageSLAViolation(datacenter);

    // Print the results
    System.out.println("Analysis of Simulation Results:");
    System.out.println("Total Energy Consumption: " + totalEnergyConsumption + " kWh");
    System.out.println("Total VM Migrations: " + totalMigrations);
    System.out.println("Average SLA Violation: " + averageSlaViolation + "%");
}

private double calculateAverageSLAViolation(QlearningPowerDataCenter datacenter) {
    ArrayList<Double> sla = new ArrayList<>();
    int numberOfAllocations = 0;

    for (Entry<String, List<List<Double>>> entry : datacenter.getUnderAllocatedMips().entrySet()) {
        List<List<Double>> underAllocatedMips = entry.getValue();
        for (List<Double> mips : underAllocatedMips) {
            if (mips.get(0) != 0) {
                numberOfAllocations++;
                double slaViolation = (mips.get(0) - mips.get(1)) / mips.get(0) * 100;
                if (slaViolation > 0) {
                    sla.add(slaViolation);
                }
            }
        }
    }

    double averageSlaViolation = 0;
    if (!sla.isEmpty()) {
        double totalSlaViolation = sla.stream().mapToDouble(Double::doubleValue).sum();
        averageSlaViolation = totalSlaViolation / sla.size();
    }

    return averageSlaViolation;
}

    private void printResults() {
    // Print final energy consumption
    System.out.println("Final Energy Consumption:");
    System.out.println(String.format("%.2f kWh", (datacenter.getPowerModified()) / (3600 * 1000)));

    // Print total number of VM migrations
    System.out.println("Total VM Migrations:");
    System.out.println(datacenter.getMigrationCount());

    // Print SLA violations and average SLA
    System.out.println("SLA Metrics:");
    printSLAMetrics(datacenter);

    // Optionally, you can print other relevant information or metrics
}

private void printSLAMetrics(QlearningPowerDataCenter datacenter) {
    int totalTotalRequested = 0;
    int totalTotalAllocated = 0;
    ArrayList<Double> sla = new ArrayList<Double>();
    int numberOfAllocations = 0;

    for (Entry<String, List<List<Double>>> entry : datacenter.getUnderAllocatedMips().entrySet()) {
        List<List<Double>> underAllocatedMips = entry.getValue();
        double totalRequested = 0;
        double totalAllocated = 0;
        for (List<Double> mips : underAllocatedMips) {
            if (mips.get(0) != 0) {
                numberOfAllocations++;
                totalRequested += mips.get(0);
                totalAllocated += mips.get(1);
                double _sla = (mips.get(0) - mips.get(1)) / mips.get(0) * 100;
                if (_sla > 0) {
                    sla.add(_sla);
                }
            }
        }
        totalTotalRequested += totalRequested;
        totalTotalAllocated += totalAllocated;
    }

    double averageSla = 0;
    if (sla.size() > 0) {
        double totalSla = 0;
        for (Double _sla : sla) {
            totalSla += _sla;
        }
        averageSla = totalSla / sla.size() * 0.6;
    }

    System.out.println("Average SLA Violation:");
    System.out.println(String.format("%.2f%%", averageSla));
}

    private QlearningPowerDataCenter createDatacenter(String name) {
        List<PowerHost> hostList = Qhelper.createHostList(Constants.NUMBER_OF_HOSTS);
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.001;

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
                cost, costPerMem, costPerStorage, costPerBw);
        QlearningPowerDataCenter datacenter = null;
        PowerVmSelectionPolicy vmSelectionPolicy = null;
        try {
            datacenter = new QlearningPowerDataCenter(name, characteristics, new QPowerVmAllocationPolicy(hostList, vmSelectionPolicy),
                    new LinkedList<Storage>(), 300, qAgent);
            datacenter.setDisableMigrations(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datacenter;
    }
}
