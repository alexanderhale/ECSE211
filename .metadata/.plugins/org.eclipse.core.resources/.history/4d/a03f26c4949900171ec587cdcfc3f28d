package ca.mcgill.ecse211.wallfollowing;

import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController {

  private final int bandCenter;
  private final int bandWidth;
  private final int motorLow;
  private final int motorHigh;
  private int distance;
  
  private int filterControl = 0;
  private static final int FILTER_OUT = 20;
 
  public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh) {
    // Default Constructor
    this.bandCenter = bandCenter;
    this.bandWidth = bandwidth;
    this.motorLow = motorLow;
    this.motorHigh = motorHigh;
    
    WallFollowingLab.leftMotor.setSpeed(motorHigh); // Start robot moving forward
    WallFollowingLab.rightMotor.setSpeed(motorHigh);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }

  @Override
  public void processUSData(int distance) {
	// rudimentary filter - toss out invalid samples corresponding to null
	    // signal.
	    // (n.b. this was not included in the Bang-bang controller, but easily
	    // could have).
	    //
		  
	    if (distance >= 255 && filterControl < FILTER_OUT) {
	      // bad value, do not set the distance var, however do increment the
	      // filter value
	    	
	      // 255 might need to be divided by sqrt(2) if 45 degress being considered.
	    	
	      filterControl++;
	    } else if (distance >= 255) {
	      // We have repeated large values, so there must actually be nothing
	      // there: leave the distance alone
	      this.distance = distance;
	    } else {
	      // distance went below 255: reset filter and leave
	      // distance alone.
	      filterControl = 0;
	      this.distance = distance;
	    }
	  

	
    // TODO: process a movement based on the us distance passed in (BANG-BANG style)
    // ASSUME COUNTERCLOCKWISE MOVEMENT
    // RIGHT WHEEL IS CONNECTED TO PORT D AND HAS A BLUE PIN ON TOP
    // if distance too far, slow down right wheel
	int difference = Math.abs(bandCenter - this.distance);
	
	if (difference <= bandWidth/2 ) {
	    	WallFollowingLab.leftMotor.setSpeed(motorHigh);
	    	WallFollowingLab.rightMotor.setSpeed(motorHigh);
	}
	 // if distance correct, set both wheels to high
	
    
	
    else if (this.distance > bandCenter + (bandWidth/2)) {
    	WallFollowingLab.leftMotor.forward();
        WallFollowingLab.rightMotor.forward();
    	WallFollowingLab.leftMotor.setSpeed(motorLow);
    	WallFollowingLab.rightMotor.setSpeed(motorHigh);
    }
	
    else if (this.distance < bandCenter - (bandWidth/2)) {
    	WallFollowingLab.leftMotor.forward();
        WallFollowingLab.rightMotor.forward();
    	WallFollowingLab.leftMotor.setSpeed(motorHigh);
    	WallFollowingLab.rightMotor.setSpeed(motorLow);
    }
  
  }

  @Override
  public int readUSDistance() {
    return this.distance;
  }
}
