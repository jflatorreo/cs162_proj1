package nachos.threads;
import nachos.machine.*;
import nachos.threads.PriorityScheduler.ThreadState;

public class KThread {
	//Fields
	private static final char dbgThread = 't';
	public Object schedulingState = null;
	
	private static final int statusNew = 0;
	private static final int statusReady = 1;
	private static final int statusRunning = 2;
	private static final int statusBlocked = 3;
	private static final int statusFinished = 4;
	
	private int status = statusNew; //0
	private String name = "(unnamed thread)";
	private Runnable target;
	private TCB tcb;
	
	private int id = numCreated++;
	private static int numCreated = 0;
	
	private static ThreadQueue readyQueue = null;
	private static KThread currentThread = null;
	private static KThread toBeDestroyed = null;
	private static KThread idleThread = null;
	
	public KThread joinThread = null; //#1
	public boolean isJoined = false; //#1
	
	public static KThread currentThread() {
		Lib.assertTrue(currentThread != null);
		return currentThread;
	}
	
	//Constructors
	public KThread() {
		if (currentThread != null) tcb = new TCB();
		else {
			readyQueue = ThreadedKernel.scheduler.newThreadQueue(false);
			readyQueue.acquire(this);
			
			currentThread = this; //set this to be the current Thread
			tcb = TCB.currentTCB(); //set tcb to be the currentTCB
			name = "main";
			restoreState();
			createIdleThread();
		}
	}
	
	public KThread(Runnable target) {
		this(); //calling KThread()
		this.target = target; //set target additional to KThread()
	}
	
	//Miscellany Methods
	public KThread setTarget(Runnable target) {
		Lib.assertTrue(status == statusNew);
		this.target = target;
		return this;
	}
	
	public KThread setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return (name + " (#" + id + ")");
	}
	
	public int compareTo(Object o) {
		KThread thread = (KThread) o;
		if (id < thread.id) return -1;
		else if (id > thread.id) return 1;
		else return 0;
	}
	
	//Action Methods
	public void fork() {
		Lib.assertTrue(status == statusNew);
		Lib.assertTrue(target != null);
		Lib.debug(dbgThread, "Forking thread: " + toString() + " Runnable: " + target);
		
		boolean intStatus = Machine.interrupt().disable();
		tcb.start(new Runnable() { //Start TCB with this thread
			public void run() {
				runThread();
			}
		});
		
		ready();
		Machine.interrupt().restore(intStatus); //set Machine.interrupt status
	}
	
	private void runThread() {
		begin();
		target.run();
		finish();
	}
	
	private void begin() {
		Lib.debug(dbgThread, "Beginning thread: " + toString());
		Lib.assertTrue(this == currentThread);
		
		restoreState();
		Machine.interrupt().enable();
	}
	
	public static void finish() {
		Lib.debug(dbgThread, "Finishing thread: " + currentThread.toString());
		
		Machine.interrupt().disable();
		Machine.autoGrader().finishingCurrentThread();
		
		Lib.assertTrue(toBeDestroyed == null);
		
		toBeDestroyed = currentThread;
		currentThread.status = statusFinished;
		
		//#1
		if (currentThread.isJoined) {
			currentThread.joinThread.ready();
		}
		
		sleep();
	}
	
	public static void sleep() {
		Lib.debug(dbgThread, "Sleeping thread: " + currentThread.toString());
		Lib.assertTrue(Machine.interrupt().disabled());
		
		if (currentThread.status != statusFinished)
			currentThread.status = statusBlocked;
		runNextThread();
	}
	
	public void ready() {
		Lib.debug(dbgThread, "Ready thread: " + toString());
		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(status != statusReady);
		
		status = statusReady;
		if (this != idleThread)
			readyQueue.waitForAccess(this);
		Machine.autoGrader().readyThread(this);
	}
	
	public void join() {
		Lib.debug(dbgThread, "Joining to thread: " + toString());
		Lib.assertTrue(this != currentThread);
		
		//#1
		Machine.interrupt().disable();
		if (this != currentThread && this.status != statusFinished && this.isJoined != true) {
			this.joinThread = currentThread;
			this.isJoined = true;
			sleep();
			//Donate effectivePrioroity
			
			ThreadState currState = (ThreadState) currentThread.schedulingState;
			((ThreadState) this.schedulingState).setEffectivePriority(currState);
		}
		Machine.interrupt().enable();
	}
	
	public static void yield() {
		Lib.debug(dbgThread, "Yielding thread: " + currentThread.toString());
		System.out.println("Yielding thread: " + currentThread.toString());
		Lib.assertTrue(currentThread.status == statusRunning);
		
		boolean intStatus = Machine.interrupt().disable();
		currentThread.ready();
		runNextThread();
		Machine.interrupt().restore(intStatus);
	}
	
	private static void createIdleThread() {
		Lib.assertTrue(idleThread == null);
		
		idleThread = new KThread(new Runnable() {
			public void run() {
				while (true) yield();
			}
		});
		idleThread.setName("idle");
		Machine.autoGrader().setIdleThread(idleThread);
		idleThread.fork();
	}
	
	private static void runNextThread() {
		KThread nextThread = readyQueue.nextThread();
		if (nextThread == null)
			nextThread = idleThread;
		nextThread.run();
	}
	
	private void run() {
		Lib.assertTrue(Machine.interrupt().disabled());
		
		Machine.yield(); //equal to Thread.yield()
		currentThread.saveState(); 
		
		Lib.debug(dbgThread, "Switching from: " + currentThread.toString() + " to: " + toString());
		
		currentThread = this;
		tcb.contextSwitch(); //this makes sure that the current thread is bound to the current TCB. This check can only fail if non-Nachos threads invoke start().
		currentThread.restoreState();
	}
	
	protected void restoreState() {
		Lib.debug(dbgThread, "Running thread: " + currentThread.toString());
		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(this == currentThread);
		Lib.assertTrue(tcb == TCB.currentTCB());
		Machine.autoGrader().runningThread(this);
		
		status = statusRunning;
		if (toBeDestroyed != null) {
			toBeDestroyed.tcb.destroy();
			toBeDestroyed.tcb = null;
			toBeDestroyed = null;
		}
	}
	
	protected void saveState() {
		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(this == currentThread);
	}
	
	public static void selfTest() {
		Lib.debug(dbgThread, "Enter KThread.selfTest");
		
		new KThread(new PingTest(1)).setName("forked thread").fork();
		new PingTest(0).run();
	}
	
	private static class PingTest implements Runnable {
		//Fields
		private int which;
		
		//Constructor
		PingTest(int which) {
			this.which = which;
		}
		
		public void run() {
			for (int i=0; i<5; i++) {
				System.out.println("*** thread " + which + " looped " + i + " times");
				currentThread.yield();
			}
		}
	}
}
		
		