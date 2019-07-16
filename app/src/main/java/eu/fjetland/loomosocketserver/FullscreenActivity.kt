package eu.fjetland.loomosocketserver

import android.content.Context
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_fullscreen.*


import java.net.ServerSocket
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

var isWifiOn = false

val SERVERPORT = 1337
var serverSocket = ServerSocket()
var updateConversationHandler = Handler()
var serverThread = Thread()
var txtSocketLog:TextView? = null
var txtClientIp:TextView? = null

class FullscreenActivity : AppCompatActivity() {

    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()

        txtSocketLog = findViewById<TextView>(R.id.txtSocketLogg)
        txtSocketLog!!.movementMethod = ScrollingMovementMethod()

        txtClientIp = findViewById<TextView>(R.id.txtCliIPPrint)

        serverThread = Thread(ServerThread())
        serverThread.start()

    }

    override fun onResume() {
        super.onResume()
        // Search for WiFi address until detected
            setIPtext()
    }

    ///////////////////////
    //////// IP  //////////
    ///////////////////////

    private fun setIPtext(){
        val ipStr = getDeviceIp()
        this.txtIpPrint.setText(ipStr)
    }

    private fun getDeviceIp(): String {
        val wifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            isWifiOn = false
            return getString(R.string.no_wifi)
            //wifiManager.isWifiEnabled = true
        }
        isWifiOn = true
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress
        return (ipAddress and 0xFF).toString() + "." +
                (ipAddress shr 8 and 0xFF) + "." +
                (ipAddress shr 16 and 0xFF) + "." +
                (ipAddress shr 24 and 0xFF) + ":" + SERVERPORT.toString()
    }

    ///////////////////////
    /////// TCP ///////////
    ///////////////////////

    internal inner class ServerThread : Runnable {
        override fun run() {
            var socket: Socket? = null
            try {
                serverSocket = ServerSocket(SERVERPORT)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            while (!Thread.currentThread().isInterrupted) {
                try {
                    socket = serverSocket.accept()
                    val commThread= CommunicationThread(socket as Socket)
                    Thread(commThread).start()

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    internal inner class CommunicationThread(private val clientSocket: Socket) : Runnable {
        private var input: BufferedReader? = null
        init {
            try {
                this.input = BufferedReader(InputStreamReader(this.clientSocket.getInputStream()))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun run() {
            val adrStr = clientSocket.remoteSocketAddress.toString()
            updateConversationHandler.post(updateStuff(adrStr))
            updateConversationHandler.post(updateUIThread("Connected to: " + adrStr ))
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val read = input!!.readLine()
                    updateConversationHandler.post(updateUIThread(read))
                } catch (e: IOException) {
                    //e.printStackTrace()
                }
            }
        }

        internal inner class updateUIThread(private val msg: String) : Runnable {

            override fun run() {
                txtSocketLog!!.append("\n" + msg)
            }
        }

    }

    internal inner class updateStuff(private val msg: String) : Runnable {

        override fun run() {
            txtClientIp!!.text = msg
        }
    }

}
