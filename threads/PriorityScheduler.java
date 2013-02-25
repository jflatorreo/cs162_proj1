package nachos.threads;

import nachos.machine.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.lang.Math;
 
public class PriorityScheduler extends Scheduler {
	//Fields
    public static final int priorityDefault = 1;
    public static final int priorityMinimum = 0;
    public static final int priorityMaximum = 7;  
	public boolean transferPriority;
	
	//Constructor
	public PriorityScheduler() {
    }

	//Helper Methods
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
		Lib.assertTrue(priority >= priorityMinimum && priority <= priorityMaximum);
		getThreadState(thread).setPriority(priority);
    }
	
	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);
		return (ThreadState) thread.schedulingState;
    }
	
	//Action Methods
    public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new PriorityQueue(transferPriority);
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

    protected class PriorityQueue extends ThreadQueue {
		//Fields
		public ThreadState holder;
		public TreeSet<ThreadState> waitQueue ; //max effectivePriority/time pops first.
		public boolean transferPriority;
		
		//Constructor
		PriorityQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
			this.holder = null;
			
			waitQueue = new TreeSet<ThreadState>(new Comparator<ThreadState>() {
				public int compare(ThreadState ts1, ThreadState ts2) {
					if (ts1.getEffectivePriority() == ts2.getEffectivePriority())
						return new Long(ts2.time).compareTo(ts1.time);
					return (new Integer(ts1.getEffectivePriority()).compareTo(ts2.getEffectivePriority()));
				}
			});
		}
		
		//Helper Methods
		/*
		protected ThreadState pickNextThread() {
			assert (false);
			if (this.waitQueue.isEmpty())
				return null;
			return this.waitQueue.last(); //retrieve top element.
		}*/
		
		//Action Methods
		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).waitForAccess(this);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).acquire(this);
		}

		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());
			ThreadState newHolder = this.waitQueue.pollLast(); //return null if waitQueue is empty
			if (newHolder != null) {
				this.acquire(newHolder.thread);
				return newHolder.thread;
			}
			else {
				this.holder = null;
			    return null;
			}
		}
		
		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
			//TODO (optional)
		}
    }

    protected class ThreadState {
		//Fields
		protected KThread thread;
		protected int priority;
		public int effectivePriority;
		public ArrayList<PriorityQueue> pqHave;
		public PriorityQueue pqWant;
		public long time;
		
		
		//Constructor
		public ThreadState(KThread thread) {
			this.thread = thread;
			setPriority(priorityDefault);
			pqHave = new ArrayList<PriorityQueue>();
			pqWant = null;
			time = 0; //FIXME
		}

		//Helper Methods
		public int getPriority() {
			return priority;
		}
		
		public int getEffectivePriority() {
			return effectivePriority;
		}
		
		public void setPriority(int priority) {
			this.priority = priority;
			this.effectivePriority = priority;
		}
		
		public void setEffectivePriority(ThreadState donator) {
		    this.effectivePriority = java.lang.Math.max(this.priority, java.lang.Math.max(this.effectivePriority, java.lang.Math.max(donator.priority, donator.effectivePriority)));
			//Recursion here
			if (this.pqWant != null && pqWant.holder != this) {
				this.pqWant.holder.setEffectivePriority(this);
			}
		}
		
		public void updateEffectivePriority() {
			int maxPriority = -1;
			for (PriorityQueue pq: pqHave) {
				maxPriority = java.lang.Math.max(maxPriority, pq.holder.getEffectivePriority());
			}
			this.effectivePriority = java.lang.Math.max(this.priority, maxPriority);
		}
		
		//Action Methods
		public void waitForAccess(PriorityQueue pq) {
			//Add this ThreadState to pq.waitQueue
			//assert (pq.holder != null);
			if (this.pqHave.contains(pq) == true)
				this.pqHave.remove(pq);
			this.pqWant = pq;
			pq.waitQueue.add(this);

			if (pq.transferPriority == true)
				pq.holder.setEffectivePriority(this);
		}
		
		public void acquire(PriorityQueue pq) {
			//Adjust the state of prevHolder of pq
			ThreadState prevHolder = pq.holder;
			if (prevHolder != null)
				prevHolder.pqHave.remove(pq);
			
			//Set this ThreadState to the holder of pq
			if (this.pqWant == pq)
				this.pqWant = null;
			this.pqHave.add(pq);
			pq.holder = this;
			
			//Set this ThreadState's effectivePriority back to its former state
			/*
			tempPriority = priorityMinimum;
			pqHaveAry = prevHolder.pqHave.toArray();
			for (i=0; i<pqHave.size(); i++) {
				if (tempPriority < pqHaveAry[i].waitQueue.first().effectivePriority)
					tempPriority = pqHaveAry[i].waitQueue.first().effectivePriority;
			}
			if (prevHolder.priority < tempPriority)
				prevHolder.effectivePriority = tempPriority;
			else
				prevHolder.effectivePriority = prevHolder.priority;
			*/
			if (pq.transferPriority == true)
				this.updateEffectivePriority();
		}	
    }
}