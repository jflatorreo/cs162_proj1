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
	Lock lock;
	Condition2 waitingReceivers;
	Condition2 waitingSenders;
	int senders;
	int receivers;
	int live;
	int value;
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    	lock = new Lock();
    	waitingReceivers = new Condition2(lock);
    	waitingSenders = new Condition2(lock);
    	receivers = 0;
    	senders = 0;
    	live = 0;
    	value = 0;
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
    	senders++;
    	while(live == 1 || receivers == 0) {
    		waitingReceivers.wake();
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
    	receivers++;
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
    	waitingSenders.wake();	
    	lock.release();
    	return result;
    }
}
