package com.weng.maphw.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object ImageUtil {
    /**
     * 轉drawable to bitmap
     */
    fun getBitmapDescriptor(  //todo: study and change to my version 版權
        context: Context,
        drawableId: Int,
        width: Int = 0,
        height: Int = 0
    ): BitmapDescriptor? {
        val vectorDrawable: Drawable? = ContextCompat.getDrawable(context, drawableId)
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                context.getDrawable(id)
//            } else {
//                ContextCompat.getDrawable(context, id)
//            }
        return if (vectorDrawable != null) {
            if (width == 0) vectorDrawable.intrinsicWidth
            if (height == 0) vectorDrawable.intrinsicHeight
            vectorDrawable.setBounds(0, 0, width, height)
            val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            val canvas = Canvas(bm);
            vectorDrawable.draw(canvas);
            BitmapDescriptorFactory.fromBitmap(bm);
        } else {
            null
        }
    }

    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()

    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
}