package nachos.threads;

import nachos.machine.*;

/**
 * Represents a KThread that is waiting until a specific time to be woken
 */
public class TimeWaitingKThread implements Comparable{

	public TimeWaitingKThread(KThread thread,long time){
		waitingThread = thread;
		wakeTime = time;
	}
	
	public void wake(){
		waitingThread.ready();
	}

	public long getWakeTime(){
		return wakeTime;
	}

	public int compareTo(Object o){
		TimeWaitingKThread other = (TimeWaitingKThread) o;
		if(other.getWakeTime() < this.getWakeTime()){
			return -1;
		}
		else if(other.getWakeTime() > this.getWakeTime()){
			return 1;
		}
		else{
			return 0;
		}
	}

	private KThread waitingThread;
	private long wakeTime;
}
