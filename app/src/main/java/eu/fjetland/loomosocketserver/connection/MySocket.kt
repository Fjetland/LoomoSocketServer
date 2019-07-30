package eu.fjetland.loomosocketserver.connection

import android.content.Context
import android.util.Log
import eu.fjetland.loomosocketserver.*
import eu.fjetland.loomosocketserver.data.Action
import eu.fjetland.loomosocketserver.data.Head
import org.json.JSONObject
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class MySocket(myContext: Context) : Runnable {
    private lateinit var _myThread:Thread
    private lateinit var serverSocket: ServerSocket
    private  lateinit var input: InputStream
    private  lateinit var output: OutputStream
    private var isConnected = false
    private val context = myContext
    private var loomo = Loomo(myContext)

    override fun run() {
        Log.i(LOG_TAG, "Starting: MySocket Run")
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT)
        _myThread = Thread.currentThread()
        serverSocket = ServerSocket(SOCKET_PORT)

        // Main TCP communication Loop
        while (!_myThread.isInterrupted) {

            // Connect to client
            val socket = awaitClient()
            updateClientIp(socket!!.remoteSocketAddress.toString())
            updateSocketLog("Connected to: ${socket.remoteSocketAddress}")
            //loomo.mTransferPresenter.start()
            // Listen to client
            mainListenerLoop(socket)
            //loomo.mTransferPresenter.stop()
            // Notify of lost connection
            updateClientIp(context.getString(R.string.lost_client_msg))
            updateSocketLog(context.getString(R.string.tcp_reconnect_message))
            Log.i(LOG_TAG, context.getString(R.string.tcp_reconnect_message))
        }
        try {
            serverSocket.close()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Close socket exeption: ",e)
        }

        updateConversationHandler.post { viewModel.fullStoppMessage() }
        Log.e(LOG_TAG,"Shutting Down MySocket")
        loomo.onDelete()

    }

    private fun awaitClient():Socket? {
        var socket: Socket? = null
        while (!_myThread.isInterrupted and !isConnected){
            socket = serverSocket.accept()
            isConnected = socket.isConnected
        }

        return socket
    }

    private fun mainListenerLoop(socket: Socket) : Boolean {
        input = socket.getInputStream()
        output = socket.getOutputStream()
        Log.i(LOG_TAG,"Ready for incoming messages from $socket")

        while (!_myThread.isInterrupted and isConnected) {
                val read = input.read()
                when {
                    read == 1 -> readResponseID()
                    read>1 -> readActionIntent(read)
                    read == -1 -> disconnectResponse()
                    else -> {
                        Log.i(LOG_TAG, "Unexpected initial read bit value: $read")
                        Thread.sleep(10)}
                }
            //}
        }
        return false
    }

    private fun readResponseID() {
        Log.i(LOG_TAG, "Receiving a 1 bit message")
        when (val messageID = input.read()) {
            ResponsID.STOP -> loomo.mBase.stop()
            ResponsID.DISCONNECT ->disconnectResponse()
            ResponsID.STRING_NEXT -> readString()
            ResponsID.RETURNTEST -> responseTester()
            ResponsID.DATA_SURROUNDINGS_ID -> sendString2Client(loomo.returnSurroundings())
            else -> Log.e(LOG_TAG,"Unknown Response ID: $messageID")
        }
    }
    private fun readActionIntent(bytes: Int) {
        Log.i(LOG_TAG,"Receiving Action intent with: $bytes bytes")
        val string = readBytesToString(bytes)
        updateSocketLog(string)
        val data = Action(string)
        val json = JSONObject(string)
        if (json.has(ActionID.ACTION)) {
            when (json.getString(ActionID.ACTION)) {
                ActionID.VELOCITY -> loomo.setSpeeds(json)
                ActionID.SPEAK -> actionSpeak(json)
                ActionID.VOLUME -> updateConversationHandler.post { viewModel.volume.value = data.json2volume() }
                ActionID.HEAD -> loomo.headPosition(json)
                else -> Log.w(LOG_TAG, "Unknown JSON Class")
            }
        } else {
            Log.e(LOG_TAG, "JSON Action(${ActionID.ACTION}) missing in: $string")
        }
    }

    private fun readString() {
        val length = input.read()
        val string = readBytesToString(length)
        updateSocketLog("Received string: $string")
    }

    private fun readBytesToString(int: Int) : String{
        val string = readBytes(int).toString(charset(ENCODING))
        return string
    }

    private fun readBytes(int: Int) : ByteArray{
        val bytes = ByteArray(int)
        input.read(bytes,0,int)
        return bytes
    }

    private fun responseTester(){
        //val string = "Hello" //loomo.returnSurroundings()

        val bytes = loomo.getBytesFromBitmap()//string.toByteArray(charset(ENCODING))
        //val string = "Hello" //loomo.returnSurroundings()
        respondWithLongByteArray(bytes)

        loomo.testFun()
        Log.d(LOG_TAG,"Response tester finished")
    }

    private fun sendString2Client(string: String){
        val bytes = string.toByteArray(charset(ENCODING))
        respondToClient(bytes)
    }

    private fun respondToClient(byteArray: ByteArray) {
        val length = byteArray.size
        output.write(length)
        output.write(byteArray)
    }

    private fun respondWithLongByteArray(byteArray: ByteArray) {
        val length = byteArray.size
        val stringNum = length.toString().toByteArray(charset(ENCODING))
        val lengthOfLength = stringNum.size
        output.write(lengthOfLength)
        output.write(stringNum)
        output.write(byteArray)
    }

    private fun updateClientIp(string: String) {
        updateConversationHandler.post {
            viewModel.updateClientIp(string)
        }
    }

    private fun updateSocketLog(string: String) {
        Log.i(LOG_TAG, "Socket Log: $string")
        updateConversationHandler.post {
            viewModel.updateComLogText(string)
        }
    }


    private fun disconnectResponse(){
        Log.e(LOG_TAG, "Unexpected Client disconnect: -1 from $serverSocket")
        isConnected = false

    }

    private fun actionSpeak(obj: JSONObject){
        val length = obj.getInt(ActionID.SPEAK_LENGTH)
        val que = obj.getInt(ActionID.SPEAK_QUE)
        val pitch = obj.getDouble(ActionID.SPEAK_PITCH)
        readyForData()
        val message = readBytesToString(length)
        //loomo.speak(message,que,pitch)
    }

    private fun readyForData() {
        output.write(ResponsID.READY4DATA)
    }

}