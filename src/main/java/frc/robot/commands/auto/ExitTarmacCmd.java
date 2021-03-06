/**
 * Copyright (C) 2022, Tigerbotics' team members and all other contributors.
 * Open source software; you can modify and/or share this software.
 */
package frc.robot.commands.auto;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelRaceGroup;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import frc.robot.subsystems.ArmSubsys;
import frc.robot.subsystems.DrivetrainSubsys;
import frc.robot.subsystems.IntakeSubsys;

/**
 * An autonomous routine which drives backwards to exit the tarmac
 *
 * @author Jeffrey Morris | Tigerbotics 7125
 */
public class ExitTarmacCmd extends SequentialCommandGroup {

    // Subsystems used in command.
    private DrivetrainSubsys mDrivetrain;
    private ArmSubsys mArm;
    private IntakeSubsys mIntake;

    public ExitTarmacCmd(DrivetrainSubsys drivetrain, ArmSubsys arm, IntakeSubsys intake) {
        mDrivetrain = drivetrain;
        mArm = arm;
        mIntake = intake;
        addRequirements(mDrivetrain, mArm, mIntake);

        // Set name for so we can tell what auto we selected on dashboard.
        setName("Auto: Exit Tarmac");

        /**
         * Takes 1 second ejecting the ball. Drives backwards at half speed for 5 1/2 seconds.
         * Lowers the arm.
         */
        addCommands(
                new RunCommand(mIntake::eject).withTimeout(1),
                new InstantCommand(mIntake::disable),
                new RunCommand(() -> mDrivetrain.drive(0, -.5, 0)).withTimeout(5.5),
                new ParallelRaceGroup(
                        new RunCommand(mArm::lower, mArm),
                        new WaitUntilCommand(mArm::isDown),
                        new WaitCommand(3)));
    }
}
