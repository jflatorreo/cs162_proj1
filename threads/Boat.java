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
	private boolean pilot;
	private boolean passenger;
	private boolean boatInA;
	private boolean boatInB;
	private boolean boatDeparting;
	private int childInA;
	private int childInB;
	private int adultInA;
	private int adultInB;

	//Action Methods
  public static void begin( int adults, int children, BoatGrader b ) {
    bg = b;
    pilot = false;
    passenger = false;
    boatinA = true;
    boatInB = false;
    boatDeparting = false;
    childInA = children;
    childInB = 0;
    adultInA = adults;
    adultInB = 0;

    // Instantiate global variables here

    // Create threads here. See section 3.4 of the Nachos for Java
    // Walkthrough linked from the projects page.

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
public void ChildRowToMolokai()
public void ChildRowToOahu()
public void ChildRideToMolokai()
public void ChildRideToOahu()
public void AdultRowToMolokai()
public void AdultRowToOahu()
public void AdultRideToMolokai()
public void AdultRideToOahu()
