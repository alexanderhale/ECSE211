/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometerlab;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.NXTLightSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;

public class OdometryCorrection extends Thread {
  private static final long CORRECTION_PERIOD = 10;
  private Odometer odometer;
  private static final int blackValue = 100;	// TODO: FINE-TUNE THIS. THIS IS AN ESTIMATE. Use an average!
  private static final double sensorToAxle = 3.25;	// TODO: fine-tune this. Only measured once
  private static final double tileLength = 30.48;	// from lab instructions
  private static final double halfTileLength = 30.48 / 2;	// from lab instructions
  private static int counterY = 0;
  private static int counterX = 0;
  
  private static Port lsPort = LocalEV3.get().getPort("S1");
  private SensorModes lsSensor;
  private SampleProvider lsColour;
  private float[] lsData;
  
  // constructor
  public OdometryCorrection(Odometer odometer) {
    this.odometer = odometer;
    this.lsSensor = new EV3ColorSensor(lsPort);
    this.lsColour = lsSensor.getMode("Red");
    this.lsData = new float[lsSensor.sampleSize()];
  }

  // run method (required for Thread)
  public void run() {
    long correctionStart, correctionEnd;
    boolean lineFound = false; // track whether we've hit a line or not
    
    final TextLCD t = LocalEV3.get().getTextLCD();
    while (true) {
      correctionStart = System.currentTimeMillis();
      
      // get light value
      lsColour.fetchSample(lsData, 0);
      float lightValue = lsData[0] * 1000;		// scale up for more accuracy
      t.drawString("Color: " + lightValue, 0, 4);
      t.drawString("CounterX: " + counterX, 0, 5);
      t.drawString("CounterY: " + counterY, 0, 6);

      // check if the light value indicates black (i.e. a line)
      if (lightValue <= blackValue && !lineFound) {
    	  // get heading
    	  double theta = odometer.getTheta();
    	  
    	  // check which direction we just crossed
    	  
    	  if ((theta >= Math.PI/4 && theta < 3*Math.PI/4) || (theta >= 5*Math.PI/4 && theta < 7*Math.PI/4)) {
			  counterX++;
    		  Sound.playNote(Sound.FLUTE, 880, 250);
    		  
    		  // calculate how far the axle is from the line
			  double offset = Math.sin(theta) * sensorToAxle;
    		  double tempX;
    		  
    		  // adjust x coordinate to the closest line
			  if (counterX == 1 || counterX == 6) {
				  tempX = 0;
			  } else if (counterX > 1 && counterX < 4){
				  tempX = (counterX-1)*tileLength;
			  } else if (counterX == 4) {
				  tempX = 2*tileLength;
			  } else {
				  // counter is at 5
				  tempX = tileLength;
			  }
			  
			  // set the odometer to the closest line, removing offset
			  odometer.setX(tempX - offset);
    	  } else {
    		  counterY++;
    		  Sound.playNote(Sound.FLUTE, 440, 250);
    		  
    		  // calculate how far the axle is from the line
			  double offset = Math.cos(theta) * sensorToAxle;
    		  double tempY;
    		  
    		  // adjust y coordinate to the closest line
			  if (counterY == 1 || counterY == 6) {
				  tempY = 0;
			  } else if (counterY > 1 && counterY < 4){
				  tempY = (counterY-1)*tileLength;
			  } else if (counterY == 4) {
				  tempY = 2*tileLength;
			  } else {
				  // counter is at 5
				  tempY = tileLength;
			  }
			  
			  // set the odometer to the closest line, removing offset
			  odometer.setY(tempY - offset);
    	  }
    	  lineFound = true;
      } else {
    	  lineFound = false;
	  }
      
      // this ensure the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here because it is not
          // expected that the odometry correction will be
          // interrupted by another thread
        }
      }
    }
  }
}
