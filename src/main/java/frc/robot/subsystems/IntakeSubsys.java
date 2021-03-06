/**
 * Copyright (C) 2022, Tigerbotics' team members and all other contributors.
 * Open source software; you can modify and/or share this software.
 */
package frc.robot.subsystems;

import static frc.robot.Constants.Intake.kId;
import static frc.robot.Constants.Intake.kMotorType;
import static frc.robot.Constants.Intake.kSpeed;

import com.revrobotics.CANSparkMax;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Controls the intake of the robot. Forward motor direction results in intake moving in.
 *
 * @author Jeffrey Morris | Tigerbotics 7125
 */
public class IntakeSubsys extends SubsystemBase {

    // Motor controller for intake.
    final CANSparkMax mIntake = new CANSparkMax(kId, kMotorType);

    public IntakeSubsys() {}

    /** Stops all motor output. */
    public void disable() {
        mIntake.stopMotor();
    }

    /** Set the motor to intake. */
    public void intake() {
        mIntake.set(1 * kSpeed);
    }

    /** Set the motor to eject. */
    public void eject() {
        mIntake.set(-1 * kSpeed);
    }
}
