package localization;

/**
 * Interface for the ultrasonic sensor.
 * 
 * @author Alex Hale
 * @author Xianyi Zhan
 */
public interface UltrasonicController {

  public void processUSData(int distance);

  public int readUSDistance();
}
