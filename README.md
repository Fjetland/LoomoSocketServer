# Loomo Socket Server
###### A TCP communication tool for the Segway Loomo Robot
This tool was developed at the University Of Agder Norway in order to allow for student programming projects on the Loomo platform in different programming languages then Kotlin / Java.

The software's main task is to control the Loomo robot by use of the Loomo API tools triggerd trough TCP commaneds. The Loomo vil act as a slave to the connected client only responding to givven TCP commands.

A corresponding MATLAB client tool can be found at: (To be listed)

# Documentation

## Communication protocol
Description of the agreed upon communication protocol between sender and receiver. The Loomo server will be a slave to the client and only respond to given commands found in the **_Action Intention_** list or **_Sensor readings_** list.

 Stepp | #1 | #2 | #3
 ------|----|----|----
 **Sender** | Next bytes | Intention  | Receive/Transfer*
 **Receiver** | *Listen* | *Listen* | Receive/Transfer*
 Data Type | *uint8* | *JSON string[<256]* | *bitArray**

 > The client is designed to be the main sender, and the Loomo robot / is designed to me the main listener. As such any contact should be initialized by the tcp client.
 > Items marked * will depend on the desired action

###### Description

1. **Next byte**: An uint8 (0,255] value informing the number of bits to be sent in the message containing the JSON **_Action intention_**
2. **Intention**: A JSON structure with a max string length of 255 chars informing of the action  response or transfer to be done. Can also hold associated variables with the intended action.


### Action Commands to Loomno
List of action intentions and accepted JSON structures.

#### Head control
hed: Head control

Variable | Key | Description
---|---|---
act  | hed | Activate Head Commands
**_Variable_** | **_Value_** | **_Description_**
p | -PI / 2 to PI [float] |  Desired pitch position in radian
t | -PI / 1.2 to PI / 1.2 [float] | Desired yaw position in radian
li| 0-13 [Int] | Light modus (Optional). Default: 10
m | 0/1 [Int] | Head control modus (Optional). Default: 0

