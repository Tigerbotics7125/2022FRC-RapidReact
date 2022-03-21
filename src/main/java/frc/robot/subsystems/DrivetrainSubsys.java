/**
 * Copyright (C) 2022, Tigerbotics' team members and all other contributors.
 * Open source software; you can modify and/or share this software.
 */
package frc.robot.subsystems;

import static frc.robot.Constants.Drivetrain.kDistancePerPulse;
import static frc.robot.Constants.Drivetrain.kFrontLeftId;
import static frc.robot.Constants.Drivetrain.kFrontLeftOffset;
import static frc.robot.Constants.Drivetrain.kFrontRightId;
import static frc.robot.Constants.Drivetrain.kFrontRightOffset;
import static frc.robot.Constants.Drivetrain.kMotorType;
import static frc.robot.Constants.Drivetrain.kRPMtoMPSConversionFactor;
import static frc.robot.Constants.Drivetrain.kRearLeftId;
import static frc.robot.Constants.Drivetrain.kRearLeftOffset;
import static frc.robot.Constants.Drivetrain.kRearRightId;
import static frc.robot.Constants.Drivetrain.kRearRightOffset;

import com.ctre.phoenix.sensors.WPI_PigeonIMU;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.ControlType;
import com.revrobotics.REVPhysicsSim;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.MecanumDriveKinematics;
import edu.wpi.first.math.kinematics.MecanumDriveOdometry;
import edu.wpi.first.math.kinematics.MecanumDriveWheelSpeeds;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.drive.MecanumDrive.WheelSpeeds;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Robot;
import frc.tigerlib.Util;

/**
 * Controls the mecanum drivetrain of the robot
 *
 * @author 7125 Tigerbotics - Jeffrey Morris
 */
public class DrivetrainSubsys extends SubsystemBase {

    // Motors, PID controllers, and encoders
    final CANSparkMax m_frontLeft = new CANSparkMax(kFrontLeftId, kMotorType);
    final CANSparkMax m_rearLeft = new CANSparkMax(kRearLeftId, kMotorType);
    final CANSparkMax m_frontRight = new CANSparkMax(kFrontRightId, kMotorType);
    final CANSparkMax m_rearRight = new CANSparkMax(kRearRightId, kMotorType);

    final SparkMaxPIDController m_frontLeftPID = m_frontLeft.getPIDController();
    final SparkMaxPIDController m_rearLeftPID = m_rearLeft.getPIDController();
    final SparkMaxPIDController m_frontRightPID = m_frontRight.getPIDController();
    final SparkMaxPIDController m_rearRightPID = m_rearRight.getPIDController();

    final RelativeEncoder m_frontLeftEncoder = m_frontLeft.getEncoder();
    final RelativeEncoder m_rearLeftEncoder = m_rearLeft.getEncoder();
    final RelativeEncoder m_frontRightEncoder = m_frontRight.getEncoder();
    final RelativeEncoder m_rearRightEncoder = m_rearRight.getEncoder();

    // IMU, kinematics, and odometry
    final WPI_PigeonIMU m_pigeon = new WPI_PigeonIMU(Constants.kPigeonId);

    final MecanumDriveKinematics m_kinematics =
            new MecanumDriveKinematics(
                    kFrontLeftOffset, kFrontRightOffset, kRearLeftOffset, kRearRightOffset);

    final MecanumDriveOdometry m_odometry =
            new MecanumDriveOdometry(m_kinematics, new Rotation2d());

    // Variables used for different driving techniques
    boolean m_turning; // whether or not the robot should be turning
    boolean m_fieldOriented; // whether or not the robot should drive field-oriented
    Rotation2d m_angleOffset; // the angle to keep the robot facing
    boolean m_fieldOffset; // which side of the field the robot consideres forward

    public DrivetrainSubsys() {
        // set default driving options
        setTurning(true);
        setFieldOriented(false);

        // invert right side because motors backwards.
        m_frontLeft.setInverted(false);
        m_rearLeft.setInverted(false);
        m_frontRight.setInverted(true);
        m_rearRight.setInverted(true);

        m_frontLeftPID.setP(5e-5);
        m_rearLeftPID.setP(5e-5);
        m_frontRightPID.setP(5e-5);
        m_rearRightPID.setP(5e-5);

        // changes encoder distance from encoder ticks to meters
        m_frontLeftEncoder.setPositionConversionFactor(kDistancePerPulse);
        m_rearLeftEncoder.setPositionConversionFactor(kDistancePerPulse);
        m_frontRightEncoder.setPositionConversionFactor(kDistancePerPulse);
        m_rearRightEncoder.setPositionConversionFactor(kDistancePerPulse);

        // changes encoder velocity from rotations per minute to meters per second
        m_frontLeftEncoder.setVelocityConversionFactor(kRPMtoMPSConversionFactor);
        m_rearLeftEncoder.setVelocityConversionFactor(kRPMtoMPSConversionFactor);
        m_frontRightEncoder.setVelocityConversionFactor(kRPMtoMPSConversionFactor);
        m_rearRightEncoder.setVelocityConversionFactor(kRPMtoMPSConversionFactor);

        // make sure stuff starts on 0
        m_frontLeftEncoder.setPosition(0.0);
        m_rearLeftEncoder.setPosition(0.0);
        m_frontRightEncoder.setPosition(0.0);
        m_rearRightEncoder.setPosition(0.0);
        m_pigeon.setFusedHeading(0.0);

        if (Robot.isSimulation()) {
            REVPhysicsSim.getInstance().addSparkMax(m_frontLeft, DCMotor.getNEO(1));
            REVPhysicsSim.getInstance().addSparkMax(m_rearLeft, DCMotor.getNEO(1));
            REVPhysicsSim.getInstance().addSparkMax(m_frontRight, DCMotor.getNEO(1));
            REVPhysicsSim.getInstance().addSparkMax(m_rearRight, DCMotor.getNEO(1));
        }
    }

