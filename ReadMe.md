Below is the `README.md` file for your distributed text search system project.

```markdown
# Distributed Text Search System

## Overview

This project implements a distributed text search system using Java, consisting of a **Coordinator** and multiple **Worker** nodes. The system allows users to query text documents distributed across different workers, which compute term frequencies (TF) and document scores based on TF-IDF (Term Frequency-Inverse Document Frequency).

### Key Components
1. **Coordinator**:
   - Manages the query process.
   - Communicates with workers to fetch and aggregate results.
   - Computes final document scores and sorts results.

2. **Worker**:
   - Processes a subset of documents.
   - Computes term frequencies for the received query.
   - Sends results back to the coordinator.

3. **Service Registry**:
   - Maintains a list of active worker addresses.
   - Facilitates dynamic registration and discovery of workers.

---

## Features

- **Distributed Query Processing**: Enables querying large datasets distributed across multiple nodes.
- **TF-IDF Scoring**: Calculates document scores based on term frequency and inverse document frequency.
- **Dynamic Worker Registration**: Workers can register and deregister dynamically.
- **Threaded Processing**: Uses multithreading for efficient communication and computation.

---

## Prerequisites

- **Java Development Kit (JDK)**: Version 11 or higher.
- **IDE**: IntelliJ IDEA, Eclipse, or any Java-supported IDE.
- **Documents**: Text files stored in worker directories for querying.

---

## Project Structure

```
src/main/java/Registration_Discovery/
├── Coordinator.java          # Coordinates the distributed query process
├── Worker.java               # Processes documents and computes term frequencies
├── ServiceRegistry.java      # Registers and manages worker nodes
├── DocumentTermsInfo.java    # Represents document term frequency information
├── Application.java
├── LeaderElection.java
├── OnElectionAction.java
├── OnElectionCallback.java
```

---

## Setup Instructions

### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd Distributed_text_search_system
```

### Step 2: Compile the Project
Compile the Java files using your IDE or command line:
```bash
javac -d out src/main/java/Registration_Discovery/*.java
```

### Step 3: Prepare Documents
1. Create directories for each worker under `resources/documents`.
2. Add `.txt` files containing text data to these directories.

### Step 4: Start Coordinator
Run the coordinator and provide a query:
```bash
java -cp out Registration_Discovery.Coordinator
```

---
### Step 5: Start Worker Nodes
Run each worker on a different port:
```bash
java -cp out Registration_Discovery.Worker <port>
```



## Code Highlights

### Coordinator

- **Query Distribution**: Sends the query to all registered workers and collects results.
- **TF-IDF Scoring**: Computes scores for documents based on term frequencies and IDF values.

### Worker

- **Term Frequency Calculation**: Computes term frequencies for query words in the document set.
- **File Processing**: Reads and processes `.txt` files to calculate scores.

### Service Registry

- **Dynamic Management**: Keeps track of all active workers and their addresses.

### DocumentTermsInfo

- Encapsulates document name and term frequency information.
- Serializable for seamless transmission between coordinator and workers.

---
