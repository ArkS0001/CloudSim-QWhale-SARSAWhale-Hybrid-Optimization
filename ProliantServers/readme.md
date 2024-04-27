# Servers
       private static double calculateHostEnergyConsumption(Host host, Type type) {
              double totalPower = HOST_IDLE_POWER;
              double utilizationThreshold = 0; // Default utilization threshold
      
              // Set utilization threshold based on data center type
              switch (type) {
                  case LOW:
                      utilizationThreshold = 0.99; // 99% utilization for low data center
                      break;
                  case MEDIUM:
                      utilizationThreshold = 0.75; // 75% utilization for medium data center
                      break;
                  case HIGH:
                      utilizationThreshold = 0.50; // 50% utilization for high data center
                      break;
                  // Add more cases if needed for additional data center types
              }
