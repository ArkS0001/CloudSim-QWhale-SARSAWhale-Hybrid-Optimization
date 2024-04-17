😢👍🐱‍🏍
Cant Contact them

https://www.sciencedirect.com/science/article/abs/pii/S0167739X19313858



 		// print different host utilization and energy
	public static void printUtilization(QDatacenter datacenter0){
		List<Host> hosts=datacenter0.getHostList();
		double totalutilization=0;//utilization of each host
		double aveutilization=0;// average utilization of each host
		double aveu=0;
		double totalworkload=0;//workload of each host
		double aveworkload=0;//average workload of each host
		int j=0;
		double power=0;double totalpower=0;double avepower=0;double totalpowers=0;
		
		for(Host host:hosts){
			for(int i=0;i<host.getStateHistory().size();i++){
			//	System.out.println("host"+i+"\t"+host.getStateHistory().get(i).getUtilization()+"\t"+host.getStateHistory().size()+"\t"+host.getStateHistory().get(i).getTime());
				totalutilization=host.getStateHistory().get(i).getUtilization()+totalutilization;				
			//	totalworkload=host.getStateHistory().get(i).getWorkLoad()+totalworkload;
				if(host.getStateHistory().get(i).getUtilization()>0.5){
					power=(-7.79979*host.getStateHistory().get(i).getUtilization()+150.56995)*host.getStateHistory().get(i).getWorkLoad();
				
				}else{
					power=(132.47581*host.getStateHistory().get(i).getUtilization()+8.84754)*host.getStateHistory().get(i).getWorkLoad();
				}
				totalpower=power+totalpower;
			}
			totalpowers=totalpower/host.getStateHistory().size();
			aveutilization=totalutilization/host.getStateHistory().size();
		//	aveworkload=totalworkload/host.getStateHistory().size();
			totalutilization=0;
			j++;
		}
		aveu=totalutilization/hosts.size();
	//	avepower=totalpower/host.getStateHistory().size();
		System.out.println("utilization:" + aveutilization);//print each host utilization
		System.out.println("Energy:" + totalpowers);//print each host energy
	}
