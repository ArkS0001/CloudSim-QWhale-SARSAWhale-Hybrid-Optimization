[Parameters used for Ant Colony Algorithm](https://github.com/sidjee/Ant-Colony-Optimization-Framework/blob/master/implementation/specs.txt):-
-----------------------------------------
Q = 1 -------- multiplier for pheromone update

m = 37 --------- No of ants used

vms = 20 ---------- No of VMs used

alpha = 2 ----------- Exponent of pheromones

beta = 1 ----------- Exponent of computing capacity

gamma = 4 ------------ Exponent of Load balancing factor

rho = 0.05 ----------- vapourization constant for pheromone trail


Configurations for VMs :-
-----------------------
size = 10000; //image size (MB)

ram = 512; //vm memory (MB)

mips = random (500 to 1000)

bw = 1000(can be kept random)

pesNumber = 1(can be kept random) //number of cpus

vmm = "Xen"//VMM name


Configurations for cloudlets :-
-----------------------------
length = Random (100 to 1000)

fileSize = 300 (can be kept random)

outputSize = 300

pesNumber = 1 (can be kept random)
