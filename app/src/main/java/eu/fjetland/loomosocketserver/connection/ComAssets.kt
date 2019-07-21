package eu.fjetland.loomosocketserver.connection

import android.util.Log
import eu.fjetland.loomosocketserver.LOG_TAG
import org.json.JSONObject

class ComAssets {
    companion object {
        fun readAndLogJsonString(string: String) {
            val obj = JSONObject(string)
            for (key in obj.keys()) {
                Log.i(LOG_TAG,"$key has value: ${obj.get(key)}")
            }
        }

    }
}