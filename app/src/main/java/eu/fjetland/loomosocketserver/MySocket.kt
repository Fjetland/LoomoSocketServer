package eu.fjetland.loomosocketserver

import android.util.Log
import java.net.ServerSocket
import java.net.Socket

class MySocket : Runnable {
    private lateinit var _myThread:Thread
    private lateinit var serverSocket: ServerSocket


    override fun run() {
        Log.i(LOG_TAG, "Starting: MySocket Run")
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT)
        _myThread = Thread.currentThread()

    awaitClient()

    }

    fun awaitClient():Socket? {
        val socket: Socket? = null
        serverSocket = ServerSocket(SOCKET_PORT)
        Log.i(LOG_TAG, "ServerSocket Created")

        //while (!_myThread.isInterrupted){
            //Log.i(LOG_TAG, "Awaitinhg Client")
        //}

        return socket
    }
}