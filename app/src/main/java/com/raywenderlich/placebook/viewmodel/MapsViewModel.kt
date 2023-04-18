package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils

class MapsViewModel(application: Application):AndroidViewModel( application) {

    private  var bookmars: LiveData<List<BookmarkMarkerView>>? = null
    private val TAG = "MapsViewModel"

    private val bookmarkRepo:BookmarkRepo = BookmarkRepo(getApplication())

    private fun getPlaceCategory( places: Place): String{

        var category = "Other"
        val types  = places.types

        types?.let {
            if (types.size > 0){
              category =  bookmarkRepo.placeTypeToCategory(types[0])

            }
        }
        return category
    }

    fun addBookmarkFromPlace(place: Place, image: Bitmap?) {
        val bookmark = bookmarkRepo.createBookmark()

        bookmark.placeId = place.id
        bookmark.address = place.address!!.toString()
        bookmark.name = place.name!!.toString()
        bookmark.latitude = place.latLng?.latitude ?: 0.0
        bookmark.longitude = place.latLng?.longitude ?: 0.0
        bookmark.phone = place.phoneNumber?.toString() ?: ""
        bookmark.category = getPlaceCategory(place)

        val newId = bookmarkRepo.addBookmark(bookmark)
        image?.let{
            bookmark.setImage(image,getApplication())
        }
        Log.i(TAG,"new bookmark $newId")
    }

    data class BookmarkMarkerView(
        var id:Long? = null,
        var location: LatLng = LatLng(0.0,0.0),
        var name:String = "",
        var phone:String = "",
        var categoryResourceId: Int? = null
    ){
        fun getImage(context: Context): Bitmap?  {
            return id?.let {
                ImageUtils.loadBitmapFromFile(context, Bookmark.generateImageFileName(it))
            }
        }
    }

    private fun BookMarkToMarkerView(bookmark: Bookmark)
    = BookmarkMarkerView(bookmark.id,
        LatLng(bookmark.latitude,bookmark.longitude),
        bookmark.name,
        bookmark.phone,
        bookmarkRepo.getCategoryResourceId(bookmark.category)
    )

    fun getBookmarksMarkerViews(): LiveData<List<BookmarkMarkerView>>?{
        if(bookmars == null){
            mapBookmarksToMarkerView()
        }
        return  bookmars


    }

    private fun mapBookmarksToMarkerView(){

        bookmars = bookmarkRepo.allBookmarks.map { repoBookmarks ->
            Log.d(TAG,"mapBookmarksTomarkerView")
            repoBookmarks.map { bookmark ->
                BookMarkToMarkerView(bookmark)
            }
        }

        }

    }




