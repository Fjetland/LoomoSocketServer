package eu.fjetland.loomosocketserver.loomo

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.vision.Vision
import com.segway.robot.sdk.vision.frame.Frame
import com.segway.robot.sdk.vision.stream.StreamType
import eu.fjetland.loomosocketserver.updateConversationHandler
import eu.fjetland.loomosocketserver.viewModel
import kotlinx.coroutines.*


class LoomoRealSense(context: Context) {
    companion object {
        const val COLOR_WIDTH = 640
        const val COLOR_HEIGHT = 480

        const val SMALL_COLOR_WIDTH = 320
        const val SMALL_COLOR_HEIGHT = 240

        const val DEPTH_WIDTH = 320
        const val DEPTH_HEIGHT = 240
    }


    val TAG = "LoomoRealSense"

    var mVision: Vision = Vision.getInstance()
    var isActive = false

    var mImageColor: Bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)
    var mImageDepth: Bitmap = Bitmap.createBitmap(320, 240, Bitmap.Config.RGB_565)

    init {
        mVision.bindService(context.applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                Log.d(TAG, "Vision onBind")
            }

            override fun onUnbind(reason: String?) {
            }
        })

        //val infos : Array<out StreamInfo>? = mVision.activatedStreamInfo
        Log.i(TAG, "Vision isBound: ${mVision.isBind}")
    }

    fun startCamera() {

        GlobalScope.launch {
            // launch a new coroutine in background and continue
            while (!mVision.isBind) {
                delay(10L) // non-blocking delay for 1 second (default time unit is ms)
            }

            mVision.startListenFrame(StreamType.COLOR, object : Vision.FrameListener {
                override fun onNewFrame(streamType: Int, frame: Frame) {
                    mImageColor.copyPixelsFromBuffer(frame.byteBuffer)
                    val colorMapLarge = colorLarge2ByteArray(mImageColor)
                    val colorMapSmall = colorSmall2ByteArray(mImageColor)

                    updateConversationHandler.post {
                        viewModel.realSenseColorImage.value = mImageColor
                        viewModel.colorLargeBitArray.value = colorMapLarge
                        viewModel.colorSmallBitArray.value = colorMapSmall
                    }
                }
            })

            mVision.startListenFrame(StreamType.DEPTH, object : Vision.FrameListener {
                override fun onNewFrame(streamType: Int, frame: Frame) {
                    mImageDepth.copyPixelsFromBuffer(frame.byteBuffer)
                    val grayImage = depth2Grey2(mImageDepth)
                    val depthBytes = depth2bnyteArray(mImageDepth)

                    updateConversationHandler.post {
                        viewModel.realSenseDepthImage.value = grayImage
                        viewModel.colorDepthBitArray.value = depthBytes
                    }
                }
            })

            updateConversationHandler.post {
                viewModel.visionIsActive.value = true
            }

            this@LoomoRealSense.isActive = true
            Log.i(TAG, "Vision isBound: ${mVision.isBind}")
        }
    }

    fun stopCamera() {
        if (mVision.isBind) {
            mVision.stopListenFrame(StreamType.COLOR)
            mVision.stopListenFrame(StreamType.DEPTH)
            updateConversationHandler.post {
                viewModel.visionIsActive.value = false
            }
        }
    }

    private fun depth2Grey(img: Bitmap): Bitmap {
        val width = img.width
        val height = img.height
        val pixels = IntArray(width * height)

        img.getPixels(pixels, 0, width, 0, 0, width, height)
        val alpha = 0xFF shl 24
        for (i in 0 until height) {
            for (j in 0 until width) {
                var grey = pixels[width * i + j]

                val red = grey and 0x00FF0000 shr 16
                val green = grey and 0x0000FF00 shr 8
                val blue = grey and 0x000000FF

                //grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);
                grey = red * 38 + green * 75 + blue * 15 shr 7
                grey = alpha or (grey shl 16) or (grey shl 8) or grey
                pixels[width * i + j] = grey
            }
        }
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    private fun depth2Grey2(img: Bitmap): Bitmap {
        val width = img.width
        val height = img.height
        val pixels = IntArray(width * height)

        img.getPixels(pixels, 0, width, 0, 0, width, height)
        val alpha = 0xFF shl 24
        for (i in 0 until height) {
            for (j in 0 until width) {
                var grey = pixels[width * i + j]

                val distance = grey and 0x00FFFFFF shr 8
                grey = distance
                grey = alpha or (grey shl 16) or (grey shl 8) or grey
                pixels[width * i + j] = grey
            }
        }
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }


    private fun colorLarge2ByteArray(img: Bitmap): ByteArray {
        val width = img.width
        val height = img.height
        val pixels = IntArray(width * height)
        val bytes = ByteArray(width * height * 3)

        img.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in 0 until height) {
            for (j in 0 until width) {
                val grey = pixels[width * i + j]

                val red = grey and 0x00FF0000 shr 16
                val green = grey and 0x0000FF00 shr 8
                val blue = grey and 0x000000FF

                bytes[(width * i + j) * 3 + 0] = red.toByte()
                bytes[(width * i + j) * 3 + 1] = green.toByte()
                bytes[(width * i + j) * 3 + 2] = blue.toByte()
            }
        }

        return bytes
    }

    private fun colorSmall2ByteArray(img: Bitmap): ByteArray {
        val width = img.width
        val height = img.height
        val pixels = IntArray(width * height)
        val bytes = ByteArray(width/2 * height/2 * 3)

        img.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in 0 until height/2) {
            for (j in 0 until width/2) {
                val grey = pixels[width * i*2 + j*2]

                val red = grey and 0x00FF0000 shr 16
                val green = grey and 0x0000FF00 shr 8
                val blue = grey and 0x000000FF

                bytes[(width/2 * i + j) * 3 + 0] = red.toByte()
                bytes[(width/2 * i + j) * 3 + 1] = green.toByte()
                bytes[(width/2 * i + j) * 3 + 2] = blue.toByte()
            }
        }

        return bytes
    }

    private fun depth2bnyteArray(img: Bitmap): ByteArray {
        val width = img.width
        val height = img.height
        val pixels = IntArray(width * height)
        val bytes = ByteArray(width * height * 2)

        img.getPixels(pixels, 0, width, 0, 0, width, height)
        val alpha = 0xFF shl 24
        for (i in 0 until height) {
            for (j in 0 until width) {
                val grey = pixels[width * i + j]

                bytes[(width * i + j) * 2 + 0] = (grey and 0x00FF00 shr 8).toByte()
                bytes[(width * i + j) * 2 + 1] = (grey and 0x0000FF).toByte()
            }
        }
        return bytes
    }


}