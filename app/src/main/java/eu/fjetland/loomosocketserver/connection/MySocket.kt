package eu.fjetland.loomosocketserver.connection

import android.content.Context
import android.util.Log
import eu.fjetland.loomosocketserver.*
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

    override fun run() {
        Log.i(LOG_TAG, "Starting: MySocket Run")
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT)
        _myThread = Thread.currentThread()
        serverSocket = ServerSocket(SOCKET_PORT)

        while (!_myThread.isInterrupted) {
            val socket = awaitClient()
            updateClientIp(socket!!.remoteSocketAddress.toString())
            updateSocketLog("Connected to: ${socket.remoteSocketAddress.toString()}")
            biReadInitializer(socket)
            updateClientIp(context.getString(R.string.lost_client_msg))
            //listenForMessages(socket as Socket)
            updateSocketLog("Disconnected from client, awaiting new connection")
            Log.i(LOG_TAG, "Trying to re-connect to Socket")
        }

    }

    private fun awaitClient():Socket? {
        Log.i(LOG_TAG,"Pre-connect")
        var socket: Socket? = null
        while (!_myThread.isInterrupted and !isConnected){
            socket = serverSocket.accept()
            isConnected = socket.isConnected
        }
        Log.i(LOG_TAG, "Socket: $socket")

        return socket
    }

    private fun biReadInitializer(socket: Socket) : Boolean {
        input = socket.getInputStream()
        output = socket.getOutputStream()
        Log.i(LOG_TAG,"Initialized bit Listener")

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
                        Log.i(LOG_TAG,"Receiving message of: ${read.toString()}bytes")
                        readInitializer(read)
                    }
                    else -> Thread.sleep(10)
                }
            }
            Log.i(LOG_TAG,"Received Message")
        }
        Log.e(LOG_TAG, "Lost Connection to: $socket")
        return false
    }

    private fun readResponseID() {
        val messageID = input.read()
        when (messageID) {
            ResponsID.DISCONNECT -> {
                isConnected = false
                Log.e(LOG_TAG, "Connection lost")
            }
            else -> Log.e(LOG_TAG,"Unknown Response ID: $messageID")
        }
        Log.i(LOG_TAG, "Response ID: $messageID")
    }

    private fun readInitializer(bytes: Int) {
        val data = ByteArray(bytes)
        input.read(data,0,bytes)
        val string = data.toString(charset(ENCODING))
        updateSocketLog(string)
        Log.i(LOG_TAG,"Decoded string: $string")
    }

    private fun updateClientIp(string: String) {
        updateConversationHandler.post {
            viewModel.updateClientIp(string)
        }
    }

    private fun updateSocketLog(string: String) {
        updateConversationHandler.post {
            viewModel.updateComLogText(string)
        }
    }



}