/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
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
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;


/**
 * A simple example showing how to create
 * two datacenters with one host each and
 * run cloudlets of two users on them.
 */
public class SLATest1 {

	/** The cloudlet lists. */
	private static List<Cloudlet> cloudletList1;
	private static List<Cloudlet> cloudletList2;
	//private static List<Cloudlet> cloudletList3;
	//private static List<Cloudlet> cloudletList4;

	/** The vmlists. */
	private static List<Vm> vmlist1;
	private static List<Vm> vmlist2;

	public static ArrayList<Double> consistencyArray = new ArrayList<Double>(100);
	
	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.printLine("Starting CloudSimExample...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 2;   // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			@SuppressWarnings("unused")
			Datacenter datacenter1 = createDatacenter("Datacenter_1");

			//Third step: Create Brokers
			DatacenterBroker broker1 = createBroker(1);
			int brokerId1 = broker1.getId();

			DatacenterBroker broker2 = createBroker(2);
			int brokerId2 = broker2.getId();

			//Fourth step: Create one virtual machine for each broker/user
			vmlist1 = new ArrayList<Vm>();
			vmlist2 = new ArrayList<Vm>();

			//VM description
			int vmid = 0;
			int mips = 250;
			long size = 10000; //image size (MB)
			int ram = 2048; //vm memory (MB)
			long bw = 1000;
			int pesNumber = 1; //number of cpus
			String vmm = "Xen"; //VMM name

			//create two VMs: the first one belongs to user1
			Vm vm1 = new Vm(vmid, brokerId1, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			//the second VM will have twice the priority of VM1 and so will receive twice CPU time
			vmid++;
			Vm vm2 = new Vm(vmid, brokerId1, mips * 2, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			
			vmid++;
			Vm vm3 = new Vm(vmid, brokerId1, mips * 2.75, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			
			vmid++;
			Vm vm4 = new Vm(vmid, brokerId1, mips * 1.75, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			
			vmid++;
			Vm vm5 = new Vm(vmid, brokerId1, mips * 1.62, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			
			vmid++;
			Vm vm6 = new Vm(vmid, brokerId1, mips * 2.1, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			
			vmid++;
			Vm vm7 = new Vm(vmid, brokerId1, mips * 1.37, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			
			vmid++;
			Vm vm8 = new Vm(vmid, brokerId1, mips * 1.61, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			
			vmid++;
			Vm vm9 = new Vm(vmid, brokerId1, mips * 2.22, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			
			vmid++;
			Vm vm10 = new Vm(vmid, brokerId1, mips * 2.75, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			
			
			
			//the second VM: this one belongs to user2
			vmid++;
			Vm vm11 = new Vm(vmid, brokerId2, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			//the second VM will have twice the priority of VM3 and so will receive twice CPU time
			vmid++;
			Vm vm12 = new Vm(vmid, brokerId2, mips * 1.29, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			vmid++;
			Vm vm13 = new Vm(vmid, brokerId2, mips * 2.48, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			vmid++;
			Vm vm14 = new Vm(vmid, brokerId2, mips * 1.84, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			vmid++;
			Vm vm15 = new Vm(vmid, brokerId2, mips * 2.12, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

			//add the VMs to the vmlists
			vmlist1.add(vm1);
			vmlist1.add(vm2);
			vmlist1.add(vm3);
			vmlist1.add(vm4);
			vmlist1.add(vm5);
			vmlist1.add(vm6);
			vmlist1.add(vm7);
			vmlist1.add(vm8);
			vmlist1.add(vm9);
			vmlist1.add(vm10);
			
			
			vmlist2.add(vm11);
			vmlist2.add(vm12);
			vmlist2.add(vm13);
			vmlist2.add(vm14);
			vmlist2.add(vm15);

			//submit vm list to the broker
			broker1.submitVmList(vmlist1);
			broker2.submitVmList(vmlist2);

			//Fifth step: Create two Cloudlets
			cloudletList1 = new ArrayList<Cloudlet>();
			cloudletList2 = new ArrayList<Cloudlet>();
			//cloudletList3 = new ArrayList<Cloudlet>();
			//cloudletList4 = new ArrayList<Cloudlet>();

			//Cloudlet properties
			int id = 0;
			long length = 40000;
			long fileSize = 300;
			long outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();

			Cloudlet cloudlet1 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet1.setUserId(brokerId1);
			
			id++;
			Cloudlet cloudlet2 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet2.setUserId(brokerId1);
			
			id++;
			Cloudlet cloudlet3 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet3.setUserId(brokerId1);
			
			id++;
			Cloudlet cloudlet4 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet4.setUserId(brokerId1);
			
			id++;
			Cloudlet cloudlet5 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet5.setUserId(brokerId1);
			
			id++;
			Cloudlet cloudlet6 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet6.setUserId(brokerId1);
			
			id++;
			Cloudlet cloudlet7 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet7.setUserId(brokerId1);
			
			id++;
			Cloudlet cloudlet8 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet8.setUserId(brokerId1);
			
			id++;
			Cloudlet cloudlet9 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet9.setUserId(brokerId1);
			
			id++;
			Cloudlet cloudlet10 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet10.setUserId(brokerId1);
			
			id++;
			Cloudlet cloudlet11 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet11.setUserId(brokerId2);
			
			id++;
			Cloudlet cloudlet12 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet12.setUserId(brokerId2);
			
			id++;
			Cloudlet cloudlet13 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet13.setUserId(brokerId2);
			
			id++;
			Cloudlet cloudlet14 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet14.setUserId(brokerId2);
			
			id++;
			Cloudlet cloudlet15 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet15.setUserId(brokerId2);

			//add the cloudlets to the lists: each cloudlet belongs to one user
			cloudletList1.add(cloudlet1);
			cloudletList1.add(cloudlet2);
			cloudletList1.add(cloudlet3);
			cloudletList1.add(cloudlet4);
			cloudletList1.add(cloudlet5);
			cloudletList1.add(cloudlet6);
			cloudletList1.add(cloudlet7);
			cloudletList1.add(cloudlet8);
			cloudletList1.add(cloudlet9);
			cloudletList1.add(cloudlet10);
			
			cloudletList2.add(cloudlet11);
			cloudletList2.add(cloudlet12);
			cloudletList2.add(cloudlet13);
			cloudletList2.add(cloudlet14);
			cloudletList2.add(cloudlet15);

			//submit cloudlet list to the brokers
			broker1.submitCloudletList(cloudletList1);
			//broker1.submitCloudletList(cloudletList2);
			//broker2.submitCloudletList(cloudletList3);
			broker2.submitCloudletList(cloudletList2);

			//bind the cloudlets to the vms. This way, the broker
			// will submit the bound cloudlets only to the specific VM
			broker1.bindCloudletToVm(cloudlet1.getCloudletId(),vm1.getId());
			broker1.bindCloudletToVm(cloudlet2.getCloudletId(),vm2.getId());
			broker1.bindCloudletToVm(cloudlet3.getCloudletId(),vm3.getId());
			broker1.bindCloudletToVm(cloudlet4.getCloudletId(),vm4.getId());
			broker1.bindCloudletToVm(cloudlet5.getCloudletId(),vm5.getId());
			broker1.bindCloudletToVm(cloudlet6.getCloudletId(),vm6.getId());
			broker1.bindCloudletToVm(cloudlet7.getCloudletId(),vm7.getId());
			broker1.bindCloudletToVm(cloudlet8.getCloudletId(),vm8.getId());
			broker1.bindCloudletToVm(cloudlet9.getCloudletId(),vm9.getId());
			broker1.bindCloudletToVm(cloudlet10.getCloudletId(),vm10.getId());
			
			broker2.bindCloudletToVm(cloudlet11.getCloudletId(),vm11.getId());
			broker2.bindCloudletToVm(cloudlet12.getCloudletId(),vm12.getId());
			broker2.bindCloudletToVm(cloudlet13.getCloudletId(),vm13.getId());
			broker2.bindCloudletToVm(cloudlet14.getCloudletId(),vm14.getId());
			broker2.bindCloudletToVm(cloudlet15.getCloudletId(),vm15.getId());
			
			
			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList1 = broker1.getCloudletReceivedList();
			List<Cloudlet> newList2 = broker2.getCloudletReceivedList();
			//List<Cloudlet> newList2 = broker2.getCloudletReceivedList();
			//List<Cloudlet> newList2 = broker2.getCloudletReceivedList();

			CloudSim.stopSimulation();

			Log.print("=============> User "+brokerId1+"    ");
			printCloudletList(newList1,0);

			Log.print("=============> User "+brokerId2+"    ");
			printCloudletList(newList2,10);

			Log.printLine("CloudSimExample finished!");
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		//    our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips=10000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		//4. Create Host with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 65536; //host memory (MB)
		long storage = 100000; //host storage
		int bw = 100000;


		//in this example, the VMAllocatonPolicy in use is SpaceShared. It means that only one VM
		//is allowed to run on each Pe. As each Host has only one Pe, only one VM can run on each Host.
		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList,
    				new VmSchedulerSpaceShared(peList)
    			)
    		); // This is our first machine
		
		//create another machine in the Data center
				List<Pe> peList2 = new ArrayList<Pe>();

				peList2.add(new Pe(0, new PeProvisionerSimple(mips)));

				hostId++;

				hostList.add(
		    			new Host(
		    				hostId,
		    				new RamProvisionerSimple(ram),
		    				new BwProvisionerSimple(bw),
		    				storage,
		    				peList2,
		    				new VmSchedulerTimeShared(peList2)
		    			)
		    		); // This is our second machine

		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.001;	// the cost of using storage in this resource
		double costPerBw = 0.0;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker(int id){

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker"+id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list, int index) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time" + indent + "Round Trip Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);
			double rtt=0.0;
			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");
				//String p_time = (dft.format(cloudlet.getActualCPUTime()));
				//System.out.println(p_time);
				float process_time= Float.parseFloat(dft.format(cloudlet.getActualCPUTime()));
				
				rtt= (300*8/1000)+ 10 + process_time +(300*8/1000)+10;
				Log.printLine( indent + indent + dft.format(cloudlet.getResourceId()) + indent + indent + indent + indent +dft.format(cloudlet.getVmId()) +
						indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
						indent + indent + dft.format(cloudlet.getFinishTime()) + indent + indent + indent +rtt);
				
			}
			printSLAverification(rtt,0);
		}
		printSLAverification(0.0,index);
	}
	
	
	private static void printSLAverification(double rtt, int index) {
		String [][] subSla = new String [4][3];
		//System.out.println("Shopping cart application SLA:");
		subSla[0][0] = " Consistency ";
		subSla[0][1] = "   Latency ";
		subSla[0][2] = " Utility ";
		subSla[1][0] = " strong         ";
		subSla[1][1] = " 300ms ";
		subSla[1][2] = " 1.0 ";
		subSla[2][0] = " read my writes ";
		subSla[2][1] = " 300ms ";
		subSla[2][2] = " 0.5 ";    
		subSla[3][0] = " eventual       ";
		subSla[3][1] = " 300ms ";
		subSla[3][2] = " 0.1 ";
		
		/**for(int i=0; i<4; i++) {
			for(int j=0; j<3; j++) {
				System.out.print(subSla[i][j]);
			}
			System.out.println();
		}*/
		//System.out.println(rtt);
		
		double consistency[] = new double[3];
		double max=0.0;
		if(rtt>0) {
			for(int i=0; i<3; i++) {
				consistency[i]=rtt* Double.parseDouble(subSla[i+1][2]);
				//System.out.println("consistency "+consistency[i]);
				if(consistency[i]>max) {
					max=consistency[i];
					//System.out.println("maximum " + max);
				}
			}
			//System.out.println("outside max "+max);
			consistencyArray.add(max);
		}
		//System.out.println("check1 "+consistencyArray.size());
		double totalRTT=0.0;
		double avgRTT=0.0;
		if(rtt == 0.0) {
			//System.out.println(consistencyArray.size());
			//System.out.println("Inside check");
			for (int x=index; x<consistencyArray.size(); x++) {
				//System.out.println(consistencyArray.get(x));
			//for (Double d: consistencyArray) {
				//System.out.println("inside for");
				//System.out.println(d); 
				totalRTT+=consistencyArray.get(x);
				//System.out.println("Total Rtt "+ totalRTT);
			}
			avgRTT=totalRTT/(consistencyArray.size()-index);
			
			System.out.println("Average RTT is "+ avgRTT);
			if(avgRTT>10.0 && avgRTT<100.0) {
				System.out.println("Strong Consistency Met");
			}
			else if(avgRTT>=100.0 && avgRTT<120.0) {
				System.out.println("Read My Writes Consistency Met");
			}
			else if(avgRTT>=120.0 && avgRTT<150.0) {
				System.out.println("Eventual Consistency Met");
			}	
			else if(avgRTT>=150.0 && avgRTT<200.0) {
				System.out.println("SLA Violation Mild");
			}
			else if(avgRTT>=200.0) {
				System.out.println("SLA Violation Severe");
			}
			
		}
					
	}
}
