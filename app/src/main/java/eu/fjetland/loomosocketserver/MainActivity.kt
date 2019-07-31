package eu.fjetland.loomosocketserver

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log

import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import eu.fjetland.loomosocketserver.connection.Communicator
import eu.fjetland.loomosocketserver.connection.MySocket
import eu.fjetland.loomosocketserver.loomo.LoomoAudio
import eu.fjetland.loomosocketserver.loomo.LoomoBase
import eu.fjetland.loomosocketserver.loomo.LoomoHead
import eu.fjetland.loomosocketserver.loomo.LoomoRealSense
import kotlinx.android.synthetic.main.activity_main.*


var updateConversationHandler = Handler()
lateinit var viewModel : MainViewModel

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"


    var loomoAudio = LoomoAudio(this)
    lateinit var loomoHead : LoomoHead
    lateinit var loomoBase: LoomoBase
    lateinit var loomoRealSense: LoomoRealSense

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

        loomoAudio.onCreate() // Setup Audio
        loomoHead = LoomoHead(this)
        loomoBase = LoomoBase(this)


        lifecycle.addObserver(MyLifecycleObserver())

        viewModel = ViewModelProviders.of(this)
            .get(MainViewModel::class.java)

        loomoRealSense = LoomoRealSense(this, viewModel)

        /**
         * Display actions
         */
        viewModel.myIp.observe(this, Observer {
            txtIpDisplay.text = it
        })
        viewModel.clientIp.observe(this, Observer {
            txtClientIpDisplay.text = it
        })
        viewModel.readLog.observe(this, Observer {
            txtSocketLogg.text = it
        })

        /**
         * Loomo Actions
         */

        viewModel.volume.observe(this, Observer {
            if (it != null) {
                Log.i(TAG, "Action: $it")
                loomoAudio.setVolume(it)
            }
        })

        viewModel.speak.observe(this, Observer {
            if (it != null){
                Log.i(TAG, "Action: $it")
                loomoAudio.speak(it)
            }
        })

        viewModel.endableDrive.observe(this, Observer {
            if (it != null) {
                Log.i(TAG, "Action: $it")
                loomoBase.setEnableDrive(it)
            }
        })

        viewModel.velocity.observe(this, Observer {
            if (it != null){
                Log.i(TAG, "Action: $it")
                loomoBase.setVelocity(it)
            }
        })

        viewModel.position.observe(this, Observer {
            if (it != null){
                Log.i(TAG, "Action: $it")
                loomoBase.setPosition(it)
            }
        })

        viewModel.head.observe(this, Observer {
            if (it != null){
                Log.i(TAG, "Action: $it")
                loomoHead.setHead(it)
            }
        })


        /**
         * Start Communication thread
         */


        socketThread = Thread(Communicator(this))
        //socketThread = Thread(MySocket(this))

        socketThread.start()

    }

    override fun onResume() {
        Log.i(LOG_TAG, "onResume")
        viewModel.updateMyIp()
        super.onResume()

        loomoRealSense.startCamera()
    }

    override fun onDestroy() {
        socketThread.interrupt()
        Log.i(LOG_TAG, "onDestroy")
        loomoAudio.onDelete()
        super.onDestroy()
    }

    override fun onStop() {

        //socketThread.interrupt()
        Log.i(LOG_TAG,"Interupt Socket")
        super.onStop()
    }



}
