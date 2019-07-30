package eu.fjetland.loomosocketserver.connection

import android.content.Context
import android.util.Log
import eu.fjetland.loomosocketserver.*
import eu.fjetland.loomosocketserver.data.*
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class Communicator(private val context: Context) : Runnable {
    private val TAG = "Communicator"

    private lateinit var thread: Thread
    private lateinit var serverSocket: ServerSocket

    var shutDownSocket = false
    var isConnected = false
    lateinit var input: InputStream
    lateinit var output: OutputStream

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

                updateClientIp(context.getString(R.string.lost_client_msg))
                socket.close()
                isConnected = false
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
            if (action.actionType in Action.ACTIONLIST){ // Check if known action
                when (action.actionType) { // Decide responce
                    Action.HEAD -> updateHead(action.json2head())
                    Action.VELOCITY -> updateVelocity(action.json2velocity())
                    Action.POSITION -> updatePosition(action.json2position())
                    Action.SPEAK -> updateSpeak(action.json2speak())
                    Action.VOLUME ->  updateVolume(action.json2volume())
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
        }
    }

    private fun updateSocketLog(string: String) {
        updateConversationHandler.post {
            viewModel.updateComLogText(string)
        }
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
}