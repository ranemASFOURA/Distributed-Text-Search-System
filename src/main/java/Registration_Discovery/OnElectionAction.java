package Registration_Discovery;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

public class OnElectionAction implements OnElectionCallback {
    private final ServiceRegistry serviceRegistry;
    private final int port;
    public OnElectionAction(ServiceRegistry serviceRegistry, int port) {
        this.serviceRegistry = serviceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBeLeader() {
        try {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            serviceRegistry.unregisterFromCluster();
            serviceRegistry.registerToClusterAsCoordinator(ipAddress);
            serviceRegistry.registerForUpdates();
            System.out.println("I am the Leader. Monitoring new workers...");
            List<String> workerAddresses = serviceRegistry.getWorkerAddresses();
            System.out.println("Current worker addresses: " + workerAddresses);
            System.out.println("Total workers: " + workerAddresses.size());

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter query: ");
            Scanner q = new Scanner(System.in);
            String query = q.nextLine();
            Coordinator coordinator=new Coordinator(serviceRegistry);
            coordinator.start(query);

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onWorker() {
        try {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            String currentServerAddress = String.format("%s:%s", ipAddress, port);
            serviceRegistry.registerToCluster(currentServerAddress);
            String leaderAddress = serviceRegistry.getLeaderAddress();
            Worker worker = new Worker(port);
            worker.start();

            if (leaderAddress != null) {
                System.out.println("Leader Address: " + leaderAddress);
            } else {
                System.out.println("No leader found.");
            }
            System.out.println("I am a Worker. Monitoring for updates...");
        } catch (InterruptedException | KeeperException | UnknownHostException e) {
            e.printStackTrace();
        }

    }
}
