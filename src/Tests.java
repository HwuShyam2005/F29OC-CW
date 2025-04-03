
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//        EXAMPLE TESTS
//        Provided as examples ONLY
//
//        NOTE: 1. When these tests are run with the original JobManager.java file they 
//                 will fail to produce the correct output (i.e. that described in the tests below).
//				   As the JobManager supplied has no code in its .serverLogin() method, calls to this 
//                 method will return immediately (thereby 'releasing' ALL server threads to continue).
//              2. You must write your own tests here to make sure that your JobManager.java meets the UR
//              3. You may use any Java SE17 libraries or code in THIS Tests.java file but the only concurrent, 
//				   thread safe classes you may use in JobManager.java are ReentrantLock and its Condition variables.
//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// v001 11/10/2024

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.*;

public class Tests {
	// Declare global list of events to log ServerThread completions in:
	ConcurrentLinkedQueue<String> events; // "wait-free" FIFO queue
	String threadName = Thread.currentThread().getName();

	// TEST CASE UR1
	// --------------------------------------------------------------------------------------------------------------------------
	// COMPUTESERVER LOGINS ARE FOLLOWED BY AN O
	public void userRequirement1() {
		// INITIALIZE EVENT LOG AND JOB MANAGER
		events = new ConcurrentLinkedQueue<String>();
		JobManager manager = new JobManager();

		events.add(threadName + ": --- Testing UR1: ComputeServer Logins followed by One Job Request ---");

		// STARTING 5 COMPUTESERVERS
		events.add(threadName + ": starting 5 ComputeServer threads");
		for (int i = 0; i < 5; i++) {
			(new ServerThread(manager, "ComputeServer", i)).start();
		}

		// ALLOWING SERVERS TO LOGIN
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// CREATING AND SPECIFYING AN JOB THAT WILL REQUIRE 2 COMPUTESERVERS
		JobRequest job01 = new JobRequest("job01");
		job01.put("ComputeServer", 2);
		events.add(threadName + ": calling specifyJob(" + job01.toString() + ")");
		events.add(threadName + ": expect 2 ComputeServers to be released and 3 to remain blocked");
		manager.specifyJob(job01);

		// ALLOWING THE TIME FOR SERVERS TO BE RELEASED
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// PRINTING THE EVENT LOG
		System.out.println("UR1 Test - Event log:");
		for (String event : events)
			System.out.println(event);
	}

