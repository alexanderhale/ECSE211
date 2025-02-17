package localization;

import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;

/**
 * 
 * The lab4 class is the hub of the localization process. It defines the ports for motors and sensors, gives the 
 * user the choice of rising or falling edge localization, starts the necessary threads, and starts the localization
 * processes.
 * 
 * @author Alex Hale
 * @author Xianyi Zhan
 */

public class lab4 {
	// setting up connections
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S2");			
	public static final double axleWidth = 12.7, wheelRadius = 2.1;
	
	/**
	 * The main method runs automatically after the program starts, so all the setup code is placed within it.
	 * 
	 * @param args - unused
	 */
	public static void main(String[] args) {
		final TextLCD display = LocalEV3.get().getTextLCD();
		
		// US sensor setup
		@SuppressWarnings("resource")							    	// do not bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			// provides samples from this instance
		float[] usData = new float[usValue.sampleSize()];				// buffer in which data are returned
		
		//set up odometer		
		Odometer odometer = new Odometer(leftMotor, rightMotor);
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, display);
		
		int buttonChoice;
	    do {
			// clear the display
			display.clear();

			// ask the user whether the motors should Avoid Block or Go to locations
			display.drawString("< Left   | Right >", 0, 0);
			display.drawString("         |        ", 0, 1);
			display.drawString(" Falling | Rising ", 0, 2);
			display.drawString(" Edge    | Edge   ", 0, 3);
			display.drawString("         |		  ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);
	    
	    USLocalizer usLoc;
		
	    if (buttonChoice == Button.ID_LEFT) {
	    	usLoc = new USLocalizer(leftMotor, rightMotor, odometer, USLocalizer.LocalizationType.FALLING_EDGE);
	    } else {
	    	usLoc = new USLocalizer(leftMotor, rightMotor, odometer, USLocalizer.LocalizationType.RISING_EDGE);
	    }
	    UltrasonicPoller usPoller = new UltrasonicPoller(usValue, usData, usLoc);
		odometer.start();
		odometryDisplay.start();
	    usPoller.start();
		usLoc.doLoc();
		
		Button.waitForAnyPress();
		
		LightLocalizer lsLoc = new LightLocalizer(leftMotor, rightMotor, odometer);
		lsLoc.doLoc();
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);	
	}
}
