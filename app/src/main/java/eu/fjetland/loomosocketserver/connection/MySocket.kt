package eu.fjetland.loomosocketserver.connection

import android.content.Context
import android.util.Log
import eu.fjetland.loomosocketserver.*
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

            // Listen to client
            bitReadInitializer(socket)

            // Notify of lost connection
            updateClientIp(context.getString(R.string.lost_client_msg))
            updateSocketLog(context.getString(R.string.tcp_reconnect_message))
            Log.i(LOG_TAG, context.getString(R.string.tcp_reconnect_message))
        }

    }

    private fun awaitClient():Socket? {
        var socket: Socket? = null
        while (!_myThread.isInterrupted and !isConnected){
            socket = serverSocket.accept()
            isConnected = socket.isConnected
        }

        return socket
    }

    private fun bitReadInitializer(socket: Socket) : Boolean {
        input = socket.getInputStream()
        output = socket.getOutputStream()
        Log.i(LOG_TAG,"Ready for incoming messages from $socket")

        while (!_myThread.isInterrupted and isConnected) {
            var newMessage = false
            while (newMessage.not()) {
                val read = input.read()
                when {
                    read == 1 -> {
                        newMessage = true
                        Log.i(LOG_TAG, "Receiving a 1 bit message")
                        readResponseID()
                    }
                    read>1 -> {
                        newMessage = true
                        Log.i(LOG_TAG,"Receiving message of: $read bytes")
                        readInitializer(read)
                    }
                    read == -1 -> {
                        disconnectResponse()
                        Log.e(LOG_TAG, "Unexpected Client disconnect: $read from $socket")
                    }
                    else -> {
                        Log.i(LOG_TAG, "Unexpected initial read bit value: $read")
                        Thread.sleep(10)}
                }
            }
        }
        return false
    }

    private fun readResponseID() {
        when (val messageID = input.read()) {
            ResponsID.DISCONNECT -> {
                disconnectResponse()
                Log.i(LOG_TAG, "Disconnect message received from client")
            }
            else -> Log.e(LOG_TAG,"Unknown Response ID: $messageID")
        }
    }

    private fun readInitializer(bytes: Int) {
        val data = ByteArray(bytes)
        input.read(data,0,bytes)
        val string = data.toString(charset(ENCODING))
        updateSocketLog(string)
        //readAndLogJsonString(string)
        decodeJSONandAct(string)
        Log.i(LOG_TAG,"Decoded string: $string")
    }

    private fun decodeJSONandAct(string: String) {
        val obj = JSONObject(string)
        if (obj.has(ActionID.ACTION)) {
            when (obj.getString(ActionID.ACTION)) {
                ActionID.SPEAK -> actionSpeak(obj)
                ActionID.VOLUME -> loomo.setVolume(obj.getDouble(ActionID.VOLUME_VALUE))
                else -> {
                    Log.w(LOG_TAG, "Unknown JSON Class")
                }
            }
        } else {
            Log.e(LOG_TAG, "JSON Action(${ActionID.ACTION}) missing in: $string")
        }
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

    private fun readLongText(length: Int) : String {
        readyForData()
        val bytes = ByteArray(length)
        input.read(bytes,0,length)
        val string = bytes.toString(charset(ENCODING))
        return string
    }

    private fun disconnectResponse(){
        isConnected = false

    }

    private fun actionSpeak(obj: JSONObject){
        val length = obj.getInt(ActionID.SPEAK_LENGTH)
        val que = obj.getInt(ActionID.SPEAK_QUE)
        val pitch = obj.getDouble(ActionID.SPEAK_PITCH)
        val message = readLongText(length)
        loomo.speak(message,que,pitch)
    }

    private fun readyForData() {
        output.write(ResponsID.READY4DATA)
    }

}