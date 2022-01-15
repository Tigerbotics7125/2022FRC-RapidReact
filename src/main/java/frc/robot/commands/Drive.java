package frc.robot.commands;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.drive.DifferentialDrive.WheelSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Gamepads;
import frc.robot.subsystems.DifferentialDrivetrain;

public class Drive extends CommandBase {

  DifferentialDrivetrain m_drivetrain;

  public Drive(DifferentialDrivetrain drivetrain) {
    addRequirements(drivetrain);
    m_drivetrain = drivetrain;
  }

  @Override
  public void initialize() {}

  @Override
  public void execute() {
    WheelSpeeds ws =
        DifferentialDrive.arcadeDriveIK(
            Gamepads.getDriveJoystick().getY(), Gamepads.getDriveJoystick().getX() * 2, true);
    m_drivetrain.setOutput(ws.left, ws.right);
  }

  // Keep command always active.
  @Override
  public boolean isFinished() {
    return false;
  }
}