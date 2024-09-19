/*
 * Copyright (c) 2019 OpenFTC Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.firstinspires.ftc.teamcode.Auto;

import static org.firstinspires.ftc.teamcode.Auto.HardwarePushbot_TC.armIntakePosition;
import static org.firstinspires.ftc.teamcode.Auto.HardwarePushbot_TC.armOuttakePosition;
import static org.firstinspires.ftc.teamcode.Auto.HardwarePushbot_TC.bucketDropfPosition;
import static org.firstinspires.ftc.teamcode.Auto.HardwarePushbot_TC.bucketHalfPosition;
import static org.firstinspires.ftc.teamcode.Auto.HardwarePushbot_TC.bucketIntakePosition;
import static org.firstinspires.ftc.teamcode.Auto.HardwarePushbot_TC.intakePrimary;
import static org.firstinspires.ftc.teamcode.Auto.HardwarePushbot_TC.intakeSecondary;
import static org.firstinspires.ftc.teamcode.Auto.HardwarePushbot_TC.smallServoClosed;
import static org.firstinspires.ftc.teamcode.Auto.HardwarePushbot_TC.smallServoOpen;
import static org.firstinspires.ftc.teamcode.Auto.HardwarePushbot_TC.stackServo;
import static org.firstinspires.ftc.teamcode.Auto.HardwarePushbot_TC.stackServoHor;
import static org.firstinspires.ftc.teamcode.Auto.HardwarePushbot_TC.stackServoVer;

import com.acmerobotics.roadrunner.drive.Drive;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.drive.DriveConstants;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

import java.util.List;

@Autonomous(name = "Auto_WhiteStack_Test", group = "TEST")
@Disabled
public class Auto_WhiteStackTest extends LinearOpMode {
    Pose2d initPose,backboardPose, thirdPose, dropPurplePose,b34dropPurplePose,b45dropPurplePose,b6dropYellowPose ;// Starting Pose
    Vector2d wallPose,secondPose, fourthPose,backPose, fivePose, sixPose,thirdPoseV;

    Vector2d b4dropPurpleVector,b4backupVector,b6dropYellowVector,b1pickLWhiteVector,c5lineupRWhiteVector,c1pickRWhiteVector,c1backupToWallVector,c6parkVector;
    TrajectorySequence trajectoryParking;
    private ElapsedTime runtime = new ElapsedTime();
    private static int DESIRED_TAG_ID = -1;
    private static final boolean USE_WEBCAM = true;  // true for webcam, false for phone camera
    private AprilTagDetection desiredTag = null;

    /**
     * The variable to store our instance of the AprilTag processor.
     */
    private AprilTagProcessor aprilTag;
    private boolean startAprilTagCamera = false;

    /**
     * The variable to store our instance of the vision portal.
     */
    private VisionPortal visionPortal;
    OpenCvWebcam webcam;
    DuckPosDeterminationPipeline pipeline;
    HardwarePushbot_TC  robot;

    double newTarget;

    @Override
    public void runOpMode() {
        /*
         * Instantiate an OpenCvCamera object for the camera we'll be using.
         * In this sample, we're using a webcam. Note that you will need to
         * make sure you have added the webcam to your configuration file and
         * adjusted the name here to match what you named it in said config file.
         *
         * We pass it the view that we wish to use for camera monitor (on
         * the RC phone). If no camera monitor is desired, use the alternate
         * single-parameter constructor instead (commented out below)
         */
        boolean targetFound     = false;
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        robot = new HardwarePushbot_TC(hardwareMap);
        robot.leftFront.setDirection(DcMotorEx.Direction.REVERSE); // Set to REVERSE if using AndyMark motors
        robot.rightFront.setDirection(DcMotorEx.Direction.FORWARD);// Set to FORWARD if using AndyMark motors
        robot.leftRear.setDirection(DcMotorEx.Direction.REVERSE);
        robot.rightRear.setDirection(DcMotorEx.Direction.FORWARD);



        robot.leftLinearSlide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rightLinearSlide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
;
        // Wait for the DS start button to be touched.
        telemetry.addData("DS preview on/off", "3 dots, Camera Stream");
        telemetry.addData(">", "Touch Play to start OpMode");
        telemetry.update();

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        pipeline = new DuckPosDeterminationPipeline();
        webcam.setPipeline(pipeline);
           /*
             * Open the connection to the camera device. New in v1.4.0 is the ability
             * to open the camera asynchronously, and this is now the recommended way
             * to do it. The benefits of opening async include faster init time, and
             * better behavior when pressing stop during init (i.e. less of a chance
             * of tripping the stuck watchdog)
             *
             * If you really want to open synchronously, the old method is still available.
             */
        webcam.setMillisecondsPermissionTimeout(2500); // Timeout for obtaining permission is configurable. Set before opening.
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
        @Override
        public void onOpened() {
            /*
             * Tell the webcam to start streaming images to us! Note that you must make sure
             * the resolution you specify is supported by the camera. If it is not, an exception
             * will be thrown.
             *
             * Keep in mind that the SDK's UVC driver (what OpenCvWebcam uses under the hood) only
             * supports streaming from the webcam in the uncompressed YUV image format. This means
             * that the maximum resolution you can stream at and still get up to 30FPS is 480p (640x480).
             * Streaming at e.g. 720p will limit you to up to 10FPS and so on and so forth.
             *
             * Also, we specify the rotation that the webcam is used in. This is so that the image
             * from the camera sensor can be rotated such that it is always displayed with the image upright.
             * For a front facing camera, rotation is defined assuming the user is looking at the screen.
             * For a rear facing camera or a webcam, rotation is defined assuming the camera is facing
             * away from the user.
             */
            webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
        }

            @Override
        public void onError(int errorCode) {
            telemetry.addData("Error opening camera", webcam.getCurrentPipelineMaxFps());
            telemetry.update();
        }
        });

         //Detect the spike mark

        telemetry.addData("Frame Count", webcam.getFrameCount());
        telemetry.addData("FPS", String.format("%.2f", webcam.getFps()));
        telemetry.addData("Total frame time ms", webcam.getTotalFrameTimeMs());
        telemetry.addData("Pipeline time ms", webcam.getPipelineTimeMs());
        telemetry.addData("Overhead time ms", webcam.getOverheadTimeMs());
        telemetry.addData("Theoretical max FPS", webcam.getCurrentPipelineMaxFps());

        telemetry.addData("Analysis", pipeline.getAnalysis());
        telemetry.update();

        while(!isStarted() && !isStopRequested()) {

            pipeline.position =pipeline.getAnalysis();
            // This tag is NOT in the library, so we don't have enough information to track to it.
            telemetry.addData("POSITION TAG ID ", "Tag ID %s ", pipeline.position);
            telemetry.update();

        }


            webcam.stopStreaming();
            webcam.closeCameraDevice();

            telemetry.addData("Based on Strike Mark - Desired TAG ID ", "Tag ID %d ", DESIRED_TAG_ID);
            telemetry.update();

        sleep(1000);
        /*
         * Wait for the user to press start on the Driver Station
         */

        waitForStart();

        if (opModeIsActive() && !targetFound){
            /*
             * Send some stats to the telemetry
             */

            if (pipeline.position == DuckPosDeterminationPipeline.DuckPosition.LEFT) {
                DESIRED_TAG_ID = 1;
                // This tag is NOT in the library, so we don't have enough information to track to it.
                telemetry.addData("LEFT POSITION TAG ID ", "Tag ID %d ", DESIRED_TAG_ID);
                telemetry.update();
                sleep(10);
                // break;
            }
            else if (pipeline.position == DuckPosDeterminationPipeline.DuckPosition.CENTER) {
                DESIRED_TAG_ID = 2;
                // This tag is NOT in the library, so we don't have enough information to track to it.
                telemetry.addData("CENTER POSITION TAG ID ", "Tag ID %d ", DESIRED_TAG_ID);
                telemetry.update();
                sleep(10);
                // break;
            }
            else if (pipeline.position == DuckPosDeterminationPipeline.DuckPosition.RIGHT) {
                DESIRED_TAG_ID = 3;
                // This tag is NOT in the library, so we don't have enough information to track to it.
                telemetry.addData(" RIGHT POSITION TAG ID ", "Tag ID %d ", DESIRED_TAG_ID);
                telemetry.update();
                sleep(10);
                // break;
            }

          //  robot.openServo.setPosition(smallServoClosed);
          robot.resetLinearEncoder();
          DESIRED_TAG_ID = 1;


          if (DESIRED_TAG_ID ==1) {

              initPose = new Pose2d(0, 0, Math.toRadians(0));//Starting pose
              b45dropPurplePose = new Pose2d(0,-10, Math.toRadians(0));
              b4dropPurpleVector = new Vector2d(0,-10);
              // turn towards backboard
              stackServo.setPosition(0.7);
              trajectoryParking  = drive.trajectorySequenceBuilder(initPose)
                      .back(20)
                      // goes forward to put purple
                      .UNSTABLE_addTemporalMarkerOffset(0.01, () ->
                              {
                                  stackServo.setPosition(0.9);
                              }
                      )
                      .waitSeconds(0.2)
                      .forward(5)
                      .UNSTABLE_addTemporalMarkerOffset(0.01, () ->
                              {
                                  stackServo.setPosition(0.7);
                              }
                      )
                      .UNSTABLE_addTemporalMarkerOffset(0.2, () ->
                              {
                                  intakePrimary.setPower(1);
                                  intakeSecondary.setPower(1);
                              }
                      )
                      .back(5)
                      .waitSeconds(0.1)
                      .forward(5)
                      .build();

              drive.followTrajectorySequence(trajectoryParking);

          }
          if (DESIRED_TAG_ID ==2) {

                initPose = new Pose2d(0, 0, Math.toRadians(0));//Starting pose
                b4dropPurpleVector = new Vector2d(31,0);  // to drop purple pixel
                b4backupVector = new Vector2d(22,0); // to backup
                b6dropYellowPose = new Pose2d(26,44, Math.toRadians(90)); // turn towards backboard
                b6dropYellowVector = new Vector2d(26, 44);
                b1pickLWhiteVector = new Vector2d(25, -71);
                c5lineupRWhiteVector = new Vector2d(52,20);
                c1pickRWhiteVector = new Vector2d(48,-73);
                c1backupToWallVector = new Vector2d(48, -74.5);
                c6parkVector = new Vector2d(52,18);

/*
              secondPose = new Vector2d(31,0);  // to drop purple pixel
              backPose = new Vector2d(22,0); // to backup
              thirdPose = new Pose2d(26,44, Math.toRadians(90)); // turn towards backboard
              thirdPoseV = new Vector2d(23, 42);
              fourthPose = new Vector2d(25, -71);
              fivePose = new Vector2d(52,20);
              sixPose = new Vector2d(48,-72);
              wallPose = new Vector2d(48, -73.5);
*/

              stackServo.setPosition(stackServoHor);
                trajectoryParking  = drive.trajectorySequenceBuilder(initPose)
                        .lineTo(b4dropPurpleVector) // goes forward to put purple
                        .lineTo(b4backupVector) // comes back
                        // this below lifts the linear slide
                        // all subsystems in outtake position
                        .UNSTABLE_addTemporalMarkerOffset(0.01, () ->
                                {
                                robot.bucketServo.setPosition(bucketHalfPosition);
                                robot.firstPosition(1);
                                robot.mainServo.setPosition(armOuttakePosition);
                                robot.bucketServo.setPosition(bucketDropfPosition);
                                robot.openServo.setPosition(smallServoClosed);}
                        )
                        // going to the backdrop to deliver yellow
                        .lineToLinearHeading(b6dropYellowPose)
                        // drops the yellow pixels
                        .UNSTABLE_addTemporalMarkerOffset(0.01, () ->
                                robot.openServo.setPosition(smallServoOpen)
                        )
                        .waitSeconds(1)
                        // moves arm and bucket and bucketclose servos in intake position
                        .UNSTABLE_addTemporalMarkerOffset(0.2, () ->
                                {
                                    robot.mainServo.setPosition(armIntakePosition);
                                    robot.bucketServo.setPosition(bucketIntakePosition);
                                    robot.openServo.setPosition(smallServoClosed);

                                }
                        )
                        // pull the linear slide down
                        .UNSTABLE_addTemporalMarkerOffset(0.3, () ->
                                    robot.fullDown(1)
                         )
                        .lineTo(c5lineupRWhiteVector) //aligning on the outside to pick the first set of white pixels
                        .lineTo(c1pickRWhiteVector) // going towards the back field to pick the white pixels
                        //drop the servo arm to push the 3 pixels out
                        .UNSTABLE_addTemporalMarkerOffset(0.2, () ->
                                {
                                    stackServo.setPosition(stackServoVer);
                                }
                        )
                        .waitSeconds(0.5)
                        //turn on the intake to suck the white pixels
                        //moves arm and bucket and bucket-close servos in intake position
                        .UNSTABLE_addTemporalMarkerOffset(0.2, () ->
                                {
                                    intakePrimary.setPower(1);
                                    intakeSecondary.setPower(1);
                                }
                        )
                        //move back a little to suck the pixels
                        .lineTo(c1backupToWallVector)
                        .waitSeconds(0.5)
                        //off the intake wheels
                        .UNSTABLE_addTemporalMarkerOffset(0.2, () ->
                                {
                                  intakePrimary.setPower(0);
                                }
                        )
                        //move towards the backboard
                        .lineTo(c5lineupRWhiteVector) // going to backdrop with white pixels
                        // this below lifts the linear slide
                        // all subsystems in outtake position
                        .UNSTABLE_addTemporalMarkerOffset(0.01, () ->
                                {
                                    robot.mainServo.setPosition(0.9);
                                    robot.lineTwo(1);
                                    robot.mainServo.setPosition(armOuttakePosition);
                                    robot.bucketServo.setPosition(bucketDropfPosition);
                                    robot.openServo.setPosition(smallServoClosed);}
                        )
                        //align in front the backboard
                        .lineTo(b6dropYellowVector) // going to backdrop with white pixels

                        .UNSTABLE_addTemporalMarkerOffset(0.01, () ->
                                robot.openServo.setPosition(smallServoOpen)
                        )
                        .waitSeconds(0.4)
                        .back(2)
                        // moves arm and bucket and bucketclose servos in intake position
                        .UNSTABLE_addTemporalMarkerOffset(0.2, () ->
                                {
                                    //robot.openServo.setPosition(smallServoOpen);
                                    robot.mainServo.setPosition(armIntakePosition);
                                    robot.bucketServo.setPosition(bucketIntakePosition);
                                    robot.openServo.setPosition(smallServoClosed);

                                }
                        )
                        //get the linear slide
                        .UNSTABLE_addTemporalMarkerOffset(0.3, () ->
                                robot.fullDown(1)
                        )
                        .lineTo(c6parkVector)
                        .forward(25)
                        .build();

                drive.followTrajectorySequence(trajectoryParking);
          }
          if (DESIRED_TAG_ID ==3) {
              robot.resetLinearEncoder();
              initPose = new Pose2d(0, 0, Math.toRadians(0));//Starting pose

              b34dropPurplePose = new Pose2d(27,-10, Math.toRadians(-30)); // turn towards backboard
              b6dropYellowPose = new Pose2d(34,42, Math.toRadians(90)); // turn towards backboard

              stackServo.setPosition(stackServoHor);
              trajectoryParking  = drive.trajectorySequenceBuilder(initPose)
                      .forward(15)
                      .lineToLinearHeading(dropPurplePose) // goes forward to put purple
                      .strafeLeft(10)
                      .back(15)
                      // this below lifts the linear slide
                      // all subsystems in outtake position
                      .UNSTABLE_addTemporalMarkerOffset(0.01, () ->
                              {
                                  robot.bucketServo.setPosition(bucketHalfPosition);
                                  robot.firstPosition(1);
                                  robot.mainServo.setPosition(armOuttakePosition);
                                  robot.bucketServo.setPosition(bucketDropfPosition);
                                  robot.openServo.setPosition(smallServoClosed);}
                      )
                      .lineToLinearHeading(backboardPose)

                      .UNSTABLE_addTemporalMarkerOffset(0.01, () ->
                              robot.openServo.setPosition(smallServoOpen)
                      )
                      .waitSeconds(1)
                      // moves arm and bucket and bucketclose servos in intake position
                      .UNSTABLE_addTemporalMarkerOffset(0.2, () ->
                              {
                                  robot.mainServo.setPosition(armIntakePosition);
                                  robot.bucketServo.setPosition(bucketIntakePosition);
                                  robot.openServo.setPosition(smallServoClosed);

                              }
                      )
                      // pull the linear slide down
                      //get the linear slide

                      .UNSTABLE_addTemporalMarkerOffset(0.3, () ->
                              robot.fullDown(1)
                      )
                      .back(10)
                      .build();

              drive.followTrajectorySequence(trajectoryParking);


          }
        } //end of the if condition

      //  sleep(5000);
    }


    public static class DuckPosDeterminationPipeline extends OpenCvPipeline
    {
        /*
         * An enum to define the freight position
         */
        public Telemetry telemetry;
        public enum DuckPosition
        {
            LEFT,
            CENTER,
            RIGHT,
            NODUCK
        }

        /*
         * Some color constants
         */
        static final Scalar BLUE = new Scalar(0, 0, 255);
        static final Scalar RED = new Scalar(255, 0, 0);
        static final Scalar GREEN = new Scalar(0,255,0);

        /*
         * The core values which define the location and size of the sample regions
         coordinates for c930E camera
        static final Point REGION1_TOPLEFT_ANCHOR_POINT = new Point(35,30);
        static final Point REGION2_TOPLEFT_ANCHOR_POINT = new Point(130,30);
        static final Point REGION3_TOPLEFT_ANCHOR_POINT = new Point(235,30);
        static final int REGION_WIDTH = 50;
        static final int REGION_HEIGHT = 50;*/

        /* coordinates for c270 camera     */
     //   static final Point REGION1_TOPLEFT_ANCHOR_POINT = new Point(50,55);
        static final Point REGION1_TOPLEFT_ANCHOR_POINT = new Point(10,95);
      //  static final Point REGION2_TOPLEFT_ANCHOR_POINT = new Point(150,55);
        static final Point REGION2_TOPLEFT_ANCHOR_POINT = new Point(130,80);
    //    static final Point REGION3_TOPLEFT_ANCHOR_POINT = new Point(250,55);
        static final Point REGION3_TOPLEFT_ANCHOR_POINT = new Point(250,95);
        static final int REGION_WIDTH = 50;
        static final int REGION_HEIGHT = 50;

        /*
         * Points which actually define the sample region rectangles, derived from above values
         *
         * Example of how points A and B work to define a rectangle
         *
         *   ------------------------------------
         *   | (0,0) Point A                    |
         *   |                                  |
         *   |                                  |
         *   |                                  |
         *   |                                  |
         *   |                                  |
         *   |                                  |
         *   |                  Point B (70,50) |
         *   ------------------------------------
         *
         */
        Point region1_pointA = new Point(
                REGION1_TOPLEFT_ANCHOR_POINT.x,
                REGION1_TOPLEFT_ANCHOR_POINT.y);
        Point region1_pointB = new Point(
                REGION1_TOPLEFT_ANCHOR_POINT.x + REGION_WIDTH,
                REGION1_TOPLEFT_ANCHOR_POINT.y + REGION_HEIGHT);
        Point region2_pointA = new Point(
                REGION2_TOPLEFT_ANCHOR_POINT.x,
                REGION2_TOPLEFT_ANCHOR_POINT.y);
        Point region2_pointB = new Point(
                REGION2_TOPLEFT_ANCHOR_POINT.x + REGION_WIDTH,
                REGION2_TOPLEFT_ANCHOR_POINT.y + REGION_HEIGHT);
        Point region3_pointA = new Point(
                REGION3_TOPLEFT_ANCHOR_POINT.x,
                REGION3_TOPLEFT_ANCHOR_POINT.y);
        Point region3_pointB = new Point(
                REGION3_TOPLEFT_ANCHOR_POINT.x + REGION_WIDTH,
                REGION3_TOPLEFT_ANCHOR_POINT.y + REGION_HEIGHT);

        /*
         * Working variables
         */
        Mat region1_Cb, region2_Cb, region3_Cb;
        Mat YCrCb = new Mat();
        Mat Cb = new Mat();
        int avg1, avg2, avg3;


        // Volatile since accessed by OpMode thread w/o synchronization
        private volatile DuckPosition position;

        {
            position = DuckPosition.NODUCK;
        }

        /*
         * This function takes the RGB frame, converts to YCrCb,
         * and extracts the Cb channel to the 'Cb' variable
         `*/
        void inputToCb(Mat input)
        {
            Imgproc.cvtColor(input, YCrCb, Imgproc.COLOR_RGB2YCrCb);
         //   Imgproc.cvtColor(input, YCrCb, Imgproc.COLOR_RGB2Y);
            Core.extractChannel(YCrCb, Cb, 2);
        }

        @Override
        public void init(Mat firstFrame)
        {
            /*
             * We need to call this in order to make sure the 'Cb'
             * object is initialized, so that the submats we make
             * will still be linked to it on subsequent frames. (If
             * the object were to only be initialized in processFrame,
             * then the submats would become delinked because the backing
             * buffer would be re-allocated the first time a real frame
             * was crunched)
             */
            inputToCb(firstFrame);

            /*
             * Submats are a persistent reference to a region of the parent
             * buffer. Any changes to the child affect the parent, and the
             * reverse also holds true.
             */
            region1_Cb = Cb.submat(new Rect(region1_pointA, region1_pointB));
            region2_Cb = Cb.submat(new Rect(region2_pointA, region2_pointB));
            region3_Cb = Cb.submat(new Rect(region3_pointA, region3_pointB));
        }

        @Override
        public Mat processFrame(Mat input)
        {
            /*
             * Overview of what we're doing:
             *
             * We first convert to YCrCb color space, from RGB color space.
             * Why do we do this? Well, in the RGB color space, chroma and
             * luma are intertwined. In YCrCb, chroma and luma are separated.
             * YCrCb is a 3-channel color space, just like RGB. YCrCb's 3 channels
             * are Y, the luma channel (which essentially just a B&W image), the
             * Cr channel, which records the difference from red, and the Cb channel,
             * which records the difference from blue. Because chroma and luma are
             * not related in YCrCb, vision code written to look for certain values
             * in the Cr/Cb channels will not be severely affected by differing
             * light intensity, since that difference would most likely just be
             * reflected in the Y channel.
             *
             * After we've converted to YCrCb, we extract just the 2nd channel, the
             * Cb channel. We do this because stones are bright yellow and contrast
             * STRONGLY on the Cb channel against everything else, including SkyStones
             * (because SkyStones have a black label).
             *
             * We then take the average pixel value of 3 different regions on that Cb
             * channel, one positioned over each stone. The brightest of the 3 regions
             * is where we assume the SkyStone to be, since the normal stones show up
             * extremely darkly.
             *
             * We also draw rectangles on the screen showing where the sample regions
             * are, as well as drawing a solid rectangle over top the sample region
             * we believe is on top of the SkyStone.
             *
             * In order for this whole process to work correctly, each sample region
             * should be positioned in the center of each of the first 3 stones, and
             * be small enough such that only the stone is sampled, and not any of the
             * surroundings.
             */

            /*
             * Get the Cb channel of the input frame after conversion to YCrCb
             */
            inputToCb(input);

            /*
             * Compute the average pixel value of each submat region. We're
             * taking the average of a single channel buffer, so the value
             * we need is at index 0. We could have also taken the average
             * pixel value of the 3-channel image, and referenced the value
             * at index 2 here.
             */
            avg1 = (int) Core.mean(region1_Cb).val[0];
            avg2 = (int) Core.mean(region2_Cb).val[0];
            avg3 = (int) Core.mean(region3_Cb).val[0];



            /*
             * Draw a rectangle showing sample region 1 on the screen.
             * Simply a visual aid. Serves no functional purpose.
             */
            Imgproc.rectangle(
                    input, // Buffer to draw on
                    region1_pointA, // First point which defines the rectangle
                    region1_pointB, // Second point which defines the rectangle
                    GREEN, // The color the rectangle is drawn in
                    2); // Thickness of the rectangle lines

            /*
             * Draw a rectangle showing sample region 2 on the screen.
             * Simply a visual aid. Serves no functional purpose.
             */
            Imgproc.rectangle(
                    input, // Buffer to draw on
                    region2_pointA, // First point which defines the rectangle
                    region2_pointB, // Second point which defines the rectangle
                    GREEN, // The color the rectangle is drawn in
                    2); // Thickness of the rectangle lines

            /*
             * Draw a rectangle showing sample region 3 on the screen.
             * Simply a visual aid. Serves no functional purpose.
             */
            Imgproc.rectangle(
                    input, // Buffer to draw on
                    region3_pointA, // First point which defines the rectangle
                    region3_pointB, // Second point which defines the rectangle
                    GREEN, // The color the rectangle is drawn in
                    2); // Thickness of the rectangle lines


            /*
             * Find the min of the 3 averages
             */
            int minOneTwo = Math.max(avg1, avg2);
            int min = Math.max(minOneTwo, avg3);

            /*
             * Now that we found the max, we actually need to go and
             * figure out which sample region that value was from
             */
            if(min == avg1) // Was it from region 1?
            {
                position = DuckPosition.LEFT; // Record our analysis

                /*
                 * Draw a solid rectangle on top of the chosen region.
                 * Simply a visual aid. Serves no functional purpose.
                 */
                Imgproc.rectangle(
                        input, // Buffer to draw on
                        region1_pointA, // First point which defines the rectangle
                        region1_pointB, // Second point which defines the rectangle
                        GREEN, // The color the rectangle is drawn in
                        -1); // Negative thickness means solid fill
            }
            else if(min == avg2) // Was it from region 2?
            {
                position = DuckPosition.CENTER; // Record our analysis

                /*
                 * Draw a solid rectangle on top of the chosen region.
                 * Simply a visual aid. Serves no functional purpose.
                 */
                Imgproc.rectangle(
                        input, // Buffer to draw on
                        region2_pointA, // First point which defines the rectangle
                        region2_pointB, // Second point which defines the rectangle
                        GREEN, // The color the rectangle is drawn in
                        -1); // Negative thickness means solid fill
            }
            else if(min == avg3) // Was it from region 3?
            {
                position = DuckPosition.RIGHT; // Record our analysis

                /*
                 * Draw a solid rectangle on top of the chosen region.
                 * Simply a visual aid. Serves no functional purpose.
                 */
                Imgproc.rectangle(
                        input, // Buffer to draw on
                        region3_pointA, // First point which defines the rectangle
                        region3_pointB, // Second point which defines the rectangle
                        GREEN, // The color the rectangle is drawn in
                        -1); // Negative thickness means solid fill
            }
            else if(min < avg1) // Was it from region 3?
            {
                position = DuckPosition.NODUCK; // Record our analysis

            }


            /*
             * Render the 'input' buffer to the viewport. But note this is not
             * simply rendering the raw camera feed, because we called functions
             * to add some annotations to this buffer earlier up.
             */
            return input;
        }

        /*
         * Call this from the OpMode thread to obtain the latest analysis
         */
        public DuckPosition getAnalysis()
        {
            return position;
        }
    }

    /**
     * Initialize the AprilTag processor.
     */
    private void initAprilTag() {

        // Create the AprilTag processor the easy way.
        aprilTag = AprilTagProcessor.easyCreateWithDefaults();

        // Create the vision portal the easy way.
        if (USE_WEBCAM) {
            visionPortal = VisionPortal.easyCreateWithDefaults(
                    hardwareMap.get(WebcamName.class, "Webcam 1"), aprilTag);
        } else {
            visionPortal = VisionPortal.easyCreateWithDefaults(
                    BuiltinCameraDirection.BACK, aprilTag);
        }

    }   // end method initAprilTag()

    /**
     * Add telemetry about AprilTag detections.
     */
    private void telemetryAprilTag() {

        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        telemetry.addData("# AprilTags Detected", currentDetections.size());

        // Step through the list of detections and display info for each one.
        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata != null) {
                telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name));
                telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z));
                telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw));
                telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation));
            } else {
                telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id));
                telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y));
            }
        }   // end for() loop

        // Add "key" information to telemetry
        telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.");
        telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)");
        telemetry.addLine("RBE = Range, Bearing & Elevation");

    }
    // end method telemetryAprilTag()
    /*
     *  Method to perform a relative move, based on encoder counts.
     *  Encoders are not reset as the move is based on the current position.
     *  Move will stop if any of three conditions occur:
     *  1) Move gets to the desired position
     *  2) Move runs out of time
     *  3) Driver stops the opmode running.
     */
