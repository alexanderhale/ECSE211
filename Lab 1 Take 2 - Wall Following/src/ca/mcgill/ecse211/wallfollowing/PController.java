package ca.mcgill.ecse211.wallfollowing;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {

  /* Constants */
  private static final int MOTOR_SPEED = 200;
  private static final int FILTER_OUT = 20;

  private final int bandCenter;
  private final int bandWidth;
  private int distance;
  private int filterControl;
  private int error_constant = 5;

  public PController(int bandCenter, int bandwidth) {
    this.bandCenter = bandCenter;
    this.bandWidth = bandwidth;
    this.filterControl = 0;

    WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED); // Initialize motor rolling forward
    WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }

  @Override
  public void processUSData(int distance) {
	  
	  //distance = (int)(distance / Math.sqrt(2)); // account for filter being mounted at 45 degrees

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

    // TODO: process a movement based on the us distance passed in (P style)
    // ASSUME COUNTERCLOCKWISE MOVEMENT
    // RIGHT (inner) MOTOR IS CONNECTED TO PORT D AND HAS A BLUE PIN ON TOP
    
    int error = Math.abs(this.distance - bandCenter);
	int adjustSpeed = error_constant*error; 
	
	if (adjustSpeed > 190){
		adjustSpeed = 190;
	}
	// set the limit for max. of adjustSpeed
	
	if (this.distance > bandCenter + (bandWidth/2)) {
	// proportionally reduce speed of inner wheel
    	WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED - (adjustSpeed/2));
    	WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED + adjustSpeed);	// TODO 5 is a constant that
    														// should be changed 
    } else if (this.distance < bandCenter - (bandWidth/2)) {
    	// proportionally reduce speed of outer wheel
    	WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED + adjustSpeed);// TODO 5 is a constant that
														  // should be changed
    	WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED - (adjustSpeed/2));
    } else {
    	// distance is on track
    	WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED);
    	WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED);
    }
  }


  @Override
  public int readUSDistance() {
    return this.distance;
  }

}
