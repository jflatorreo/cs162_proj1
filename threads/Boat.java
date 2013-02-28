package nachos.threads;
import nachos.ag.BoatGrader;

/*
from A to B (boatLocation A)
if there are 2 or more child in A:
	send 2 child to B
elif there is 1 child and no adults:
	send 1 child to B
elif there are 2 or more adult in A:
	send 2 adult to B
else:
	send 1 adult to B
	
from B to A (boatLocation B)
if there is a child in B:
	send 1 child to A
else:
	send 1 adult to A
*/
public class Boat {
	//Fields
	static BoatGrader bg;
	static Lock boatLock;
	static Condition waitingOnOahu;
	static Condition waitingOnMolokai;
	static Communicator notifyToBegin;
	private static boolean boatInOahu;
	private static boolean pilotIsChild;
	private static int childInOahu;
	private static int childInMolokai;
	private static int adultInOahu;
	private static int adultInMolokai;

	//Action Methods
	public static void begin( int adults, int children, BoatGrader b ) {
		// Instantiate global variables here
		bg = b;
		boatLock = new Lock();
		waitingOnOahu = new Condition(boatLock);
		waitingOnMolokai = new Condition(boatLock);
		notifyToBegin = new Communicator();
		boatInOahu = true;
		pilotIsChild = false;
		childInOahu = 0;
		childInMolokai = 0;
		adultInOahu = 0;
		adultInMolokai = 0;
		
		//Start Threads
		for (int i=0; i<children; i++){
			//Start Children Threads here
			KThread childThread = new KThread(new Runnable() {
				public void run () {ChildItinerary();}
			});
			childThread.setName("CHILD " + i+1);
			childThread.fork();
		}
		for (int i=0; i<adults; i++){
			//Start Adults Threads here
			KThread adultThread = new KThread(new Runnable() {
				public void run () {AdultItinerary();}
			});
			adultThread.setName("ADULT " + i+1);
			adultThread.fork();
		}
		
		while (true) {
			notifyToBegin.listen();
			boatLock.acquire();
			
			if (childInMolokai == children && adultInMolokai == adults) {
				break;
			}
			
			if (boatInOahu) {
				waitingOnOahu.wake();
			}
			else {
				waitingOnMolokai.wake();
			}
			boatLock.release();
		}
	}

	static void AdultItinerary() {
		boatLock.acquire();
		adultInOahu++;
		boolean inOahu = true;
		// do things later
		while (true) {
			if (inOahu && boatInOahu && childInOahu < 2 && !pilotIsChild){
				adultInOahu--;
				bg.AdultRowToMolokai();
				boatInOahu = !boatInOahu;
				inOahu = !inOahu;
				adultInMolokai++;
				
				if(childInMolokai == 0) {
					adultInMolokai--;
					bg.AdultRowToOahu();
					boatInOahu = !boatInOahu;
					inOahu = !inOahu;
					adultInOahu++;
					waitingOnOahu.wake();
					waitingOnOahu.sleep();
				} else {
					waitingOnMolokai.wake();
					waitingOnMolokai.sleep();
				}
			}
			else {
				if (inOahu) {
					waitingOnOahu.wake();
					waitingOnOahu.sleep();
				}
				else {
					waitingOnMolokai.wake();
					waitingOnMolokai.sleep();
				}
			}
				
		}
	}

	static void ChildItinerary() {
		boatLock.acquire();
		childInOahu++;
		boolean inOahu = true;
		int childInOahu_past = 0;
		int adultInOahu_past = 0;
		while (true) {
			if (inOahu && boatInOahu && ((childInOahu > 1 && !pilotIsChild) || pilotIsChild ) ) {
				//Get pilot or passenger if 2 childs or more in Oahu
				if (pilotIsChild == false) {
					pilotIsChild = true;
					childInOahu--;
					bg.ChildRowToMolokai();
					inOahu = !inOahu;
					childInMolokai++;
					
					//If we can load a passenger, do not move boat yet, and try to load child
					if (childInOahu != 0) {
						waitingOnOahu.wake();
					}
					//Else do the trip and search for next to go back. This is done so that is we have only a child Thread active we dont infinite loop
					else {
						boatInOahu = !boatInOahu;
						pilotIsChild = false;
						waitingOnMolokai.wake();
					}
					waitingOnMolokai.sleep();
				}
				else {
					pilotIsChild = false;
					childInOahu--;
					childInOahu_past = childInOahu;
					adultInOahu_past = adultInOahu;
					bg.ChildRideToMolokai();
					inOahu = !inOahu;
					boatInOahu = !boatInOahu;
					childInMolokai++;
					
					if (childInOahu_past == 0 && adultInOahu_past == 0) {
						notifyToBegin.speak(1);
					}
					else { 
						waitingOnMolokai.wake();
					}
					waitingOnMolokai.sleep();
				}
			} else if (inOahu && boatInOahu && childInOahu == 1 && adultInOahu == 0) {
				pilotIsChild = false;
				childInOahu--;
				bg.ChildRowToMolokai();
				inOahu = !inOahu;
				boatInOahu = !boatInOahu;
				childInMolokai++;
				
				notifyToBegin.speak(1);
				waitingOnMolokai.sleep();
			}
			else if (!inOahu && !boatInOahu) {
				childInMolokai--;
				bg.ChildRowToOahu();
				boatInOahu = !boatInOahu;
				inOahu = !inOahu;
				childInOahu++;
				waitingOnOahu.wake();
				waitingOnOahu.sleep();
			}
			else {
				if (inOahu) {
					waitingOnOahu.wake();
					waitingOnOahu.sleep();
				}
				else {
					waitingOnMolokai.wake();
					waitingOnMolokai.sleep();
				}
			}
		}
	}

	static void SampleItinerary() {
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}
	
	public static void selfTest() {
		/*
		BoatGrader b = new BoatGrader();
	
		System.out.println("\n ***Testing Boats with only 2 children***");
		//begin(0, 2, b);

		System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		begin(1, 2, b);

		System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		//begin(3, 3, b);
		
		//begin(10, 10, b);

		System.out.println("\n ***Testing Boats with 100 children, 100 adults***");
		//begin(100, 100, b);
		//*/
	}
}
