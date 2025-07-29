package src.hldConcepts;
// package hldConcepts;
import java.util.*;

// Singleton Design Pattern
class RoundRobin{
    private static RoundRobin instance;
    int currIdx;
    List<String> servers;

    private RoundRobin(List<String> servers) {
        this.currIdx = 0;
        this.servers = new ArrayList<>(servers);
    }

    public static RoundRobin getInstance(List<String> servers) {
        if(instance == null){
            instance = new RoundRobin(servers);
        }
        return instance;
    }

    public String getNextServer() {
        String nextServer = servers.get(currIdx);
        currIdx = (currIdx + 1) % servers.size(); 
        return nextServer;
    }
}

class Server {
    String name;
    int weight;
    int currentWeight; // Used for WRR algorithm

    public Server(String name, int weight) {
        this.name = name;
        this.weight = weight;
        this.currentWeight = 0; // Initial value
    }
}


class WeightedRoundRobin {
    private static WeightedRoundRobin instance;
    private List<Server> servers;
    private int totalWeight;

    private WeightedRoundRobin(List<Server> servers) {
        this.servers = servers;
        this.totalWeight = servers.stream().mapToInt(s -> s.weight).sum();
    }

    public static WeightedRoundRobin getInstance(List<Server> servers) {
        if (instance == null) {
            instance = new WeightedRoundRobin(servers);
        }
        return instance;
    }

    public synchronized String getNextServer() {
        Server best = null;
        for (Server server : servers) {
            server.currentWeight += server.weight;
            if (best == null || server.currentWeight > best.currentWeight) {
                best = server;
            }
        }

        if (best == null) return null;

        best.currentWeight -= totalWeight;
        return best.name;
    }
}


public class LoadBalancer {
    public static void main(String[] args){
        RoundRobin ins = RoundRobin.getInstance(new ArrayList<>(Arrays.asList("Server1", "Server2")));
        RoundRobin ins1 = RoundRobin.getInstance(new ArrayList<>(Arrays.asList("Server1", "Server2")));
        System.out.println(ins);
        System.out.println(ins1);

        List<Server> servers = new ArrayList<>();
        servers.add(new Server("Server1", 5)); // High weight
        servers.add(new Server("Server2", 1)); // Low weight
        servers.add(new Server("Server3", 2));

        WeightedRoundRobin lb = WeightedRoundRobin.getInstance(servers);

        // Simulate 10 requests
        for (int i = 0; i < 10; i++) {
            System.out.println("Request " + (i+1) + ": " + lb.getNextServer());
        }
    }
}
