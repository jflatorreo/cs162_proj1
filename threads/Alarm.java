package nachos.threads;

import nachos.machine.*;
import java.util.PriorityQueue;
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
    waitingThreads = new PriorityQueue();
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {

    long currentTime = Machine.timer().getTime();
    
    TimeWaitingKThread current =  waitingThreads.peek();
    boolean intStatus = Machine.interrupt().disable();
    
    System.out.println("interrupt " + currentTime);
    while((current!=null)&&(current.getWakeTime()<currentTime)){
        current = waitingThreads.poll();
        current.wake();
        Machine.interrupt().restore(intStatus);
        current = waitingThreads.peek();
    }

    Machine.interrupt().restore(intStatus);
	
    KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	   long wakeTime = Machine.timer().getTime() + x;
        System.out.println(wakeTime);
       boolean intStatus = Machine.interrupt().disable();
	   waitingThreads.add(new TimeWaitingKThread(KThread.currentThread(), wakeTime));
       KThread.sleep();
       Machine.interrupt().restore(intStatus);

    }

    private static class AlarmTest implements Runnable {
        //Fields
        private int which;
        
        //Constructor
        AlarmTest(int which) {
            this.which = which;
        }
        
        public void run() {
            for (int i=0; i<5; i++) {
                System.out.println("*** thread " + which + " looped " + i + " times");
                
                KThread.currentThread().yield();
            }
        }
    }
    private static class AlarmTestWait implements Runnable {
        //Fields
        private double which;
        
        //Constructor
        AlarmTestWait(double which) {
            this.which = which;
        }
        
        public void run() {
                System.out.println("*** thread " + which + " begins wait");
                ThreadedKernel.alarm.waitUntil(((int) (which*500))+500);
                System.out.println("*** thread " + which + " awakes");
        }
    }
    public static void selfTest(){
        new KThread(new AlarmTest(3)).setName("forked thread").fork();
        new KThread(new AlarmTest(2)).fork();
        new KThread(new AlarmTestWait(0)).fork();
        new KThread(new AlarmTestWait(0.4)).fork();
        new KThread(new AlarmTestWait(0.1)).fork();
        new AlarmTestWait(1).run();
        //ThreadedKernel.alarm.waitUntil(600);
    }
    PriorityQueue <TimeWaitingKThread> waitingThreads;
}
