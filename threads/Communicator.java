package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    	Lock lock = new Lock();
    	Condition2 waitingReceivers = new Condition2(lock);
    	Condition2 waitingSenders = new Condition2(lock);
    	int receivers = 0;
    	int senders = 0;
    	int live = 0;
    	int value;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	lock.acquire();
    	while(live == 1|| receivers == 0) {
    		waitingSenders.sleep();
    	}
    	receivers--;
    	value = word;
    	live = 1;
    	waitingReceivers.wake();
    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	lock.acquire();
    	while(live == 0){
    		if(senders == 0)
    			waitingReceivers.sleep();
    		else {
    			waitingSenders.wake();
    			waitingReceivers.sleep();
    		}
    	}
    	senders--;
    	int result = value;
    	live = 0;
    	lock.release();
    	return result;
}