*Head control modus:*
0. _The Smooth Tracking mode:_ In this mode, the Pitch axis of the head is stable and can effectively filter the impact of the body. The Yaw axis can rotate following the base. In this state, the head can be controlled by setting the angle using the base as the reference frame. [Ref](https://developer.segwayrobotics.com/developer/documents/segway-robots-sdk.html)
1. _The Lock mode:_ In this mode, the Pitch axis on the head is stable and can effectively filter the impact of the body. The Yaw axis points to a certain direction in the world coordinate system. For a fixed point, this model can be used to achieve stable shooting results. In this mode, you can control the head orientation by setting the head rotation velocity. [Ref](https://developer.segwayrobotics.com/developer/documents/segway-robots-sdk.html)


#### Enable drive
###### Keyword: enableDrive
enableDrive: Enables loomo to drive on velocity and position commands

This value is by default set to _false_ and must be set to _true_ before any driving commands can be accepted. By setting this to _false_, all locomotion except balance will stop and new commands will not be accepted.

Variable | Key | Description
---|---|---
act  | enableDrive | Enable drive
**_Variable_** | **_Value_** | **_Description_**
value | boolean | True or False

#### Velocity control
###### Keyword: vel
vel: Loomo velocity control

Variable | Key | Description
---|---|---
act  | vel | Activate Velocity Commands
**_Variable_** | **_Value_** | **_Description_**
v | 0-4 m/s [Float] | Desired velocity
av | 0-4 rad/s [Float] | Desired CCW turn rate

###### Keyword: pos
Variable | Key | Description
---|---|---
act  | pos | Activate Locomotion Commands
**_Variable_** | **_Value_** | **_Description_**
x | m [Float] | X displacement from current position
y | m [Float] | Y displacement from current position
th | rad [Float] | CCW Rotational displacement from current position
~~add~~ | ~~[Boolean]~~ | ~~Add point to list~~

#### Speak
Variable | Key | Description
---|---|---
act  | spk | Activate Locomotion Commands
**_Variable_** | **_Value_** | **_Description_**
l | [Int] | Length of text
p | 0.5-2 [Float] | (Optional) Pitch 1 is Default.
q | 0-1 [Int] |  Que Mode: 1= now (default) 2 = add

Directly followed by a string to speak of the exact length **_l_**.

#### Volume
Variable | Key | Description
---|---|---
act  | vol | Activate Locomotion Commands
**_Variable_** | **_Value_** | **_Description_**
v | 0-1 [Float] | Volume

Followed by a string of the exact length **_l_**


### Sensor readings from Loomo
Sensor sets can be requested by a JSON string with variable act matching any of the listed keys sent to the loomo. The loomo will respond with the given JSON structures.

**Note:** The unit of Distance is the millimeter. The unit of Angle is the radian. The unit of LinearVelocity is meters per second. The unit of AngularVelcity is radians per second. The unit of LeftTicks and RightTicks is Tick, which equals one centimeter when the tires are properly inflated.

#### Surroundings
Returns IR sensor data and Ultrasonic sensor data.

Variable | Key | Description
---|---|---
act  | sSur | Surrounding distances (IR and USS)
**_Variable_** | **_Value_** | **_Description_**
irl | mm [Int] | Left infrared sensor distance in mm
irr | mm [Int] | Right infrared sensor distance in mm
uss | mm [Int] | Forward Ultrasonic sensor in mm

The ultrasonic sensor is designed to detect obstacles and avoid collisions. The ultrasonic sensor is mounted in the front of Loomo, with a detection distance from 250 millimeters to 1500 millimeters and an angle beam of 40 degrees.

**Note:** There is a known issue that when the distance between the obstacle and the ultrasonic sensor is less than 250 millimeters, an incorrect value may be returned.

#### Wheel speed
Returns individual wheel speed in m/s

Variable | Key | Description
---|---|---
act  | sWS | Individual wheel speed
**_Variable_** | **_Value_** | **_Description_**
vl | m/s [Float] | Left wheel speed
vr | m/s [Float] | Right wheel speed


#### Pose2D
Returns base Pose and velocity -Pose is relative to start position or last Pose reset (runs in set position)

Variable | Key | Description
---|---|---
act  | sP2d | Pose 2D
**_Variable_** | **_Value_** | **_Description_**
x | m [Float] | X- displacement from last reset
y | m [Float] | Y- displacement from last reset
th | rad [Float] | Rotational displacement from last reset
vl | m/s [Float] | Linear velocity
va | rad/s [Float] | Angular velocity

#### Base Imu
Returns Base IMU

Variable | Key | Description
---|---|---
act  | sBP | Base Imu
**_Variable_** | **_Value_** | **_Description_**
p | rad [Float] | Pitch
r | rad [Float] | Roll
y | rad [Float] | Yaw


#### Head world
Returns the heads world position as measured by the internal head IMU (Inertial Measurement Unit)

Variable | Key | Description
---|---|---
act  | sHPw | Head World
**_Variable_** | **_Value_** | **_Description_**
p | rad [Float] | Pitch
r | rad [Float] | Roll
y | rad [Float] | Yaw

#### Head joint
Returns the head position as measured by the joints to the base. In relations to the base frame.

Variable | Key | Description
---|---|---
act  | sHPj | Head Joint
**_Variable_** | **_Value_** | **_Description_**
p | rad [Float] | Pitch
r | rad [Float] | Roll
y | rad [Float] | Yaw

#### Base ticks / Wheels ticks
Returns the measured encoder wheel position. 1 tick is ~1cm on correctly inflated wheels.

Variable | Key | Description
---|---|---
act  | sBT | Base ticks / Wheels ticks
**_Variable_** | **_Value_** | **_Description_**
l |  [Int] | Left wheel Ticks
r |  [Int] | Right wheel Ticks
