package com.coheser.app.simpleclasses.mapclasses

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.coheser.app.Constants
import com.coheser.app.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.model.DirectionsResult

class MapWorker(var context: Context, var googleMap: GoogleMap) {
    var locationPin: Bitmap
    var currentPin: Bitmap

    init {
        locationPin = Bitmap.createScaledBitmap(getBitmapFromVectorDrawable(context,R.drawable.location_selected), 50, 50, false)
        currentPin = Bitmap.createScaledBitmap(
            (context.resources
                .getDrawable(R.drawable.ic_current_location_dot) as BitmapDrawable).bitmap, 50, 50, false
        )
    }

    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable: Drawable? = ContextCompat.getDrawable(context, drawableId)
        if (drawable == null) {
            Log.d(Constants.tag,"Drawable with ID $drawableId not found.")
        }

        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
    fun addMarker(postion: Int,title: String?, latLng: LatLng?, marker_image: Bitmap): Marker {
        val markerOptions = MarkerOptions()
            .position(latLng!!)
            .icon(BitmapDescriptorFactory.fromBitmap(marker_image))
            .title(title)
        val m = googleMap.addMarker(markerOptions)
        m?.tag = postion
        return m!!
    }


    fun addMarker(
        latLng: LatLng?,
        markerImage: Bitmap
    ): Marker {
        val markerOptions = MarkerOptions()
            .position(latLng!!)
            .icon(BitmapDescriptorFactory.fromBitmap(markerImage))
        return googleMap.addMarker(markerOptions)!!
    }

    fun getDistanceFromRoute(results: DirectionsResult): String {
        return results.routes[0].legs[0].distance.humanReadable
    }

    fun getDistanceTime(results: DirectionsResult): String {
        return results.routes[0].legs[0].duration.humanReadable
    }

    fun durationInTraffic(results: DirectionsResult): String {
        return results.routes[0].legs[0].durationInTraffic.humanReadable
    }

    fun arrivalTime(results: DirectionsResult): String {
        return results.routes[0].legs[0].arrivalTime.toString()
    }

}
