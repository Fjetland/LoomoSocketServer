package eu.fjetland.loomosocketserver.connection

import android.content.Context
import android.util.Log
import eu.fjetland.loomosocketserver.*
import eu.fjetland.loomosocketserver.data.*
import eu.fjetland.loomosocketserver.loomo.LoomoSensor
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class Communicator(private val context: Context) : Runnable {
    private val TAG = "Communicator"

    private lateinit var thread: Thread
    private lateinit var serverSocket: ServerSocket

    private var shutDownSocket = false
    private var isConnected = false
    private lateinit var input: InputStream
    private lateinit var output: OutputStream

    private val mSensor = LoomoSensor(context)

    override fun run() {
        Log.i(TAG, "Starting Communicator")

        /**
         * Setup Thread
         */
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT)
        thread = Thread.currentThread()

        /**
         * Run TCP socket server
         */
        serverSocket = ServerSocket(SOCKET_PORT)
        while (!shutDownSocket){
            val socket = connect2client()
            if (socket != null){
                updateClientIp(socket.remoteSocketAddress.toString())

                mainListenerLoop(socket)

                isConnected = false
                updateClientIp(context.getString(R.string.lost_client_msg))
                socket.close()
            }
            while (thread.isInterrupted){
                Thread.sleep(10L)
            }
        }

        try {
            serverSocket.close()
        } catch (e: Exception) {
            Log.e(TAG, "Ending exeption: ", e)
        }
    }


    /**
     * Listen for TCP messages and reply
     */
    private fun mainListenerLoop(socket: Socket) {
        input = socket.getInputStream()
        output = socket.getOutputStream()
        Log.d(TAG, "Main Listener is active")

        while (!thread.isInterrupted and isConnected and !shutDownSocket) {
            val read = input.read()
            when {
                //read == 1 -> readResponseID()
                read>1 -> readAction(read) // JsonString Incomming
                read == -1 -> isConnected = false // Connection is lost
                else -> {
                    Log.i(TAG, "Unexpected initial read bit value: $read")
                    Thread.sleep(10)}
            }
        }
    }

    private fun readAction(bytes : Int){
        try {
            /**
             * Read and translate actions here
             */
            val action = Action(readBytes(bytes)) // Read and phrase JSON
            if (action.actionType in Action.ACTIONLIST || action.actionType in DataResponce.DATALIST){ // Check if known action
                when (action.actionType) { // Decide responce
                    Action.HEAD -> updateHead(action.json2head())
                    Action.ENABLE_DRIVE -> updateEnableDrive(action.json2enableDrive())
                    Action.VELOCITY -> updateVelocity(action.json2velocity())
                    Action.POSITION -> updatePosition(action.json2position())
                    Action.SPEAK -> updateSpeak(action.json2speak())
                    Action.VOLUME ->  updateVolume(action.json2volume())
                    DataResponce.SURROUNDINGS -> sendSurroundings()
                    DataResponce.WHEEL_SPEED -> sendWheelSpeed()
                    DataResponce.POSE2D -> sendPose2D()
                    DataResponce.HEAD_WORLD -> sendHeadPoseWorld()
                    DataResponce.HEAD_JOINT -> sendHeadPoseJoint()
                    DataResponce.BASE_IMU -> sendBaseImu()
                    DataResponce.BASE_TICK -> sendBaseTick()
                    else -> Log.w(TAG,"Action Not implemented")
                }
            } else {
                Log.w(TAG, "Action type: '${action.actionType}' is unknown ")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in reading JSON", e)
        }

    }

    private fun connect2client() : Socket?{
        Log.i(TAG, "Awaiting connection from client")
        var socket : Socket? = null
        while (!thread.isInterrupted and !isConnected and !shutDownSocket) {
            Log.i(TAG, "Connection from client")
           socket = serverSocket.accept()
           isConnected = socket.isConnected
        }
        return socket
    }

    /**
     *  Viewmodel Update functions
     */
    private fun updateHead(head: Head) {
        updateConversationHandler.post {
            viewModel.head.value = head
        }
    }

    private fun updateEnableDrive(enableDrive: EnableDrive) {
        updateConversationHandler.post {
            viewModel.endableDrive.value = enableDrive
        }
    }

    private fun updateVelocity(velocity: Velocity) {
        updateConversationHandler.post {
            viewModel.velocity.value = velocity
        }
    }

    private fun updatePosition(position: Position) {
        updateConversationHandler.post {
            viewModel.position.value = position
        }
    }

    private fun updateSpeak(speak: Speak) {
        speak.string = readString(speak.length)
        updateConversationHandler.post {
            viewModel.speak.value = speak
        }
    }

    private fun updateVolume(volume: Volume){
        updateConversationHandler.post {
            viewModel.volume.value = volume
        }
    }
    private fun updateClientIp(string: String) {
        updateSocketLog("Connected to: $string")
        updateConversationHandler.post {
            viewModel.updateClientIp(string)
            viewModel.isConnected.value = isConnected
        }
    }

    private fun updateSocketLog(string: String) {
        updateConversationHandler.post {
            viewModel.updateComLogText(string)
        }
    }

    /**
     * Sensor reading responces
     */

    private fun sendSurroundings() {
        val data = mSensor.getSurroundings()
        sendString(DataResponce.sensSurroundings2JSONstring(data))
    }

    private fun sendWheelSpeed() {
        val data = mSensor.getWheelSpeed()
        sendString(DataResponce.sensWheelSpeed2JSONstring(data))
    }

    private fun sendHeadPoseWorld() {
        val data = mSensor.getHeadPoseWorld()
        sendString(DataResponce.sensHeadPoseWorld2JSONstring(data))
    }

    private fun sendHeadPoseJoint() {
        val data = mSensor.getHeadPoseJoint()
        sendString(DataResponce.sensHeadPoseJoint2JSONstring(data))
    }

    private fun sendBaseImu() {
        val data = mSensor.getSensBaseImu()
        sendString(DataResponce.sensBasePose2JSONstring(data))
    }

    private fun sendBaseTick() {
        val data = mSensor.getSensBaseTick()
        sendString(DataResponce.sensBaseTick2JSONstring(data))
    }

    private fun sendPose2D() {
        val data = mSensor.getSensPose2D()
        sendString(DataResponce.sensPose2D2JSONstring(data))
    }

    /**
     * Utilitys
     */

    private fun readBytes(int: Int) : ByteArray{
        val bytes = ByteArray(int)
        input.read(bytes,0,int)
        return bytes
    }

    private fun readString(int: Int) : String {
        return try {
            readBytes(int).toString(charset(ENCODING))
        } catch (e: Exception) {
            "Nothing"
        }
    }

    private fun sendBytes(byteArray: ByteArray) {
        output.write(byteArray)
    }

    private fun sendString(string: String){
        val byteArray = string.toByteArray(charset(ENCODING))
        val length = byteArrayOf(byteArray.size.toByte())
        sendBytes(length)
        sendBytes(byteArray)
    }
}