/*
    public void firstPosition(int turnage){
        newTarget = COUNTS_PER_MOTOR_REV;
//Try -1705 1741 as old value
        //new value -1615 and 1628
        robot.rightLinearSlide.setTargetPosition(-1615);
        robot.rightLinearSlide.setPower(0.5);
        robot.rightLinearSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.leftLinearSlide.setTargetPosition(1628);
        robot.leftLinearSlide.setPower(0.5);
        robot.leftLinearSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }
    public void slightUp(int turnage){
        newTarget = COUNTS_PER_MOTOR_REV;

        robot.rightLinearSlide.setTargetPosition(-1837);
        robot.rightLinearSlide.setPower(0.5);
        robot.rightLinearSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.leftLinearSlide.setTargetPosition(1876);
        robot.leftLinearSlide.setPower(0.5);
        robot.leftLinearSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    public void fullDown(int turnage){
        newTarget = COUNTS_PER_MOTOR_REV;

        robot.rightLinearSlide.setTargetPosition(0);
        robot.rightLinearSlide.setPower(0.5);
        robot.rightLinearSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.leftLinearSlide.setTargetPosition(0);
        robot.leftLinearSlide.setPower(0.5);
        robot.leftLinearSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }
    public void resetLinearEncoder(){

        robot.rightLinearSlide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robot.leftLinearSlide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }


 */
}
