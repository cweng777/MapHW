package com.weng.maphw.ui.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.weng.maphw.R

class MyInfoWindowAdapter(
    context: Context
): GoogleMap.InfoWindowAdapter {

    //指定自定義資訊視窗，顯示佈局的樣式
    @SuppressLint("InflateParams")
    var mWindow: View = (context as Activity).layoutInflater.inflate(R.layout.layout_custom_info_window, null)

    private fun render(marker: Marker, view: View) {
        val title = view.findViewById<TextView>(R.id.title_textView)
        val contentA = view.findViewById<TextView>(R.id.content_a_value)
        val contentB = view.findViewById<TextView>(R.id.content_b_value)

        title.text = marker.title
        //data: 過 marker.snippet 傳遞口罩數量，將資料拆解後，指定到對應的 UI 欄位上顯示
        val data = marker.snippet?.toString()?.split(",")
        data?.apply {
            contentA.text = this[0]
            contentB.text = this[1]
        }

    }


    override fun getInfoWindow(marker: Marker): View? {
        return null //todo: study
    }

    override fun getInfoContents(marker: Marker): View {
        render(marker, mWindow)
        return mWindow
    }
}