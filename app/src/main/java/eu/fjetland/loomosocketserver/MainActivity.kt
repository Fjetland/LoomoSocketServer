package eu.fjetland.loomosocketserver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log


class MainActivity : AppCompatActivity() {

    lateinit var socketThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()
        Log.i(LOG_TAG,"Running onCreate")

        socketThread = Thread(MySocket())
        socketThread.start()

    }


}