	// EXAMPLE TEST CASE 2
	// --------------------------------------------------------------------------------------------------------------------------
	public void exampleUR2Test() {
		// This test
		// 1. starts four ServerThreads of type = "ComputeServer" and one of type
		// "StorageServer"
		// Each of these threads calls: manager.serverLogin(type, ID);
		// 2. main thread sleeps to allow ServerThreads to start and run
		// 3. One job is specified ("job01") that requires two ComputeServers and one
		// StorageServer
		// jobName=job01, job={ComputeServer=2, StorageServer=1}
		// 4. Your JobManager should now release two ComputeServers and one
		// StorageServer to run
		// 5. A ConcurrentLinkedQueue 'events' is used to collect ServerThreads events
		// in a non-blocking way.
		// 6. The printout should be of the following form:
		// Thread Releases:
		// Thread-0: server_type=ComputeServer, job=job01, ID=100 -- released by
		// jobManager.
		// Thread-4: server_type=StorageServer, job=job01, ID=100 -- released by
		// jobManager.
		// Thread-1: server_type=ComputeServer, job=job01, ID=100 -- released by
		// jobManager.
		// Note that
		// a) the above lines can be in any order.
		// b) value of ID does not matter in this example as there is only one job
		// c) the names of the threads ('Thread-0' etc. can change)

		events = new ConcurrentLinkedQueue<String>(); // We are using this
		JobManager manager = new JobManager();

		// Start four "ComputeServer"s and one "StorageServer":
		events.add(threadName + ": starting 4 ComputeServers and 1 StorageServer:"); // "wait-free" FIFO queue
		for (int i = 0; i < 4; i++)
			(new ServerThread(manager, "ComputeServer", 100)).start();
		(new ServerThread(manager, "StorageServer", 100)).start();

		// Sleep main to allow ServerThreads to execute:
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Create job request for two "ComputeServer"s and one "StorageServer":
		JobRequest job01 = new JobRequest("job01");
		job01.put("ComputeServer", 2);
		job01.put("StorageServer", 1);
		events.add(threadName + ": calling specifyJob(" + job01.toString() + ")");
		manager.specifyJob(job01);

		// Sleep main to allow ServerThreads to complete:
		events.add(threadName + ": Sleeping main to allow Servers time to be released");
		events.add(threadName + ": Expect 2 ComputeServer and 1 StorageServer to be released:");

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Now print out the event log:
		System.out.println("Event log:");
		for (String event : events)
			System.out.println(event);
	}

//	// TEST CASE UR2 --------------------------------------------------------------------------------------------------------------------------
//	// MIXED SERVER LOGINS ARE FOLLOWED BY AN MULTIPLE JOB REQUEST
//	public void userRequirement2() {
//		// INITIALIZE EVENT LOG AND JOB MANAGER
//		events = new ConcurrentLinkedQueue<String>();
//		JobManager manager = new JobManager();
//
//		// LOGGINT AN EVENT FOR THE STARTING THE TEST
//		events.add(threadName + ": --- Testing UR2: Mixed Server Logins followed by Multiple Job requests ---");
//
//		// STARTING THE TYPES OF SERVER
//		events.add(threadName + ": starting 3 ComputeServers and 2 StorageServers");
//		// STARTING 5 COMPUTESERVERS
//		for (int i = 0; i < 3; i++) {
//			new Thread(new ServerThread(manager, "ComputeServer", i)).start();
//		}
//		// STARTING 3 STORAGE SERVERS
//		for (int i = 0; i < 2; i++) {
//			new Thread(new ServerThread(manager, "StorageServer", i)).start();
//		}
//
//		// PROVIDING TIME FOR ALLOWING THE SERVERS TO LOGIN
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
//		// CREATING AND SPECIFYING THE MULTIPLE JOBS
//		// USING 2 COMPUTE SERVER
//		// USING 1 STORAGE SERVER
//		JobRequest job01 = new JobRequest("uniqueJob01");
//		job01.put("ComputeServer", 2);
//		job01.put("StorageServer", 1);
//
//		// USING 1 COMPUTE SERVER
//		// USING 1 STORAGE SERVER
//		JobRequest job02 = new JobRequest("uniqueJob02");
//		job02.put("ComputeServer", 1);
//		job02.put("StorageServer", 1);
//
//		// SPECIFYING THE JOBS IN SEQUENCE
//		events.add(threadName + ": calling specifyJob(" + job01.toString() + ")");
//		manager.specifyJob(job01);
//
//		// PROVIDING TIME FOR THE FIRST JOB TO BE PROCESSED
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
//		events.add(threadName + ": calling specifyJob(" + job02.toString() + ")");
//		manager.specifyJob(job02);
//
//		// ALLOWING TIME FOR THE SERVERS TO PROCESS THE JOB
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
//		// PRINTING THE EVENT LOG
//		System.out.println("UR2 Test - Event log:");
//		for (String event : events) {
//			System.out.println(event);
//		}
//	}

	// TEST CASE UR3
	// --------------------------------------------------------------------------------------------------------------------------
	// MULTIPLE JOB REQUEST FOLLOWED BY MIXED SERVER LOGINS
	public void userRequirement3() {
		// INITIALIZING THE EVENT LOG AND JOB MANAGER
		events = new ConcurrentLinkedQueue<String>();
		// CREATING A NEW JOBMANAGER INSTANCE
		JobManager manager = new JobManager();

		events.add(threadName + ": --- Testing UR3: Multiple Job Requests followed by Mixed Server Logins ---");

		// CREATING AND SPECIFYING MULTIPLE JOBS
		// USING 2 COMPUTE SERVERS
		JobRequest job01 = new JobRequest("job01");
		job01.put("ComputeServer", 2);
		// USING 2 STORAGE SERVERS
		JobRequest job02 = new JobRequest("job02");
		job02.put("StorageServer", 2);

		// SPECIFYING AND LOG THE JOBS BEFORE ANY SERVERS COULD LOG IN
		events.add(threadName + ": calling specifyJob(" + job01.toString() + ")");
		manager.specifyJob(job01);
		// STARTING THE SERVER THREADS AFTER THE JOBS ARE SPECIFIED
		events.add(threadName + ": calling specifyJob(" + job02.toString() + ")");
		manager.specifyJob(job02);

		// STARTING 3 COMPUTE SERVERS
		events.add(threadName + ": starting servers after job specification");
		for (int i = 0; i < 3; i++) {
			(new ServerThread(manager, "ComputeServer", i)).start();
		}
		// STARTING 3 STORAGE SERVERS
		for (int i = 0; i < 3; i++) {
			(new ServerThread(manager, "StorageServer", i)).start();
		}
		// PROVIDING TIME FOR THE SERVERS TO LOGIN AND GET RELEASED
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// PRINTING THE EVENT LOG
		System.out.println("UR3 Test - Event log:");
		for (String event : events)
			System.out.println(event);
	}

