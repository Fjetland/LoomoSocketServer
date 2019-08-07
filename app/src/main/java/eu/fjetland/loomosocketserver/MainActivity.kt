package eu.fjetland.loomosocketserver

import android.hardware.camera2.CameraDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log
import android.widget.ImageView

import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import eu.fjetland.loomosocketserver.connection.Communicator
import eu.fjetland.loomosocketserver.data.EnableDrive
import eu.fjetland.loomosocketserver.loomo.LoomoAudio
import eu.fjetland.loomosocketserver.loomo.LoomoBase
import eu.fjetland.loomosocketserver.loomo.LoomoHead
import eu.fjetland.loomosocketserver.loomo.LoomoRealSense
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


var updateConversationHandler = Handler()
lateinit var viewModel : MainViewModel

class MainActivity : AppCompatActivity(){
    private val TAG = "MainActivity"

    private var loomoAudio = LoomoAudio(this)
    private lateinit var loomoHead : LoomoHead
    private lateinit var loomoBase : LoomoBase
    private lateinit var loomoRealSense : LoomoRealSense

    private lateinit var socketThread: Thread

    private val txtIpDisplay by lazy {
        findViewById<TextView>(R.id.txtIpDisplay)
    }
    private val txtClientIpDisplay by lazy {
        findViewById<TextView>(R.id.txtClientIpDisplay)
    }

    private val driveImageView by lazy {
        findViewById<ImageView>(R.id.driveImageView)
    }

    private val visionImageView by lazy {
        findViewById<ImageView>(R.id.visionImageView)
    }

    private val connectionImageView by lazy {
        findViewById<ImageView>(R.id.connectionImageView)
    }

    private val logoImageView by lazy {
        findViewById<ImageView>(R.id.logoImageView)
    }

    private val viewFinder by lazy {
        findViewById<ImageView>(R.id.viewFinder)
    }

    private val viewFinderDepth by lazy {
        findViewById<ImageView>(R.id.viewFinderDepth)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()

        loomoAudio.onCreate() // Setup Audio
        loomoHead = LoomoHead(this)
        loomoBase = LoomoBase(this)


        //lifecycle.addObserver(MyLifecycleObserver())

        viewModel = ViewModelProviders.of(this)
            .get(MainViewModel::class.java)

        loomoRealSense = LoomoRealSense(this)

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

        viewModel.isConnected.observe(this, Observer {
            loomoHead.setConnectedLight(it)
            updateConnectionIcon(it)
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
                updateDriveIcon(it.drive)

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

        viewModel.realSenseColorImage.observe(this, Observer {
            viewFinder.setImageBitmap(it)
        })

        viewModel.realSenseDepthImage.observe(this, Observer {
            viewFinderDepth.setImageBitmap(it)
        })

        viewModel.visionIsActive.observe(this, Observer {
            updateVisionIcon(it)
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
        loomoHead.setConnectedLight(false)
        socketThread.interrupt()
        Log.i(LOG_TAG, "onDestroy")
        loomoAudio.onDelete()
        super.onDestroy()
    }

    override fun onStop() {
        viewModel.endableDrive.value = EnableDrive(false)
        loomoRealSense.stopCamera()

        //socketThread.interrupt()
        Log.i(LOG_TAG,"Interupt Socket")
        super.onStop()
    }

    fun updateConnectionIcon(boolean: Boolean){
        if (boolean) {
            connectionImageView.setImageResource(R.drawable.ic_connection_on)
        } else {
            connectionImageView.setImageResource(R.drawable.ic_connection_off)
        }
    }

    fun updateDriveIcon(boolean: Boolean) {
        if (boolean){
            driveImageView.setImageResource(R.drawable.ic_drive_on)
        } else {
            driveImageView.setImageResource(R.drawable.ic_drive_off)
        }
    }

    fun updateVisionIcon(boolean: Boolean) {
        if (boolean){
            visionImageView.setImageResource(R.drawable.ic_camera_on)
        } else {
            visionImageView.setImageResource(R.drawable.ic_camera_off)
        }
    }



}
