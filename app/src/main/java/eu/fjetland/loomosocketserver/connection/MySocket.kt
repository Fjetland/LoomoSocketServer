package eu.fjetland.loomosocketserver.connection

import android.util.Log
import eu.fjetland.loomosocketserver.ENCODING
import eu.fjetland.loomosocketserver.LOG_TAG
import eu.fjetland.loomosocketserver.SOCKET_PORT
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class MySocket : Runnable {
    private lateinit var _myThread:Thread
    private lateinit var serverSocket: ServerSocket
    private  lateinit var input: InputStream
    private  lateinit var output: OutputStream

    override fun run() {
        Log.i(LOG_TAG, "Starting: MySocket Run")
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT)
        _myThread = Thread.currentThread()
        serverSocket = ServerSocket(SOCKET_PORT)

        while (!_myThread.isInterrupted) {
            val socket = awaitClient()
            biReadInitializer(socket as Socket)
            //listenForMessages(socket as Socket)

            Log.i(LOG_TAG, "Trying to re-connect to Socket")
        }

    }

    private fun awaitClient():Socket? {
        Log.i(LOG_TAG,"Pre-connect")
        var socket: Socket? = null
        var notConnected = true
        while (!_myThread.isInterrupted and notConnected){
            socket = serverSocket.accept()
            notConnected = !socket.isConnected
        }
        Log.i(LOG_TAG, "Socket: $socket")

        return socket
    }

    private fun biReadInitializer(socket: Socket) : Boolean {
        input = socket.getInputStream()
        output = socket.getOutputStream()
        Log.i(LOG_TAG,"Initialized bit Listener")

        while (!_myThread.isInterrupted) {
            var newMessage = false
            while (newMessage.not()){
                val read = input.read()
                if (read>=0){
                    newMessage = true
                    Log.i(LOG_TAG,"Reciving message of: ${read.toString()}bytes")
                    readInitializer(read)
                } else {
                    Thread.sleep(10)
                }
            }
            Log.i(LOG_TAG,"Recived Message")
        }
        Log.e(LOG_TAG, "Lost Connection to: $socket")
        return false
    }

    private fun readInitializer(bytes: Int) {
        val data = ByteArray(bytes)
        input.read(data,0,bytes)
        val string = data.toString(charset(ENCODING))
        Log.i(LOG_TAG,"Decoded string: $string")
    }



}