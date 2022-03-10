/**
 * Copyright (C) 2022, Tigerbotics' team members and all other contributors.
 * Open source software; you can modify and/or share this software.
 */
package frc.robot.commands.auto;

import static frc.robot.constants.MecanumDrivetrainConstants.kMaxWheelSpeedMPS;
import static frc.robot.constants.MecanumDrivetrainConstants.kThetaPID;
import static frc.robot.constants.MecanumDrivetrainConstants.kXPID;
import static frc.robot.constants.MecanumDrivetrainConstants.kYPID;

import com.pathplanner.lib.commands.PPMecanumControllerCommand;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.RobotContainer;
import frc.robot.constants.AutonomousTrajectories;
import frc.robot.subsystems.ArmSub;
import frc.robot.subsystems.MecanumDrivetrainSub;

/**
 * An autonomous path to exit the two alliance ball tarmac after scoreing our preloaded ball
 *
 * @author Jeffrey Morris | Tigerbotics 7125
 */
public class TwoBallTarmacExit extends AutonomousCommand {

    private MecanumDrivetrainSub m_drivetrain = RobotContainer.kDrivetrain;
    private ArmSub m_arm = RobotContainer.kArm;
    // private IntakeSub m_intake = RobotContainer.kIntake;

    private Command m_driveCommand =
            new PPMecanumControllerCommand(
                    AutonomousTrajectories.kTwoBallTarmacExit[0],
                    m_drivetrain::getPose,
                    m_drivetrain.getKinematics(),
                    kXPID,
                    kYPID,
                    kThetaPID,
                    kMaxWheelSpeedMPS,
                    m_drivetrain::setSpeeds,
                    m_drivetrain);

    public TwoBallTarmacExit() {
        addCommands();
    }

    @Override
    public Pose2d getInitialPose() {
        return new Pose2d(7.74, 2.83, Rotation2d.fromDegrees(69.00));
    }

    @Override
    public void preview() {}

    @Override
    public String getName() {
        return "TwoBall";
    }
}