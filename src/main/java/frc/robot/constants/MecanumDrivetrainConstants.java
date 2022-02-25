/**
 * Copyright (C) 2022, Tigerbotics' team members and all other contributors.
 * Open source software; you can modify and/or share this software.
 */
package frc.robot.constants;

import com.revrobotics.CANSparkMaxLowLevel;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.TrapezoidProfile.Constraints;
import edu.wpi.first.math.util.Units;

public class MecanumDrivetrainConstants {
    // General constants
    public static final double kWheelDiameter = Units.inchesToMeters(6);
    public static final double kGearRatio = 10.71;

    public static final double kMaxSpeed = 3.0; // 3 m/s
    public static final double kMaxAngularSpeed = Math.PI; // 1 rad/s
    public static final double kMaxAngularWheelSpeed =
            kMaxSpeed / (kWheelDiameter / 2); // max wheel speed in rad/s
    public static final double kMaxWheelSpeed =
            kMaxAngularWheelSpeed * (kWheelDiameter / 2); // max wheel speed in m/s
    public static final double kDeadband =
            0.05; // fixes joystick range to [-1, -kDeadband)(0.0)(kDeadband, 1]

    // CAN IDs
    public static final int kFrontLeftId = 1;
    public static final int kRearLeftId = 2;
    public static final int kFrontRightId = 3;
    public static final int kRearRightId = 4;

    // The motor type used for the drive motors
    public static final CANSparkMaxLowLevel.MotorType kMotorType =
            CANSparkMaxLowLevel.MotorType.kBrushless;

    // Offset in meters from center of robot (also imu)
    public static final Translation2d kFrontLeftOffset =
            new Translation2d(Units.inchesToMeters(10.18), Units.inchesToMeters(-10.857));
    public static final Translation2d kRearLeftOffset =
            new Translation2d(Units.inchesToMeters(-10.18), Units.inchesToMeters(-10.857));
    public static final Translation2d kFrontRightOffset =
            new Translation2d(Units.inchesToMeters(10.18), Units.inchesToMeters(10.857));
    public static final Translation2d kRearRightOffset =
            new Translation2d(Units.inchesToMeters(-10.18), Units.inchesToMeters(10.857));

    // PID Controllers for auto
    public static final PIDController kXPID = new PIDController(1, 0, 0);
    public static final PIDController kYPID = new PIDController(1, 0, 0);
    public static final ProfiledPIDController kThetaPID =
            new ProfiledPIDController(1, 0, 0, new Constraints(6.28, 3.14));

    // Conversion factors used for encoder ticks to various units.
    public static final double kRPMtoMPSConversionFactor =
            1.0 / (kGearRatio * (Math.PI * kWheelDiameter));
    public static final double kDistancePerPulse =
            1.0 / (42.0 /* Hall Effects CPR */ / kGearRatio * (Math.PI * kWheelDiameter));
}