	// TEST CASE UR4
	// --------------------------------------------------------------------------------------------------------------------------
	// ANY ORDER OF SERVER LOGINS AND JOB REQUESTS
	public void userRequirement4() {
		// INITIALIZE EVENT LOG AND JOB MANAGER
		events = new ConcurrentLinkedQueue<String>();
		JobManager manager = new JobManager();

		// LOGGING THE START OF THE TEST CASE
		events.add(threadName + ": --- Testing UR4: Any order of Server Logins and Job Requests ---");

		// STARTING A FEW SERVERS
		// 2 COMPUTE SERVERS
		// 1 STORAGE SERVERS
		events.add(threadName + ": starting 2 ComputeServers and 1 StorageServer initially");
		for (int i = 0; i < 2; i++) {
			(new ServerThread(manager, "ComputeServer", i)).start();
		}
		(new ServerThread(manager, "StorageServer", 0)).start();

		// PROVIDING TIME FOR SERVERS TO LOG IN BEFORE SPECIFYING A JOB
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// SPECIFYING FIRST JOB THAT REQUIRES MORE SERVERS THAN CURRENTLY AVAILABLE
		JobRequest job01 = new JobRequest("job01");
		job01.put("ComputeServer", 3);
		job01.put("StorageServer", 2);

		// STARTING MORE SERVERS TO FULFILL PENDING JOBS
		events.add(threadName + ": calling specifyJob(" + job01.toString() + ")");
		manager.specifyJob(job01);

		events.add(threadName + ": starting 2 more ComputeServers and 1 more StorageServer");
		for (int i = 2; i < 4; i++) {
			(new ServerThread(manager, "ComputeServer", i)).start();
		}
		(new ServerThread(manager, "StorageServer", 1)).start();

		// PROVIDING TIME FOR SERVERS TO LOG IN
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// SECOND JOB SPECIFICATION AFTER MORE NUMBER OF SERVERS HAVE BEEN LOGGED IN
		JobRequest job02 = new JobRequest("job02");
		job02.put("ComputeServer", 1);
		job02.put("StorageServer", 1);

		events.add(threadName + ": calling specifyJob(" + job02.toString() + ")");
		manager.specifyJob(job02);

		// STARTING THE FINAL COMPUTE SERVER TO COMPLETE JOB ASSIGNMENTS
		events.add(threadName + ": starting final ComputeServer to complete job01");
		(new ServerThread(manager, "ComputeServer", 4)).start();

		// PROVIDING TIME FOR JOBS TO BE PROCESSED
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// PRINTING THE EVENT LOG TO VERIFY THE TEST RESULTS
		System.out.println("UR4 Test - Event log:");
		for (String event : events)
			System.out.println(event);
	}

