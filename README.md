## To simulate the workloads

    org.cloudbus.cloudsim -> “Cloudlet.java”

##To simulate the regions and datacenters

    org.cloudbus.cloudsim -> “Datacenter.java”

##To simulate the task offloading and policy-related services

    org.cloudbus.cloudsim -> “VmAllocationPolicy.java”, “CloudletScheduler.java”, “DatacenterBroker.java”, etc

###Research Topics in Cloud Computing

    Optimization Approaches for Network End-to-End Delay
    Resource and Multi-Task Offloading
    Optimal VM Distribution and Deployment Policies
    Cloud Workflow Observation and Execution
    Cloud VM Deployment and Management
    QoS Negotiation in Real-Time Applications
    Migration-less Adaptive Task Scheduling
    Cloud Service Utilization Billing, Accounting and Metering
    SLA based Admission Control and Management
    Energy-aware Dynamic VM Consolidations and Migrations

#One of the interesting factors in Cloudsim is its supporting algorithms. Depending on the functionalities, it supports several approaches and algorithms and some of them are #classified below for your references.
##Supported CloudSim Algorithms

    #VM Management Algorithms
        Maximum Correlation Coefficient Approach
        Random Choice / Sample Technique
        Average Mean / Median Absolute Deviations Method
        Time Policy based  Minimum Migration Strategy
        Soft Actor Critic (SAC) Reinforcement Learning Algorithm
    #VM Load Balancing Algorithms
        Recurrent Neural Networks (RNN)
        Logistic and Linear Regression
        Convolutional Neural Networks (CNN)
        Naïve Bayes  and SVM Algorithm
        Fuzzy Wavelet Neural Network Theory
        Neural Networks and Deep Learning Algorithms
        Quality based Dynamic Service Broker Chaining
        Random Forest and Decision Tree Algorithm
        Waltz Filtering / Labeling Algorithm – Constraint Satisfaction
        Cluster Analysis (For instances: K-Means, EM, Gaussian distributions)
    #Added Approaches and Algorithms
        Queen Bee Inspired Algorithm
        Morpho Graph Algorithm
        Fuzzy-Neuro Prediction Algorithm
        Cooperative Game Theory Approach
        Honey Bee Optimization Algorithm
        Artificial Ant Colony Optimization Algorithm

#Next, we can see about the software requirements for simulating the cloud computing project in Cloudsim. Under these requirements, we can model and simulate any kind of cloud #applications.
##Requirements for CloudSim Projects

    Integrated Development Environment (IDE)
        Java Development Kit (JDK)
        Eclipse/NetBeans IDE
    Operating System (OS)
        Windows Vista (32 or 64 bit)
        Windows 7 (32 or 64 bit)
        Windows 8 and 10 (32 or 64 bit)
        Windows XP (32 bit)
        Mac OS X 10.5.8 or later (32 bit only)
        Linux (Lucid Lynx and Ubuntu Linux)


How to run Cloudsim Simulation Scenario?

    Set both CloudSim and CloudInformationService element with the present watch time
    Construct required number of Datacenter(s) as resource suppliers and any one to begin simulation.
    Construct Broker for scheduling the task, allocating the VM and deploying the VM.
    Construct one or more VM and allocate to the broker. Next further submit to the corresponding DataCenters
    Create one or more Cloudlet and add them to the cloudlet list and broker for next task scheduling process on VMs. Then, it starts processing in the time of simulation
    Starts simulation to initiate all the deployed entities to perform their tasks
    Stop simulation to terminate all the entities prior to end the simulation
    Once the simulation is completed then pint the generated outcome. It clearly shows the cloudlet execution time in VM with start and finish time
    For more detailed explanation follow the “Guide to CloudsimExample1.java simulation workflow“ article

The first and foremost version of Cloud computing simulator is Cloudsim 3.x.x which turn out to be the foundation for several other extensions. And some of them are given as follows,
Other Simulators based on Cloudsim

    CloudReports
    CloudSimEx
    iFogSim
    EdgeCloudSim
    CloudAnalyst
    WorkflowSim
    Cloudsim Plus

In the beginning, the CloudSim act as a stand-alone cloud simulator which is globally used in everywhere. Later, so many independent scholars have added some significant features into it. As a result, Cloudsim is now extended in many ways as follow, 
What are the Cloudsim Extensions?

    CloudReports – Generates GUI based report of Simulation
    ThermoSim – Empowers Learning assisted Cloud Nodes Temperature Prediction using Thermal characteristics
    Cloud2Sim – Supports Open source Hazelcast Java framework for Multi-server distributed system
    RECAP DES – Enables Synchronous Design of Hierarchical Model Architectures
        For instance: ElasticSearch
    CloudSimEx – Includes Parallel and MapReduce Simulation

###CloudSim Extension Tools

    Cloud2Sim
    Dynamic CloudSim
    CloudAnalyst
    CloudReports
    CLoudAuction
    CloudMIG Xpress
    CloudSim Automation
    CloudSimEx
    iFogSim
    RedCloudSim
    WorkflowSim
    And also FederatedCloudSim
