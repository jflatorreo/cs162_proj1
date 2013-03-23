package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;


public class LotteryScheduler extends Scheduler {
	public LotteryScheduler() {
	}
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new PriorityThreadQueue(transferPriority);
	}

	public int getPriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getPriority();
	}

	public int getEffectivePriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getEffectivePriority();
	}

	public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());

		Lib.assertTrue(priority >= priorityMinimum &&
				priority <= priorityMaximum);

		getThreadState(thread).setPriority(priority);
	}

	public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMaximum)
			return false;

		setPriority(thread, priority+1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMinimum)
			return false;

		setPriority(thread, priority-1);

		Machine.interrupt().restore(intStatus);
		return true;
	}


	/**
	 * The default priority for a new thread. Do not change this value.
	 */
	public static final int priorityDefault = 1;
	/**
	 * The minimum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMinimum = 1;
	/**
	 * The maximum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMaximum = Integer.MAX_VALUE;

	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
	}

	public static void selfTest() {
		
	}

	protected class PriorityThreadQueue extends ThreadQueue{
		PriorityThreadQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
			this.dequeuedThread = null;
		}

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).waitForAccess(this);
		}

		public void addToQueue(ThreadState threadState) {
			this.priorityQueue.add(threadState);
			//this.priorityQueue = new PriorityQueue<ThreadState>(priorityQueue);

		}

		public void removeFromQueue(ThreadState threadState) {
			this.priorityQueue.remove(threadState);
			//this.priorityQueue = new PriorityQueue<ThreadState>(priorityQueue);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).acquire(this);
		}
		public KThread nextThread(){
			int totTickets = 0;
			LinkedList<ThreadState> threads = new LinkedList<ThreadState>();
			LinkedList<Integer> tickets = new LinkedList<Integer>();
			Iterator<ThreadState> queue = priorityQueue.iterator();
			for (int i = 0; queue.hasNext(); i++){
				ThreadState current = queue.next();
				int ticketNum = (Integer)current.getEffectivePriority();
				totTickets += ticketNum;
				threads.add(current);
				tickets.add(ticketNum);
			}
			ThreadState pickedThread = null;
			boolean notFound = true;
			int ticketsSoFar = 0;
			if (totTickets > 0){
				Integer ticketChoice = generator.nextInt(totTickets);
				for(int j = 0; notFound && j < tickets.size(); j++){
					ticketsSoFar += tickets.get(j);
					if ((ticketChoice - ticketsSoFar) <= -1){
						pickedThread = threads.get(j);
						notFound = false;
					}
				}
			}
			if (transferPriority && pickedThread != null) {
				if (this.dequeuedThread != null) {
					this.dequeuedThread.removeQueue(this);
				}
				pickedThread.waiting = null;
				pickedThread.addQueue(this);
			}
			this.dequeuedThread = pickedThread;
			if (this.dequeuedThread != null){
				this.priorityQueue.remove(dequeuedThread);
				this.dequeuedThread.calcEffectivePriority();
				return this.dequeuedThread.thread;
			}
			else
				return null;
		}

		protected ThreadState pickNextThread() {
			boolean intStatus = Machine.interrupt().disable();

			Machine.interrupt().restore(intStatus);
			return this.priorityQueue.peek();
		}
		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
		}

		protected PriorityQueue<ThreadState> priorityQueue = new PriorityQueue<ThreadState>();
		public ThreadState dequeuedThread;
		public boolean transferPriority;

		protected Random generator = new Random();
	}
	protected class ThreadState implements Comparable<ThreadState>{
		public ThreadState(KThread thread) {
			this.thread = thread;
			//initialize the onQueue linkedlist
			this.onQueues = new LinkedList<PriorityThreadQueue>();
			this.age = Machine.timer().getTime();
			this.priority = priorityDefault; 
			//this.calcEffectivePriority();
			this.waiting = null;
		}

		public int getPriority() {
			return priority;
		}
		public void calcEffectivePriority() {
			int initialEffective = this.getEffectivePriority();
			int initialPriority = this.getPriority();
			int outsideEP = 0;
			if (this.onQueues.size() != 0){
				int size = this.onQueues.size();
				for(int i = 0; i < size; i++){
					PriorityThreadQueue current = onQueues.get(i);
					Iterator<ThreadState> threadIT = current.priorityQueue.iterator();
					while(threadIT.hasNext()){
						ThreadState currentThread = threadIT.next();
						outsideEP += currentThread.getEffectivePriority();
					}

				}
			}
			int totEP = initialPriority + outsideEP;
			this.effectivePriority = totEP;
			if (this.waiting != null && this.waiting.dequeuedThread != null){
				this.waiting.dequeuedThread.addToAllEffective(totEP - initialEffective);
			}
		}

		public int getEffectivePriority() {
			return this.effectivePriority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
			this.calcEffectivePriority();
		}

		public void addToAllEffective(int diff){
			this.effectivePriority += diff;
			if (this.waiting != null && this.waiting.dequeuedThread != null){
				this.waiting.dequeuedThread.addToAllEffective(diff);
			}
		}

		public void waitForAccess(PriorityThreadQueue waitQueue) {
			Lib.assertTrue(Machine.interrupt().disabled());
			long time = Machine.timer().getTime();
			this.age = time;
			waitQueue.addToQueue(this);
			if (waitQueue.transferPriority){
				this.waiting = waitQueue;
			}
			this.calcEffectivePriority();
			if (waitQueue.dequeuedThread != null) {
				waitQueue.dequeuedThread.calcEffectivePriority();
			}
		}

		public void acquire(PriorityThreadQueue waitQueue) {
			Lib.assertTrue(Machine.interrupt().disabled());
			Lib.assertTrue(waitQueue.priorityQueue.isEmpty());
			waitQueue.dequeuedThread = this;
			if (waitQueue.transferPriority) {
				this.addQueue(waitQueue);
			}
			this.calcEffectivePriority();
		}

		public int compareTo(ThreadState threadState){
			if (threadState == null)
				return -1;
			if (this.getEffectivePriority() < threadState.getEffectivePriority()){
				return 1;
			}else{ if (this.getEffectivePriority() > threadState.getEffectivePriority()){
				return -1;
			}else{
				if (this.age >= threadState.age)
					return 1;
				else{ return -1; }
			}
			}
		}

		public void removeQueue(PriorityThreadQueue queue){
			onQueues.remove(queue);
			this.calcEffectivePriority();
		}
		public void addQueue(PriorityThreadQueue queue){
			onQueues.add(queue);
			this.calcEffectivePriority();
		}

		public String toString() {
			return "ThreadState thread=" + thread + ", priority=" + getPriority() + ", effective priority=" + getEffectivePriority();
		}
		protected KThread thread;
		protected int priority;
		public long age = Machine.timer().getTime();
		protected LinkedList<PriorityThreadQueue> onQueues;
		protected int effectivePriority;
		protected PriorityThreadQueue waiting;
		protected boolean dirty;
	}
}