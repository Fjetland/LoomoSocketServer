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

### Action Intentions
List of action intentions and accepted JSON structures

### Response ID List
One bit responses for
