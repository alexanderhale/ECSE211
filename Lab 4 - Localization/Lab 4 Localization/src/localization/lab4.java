package localization;

import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;

public class lab4 {
	// setting up connections
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S2");		
	private static final Port lightPort = LocalEV3.get().getPort("S1");	
	public static final double axleWidth = 12.7, wheelRadius = 2.125;
	
	public static void main(String[] args) {
		// US sensor setting up
		@SuppressWarnings("resource")							    	// do not bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			// provides samples from this instance
		float[] usData = new float[usValue.sampleSize()];				// buffer in which data are returned
		
		
		// color sensor setting up
		SensorModes lightSensor = new EV3ColorSensor(lightPort);
		SampleProvider lightValue = lightSensor.getMode("Red");			// provides samples from this instance
		float[] lightData = new float[lightValue.sampleSize()];			// buffer in which data are returned
		
		
		//set up odometer, navigation, usLoc and lsLoc, LcdInfo
		
		
	
				
		USLocalizer usLoc = new USLocalizer(usValue, usData, leftMotor, rightMotor, navigation, 
				odom, USLocalizer.LocalizationType.RISING_EDGE);
		
		LSLocalizer lsLoc = new LSLcalizer();
		
		
		
		usLoc.doLoc();
		while(Button.waitForAnyPress() != 1){
			lsLoc.doLoc();
		}
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);	
		
	

		
		
	}

}
