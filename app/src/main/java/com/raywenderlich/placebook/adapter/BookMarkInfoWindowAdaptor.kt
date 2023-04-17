package com.raywenderlich.placebook.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.raywenderlich.placebook.databinding.ContentBookmarkInfoBinding
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel

class BookMarkInfoWindowAdaptor( val context: Activity):
GoogleMap.InfoWindowAdapter{

    private val binding = ContentBookmarkInfoBinding.inflate(context.layoutInflater)

    override fun getInfoContents(p0: Marker): View? {

        return null
    }

    override fun getInfoWindow(marker: Marker): View {
        binding.title.text = marker.title ?: ""
        binding.phone.text = marker.snippet ?: ""

        Log.d("yarrrrak","Laatllj${marker.tag}")
        Log.d("yarrrrak","bilalin siki: ${marker}")

        val imageView = binding.photo

        when(marker.tag){
            is MapsActivity.PlaceInfo -> {
                imageView.setImageBitmap((marker.tag as MapsActivity.PlaceInfo).image)
            }

            is MapsViewModel.BookmarkMarkerView -> {
                val bookmarkMarkerView = (marker.tag as MapsViewModel.BookmarkMarkerView)
                imageView.setImageBitmap(bookmarkMarkerView.getImage(context))
            }
        }

        return binding.root
    }

}