    /** general periodic updates. */
    @Override
    public void periodic() {
        // setSimHeading(HolonomicTestPath.getInstance().m_thetaPID.getSetpoint().position);
        m_odometry.update(getHeading(), getSpeeds());
    }

    /** update sparkmaxs during sim */
    @Override
    public void simulationPeriodic() {
        REVPhysicsSim.getInstance().run();
    }

    /** reset the odometry of the drivetrain */
    public void resetOdometry(final Pose2d pose) {
        m_odometry.resetPosition(pose, getHeading());
    }

    /** enables or disables turning */
    public void setTurning(boolean turning) {
        m_turning = turning;
    }

    /** enables or disables field oriented driving */
    public void setFieldOriented(boolean fieldOriented) {
        m_fieldOriented = fieldOriented;
    }

    /** sets the heading of the robot */
    public void setHeading(Rotation2d heading) {
        m_pigeon.setFusedHeading(heading.getDegrees());
    }

    public void toggleFieldOffset() {
        m_fieldOffset = !m_fieldOffset;
    }

    public void toggleFieldOriented() {
        m_fieldOriented = !m_fieldOriented;
    }

    /**
     * Sets the drivetrain to move as per the given speeds.
     *
     * @param targetSpeeds The input speeds.
     */
    public void setSpeeds(MecanumDriveWheelSpeeds targetSpeeds) {

        m_frontLeftPID.setReference(targetSpeeds.frontLeftMetersPerSecond, ControlType.kVelocity);
        m_rearLeftPID.setReference(targetSpeeds.rearLeftMetersPerSecond, ControlType.kVelocity);
        m_frontRightPID.setReference(targetSpeeds.frontRightMetersPerSecond, ControlType.kVelocity);
        m_rearRightPID.setReference(targetSpeeds.rearRightMetersPerSecond, ControlType.kVelocity);
    }

    /**
     * Drives the robot based on the input. All input is clamped to [-1, 1]; input should be scaled
     * before passing into this method.
     *
     * @param xSpeed Robot X Speed, forward is positive.
     * @param ySpeed Robot Y Speed, Right is positive.
     * @param zSpeed Robot Z/Theta Speed, Clockwise is positive.
     */
    public void drive(double xSpeed, double ySpeed, double zSpeed) {

        /*
         * TODO
         * make this work while keeping the ability to strafe; or maybe a
         * different system that only does it when going forwards and backwards.
         */

        if (m_turning && zSpeed == 0.0) {
            // if we were turning and are no longer, then set the angle offset to the
            // current angle
            m_angleOffset = getHeading();
            m_turning = false; // no longer turning
        }
        if (!m_turning && zSpeed == 0.0) {
            // if we are not turning and z speed is 0, then the difference from our
            // angleOffset and
            // currentHeading needs to be applied to keep our angle constant.
            double difference = getHeading().minus(m_angleOffset).getDegrees();
            zSpeed =
                    Util.scaleInput(
                            difference,
                            -difference,
                            difference,
                            -1,
                            1); // will return +- 1, should 100% be changed to something else.
        } else {
            // we are turning, either by boolean or zSpeed, so just make sure m_turning is
            // true
            m_turning = true;
        }

        WheelSpeeds targetSpeeds =
                MecanumDrive.driveCartesianIK(
                        ySpeed,
                        xSpeed,
                        m_turning ? zSpeed : 0.0,
                        m_fieldOriented ? getHeading().getDegrees() : 0.0);

        m_frontLeftPID.setReference(targetSpeeds.frontLeft, ControlType.kDutyCycle);
        m_rearLeftPID.setReference(targetSpeeds.rearLeft, ControlType.kDutyCycle);
        m_frontRightPID.setReference(targetSpeeds.frontRight, ControlType.kDutyCycle);
        m_rearRightPID.setReference(targetSpeeds.rearRight, ControlType.kDutyCycle);
    }

    /** Disables all motor output */
    public void disable() {
        m_frontLeft.disable();
        m_rearLeft.disable();
        m_frontRight.disable();
        m_rearRight.disable();
    }

    /** @return The current velocity of the robot. */
    public MecanumDriveWheelSpeeds getSpeeds() {
        return new MecanumDriveWheelSpeeds(
                m_frontLeftEncoder.getVelocity(),
                m_rearLeftEncoder.getVelocity(),
                m_frontRightEncoder.getVelocity(),
                m_rearRightEncoder.getVelocity());
    }

    /** @return If turning is enabled. */
    public boolean getTurning() {
        return m_turning;
    }

    /** @return If field oriented driving is enabled. */
    public boolean getFieldOriented() {
        return m_fieldOriented;
    }

    /** @return The drivetrains kinematics. */
    public MecanumDriveKinematics getKinematics() {
        return m_kinematics;
    }

    /** @return the current heading of the robot */
    public Rotation2d getHeading() {
        // pigeon headings are already +CCW, no need to negate
        return Rotation2d.fromDegrees(m_pigeon.getFusedHeading() + (m_fieldOffset ? 180 : 0));
    }

    /** @returns the current position of the robot. */
    public Pose2d getPose() {
        return m_odometry.getPoseMeters();
    }
}