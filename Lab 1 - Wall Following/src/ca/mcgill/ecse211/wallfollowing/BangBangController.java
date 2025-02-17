package ca.mcgill.ecse211.wallfollowing;

import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController {

  private final int bandCenter;
  private final int bandWidth;
  private final int motorLow;
  private final int motorHigh;
  private int distance;
  
  private int filterControl;
  private static final int FILTER_OUT = 35;
  private int tooCloseControl;
 
  public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh) {
    // Default Constructor
    this.bandCenter = bandCenter;
    this.bandWidth = bandwidth;
    this.motorLow = motorLow;
    this.motorHigh = motorHigh;
    
    this.filterControl = 0;
    this.tooCloseControl = 0;
    
    WallFollowingLab.leftMotor.setSpeed(motorHigh); // Start robot moving forward
    WallFollowingLab.rightMotor.setSpeed(motorHigh);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }

  @Override
  public void processUSData(int distance) {
	// rudimentary filter - toss out invalid samples corresponding to null signal 
    if (distance >= 255 && filterControl < FILTER_OUT) {
      // bad value: do not set the distance var, do increment the filter value
      this.filterControl++;
    } else if (distance >= 255) {
      // We have repeated large values, so there must actually be nothing
      // there: leave the distance alone
      this.distance = distance;
    } else {
      // distance went below 255: reset filter and leave
      // distance alone.
      this.filterControl = 0;
      this.distance = distance;
    }
	
    // process a movement based on the us distance passed in (BANG-BANG style)
    // ASSUME COUNTERCLOCKWISE MOVEMENT
    // RIGHT WHEEL IS CONNECTED TO PORT D AND HAS A BLUE PIN ON TOP
	if (this.distance > bandCenter + (bandWidth/2)) {
		WallFollowingLab.rightMotor.forward();
		WallFollowingLab.leftMotor.forward();
		// too far: reduce speed of inner wheel
		WallFollowingLab.rightMotor.setSpeed(motorHigh);
    	WallFollowingLab.leftMotor.setSpeed(motorLow);
    } else if (this.distance < bandCenter - (bandWidth/2) && this.distance > 10) {
    	WallFollowingLab.rightMotor.forward();
		WallFollowingLab.leftMotor.forward();
    	// too close: reduce speed of outer wheel
    	WallFollowingLab.leftMotor.setSpeed(motorHigh);
    	WallFollowingLab.rightMotor.setSpeed(motorLow);
    } else if (this.distance <= 12) {
    	// much too close, pivot. Filter to make sure it isn't an erroneous reading. 10
    	if (this.tooCloseControl < 2) {
    		this.tooCloseControl++;
    	} else {
    		this.tooCloseControl = 0;
    		WallFollowingLab.rightMotor.backward();
    		WallFollowingLab.leftMotor.forward();
    		WallFollowingLab.leftMotor.setSpeed(motorLow);
        	WallFollowingLab.rightMotor.setSpeed(motorLow);
    	}
    } else {
    	WallFollowingLab.rightMotor.forward();
		WallFollowingLab.leftMotor.forward();
    	// distance correct, set both wheels to high
		WallFollowingLab.leftMotor.setSpeed(motorHigh);
    	WallFollowingLab.rightMotor.setSpeed(motorHigh);
    }
  
  }

  @Override
  public int readUSDistance() {
    return this.distance;
  }
}
