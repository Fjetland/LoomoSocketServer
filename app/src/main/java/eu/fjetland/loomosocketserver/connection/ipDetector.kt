package eu.fjetland.loomosocketserver.connection

import android.content.Context
import android.net.wifi.WifiManager
import eu.fjetland.loomosocketserver.R
import eu.fjetland.loomosocketserver.SOCKET_PORT

class IpHelper {
    companion object {
        private var isWifiOn = false

        fun getDeviceIp(context: Context): String {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) {
                isWifiOn = false
                return R.string.no_wifi.toString()
            }
            isWifiOn = true
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            return  "/"+
                    (ipAddress and 0xFF).toString() + "." +
                    (ipAddress shr 8 and 0xFF) + "." +
                    (ipAddress shr 16 and 0xFF) + "." +
                    (ipAddress shr 24 and 0xFF) + ":" + SOCKET_PORT.toString()
        }
    }
}