	// TEST CASE UR5
	// --------------------------------------------------------------------------------------------------------------------------
	// JOB NAMES THAT HAVE BEEN RETURNED MUST MATCH WITH THE JOB SPECIFICATIONS
	public void userRequirement5() {
		// INITIALIZE EVENT LOG AND JOB MANAGER
		events = new ConcurrentLinkedQueue<String>();
		JobManager manager = new JobManager();

		events.add(threadName + ": --- Testing UR5: Returned JobNames must match Job Specifications ---");

		// CREATE JOB REQUESTS WITH SPECIFIC SERVER REQUIREMENTS
		// USING 2 COMPUTE SERVERS
		JobRequest job01 = new JobRequest("job01");
		job01.put("ComputeServer", 2);

		// SPECIFYING THE JOB TO THE JOB MANAGER
		events.add(threadName + ": calling specifyJob(" + job01.toString() + ")");
		manager.specifyJob(job01);

		// USING 1 STORAGE SERVER
		JobRequest job02 = new JobRequest("job02");
		job02.put("StorageServer", 1);

		// SPECIFYING THE JOB TO THE JOB MANAGER
		events.add(threadName + ": calling specifyJob(" + job02.toString() + ")");
		manager.specifyJob(job02);

		// START SERVERS NEEDED FOR JOB EXECUTION
		// USING 4 COMPUTE SERVERS
		// USING 2 STORAGE SERVERS
		events.add(threadName + ": starting 4 ComputeServers and 2 StorageServers");
		for (int i = 0; i < 4; i++) {
			(new ServerThread(manager, "ComputeServer", i)).start();
		}
		for (int i = 0; i < 2; i++) {
			(new ServerThread(manager, "StorageServer", i)).start();
		}

		// PROVIDING TIME FOR SERVERS TO LOGIN AND PROCESS JOB REQUESTS
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// VERIFYING THAT THE CORRECT SERVERS ARE RELEASED AS PER JOB SPECIFICATIONS
		events.add(threadName + ": expect 2 ComputeServers to be released with job01 and 1 StorageServer with job02");

		// PRINT FINAL EVENT LOG FOR TESTING
		System.out.println("UR5 Test - Event log:");
		for (String event : events)
			System.out.println(event);
	}

	// EXAMPLE TEST CASE 6
	// --------------------------------------------------------------------------------------------------------------------------
	public void exampleUR6test() {
		// This test
		// 1. starts five ServerThreads of type = "ComputeServer" and ID = 0 to 4
		// Each of these threads calls: manager.serverLogin(type, ID);
		// 2. main thread sleeps to allow ComputeServers to start and run
		// 3. job01 is specified to require two ComputeServers
		// 4. main sleeps to allow job01 to release the two ComputeServers with the
		// highest IDs (4 & 3)
		// 4. job02 is specified again to require two ComputeServers
		// 5. main sleeps to allow job02 to release the two ComputeServers with the next
		// highest IDs (2 & 1)
		// 6. The printout should be of the following form:
		// ...
		// Thread-4: server_type=ComputeServer, job=job01, ID=4 -- released by
		// jobManager.
		// Thread-3: server_type=ComputeServer, job=job01, ID=3 -- released by
		// jobManager.
		// ...
		// Thread-1: server_type=ComputeServer, job=job02, ID=1 -- released by
		// jobManager.
		// Thread-2: server_type=ComputeServer, job=job02, ID=2 -- released by
		// jobManager.
		// Note that:
		// job1's ComputeServers must have IDs of 3 & 4, but we do not know which will
		// be released first (ID 3 or 4).
		// Similarly, while job2's ComputeServers must have IDs of 1 & 2, we also do not
		// know which will be released first.
		// So:
		// ...
		// Thread-3: server_type=ComputeServer, job=job01, ID=3 -- released by
		// jobManager.
		// Thread-4: server_type=ComputeServer, job=job01, ID=4 -- released by
		// jobManager.
		// ...
		// Thread-2: server_type=ComputeServer, job=job02, ID=2 -- released by
		// jobManager.
		// Thread-1: server_type=ComputeServer, job=job02, ID=1 -- released by
		// jobManager.
		//
		// would also be a correct console output.
		// Note that the names of the threads ('Thread-0' etc. can change)
		//
		events = new ConcurrentLinkedQueue<String>(); // "wait-free" FIFO queue
		JobManager manager = new JobManager();

		JobRequest job01 = new JobRequest("job01");
		job01.put("ComputeServer", 2);
		JobRequest job02 = new JobRequest("job02");
		job02.put("ComputeServer", 2);

		// Start 5 server threads:
		events.add(threadName + ": starting 5 ComputeServers, ID=[0, 1, 2, 3, 4]");
		for (int i = 0; i < 5; i++)
			(new ServerThread(manager, "ComputeServer", i)).start();
		// Sleep main to allow server threads to execute:
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} // sleep 1
		events.add(threadName + ": threads started, now specifying job1.");

