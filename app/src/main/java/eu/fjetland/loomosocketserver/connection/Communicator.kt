package eu.fjetland.loomosocketserver.connection

import android.content.Context
import android.util.Log
import eu.fjetland.loomosocketserver.*
import eu.fjetland.loomosocketserver.data.*
import eu.fjetland.loomosocketserver.loomo.LoomoSensor
import kotlinx.coroutines.delay
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException

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
        serverSocket.soTimeout = 500
        while (!thread.isInterrupted){
            try {
                val socket = connect2client()
                if (socket != null){
                    updateClientIp(socket.remoteSocketAddress.toString())

                    mainListenerLoop(socket)

                    isConnected = false
                    updateEnableDrive(EnableDrive(false))
                    updateClientIp(context.getString(R.string.lost_client_msg))
                    socket.close()
                }
            } catch (e : Exception) {

            }

        }

        try {
            serverSocket.close()
        } catch (e: Exception) {
            Log.e(TAG, "Ending exeption: ", e)
        }
        Log.i(TAG, "Communication thread shutting down!")
    }


    /**
     * Listen for TCP messages and reply
     */
    private fun mainListenerLoop(socket: Socket) {
        socket.soTimeout = 500
        input = socket.getInputStream()
        output = socket.getOutputStream()
        Log.d(TAG, "Main Listener is active")
        while (!thread.isInterrupted and isConnected and !shutDownSocket) {
            if (thread.isInterrupted) {
                Log.i(TAG, "Thread is interrupted at MainListener")
                isConnected = false
                return
            }
            try {
                //val read = readInitialNumber()
                var read = input.read()
                if (read >= 0){
                    val r2 = input.read()
//                        Log.i(TAG, "b1 = ${read} b2 = ${r2}")
                    read = read shl 8 or r2
//                        Log.i(TAG, "Bytes to read = ${read} ")
                    if (read > 1) readAction(read)
                } else {
                    isConnected = false // Connection is lost
                }

//                when {
//                    //read == 1 -> readResponseID()
//                    //read>1 -> readAction(read) // JsonString Incoming
//
//                    else -> {
//                        val r2 = input.read()
////                        Log.i(TAG, "b1 = ${read} b2 = ${r2}")
//                        read = read shl 8 or r2
////                        Log.i(TAG, "Bytes to read = ${read} ")
//                        if (read > 1) readAction(read)
//                        //Log.i(TAG, "Unexpected initial read bit value: $read")
//                        }
//                }
            } catch (e : SocketTimeoutException) {
            }
        }
    }

    private fun readAction(bytes : Int){
        try {
            /**
             * Read and translate actions here
             */
//            val rb = readBytes(bytes)
//            Log.i(TAG,"ReadByteLength ${rb.size}")
//            val last = rb[rb.size-1].toInt()
//            Log.i(TAG,"Last byte $last")
            val action = Action(readBytes(bytes)) // Read and phrase JSON
            if (action.actionType in Action.ACTIONLIST || action.actionType in DataResponce.DATALIST){ // Check if known action
                when (action.actionType) { // Decide responce
                    //Action.SEQUENCE -> runSequence(action)
                    Action.HEAD -> updateHead(action.json2head())
                    Action.ENABLE_DRIVE -> updateEnableDrive(action.json2enableDrive())
                    Action.ENABLE_VISION -> updateEnableVision(action.json2EenableVision())
                    Action.VELOCITY -> updateVelocity(action.json2velocity())
                    Action.POSITION -> updatePosition(action.json2position())
                    Action.POSITION_ARRAY -> updatePositionArray(action.json2positionArray())
                    Action.SPEAK -> updateSpeak(action.json2speak())
                    Action.VOLUME ->  updateVolume(action.json2volume())
                    DataResponce.SURROUNDINGS -> sendSurroundings()
                    DataResponce.WHEEL_SPEED -> sendWheelSpeed()
                    DataResponce.POSE2D -> sendPose2D()
                    DataResponce.HEAD_WORLD -> sendHeadPoseWorld()
                    DataResponce.HEAD_JOINT -> sendHeadPoseJoint()
                    DataResponce.BASE_IMU -> sendBaseImu()
                    DataResponce.BASE_TICK -> sendBaseTick()
                    DataResponce.IMAGE -> sendImage(action.json2imageResponce())
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
        //Log.i(TAG, "Awaiting connection from client")
        var socket : Socket? = null
        while (!thread.isInterrupted and !isConnected) {
            try {
                socket = serverSocket.accept()
                isConnected = socket.isConnected
            } catch (e : SocketTimeoutException) {

            }

           //isConnected =
        }
        return socket
    }

    /**
     *  Viewmodel Update functions
     */


    private fun runSequence(action: Action) {

    }

    private fun updateHead(head: Head) {
        updateConversationHandler.post {
            viewModel.head.value = head
        }
    }

    private fun updateEnableVision(enableVision: EnableVision) {
        updateConversationHandler.post {
            viewModel.activeStreams.value = enableVision
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

    private fun updatePositionArray(positionArray: PositionArray){
        updateConversationHandler.post {
            viewModel.positionArray.value = positionArray
        }
    }

    private fun updateSpeak(speak: Speak) {
        //speak.string = readString(speak.length)
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

    private fun sendImage(meta : ImageResponse) {

        when (meta.type) {
            DataResponce.IMAGE_TYPE_COLOR_SMALL ->{
                meta.size = viewModel.colorSmallBitArray.value?.size ?: 1

                Log.i(TAG,"Sending small color image of ${meta.size} bytes")
                sendString(DataResponce.sensImage2JSONstring(meta))
                sendBytes(viewModel.colorSmallBitArray.value ?: byteArrayOf(0))
            }
            DataResponce.IMAGE_TYPE_COLOR -> {

                meta.size = viewModel.colorLargeBitArray.value?.size ?: 1
                Log.i(TAG,"Sending full color image of ${meta.size} bytes")
                sendString(DataResponce.sensImage2JSONstring(meta))
                sendBytes(viewModel.colorLargeBitArray.value ?: byteArrayOf(0))
            }
            DataResponce.IMAGE_TYPE_DEPTH -> {
                meta.size = viewModel.colorDepthBitArray.value?.size ?: 1
                Log.i(TAG,"Sending depth image of ${meta.size} bytes")
                sendString(DataResponce.sensImage2JSONstring(meta))
                sendBytes(viewModel.colorDepthBitArray.value ?: byteArrayOf(0))
            }
            else -> {
                Log.i(TAG,"Unknown Image")
                meta.type = "Unknown"
                meta.size = 1
                meta.width = 1
                meta.height = 1
                sendString(DataResponce.sensImage2JSONstring(meta))
                sendBytes(byteArrayOf(0))
            }
        }
    }

    /**
     * Utilitys
     */

    private fun readBytes(int: Int) : ByteArray{
        val bytes = ByteArray(int)
        for (byte in 0 until int){
            bytes[byte] = input.read().toByte()
        }

        //val x = input.read(bytes,0,int)
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