// v001 11/10/2024

// To implement the required concurrent functionality, your JobManager must use two Extrinsic Monitor classes:
//      java.util.concurrent.locks.Condition;
//      java.util.concurrent.locks.ReentrantLock;
// Note that you must not use the signalAll() method (as this creates inefficient polling activity).
//
// No other thread-safe, synchronised or scheduling classes or methods may be used. In particular:
// • The keyword synchronized, and other classes from the package java.util.concurrent must not be used. 
// • Thread.Sleep() and any other methods that affect thread scheduling must not be used.
// • “Busy waiting” techniques, such as spinlocks, must not be used. 
// Other non-thread-safe classes from SE17 may be used, e.g. LinkedLists, HashMaps, and ArrayLists 
// (these are unsynchronized and therefore not thread-safe).

// See the Coursework spec for full list of constraints and marking penalties.

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*; // USED FOR QUEUE, LINKED LIST, HASHMAP & PRIORITYQUEUE 
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class JobManager implements Manager { 
	
	// LOCKING FOR SYNCHRONIZING ACCESS
    private final ReentrantLock lock = new ReentrantLock();

	// QUEUE FOR HOLDING THE JOB REQUEST IN A FIFO ORDER
    private final LinkedList<JobRequest> pendingJobs = new LinkedList<>();
    
	// STORING THE AVAILABLE SERVERS BY THE TYPE   
    private final HashMap<String, ArrayList<ServerThread>> availableServers = new HashMap<>();

    @Override
    public void specifyJob(JobRequest job) {
        lock.lock();
        try {
        	// ADDING JOB INTO THE FIFO QUEUE
            pendingJobs.add(job);
            // ALLOCATING THE SERVERS TO JOBS
            processJobs();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String serverLogin(String type, int ID) {
        lock.lock();
        try {
            ServerThread server = new ServerThread(type, ID);
            
            if (!availableServers.containsKey(type)) {
                availableServers.put(type, new ArrayList<>());
            }
            availableServers.get(type).add(server);
            
            // WAITING FOR AN JOB TO BE AVAILABLE
            processJobs();
            
            while (!server.isAssigned()) {
                server.waitForAssignment();
            }
            
            return server.getAssignedJob();
        } finally {
            lock.unlock();
        }
    }
    
	// ==================================== PRIVATE METHODS & CLASSES
	// ===============================================
    
	// PROCESSING THE JOBS
	// CHECKS THE JOB QUEUE (FIFO ORDER) AND TRIES TO ALLOCATE AVAILABLE SERVERS TO
	// JOBS.
	// IF A JOB'S REQUIRED SERVERS ARE AVAILABLE, IT ASSIGNS THE JOB AND REMOVES IT
	// FROM THE QUEUE.
	// OTHERWISE, IT KEEPS WAITING UNTIL ENOUGH SERVERS LOG IN.
    private void processJobs() {
        for (int i = 0; i < pendingJobs.size(); i++) {
            JobRequest job = pendingJobs.get(i);
            if (canSatisfyJob(job)) {
                assignJob(job);
                pendingJobs.remove(i);
                i = -1; // Restart processing after modification
            }
        }
    }

	// CAN SATISFY THE JOBS
	// CHECKS IF A GIVEN JOB CAN BE FULLY SATISFIED WITH THE CURRENTLY AVAILABLE
	// SERVERS.
	// ITERATES THROUGH THE JOB'S REQUIRED SERVERS.
	// RETURNS `TRUE` IF ENOUGH SERVERS OF EACH TYPE ARE AVAILABLE.
	// RETURNS `FALSE` OTHERWISE.
    private boolean canSatisfyJob(JobRequest job) {
        for (Map.Entry<String, Integer> requirement : job.entrySet()) {
            String serverType = requirement.getKey();
            int requiredCount = requirement.getValue();
            
            if (!availableServers.containsKey(serverType) || 
                availableServers.get(serverType).size() < requiredCount) {
                return false;
            }
        }
        return true;
    }

	// ASSGNING THE JOBS
	// ASSIGNS A JOB TO THE REQUIRED NUMBER OF SERVERS.
	// RETRIEVES AND REMOVES THE NEEDED SERVERS FROM THE `availableServers` MAP.
	// MAPS THE SERVERS' IDS TO THE JOB NAME (`assignedJobs`).
	// SIGNALS THE WAITING SERVER THREADS TO CONTINUE EXECUTION.
    private void assignJob(JobRequest job) {
        for (Map.Entry<String, Integer> requirement : job.entrySet()) {
            String serverType = requirement.getKey();
            int requiredCount = requirement.getValue();
            ArrayList<ServerThread> servers = availableServers.get(serverType);
            
            // Sort servers by ID in reverse order (higher IDs first)
            Collections.sort(servers, Comparator.comparingInt(ServerThread::getID).reversed());
            
            for (int i = 0; i < requiredCount; i++) {
                ServerThread server = servers.remove(0);
                server.assignJob(job.jobName);
            }
        }
    }

    private class ServerThread {
        private final String type;
        private final int id;
        private String assignedJob;
        private final Condition condition;
        private boolean assigned;

        public ServerThread(String type, int id) {
            this.type = type;
            this.id = id;
            this.assigned = false;
            this.assignedJob = "";
            this.condition = lock.newCondition();
        }

        public String getType() {
            return type;
        }

        public int getID() {
            return id;
        }

        public boolean isAssigned() {
            return assigned;
        }

        public String getAssignedJob() {
            return assignedJob;
        }

        public void assignJob(String jobName) {
            this.assignedJob = jobName;
            this.assigned = true;
            this.condition.signal();
        }

        public void waitForAssignment() {
            try {
                condition.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}