		// Specify jobs:
		events.add(threadName + ": calling specifyJob(" + job01.toString() + ")");
		events.add(threadName + ": expect two ComputeServers 'job01' [ID=3&4] to be released:");
		manager.specifyJob(job01);
		// Sleep main to allow job1 server threads to complete:
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} // sleep 2
		events.add(threadName + ": job1 specified, now specifying job2.");
		events.add(threadName + ": calling specifyJob(" + job02.toString() + ")");
		events.add(threadName + ": expect two ComputeServers 'job02' [ID=1&2] to be released:");
		manager.specifyJob(job02);
		// Sleep main to allow job2 server threads to complete:
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} // sleep 3

		System.out.println("Event log:");
		for (String event : events)
			System.out.println(event);
	}

	// TEST CASE UR6
	// --------------------------------------------------------------------------------------------------------------------------
	// COMPUTE SERVERS MUST BE USED IN REVERSE ID ORDER
	public void userRequirement6() {
		// INITIALIZE A THREAD-SAFE QUEUE TO STORE EVENTS FOR LOGGING
		events = new ConcurrentLinkedQueue<>();
		JobManager manager = new JobManager();

		// ADDING LOG MESSAGE TO INDICATE TESTING OF UR6 WITH MULTIPLE SERVER TYPES
		events.add(threadName
				+ ": --- Testing UR6: ComputeServers must be used in Reverse ID Order (Multiple Server Types) ---");

		// DEFINE ARRAYS OF COMPUTE AND STORAGE SERVER IDS (NON-SEQUENTIAL IDS FOR TEST
		// VARIABILITY)
		int[] computeServerIDs = { 10, 5, 20, 15, 1 };
		int[] storageServerIDs = { 100, 200, 50, 150 };

		// START STORAGE SERVERS WITH GIVEN IDS
		events.add(threadName + ": starting " + computeServerIDs.length + " ComputeServers");
		for (int id : computeServerIDs) {
			new ServerThread(manager, "ComputeServer", id).start();
		}

		events.add(threadName + ": starting " + storageServerIDs.length + " StorageServers");
		for (int id : storageServerIDs) {
			new ServerThread(manager, "StorageServer", id).start();
		}

		// PROVIDING TIME FOR SERVERS TO REGISTER BEFORE SUBMITTING JOB REQUESTS
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// CREATE A JOB REQUEST REQUIRING BOTH COMPUTE AND STORAGE RESOURCES
		// USING 3 COMPUTE SERVERS
		// USING 2 STORAGE SERVERS
		JobRequest job01 = new JobRequest("job01");
		job01.put("ComputeServer", 3);
		job01.put("StorageServer", 2);

		// LOG THE JOB REQUEST DETAILS BEFORE SPECIFYING IT IN THE JOB MANAGER
		events.add(threadName + ": calling specifyJob(" + job01.toString() + ")");

		// EXPECT COMPUTE SERVERS TO BE USED IN DESCENDING ORDER OF THEIR IDS (REVERSE
		// ORDER)
		// ARRANGING AND SORTING COMPUTE SERVER IDS
		Arrays.sort(computeServerIDs);
		// ARRANGING AND SORTING STORAGE SERVER IDS
		Arrays.sort(storageServerIDs);
		events.add(threadName + ": expect ComputeServers with IDs " + computeServerIDs[4] + ", " + computeServerIDs[3]
				+ ", " + computeServerIDs[2] + " and StorageServers with IDs " + storageServerIDs[3] + ", "
				+ storageServerIDs[2] + " to be released");

		// SPECIFY JOB IN JOB MANAGER, WHICH SHOULD TRIGGER RESOURCE ALLOCATION
		manager.specifyJob(job01);

		// PROVIDING TIME FOR SERVERS TO PROCESS THE JOB REQUEST
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// PRINT EVENT LOG TO DISPLAY EXPECTED AND ACTUAL RESULTS
		System.out.println("UR6 Test with Multiple Server Types - Event log:");
		for (String event : events) {
			System.out.println(event);
		}
	}

	private class ServerThread extends Thread {
		JobManager manager;
		String type;
		int ID = 100;
		String threadName;

		ServerThread(JobManager manager, String type, int ID) {
			this.manager = manager;
			this.type = type;
			this.ID = ID;
		};

		public void run() {
			this.threadName = Thread.currentThread().getName();
			events.add(threadName + ": started & calling serverLogin(" + type + ", ID=" + ID + ")");
			String job = manager.serverLogin(type, ID);
			events.add(threadName + ": server_type=" + type + ", job=" + job + ", ID=" + ID
					+ " -- released by jobManager.");
		}
	}
}
