package eu.fjetland.loomosocketserver.connection

import android.content.Context
import android.util.Log
import eu.fjetland.loomosocketserver.*
import eu.fjetland.loomosocketserver.data.*
import eu.fjetland.loomosocketserver.loomo.LoomoSensor
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
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
            } catch (e : SocketTimeoutException) {
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
                    Action.SEQUENCE -> runSequence(action)
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

        }
        return socket
    }

    /**
     *  Viewmodel Update functions
     */


    private fun runSequence(action: Action) {

        /**
         * Actions
         */
        if (action.jsonObject.has(Action.HEAD)){
            val obj = Action(action.jsonObject.getJSONObject(Action.HEAD))
            updateHead(obj.json2head())
        }

        if (action.jsonObject.has(Action.ENABLE_VISION)) {
            val obj = Action(action.jsonObject.getJSONObject(Action.ENABLE_VISION))
            updateEnableVision(obj.json2EenableVision())
        }

        if (action.jsonObject.has(Action.ENABLE_DRIVE)){
            val obj = Action(action.jsonObject.getJSONObject(Action.ENABLE_DRIVE))
            updateEnableDrive(obj.json2enableDrive())
        }//    Log.i(TAG,"Replying with ${json.length()} sensor structures")


        when {
            action.jsonObject.has(Action.VELOCITY) -> {
                val obj = Action(action.jsonObject.getJSONObject(Action.VELOCITY))
                updateVelocity(obj.json2velocity())
            }
            action.jsonObject.has(Action.POSITION) -> {
                val obj = Action(action.jsonObject.getJSONObject(Action.POSITION))
                updatePosition(obj.json2position())
            }
            action.jsonObject.has(Action.POSITION_ARRAY) -> {
                val obj = Action(action.jsonObject.getJSONObject(Action.POSITION_ARRAY))
                updatePositionArray(obj.json2positionArray())
            }
        }

        if (action.jsonObject.has(Action.VOLUME)){
            val obj = Action(action.jsonObject.getJSONObject(Action.VOLUME))
            updateVolume(obj.json2volume())
        }

        if (action.jsonObject.has(Action.SPEAK)) {
            val obj = Action(action.jsonObject.getJSONObject(Action.SPEAK))
            updateSpeak(obj.json2speak())
        }

        /**
         * Sensors and data return
         */
        val json  = JSONObject()

        if (action.jsonObject.has(DataResponce.SURROUNDINGS)){
            val data = mSensor.getSurroundings()
            json.put(DataResponce.SURROUNDINGS,
                DataResponce.sensSurroundings2JSON(data))
        }

        if (action.jsonObject.has(DataResponce.WHEEL_SPEED)){
            val data = mSensor.getWheelSpeed()
            json.put(DataResponce.WHEEL_SPEED,
                DataResponce.sensWheelSpeed2JSON(data))
        }

        if (action.jsonObject.has(DataResponce.POSE2D)){
            val data = mSensor.getSensPose2D()
            json.put(DataResponce.POSE2D,
                DataResponce.sensPose2D2JSON(data))
        }

        if (action.jsonObject.has(DataResponce.HEAD_WORLD)){
            val data = mSensor.getHeadPoseWorld()
            json.put(DataResponce.HEAD_WORLD,
                DataResponce.sensHeadPoseWorld2JSON(data))
        }

        if (action.jsonObject.has(DataResponce.HEAD_JOINT)){
            val data = mSensor.getHeadPoseJoint()
            json.put(DataResponce.HEAD_JOINT,
                DataResponce.sensHeadPoseJoint2JSON(data))
        }

        if (action.jsonObject.has(DataResponce.BASE_IMU)){
            val data = mSensor.getSensBaseImu()
            json.put(DataResponce.BASE_IMU,
                DataResponce.sensBasePose2JSON(data))
        }

        if (action.jsonObject.has(DataResponce.BASE_TICK)){
            val data = mSensor.getSensBaseTick()
            json.put(DataResponce.BASE_TICK,
                DataResponce.sensBaseTick2JSON(data))
        }

        if (json.length()>0){
        //    Log.i(TAG,"Replying with ${json.length()} sensor structures")
            sendString(json.toString())
        }
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
        sendString(DataResponce.sensSurroundings2JSON(data).toString())
    }

    private fun sendWheelSpeed() {
        val data = mSensor.getWheelSpeed()
        sendString(DataResponce.sensWheelSpeed2JSON(data).toString())
    }

    private fun sendHeadPoseWorld() {
        val data = mSensor.getHeadPoseWorld()
        sendString(DataResponce.sensHeadPoseWorld2JSON(data).toString())
    }

    private fun sendHeadPoseJoint() {
        val data = mSensor.getHeadPoseJoint()
        sendString(DataResponce.sensHeadPoseJoint2JSON(data).toString())
    }

    private fun sendBaseImu() {
        val data = mSensor.getSensBaseImu()
        sendString(DataResponce.sensBasePose2JSON(data).toString())
    }

    private fun sendBaseTick() {
        val data = mSensor.getSensBaseTick()
        sendString(DataResponce.sensBaseTick2JSON(data).toString())
    }

    private fun sendPose2D() {
        val data = mSensor.getSensPose2D()
        sendString(DataResponce.sensPose2D2JSON(data).toString())
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
        return bytes
    }

    private fun sendBytes(byteArray: ByteArray) {
        output.write(byteArray)
    }

    private fun sendString(string: String){
        val byteArray = string.toByteArray(charset(ENCODING))
        val length = byteArray.size
        //Log.i(TAG,"Sending $length bytes")
        val byteLength = byteArrayOf(
            (length and 0x0000FF00 shr 8).toByte(),
            (length and 0x000000FF).toByte()
        )
        sendBytes(byteLength)
        sendBytes(byteArray)
    }
}