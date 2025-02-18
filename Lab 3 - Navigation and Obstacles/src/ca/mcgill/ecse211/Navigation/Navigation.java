package ca.mcgill.ecse211.Navigation;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation extends Thread {
	
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	boolean navigating;
	private static final long ODOMETER_PERIOD = 25; /*odometer update period, in ms*/
	
	TextLCD t;
	
	// constants
	private int FORWARD_SPEED = 200;
	private int ROTATE_SPEED = 100;
	private double axleWidth, wheelRadius;	// passed in on system startup
	private double thetaNow, xNow, yNow;	// current heading
	private double gridLength = 30.48;
	
	public Navigation (Odometer odometer, EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor,
			double axleWidth, double wheelRadius){
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.axleWidth = axleWidth;
		this.wheelRadius = wheelRadius;
		leftMotor.setAcceleration(500);
		rightMotor.setAcceleration(500);
		navigating = false;
	}
	
	public void run() {
		long updateStart, updateEnd;
		while (true) {
			updateStart = System.currentTimeMillis();
			
			getCoordinates();
			
			// this ensures that the odometer only runs once every period
		      updateEnd = System.currentTimeMillis();
		      if (updateEnd - updateStart < ODOMETER_PERIOD) {
		        try {
		          Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
		        } catch (InterruptedException e) {
		          // there is nothing to be done here because it is not
		          // expected that the odometer will be interrupted by
		          // another thread
		        }
		      }
		}
	}
	
	public void travelTo(double x, double y) {	
		// getCoordinates();
		x *= gridLength;
		y *= gridLength;
		
		double deltaX = x - xNow;
		double deltaY = y - yNow;
		
		// calculate distance between current position and next coordinate
		double distanceToNext = Math.hypot(deltaX, deltaY);
		
		// calculate angle between current position and next coordinate
		double thetaToNextPoint = Math.atan2(deltaX, deltaY) - thetaNow;
		
		// ensure the robot rotates the least amount necessary
		if (thetaToNextPoint > Math.PI) {
			thetaToNextPoint -= 2*Math.PI;
		} else if (thetaToNextPoint < -(Math.PI)){
			thetaToNextPoint += 2*Math.PI;
		}
		
		turnTo(thetaToNextPoint);
		
		leftMotor.setAcceleration(500);
		rightMotor.setAcceleration(500);
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		
		navigating = true;
		leftMotor.forward();
		rightMotor.forward();
		
		leftMotor.rotate(convertDistance(wheelRadius,distanceToNext), true);
		rightMotor.rotate(convertDistance(wheelRadius,distanceToNext), false);
		
		leftMotor.stop();
		rightMotor.stop();
		
		navigating = false;
	}
	
	public void turnTo(double theta) {	
		// slow down
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		//convert to degrees
		theta = Math.toDegrees(theta);
		
		//turn to calculated angle
		int rotation = convertAngle(wheelRadius, axleWidth, theta);
		
		navigating = true;
		// rotate the appropriate direction (sign on theta accounts for direction
		leftMotor.rotate(rotation, true);
		rightMotor.rotate(-rotation, false);
		navigating = false;
		
		leftMotor.stop();
		rightMotor.stop();
	}
	
	public boolean isNavigating() {
		return navigating;
	}
	
	public void getCoordinates() {
		// synchronize robot's current position
	 	synchronized (odometer.lock) {
			thetaNow = odometer.getTheta();
			xNow = odometer.getX();
			yNow = odometer.getY();
		}
	}
	
	// calculation methods
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}
