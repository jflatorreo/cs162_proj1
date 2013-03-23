package nachos.threads;

import nachos.machine.*;
import nachos.threads.PriorityScheduler.PriorityQueue;
import nachos.threads.PriorityScheduler.ThreadState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends Scheduler {
	  //Fields
	  public static final int priorityDefault = 1;
	  public static final int priorityMinimum = 1;
	  public static final int priorityMaximum = Integer.MAX_VALUE;
	  public static int TickTimer = 0;

	    /**
	     * Allocate a new lottery scheduler.
	     */
	    /**
	     * Allocate a new lottery thread queue.
	     *
	     * @param	transferPriority	<tt>true</tt> if this queue should
	     *					transfer tickets from waiting threads
	     *					to the owning thread.
	     * @return	a new lottery thread queue.
	     */

		//Constructor
		public LotteryScheduler() {
			System.out.println("LS");
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
		
	
	    //DONE!!!!
	    protected ThreadState getThreadState(KThread thread) {
			if (thread.schedulingState == null)
				thread.schedulingState = new ThreadState(thread);
			return (ThreadState) thread.schedulingState;
	    }
	    
		//Action Methods
	    public ThreadQueue newThreadQueue(boolean transferPriority) {
	        return new LotteryQueue(transferPriority);
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
	  
    
    protected class LotteryQueue extends ThreadQueue {		//Fields
		public ThreadState holder;
		public TreeSet<ThreadState> waitQueue ; //max effectivePriority/time pops first.
		public boolean transferPriority;

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).waitForAccess(this);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).acquire(this);
		}
		
        //In terms of picking the next thread linear in the number of threads on the queue is fine
		//Constructor
		LotteryQueue(boolean transferPriority) {
			System.out.println("LQ");
			this.transferPriority = transferPriority;
			this.holder = null;
			waitQueue = new TreeSet<ThreadState>(new Comparator<ThreadState>() {
				public int compare(ThreadState ts1, ThreadState ts2) {
					if (ts1.getEffectivePriority() < ts2.getEffectivePriority())
						return -1;
					else if (ts1.getEffectivePriority() == ts2.getEffectivePriority()) 
						return new Integer(ts2.time).compareTo(ts1.time);
					else return 1;
				}
			});
		}
        
        public void updateEntry(ThreadState ts, int newEffectivePriority) {
        	System.out.println("uE");
            int difference = newEffectivePriority - ts.getEffectivePriority();
            if(this.waitQueue.remove(ts)) {
            	ts.effectivePriority = newEffectivePriority;
            	this.waitQueue.add(ts);
            	if(difference != 0)
            		((ThreadState)(ts)).propagate(difference);
		    } else {
		    	if(holder != ts) {
		    		//entering here doesnt make any sense
		    	} else {
		    		if(ts.pqWant != null) {
		    			((LotteryQueue)(ts.pqWant)).updateEntry(ts, newEffectivePriority);
		    		} else {
		    			ts.effectivePriority = newEffectivePriority;
		    		}
		    	}
		    }
            //propagate
            //if(difference != 0)
            //    ts.propagate(difference);
        }
        
        
        //DONE!!!!!
        protected ThreadState pickNextThread() {
        	System.out.println("piNT");
            //Set up an Iterator and go through it
            Random randomGenerator = new Random();
            int ticketCount = 0;
            Iterator<ThreadState> itr = this.waitQueue.iterator();
            while(itr.hasNext()) {
                //System.out.println("ticketCount is " + ticketCount);
                ThreadState next = itr.next();
                //System.out.println("thread: " + next.thread.getName());
                ticketCount += next.getEffectivePriority();
            }
            //System.out.println("ticketCount is " + ticketCount);

            if(ticketCount > 0) {
                int num = randomGenerator.nextInt(ticketCount);
                itr = this.waitQueue.iterator();
                ThreadState temp;
                while(itr.hasNext()) {
                    temp = itr.next();
                    num -= temp.effectivePriority;
                    if(num <= 0){
                        return temp;
                    }
                }
            }
            return null;
        }
        
		public KThread nextThread() {
			System.out.println("NT");
			Lib.assertTrue(Machine.interrupt().disabled());
			ThreadState newHolder = this.pickNextThread(); //return null if waitQueue is empty

			if (newHolder != null) { //When waitQueue is not empty
				this.acquire(newHolder.thread);
				System.out.println("NT-rt-1-"+ newHolder.toString().substring(44));
				return newHolder.thread;
			}
			else { //When waitQueue is empty
				this.holder = null;
				System.out.println("NT-rt-2");				
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
		public ArrayList<LotteryQueue> pqHave; //List of resources it has
		public LotteryQueue pqWant;   //The next resource it wants
		public int time;
		
		
		//Constructor
		public ThreadState(KThread thread) {
			System.out.println("TS Const");
			this.thread = thread;
			this.pqHave = new ArrayList<LotteryQueue>();
			this.setPriority(priorityDefault);
			this.pqWant = null;
			this.time = Integer.MAX_VALUE;
		}
		//Helper Methods
		public int getPriority() {
			return this.priority;
		}
		
		public int getEffectivePriority() {
			return this.effectivePriority;
		}		
		
        //DONE!!!!
        public void setPriority(int newPriority) {
            this.priority = newPriority;
            this.updateEffectivePriority();
        }
        //DONE!!!!
        public void propagate(int difference) {
            if(pqWant != null) {
                if(pqWant.transferPriority == true) {
                    if(pqWant.holder != null)
                        ((LotteryQueue)pqWant).updateEntry(pqWant.holder, pqWant.holder.effectivePriority+difference);
                }
            }
        }
        
        public void propagateUp(int difference) {
        	if (this.pqWant != null && this.pqWant.transferPriority == true) {
        		ThreadState recipient = this.pqWant.holder;
        		if (recipient.pqWant == null)
        			recipient.effectivePriority += difference;
        		else {
        			recipient.pqWant.waitQueue.remove(recipient);
        			recipient.effectivePriority += difference;
        			recipient.pqWant.waitQueue.add(recipient);
        		}
        	}
        	return;
        }
        
        
        //DONE!!!!
        public void updateEffectivePriority() {
        	System.out.println("uEP");
            //Calculate new effectivePriority checking possible donations from threads that are waiting for me
            int sumPriority = this.priority;
            for (LotteryQueue pq: this.pqHave)
                if (pq.transferPriority == true) {
                    Iterator<ThreadState> itr = pq.waitQueue.iterator();
                    while(itr.hasNext())
                        sumPriority += itr.next().getEffectivePriority();
                }
            
            //If there is a change in priority, update and propagate to other owners
            if (sumPriority != this.effectivePriority) {
                int difference = sumPriority - this.effectivePriority;
                if (pqWant != null) {
                    pqWant.waitQueue.remove(this);
                    this.effectivePriority = sumPriority;
                    pqWant.waitQueue.add(this);
                    this.propagate(difference);
                } else {
                    this.effectivePriority = sumPriority;
                }
            }
        }
        //DONE!!!!
        public void waitForAccess(LotteryQueue pq) {
        	System.out.println("wFA" + this.toString().substring(44));
        	for (ThreadState ts: pq.waitQueue) System.out.print(ts.toString().substring(44) + ", ");
        	System.out.println();
			this.pqWant = pq;
			//this.time = Machine.timer().getTime();
			this.time = TickTimer++;
			pq.waitQueue.add(this);
			//Propagate this ThreadState's effectivePriority to holder of pq
            if (pq.transferPriority == true) {
                if(pq.holder != null)
                    ((LotteryQueue)pq).updateEntry(pq.holder, pq.holder.effectivePriority+this.effectivePriority);
            }
		}
        
        public void acquire(LotteryQueue pq) {
        	System.out.println("Aq");
			//Adjust the state of prev pq holder (ThreadState)
			ThreadState prevHolder = pq.holder;
			if (prevHolder != null) {
				prevHolder.pqHave.remove(pq);
				if (pq.transferPriority == true)
					((ThreadState)(prevHolder)).updateEffectivePriority();
			}

			//Adjust the state of this ThreadState 
			if (this.pqWant != null && this.pqWant.equals(pq))
				this.pqWant = null;
			this.pqHave.add(pq);
			
			//Adjust the state of pq
            // System.out.println("pq.waitQueue.contains(this) == " + pq.waitQueue.contains(this));
			pq.waitQueue.remove(this);
            // System.out.println("pq.waitQueue.contains(this) == " + pq.waitQueue.contains(this));
			pq.holder = this;
            this.updateEffectivePriority();
		}	
    }
    
    public static void selfTest() {
    	/*
        LotteryScheduler ls = new LotteryScheduler();
        LotteryQueue[] pq = new LotteryQueue[5];
        KThread[] t = new KThread[5];
        ThreadState lts[] = new ThreadState[5];
        
        for (int i=0; i < 5; i++)
            pq[i] = ls.new LotteryQueue(true);
        for (int i=0; i < 5; i++) {
            t[i] = new KThread();
            t[i].setName("thread" + i);
            lts[i] = ls.getThreadState(t[i]);
        }
        
        Machine.interrupt().disable();
        
        //System.out.println("===========LotteryScheduler Test============");

        Lib.assertTrue(pq[0].waitQueue.size()==0);
        pq[0].acquire(t[0]);
	Lib.assertTrue(pq[0].holder.thread.equals(t[0]));
	Lib.assertTrue(pq[0].waitQueue.size()==0);
        System.out.println("pq[0].acquire(t[0])");
	Lib.assertTrue(pq[0].holder.effectivePriority == 1);

        lts[0].setPriority(5);
	Lib.assertTrue(lts[0].priority == 5);
	Lib.assertTrue(lts[0].effectivePriority == 5);
        System.out.println("lts[0].setPriority(5)");
        
        pq[0].waitForAccess(t[1]);
        System.out.println("pq[0].waitForAccess(t[1])");
	Lib.assertTrue(lts[0].priority == 5);
	Lib.assertTrue(lts[0].effectivePriority == 6);
updateEffectivePriority
        Lib.assertTrue(pq[0].waitQueue.size() == 1);
        KThread temp = pq[0].pickNextThread().thread;
        System.out.println("pq[0].pickNextThread()");
	//System.out.println("nextThread is " + temp.getName());
        Lib.assertTrue(temp != null);
        
        pq[0].waitForAccess(t[2]);
        System.out.println("pq[0].waitForAccess(t[2])");
	Lib.assertTrue(pq[0].waitQueue.size() == 2);
	Lib.assertTrue(lts[0].priority == 5);
	Lib.assertTrue(lts[0].effectivePriority == 7);
        
        lts[1].setPriority(3);
        System.out.println("lts[1].setPriority(3)");
        Lib.assertTrue(lts[1].priority == 3);
	Lib.assertTrue(lts[1].effectivePriority == 3);
	System.out.println("lts[0].ep = " + lts[0].effectivePriority);
	Lib.assertTrue(lts[0].effectivePriority == 9);
        
        
        lts[2].setPriority(6);
        System.out.println("lts[2].setPriority(6)");
        Lib.assertTrue(lts[2].priority == 6);
	Lib.assertTrue(lts[2].effectivePriority == 6);
	Lib.assertTrue(lts[0].effectivePriority == 14);
        
        temp = pq[0].nextThread();
        //System.out.println("pq[0].nextThread() is " + temp);
        temp = pq[0].nextThread();
        //System.out.println("pq[0].nextThread() is " + temp);
        temp = pq[0].nextThread();
        //System.out.println("pq[0].nextThread() is " + temp);

	Lib.assertTrue(lts[0].effectivePriority == 5);
	Lib.assertTrue(lts[1].effectivePriority == 3);
	Lib.assertTrue(lts[2].effectivePriority == 6);

	pq[1].acquire(t[0]);
	Lib.assertTrue(lts[0].effectivePriority == 5);
	pq[1].waitForAccess(t[1]);
	Lib.assertTrue(lts[0].effectivePriority == 8);
	pq[2].acquire(t[1]);
	Lib.assertTrue(lts[1].effectivePriority == 3);
	pq[2].waitForAccess(t[2]);
	Lib.assertTrue(lts[1].effectivePriority == 9);
	Lib.assertTrue(lts[0].effectivePriority == 14);
	pq[3].acquire(t[2]);
	pq[3].waitForAccess(t[3]);
	Lib.assertTrue(lts[2].effectivePriority == 7);
	Lib.assertTrue(lts[1].effectivePriority == 10);
	Lib.assertTrue(lts[0].effectivePriority == 15);

	lts[2].setPriority(100);
	Lib.assertTrue(lts[2].effectivePriority == 101);
	Lib.assertTrue(lts[1].effectivePriority == 104);
	Lib.assertTrue(lts[0].effectivePriority == 109);

        //System.out.println("lts[2] priority is " + lts[2].priority);
        //System.out.println("lts[2] effective priority is " + lts[2].effectivePriority);
        //System.out.println("prev lock holder effective priority is " + lts[0].effectivePriority);
        
        temp = pq[0].nextThread();
        //System.out.println("pq[0].nextThread()");
        //System.out.println("nextThread == null is: " + (temp == null));
        
        ThreadState temp2 = pq[0].pickNextThread();
        //System.out.println("pq[0].pickNextThread()");
        //System.out.println("pickNextThread == null is: " + (temp2 == null));
        
        temp = pq[0].nextThread();
        //System.out.println("pq[0].nextThread()");
        //System.out.println("nextThread == null is: " + (temp == null));
        
        Machine.interrupt().enable();*/
    }
}
