package Registration_Discovery;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Coordinator {
    private ServiceRegistry serviceRegistry;
    private Map<String, Double> documentScores = new HashMap<>();

    // Constructor to initialize the Coordinator with a ServiceRegistry
    public Coordinator(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    // Start processing the query
    public void start(String query) {
        System.out.println("Query received: " + query);

        // Send queries to all workers using threads
        sendAndReceiveFromWorkers(query);

        // Display the final results after all threads finish processing
        displayResults();
    }

    // Method to send and receive data from worker nodes
    private void sendAndReceiveFromWorkers(String query) {
        List<String> workerAddresses = serviceRegistry.getWorkerAddresses(); // Get worker addresses from registry
        List<Thread> threads = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(workerAddresses.size()); // Wait for all threads to complete

        for (String workerAddress : workerAddresses) {
            Thread workerThread = new Thread(() -> {
                try {
                    startSearchOnWorker(workerAddress, query); // Perform search on the worker
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error communicating with worker: " + workerAddress + " - " + e.getMessage());
                } finally {
                    latch.countDown(); // Decrement the latch counter when the thread finishes
                }
            });
            threads.add(workerThread);
            workerThread.start();
        }

        try {
            latch.await(); // Wait until all threads complete
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage());
        }
    }

    // Method to start search on a specific worker
    private void startSearchOnWorker(String workerAddress, String query) throws IOException, ClassNotFoundException {
        String[] addressParts = workerAddress.split(":"); // Split address into IP and port
        String ipAddress = addressParts[0];
        int port = Integer.parseInt(addressParts[1]);

        System.out.println("Connecting to worker at: " + ipAddress + ":" + port);

        try (Socket socket = new Socket(ipAddress, port)) {
            // Send the query to the worker
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Sending query: " + query + " to worker at " + workerAddress);
            outputStream.writeObject(query);

            // Receive results from the worker
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            List<DocumentTermsInfo> results = (List<DocumentTermsInfo>) inputStream.readObject();
            System.out.println("Received results from worker " + workerAddress + ": " + results);

            // Process the results from the worker
            synchronized (documentScores) {
                processWorkerResults(workerAddress, results);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Process the results received from a worker
    private void processWorkerResults(String workerAddress, List<DocumentTermsInfo> results) {
        System.out.println("Processing results from worker: " + workerAddress);

        // Calculate the number of documents containing each word
        Map<String, Integer> documentFrequency = calculateDocumentFrequency(results);
        int totalDocuments = results.size();

        // Calculate the IDF (Inverse Document Frequency) values
        Map<String, Double> idfValues = calculateIDF(documentFrequency, totalDocuments);

        // Compute scores for each document
        calculateDocumentScores(results, idfValues);

        // Update and sort the final results
        updateFinalResults();

        System.out.println("Results processed for worker: " + workerAddress);
    }

    // Calculate the number of documents containing each word
    private Map<String, Integer> calculateDocumentFrequency(List<DocumentTermsInfo> results) {
        Map<String, Integer> documentFrequency = new HashMap<>();

        for (DocumentTermsInfo docInfo : results) {
            for (String word : docInfo.getTermFrequency().keySet()) {
                documentFrequency.merge(word, 1, Integer::sum); // Increment the count for each word
            }
        }

        System.out.println("Calculated document frequency: " + documentFrequency);
        return documentFrequency;
    }

    // Calculate IDF values for each word
    private Map<String, Double> calculateIDF(Map<String, Integer> documentFrequency, int totalDocuments) {
        Map<String, Double> idfValues = new HashMap<>();

        for (Map.Entry<String, Integer> entry : documentFrequency.entrySet()) {
            String word = entry.getKey();
            int docCount = entry.getValue();
            double idf = Math.log((double) totalDocuments / (1 + docCount)); // Add 1 to avoid division by zero
            idfValues.put(word, idf);
        }

        System.out.println("Calculated IDF values: " + idfValues);
        return idfValues;
    }

    // Calculate document scores using TF-IDF
    private void calculateDocumentScores(List<DocumentTermsInfo> results, Map<String, Double> idfValues) {
        for (DocumentTermsInfo docInfo : results) {
            String documentName = docInfo.getDocumentName();
            Map<String, Double> termFrequency = docInfo.getTermFrequency();

            for (Map.Entry<String, Double> entry : termFrequency.entrySet()) {
                String word = entry.getKey();
                double tf = entry.getValue();
                double idf = idfValues.getOrDefault(word, 0.0);
                double score = tf * idf; // Compute TF-IDF score

                documentScores.merge(documentName, score, Double::sum); // Aggregate scores per document
            }
        }

        System.out.println("Calculated document scores: " + documentScores);
    }

    // Sort and update the final document scores
    private void updateFinalResults() {
        List<Map.Entry<String, Double>> sortedDocuments = new ArrayList<>(documentScores.entrySet());
        sortedDocuments.sort((a, b) -> Double.compare(b.getValue(), a.getValue())); // Sort in descending order of scores

        System.out.println("Sorted Documents:");
        for (Map.Entry<String, Double> entry : sortedDocuments) {
            System.out.println("Document: " + entry.getKey() + ", Score: " + entry.getValue());
        }
    }

    // Display the final results
    private void displayResults() {
        System.out.println("Displaying final results...");
    }
}
