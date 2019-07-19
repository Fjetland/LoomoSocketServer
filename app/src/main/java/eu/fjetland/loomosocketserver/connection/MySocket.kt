package eu.fjetland.loomosocketserver.connection

import android.util.Log
import eu.fjetland.loomosocketserver.LOG_TAG
import eu.fjetland.loomosocketserver.SOCKET_PORT
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket

class MySocket : Runnable {
    private lateinit var _myThread:Thread
    private lateinit var serverSocket: ServerSocket
    private  lateinit var input: BufferedReader
    private  lateinit var output: BufferedWriter

    override fun run() {
        Log.i(LOG_TAG, "Starting: MySocket Run")
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT)
        _myThread = Thread.currentThread()
        serverSocket = ServerSocket(SOCKET_PORT)

        while (!_myThread.isInterrupted) {
            communicate(awaitClient() as Socket)
            Log.i(LOG_TAG, "Trying to re-connect to Socket")
        }


    }

    fun awaitClient():Socket? {
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

    fun communicate(socket: Socket) : Boolean {
        input = BufferedReader(InputStreamReader(socket.getInputStream()))
        output = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

        while (!_myThread.isInterrupted) {
            val read = input.readLine()
            if (read == null) break

            Log.i(LOG_TAG,"Recived: $read")
            deCodejsone(read)
            //Echo
            //output.write(read)
            //output.flush()
        }
        Log.e(LOG_TAG, "Lost Connection to: $socket")
        return false
    }

    fun deCodejsone(data: String) {
        val obj = JSONObject(data)
        val valNames = obj.names()!!.toString()
        Log.i(LOG_TAG,"Variable names: $valNames")
        Log.i(LOG_TAG,"Re-encoded: $obj")

        for (key in obj.keys()) {
            Log.i(LOG_TAG,"$key has value: ${obj.get(key)}")
        }

    }

}