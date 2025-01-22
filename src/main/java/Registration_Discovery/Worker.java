package Registration_Discovery;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;

public class Worker {
    private int port; // Port on which the worker listens for incoming connections
    private final String documentsPath; // Path to the folder containing documents
    private String receivedQuery; // Query received from the client

    public Worker(int port) {
        this.port = port;
        this.documentsPath = "D:\\Fifth year\\DS\\labs\\project\\Distributed_text_search_system\\src\\main\\resources\\documents" + port;
    }

    // Start the worker server to listen for incoming client connections
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Worker listening on port " + port);

            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    System.out.println("Client connected");

                    // Handle the client in a separate thread
                    handleClient(socket);
                } catch (IOException e) {
                    System.err.println("Error accepting client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handle client requests
    private void handleClient(Socket socket) {
        try (ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())) {

            // Receive the search query from the client
            receiveQuery(inputStream);

            // Perform the search operation in the documents
            List<DocumentTermsInfo> searchResults = searchDocuments();

            // Send the results back to the client
            sendResults(outputStream, searchResults);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error processing request: " + e.getMessage());
        }
    }

    // Receive the query from the client
    private void receiveQuery(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        receivedQuery = (String) inputStream.readObject(); // Read the query as a string
        System.out.println("Received query: " + receivedQuery);
    }

    // Perform the search operation in the documents
    private List<DocumentTermsInfo> searchDocuments() {
        System.out.println("Searching documents in path: " + documentsPath);

        File folder = new File(documentsPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt")); // Get all .txt files in the folder

        List<DocumentTermsInfo> results = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                System.out.println("Processing file: " + file.getName());
                DocumentTermsInfo documentInfo = processFile(file); // Process each file
                results.add(documentInfo);
            }
        } else {
            System.out.println("No files found in directory: " + documentsPath);
        }

        return results;
    }

    // Process a single file and calculate term frequencies (TF)
    private DocumentTermsInfo processFile(File file) {
        DocumentTermsInfo documentInfo = new DocumentTermsInfo(file.getName()); // Create a new document info object

        try {
            String content = new String(Files.readAllBytes(file.toPath())); // Read the file content
            String[] words = content.split("\\s+"); // Split the content into words
            int totalWords = words.length; // Calculate the total number of words in the document

            Map<String, Integer> termCount = new HashMap<>(); // Store the count of each term

            // Count the occurrences of each word in the document
            for (String word : words) {
                termCount.put(word, termCount.getOrDefault(word, 0) + 1);
            }

            // Update DocumentTermsInfo with TF for query terms
            for (String queryWord : receivedQuery.split("\\s+")) {
                int frequency = termCount.getOrDefault(queryWord, 0); // Frequency of the query word
                double tf = (double) frequency / totalWords; // Calculate TF
                documentInfo.addTermFrequency(queryWord, tf); // Add TF to the document info
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + file.getName());
        }

        return documentInfo;
    }

    // Send the search results back to the coordinator
    private void sendResults(ObjectOutputStream outputStream, List<DocumentTermsInfo> results) throws IOException {
        System.out.println("Sending results to coordinator...");
        outputStream.writeObject(results); // Write the list of document information to the output stream
        outputStream.flush(); // Ensure all data is sent
        System.out.println("Results sent successfully: " + results);
    }
}
