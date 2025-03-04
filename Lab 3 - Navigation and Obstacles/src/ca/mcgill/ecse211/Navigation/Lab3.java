package ca.mcgill.ecse211.Navigation;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab3 {

	public static EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	
	public static Port usPort = LocalEV3.get().getPort("S2");
    static SensorModes usSensor = new EV3UltrasonicSensor(usPort); // the instance
    static SampleProvider usDistance = usSensor.getMode("Distance"); // provides samples from this instance
    static float[] usData = new float[usDistance.sampleSize()]; // buffer in which data are returned

	// constants
	public static double axleWidth = 12.7, wheelRadius = 2.125;
	
	// coordinates
	static double[] x = {1, 2, 2, 0, 1};
    static double[] y = {0, 1, 2, 2, 1};
	
	public static void main(String[] args) {    
	    final TextLCD t = LocalEV3.get().getTextLCD();
	    Odometer odometer = new Odometer(leftMotor, rightMotor);
	    OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, t);
	    Navigation navigation = new Navigation(odometer, leftMotor, rightMotor, axleWidth, wheelRadius);
	    
	    int buttonChoice;
	    do {
			// clear the display
			t.clear();

			// ask the user whether the motors should Avoid Block or Go to locations
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("       |        ", 0, 1);
			t.drawString(" Avoid | Drive  ", 0, 2);
			t.drawString(" Block |		", 0, 3);
			t.drawString("       |		", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);
	    
	    if (buttonChoice == Button.ID_LEFT) {
	    	// start controllers
	    	NavigatorAvoid navigatorAvoid = new NavigatorAvoid(odometer, leftMotor, rightMotor, axleWidth, wheelRadius);
		    UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, usData, navigatorAvoid);
		    
		  //start odometer, display, navigator and sensor
			odometer.start();
			odometryDisplay.start();
			usPoller.start();
			navigatorAvoid.start();
			
		    // travel to all the points
		    for (int i = 0; i < x.length; i++) {
		    	navigatorAvoid.travelTo(x[i], y[i]);
		    	
		    	while (navigatorAvoid.isNavigating()) {
		    		// do nothing until the robot is done travelling
		    		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		    		System.exit(0);
		    	}
		    }
	    } else {
	    	odometer.start();
			odometryDisplay.start();
			navigation.start();
			
		    // travel to all the points
		    for (int i = 0; i < x.length; i++) {
		    	navigation.travelTo(x[i], y[i]);
		    	
		    	while (navigation.isNavigating()) {
		    		// do nothing until the robot is done travelling
		    		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		    		System.exit(0);
		    	}
		    }
	    }
	    
	    // Wait here forever until button pressed to terminate
	    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
