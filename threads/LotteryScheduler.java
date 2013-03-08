package nachos.threads;

import nachos.machine.*;

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
public class LotteryScheduler extends PriorityScheduler {
    public static final int priorityMinimum = 1;
    public static final int priorityMaximum = Integer.MAX_VALUE;
    public static int ticketCount = 0;
    /**
     * Allocate a new lottery scheduler.
     */
    public LotteryScheduler() {
    }
    
    /**
     * Allocate a new lottery thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer tickets from waiting threads
     *					to the owning thread.
     * @return	a new lottery thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
        return new LotteryQueue(transferPriority);
    }
    
    protected LotteryQueue extends PriorityQueue {
    //In terms of picking the next thread linear in the number of threads on the queue is fine
        LotteryQueue(boolean transferPriority) {
            super(transferPriority);
        }
        
        public void updateEntry(ThreadState ts, int newEffectivePriority) {
            this.ticketCount -= ts.effectivePriority;
            this.ticketCount += newEffectivePriority;
            super(ts, newEffectivePriority);
		}
        
        //DONE!!!!!
        protected ThreadState pickNextThread() {
            //Set up an Iterator and go through it
            Random randomGenerator = new Random();
            int count = tickets;
            int num = randomGenerator.nextInt(tickets);
            Iterator itr = this.waitQueue.iterator();
            while(itr.hasNext()) {
                temp = itr.next();
                count -= temp.effectivePriority;
                if(count <= 0){
                    return temp;
                }
            }
            return null;
        }
    }
    protected LotteryThreadState extends ThreadState {
        public LotteryThreadState(KThread thread) {
            super(thread);
        }
        
        public void setEffectivePriority(ThreadState donator) {
            if (this.pqWant != null && this.pqWant.transferPriority != true)
                return;
            int newPriority = this.effectivePriority + donator.effectivePriority;
            if (this.effectivePriority !=  newPriority) {
                if (pqWant != null) {
                    pqWant.updateEntry(this, newPriority);
                    this.pqWant.holder.setEffectivePriority(this);
                    return;
                }
                this.effectivePriority = newPriority;
            }
        }
        //DONE!!!!
        public void updateEffectivePriority() {
            //Calculate new effectivePriority checking possible donations from threads that are waiting for me
            int sumPriority = this.priority;
            for (PriorityQueue pq: this.pqHave)
                if (pq.transferPriority == true)
                    sumPriority = sumPriority + pq.holder.getEffectivePriority();
            
            //If there is a change in priority, update and propagate to other owners
            if (sumPriority != this.effectivePriority) {
                if(this.pqWant != null) {
                    //Readjust myself in the pq with new priority
                    sumPriority = this.priority + sumPriority;
                    pqWant.updateEntry(this, sumPriority);
                    //this.priority = maxPriority;
                    //Donate my priority to pq owner
                    if (pqWant.transferPriority == true)
                        pqWant.holder.setEffectivePriority(this);
                }
            }
        }
        //DONE!!!!
        public void waitForAccess(PriorityQueue pq) {
			this.pqWant = pq;
			//this.time = Machine.timer().getTime();
			this.time = TickTimer++;
			pq.waitQueue.add(this);
			//Propagate this ThreadState's effectivePriority to holder of pq
            if (pq.transferPriority == true)
                pq.updateEntry(pq.holder, pq.holder.effectivePriority+this.effectivePriority);
		}
        //Added a line to acquire in PriorityScheduler
        //updateEffectivePriority() at the very end
    }
}
