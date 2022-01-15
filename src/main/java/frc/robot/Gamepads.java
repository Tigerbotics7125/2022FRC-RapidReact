package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.input.ThumbStick;
import frc.robot.input.XboxGamepad;

// heavily inspired by spectrum3847's code, but modified to fit needs.
public class Gamepads {
  public static XboxGamepad driver = new XboxGamepad(0, 0.15, 0.15);
  public static boolean driverConfigured = false;

  public static void configure() {
    configureDriver();
    // optional other controllers, ie another "driver" who does different things on
    // seperate controller

  }

  public static void resetConfig() {
    CommandScheduler.getInstance().clearButtons();
    driverConfigured = false;
    configure();

    if (!driverConfigured) {
      DriverStation.reportWarning("##### Driver Controller Not Connected #####", false);
    }
  }

  public static void configureDriver() {
    // Detect whether the xbox controller has been plugged in after start-up
    if (!driverConfigured) {
      if (!driver.isConnected()) {
        return; // goes back and should warn.
      }

      driverBindings();
      driverConfigured = true;
    }
  }

  public static void driverBindings() {
    // Driver Controls
  }

  public static ThumbStick getDriveJoystick() {
    return driver.leftStick;
  }
}
