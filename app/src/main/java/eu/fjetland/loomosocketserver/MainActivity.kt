package eu.fjetland.loomosocketserver

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log

import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import eu.fjetland.loomosocketserver.connection.MySocket
import kotlinx.android.synthetic.main.activity_main.*


var updateConversationHandler = Handler()
lateinit var viewModel : DebugViewModel

class MainActivity : AppCompatActivity() {

    lateinit var socketThread: Thread
    var isWifiOn = true

    private val txtIpDisplay by lazy {
        findViewById<TextView>(R.id.txtIpDisplay)
    }
    private val txtClientIpDisplay by lazy {
        findViewById<TextView>(R.id.txtClientIpDisplay)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()

        lifecycle.addObserver(MyLifecycleObserver())

        viewModel = ViewModelProviders.of(this)
            .get(DebugViewModel::class.java)
        viewModel.myIp.observe(this, Observer {
            txtIpDisplay.text = it
        })
        viewModel.clientIp.observe(this, Observer {
            txtClientIpDisplay.text = it
        })
        viewModel.readLog.observe(this, Observer {
            txtSocketLogg.text = it
        })

        socketThread = Thread(MySocket(this))
        socketThread.start()
        Context.AUDIO_SERVICE

    }

    override fun onResume() {
        Log.i(LOG_TAG, "onResume")
        viewModel.updateMyIp()
        super.onResume()
    }

    override fun onDestroy() {
        Log.i(LOG_TAG, "onDestroy")
        socketThread.interrupt()
        super.onDestroy()
    }

    override fun onStop() {
        socketThread.interrupt()
        super.onStop()
    }



}
