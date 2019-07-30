package eu.fjetland.loomosocketserver

import android.graphics.Bitmap

/**
 *
 * @author jacob
 * @date 5/7/18
 */

interface IImageState {
    fun updateImage(Type: Int, bitmap: Bitmap)
}
