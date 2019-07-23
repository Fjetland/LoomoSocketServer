# Loomo Socket Server
###### A TCP communication tool for the Segway Loomo Robot
This tool was developed at the University Of Agder Norway in order to allow for student programming projects on the Loomo platform in different programming languages then Kotlin / Java.

The software's main task is to control the Loomo robot by use of the Loomo API tools triggerd trough TCP commaneds. The Loomo vil act as a slave to the connected client only responding to givven TCP commands.

A corresponding MATLAB client tool can be found at: (To be listed)

# Documentation

## Communication protocol
Description of the agreed upon communication protocol between sender and receiver. The Loomo server will be a slave to the client and only respond to given commands found in the **_Action Intention_** list or **_Responce ID_** list.

 Stepp | #1 | #2 | #3 | #4 | #5
 ------|----|----|----|----|----
 **Sender** | Next bytes | Intention | *Listen* | Transfer*  | Listen*
 **Receiver** | *Listen* | *Listen* | Confirm | Listen* | Confirm*
 Data Type | *uint8[1]* | *JSON string[<256]* |*uint8[1]* | *bitArray* |*uint8[1]*

 > The client is designed to be the main sender, and the Loomo robot / is designed to me the main listener. As such any contact should be initialized by the tcp client.
 > Items marked * will depend on the desired action

###### Description

1. **Next byte**: An uint8 (0,255] value informing the number of bits to be sent in the message containing the JSON **_Action intention_**
2. **Intention**: A JSON structure with a max string length of 255 chars informing of the action  response or transfer to be done. Can also hold associated variables with the intended action.
  - If "*Next Byte*"== 1, then a **_Response ID_** is used
3. Uint8 Confirmation ID in accordance with **The Response ID List**

### NO ACTIONS OR REPONCES IMPLEMENTED IN CURRET BUILD

### Action Intentions
List of action intentions and accepted JSON structures

#### Locomotion control
###### Keyword: loc
loc: Loomo velocity control

Variable | Key | Description
---|---|---
act  | loc | Activate Locomotion Commands
**_Variable_** | **_Range_** | **_Description_**
v | 0-4 m/s? | [double] Desired velocity
t | 0-0.5 /s? | [double] Desired CW turn rate

###### Keyword: pos
Variable | Key | Description
---|---|---
act  | loc | Activate Locomotion Commands
**_Variable_** | **_Range_** | **_Description_**
v | 0-4 m/s? | [double] Desired velocity
t | 0-0.5 /s? | [double] Desired CW turn rate

#### Speak
Variable | Key | Description
---|---|---
act  | spk | Activate Locomotion Commands
**_Variable_** | **_Range_** | **_Description_**
l |  | Length of text
p | 0.5-2 | [double] Pitch 1 is normal.
q | 1 or 2| [int] Que mode - 1= now (default) 2 = add

Followed by a string of the exact length **_l_**

#### Volume
Variable | Key | Description
---|---|---
act  | vol | Activate Locomotion Commands
**_Variable_** | **_Range_** | **_Description_**
v | 0-1 | (double) Volume

Followed by a string of the exact length **_l_**

### Response ID List
One bit greetings/commands

##### Basic responses and confirmations

Number | Description
-------|------------
1 | Yes
2 | No
3 | Ready for data
4 | Re-send / Not received
6 | Urgent message following
10 | Disconnecting

##### Commands to Loomo [16,31]
Number | Description
-------|------------
16 | Status
17 | Stop



##### Loomo responses [32,64]

Number | Description
-------|------------
32 | All Okay
33 | Error detected
