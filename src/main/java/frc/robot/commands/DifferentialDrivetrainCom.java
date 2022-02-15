package frc.robot.commands;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.drive.DifferentialDrive.WheelSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Gamepads;
import frc.robot.subsystems.DifferentialDrivetrainSub;

/**
 * Drives a DifferentialDrivetrain
 *
 * @author Jeffrey Morris | Tigerbotics 7125
 */
public class DifferentialDrivetrainCom extends CommandBase {

  DifferentialDrivetrainSub m_drivetrain;

  public DifferentialDrivetrainCom(DifferentialDrivetrainSub drivetrain) {
    addRequirements(drivetrain);
    m_drivetrain = drivetrain;
  }

  @Override
  public void initialize() {}

  @Override
  public void execute() {
    WheelSpeeds ws =
        DifferentialDrive.arcadeDriveIK(
            Gamepads.getDiffDriveYJoystick().getY(), Gamepads.getDiffDriveXJoystick().getX(), true);
    m_drivetrain.setOutput(ws.left * 12.0, ws.right * 12.0);
  }

  // Keep command always active.
  @Override
  public boolean isFinished() {
    return false;
  }
}