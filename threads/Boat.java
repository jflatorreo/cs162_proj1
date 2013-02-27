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
	private static boolean pilot;
	private static boolean passenger;
	private static boolean boatInOahu;
	private static boolean boatDeparting;
	private static int childInOahu;
	private static int childInMolokai;
	private static int adultInOahu;
	private static int adultInMolokai;

	//Action Methods
    public static void begin( int adults, int children, BoatGrader b ) {
    	// Instantiate global variables here
		bg = b;
		pilot = false;
		passenger = false;
		boatInOahu = true;
		boatDeparting = false;
		childInOahu = children;
		childInMolokai = 0;
		adultInOahu = adults;
		adultInMolokai = 0;
		//Start Threads
		for (int i=0; i<children; i++){
			//Start Children Threads here
			KThread childThread = new KThread(new Runnable() {
				public void run () {ChildItinerary();}
			});
			childThread.fork();
		}
		for (int i=0; i<adults; i++){
			//Start Adults Threads here
			KThread adultThread = new KThread(new Runnable() {
				public void run () {AdultItinerary();}
			});
			adultThread.fork();
		}
		//Start looping between threads till condition finish
		
		Runnable r = new Runnable() {
			public void run() {
                SampleItinerary();
            }
        };
        KThread t = new KThread(r);
        t.setName("Sample Boat Thread");
        t.fork();
    }

    static void AdultItinerary() {
		
    }

    static void ChildItinerary() {
	
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
		BoatGrader b = new BoatGrader();
	
		System.out.println("\n ***Testing Boats with only 2 children***");
		begin(0, 2, b);

		//System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		//begin(1, 2, b);

		//System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		//begin(3, 3, b);
    }
}
/*
public void ChildRowToMolokai()
public void ChildRowToOahu()
public void ChildRideToMolokai()
public void ChildRideToOahu()
public void AdultRowToMolokai()
public void AdultRowToOahu()
public void AdultRideToMolokai()
public void AdultRideToOahu()
<<<<<<< HEAD
*/
