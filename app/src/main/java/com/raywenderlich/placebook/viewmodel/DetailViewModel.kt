package com.raywenderlich.placebook.viewmodel


import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class DetailViewModel(application: Application):AndroidViewModel(application) {
    val bookmarkRepo = BookmarkRepo(getApplication())
    var detailView: LiveData<DetailView>? = null

    data class DetailView(
        var id:Long? = null,
        var name:String ="",
        var phone:String ="",
        var address: String="",
        var notes:String="",
        var category: String = ""
    ){
        fun getImage(context: Context) = id?.let {
            ImageUtils.loadBitmapFromFile(context,Bookmark.generateImageFileName(it))
        }
        fun setImage(context: Context, image: Bitmap) {
            id?.let {
                Log.d("porn", "8")
                ImageUtils.saveBitmapToFile(context, image,
                    Bookmark.generateImageFileName(it))
            }
        }

    }

    fun bookmarkToDetailView(bookmark: Bookmark) =
        DetailView(
            bookmark.id,
            bookmark.name,
            bookmark.phone,
            bookmark.address,
            bookmark.notes,
            bookmark.category
        )


    private fun mapBookmarkToDetailView(bookmarkId:Long){
        detailView = bookmarkRepo.getLiveBookmark(bookmarkId).map {
            bookmarkToDetailView(it)
        }

    }

    fun getDetailView(bookmarkId:Long): LiveData<DetailView>? {
        if (detailView == null) {
            mapBookmarkToDetailView(bookmarkId)
        }

            return detailView

    }

        fun bookmarkViewToBookmark(detailView: DetailView): Bookmark?{
            var bookmark = detailView.id?.let {
                bookmarkRepo.getBookmark(it)
            }

            if (bookmark != null) {
                bookmark.id = detailView.id ?: 0
                bookmark.name = detailView.name
                bookmark.phone = detailView.phone
                bookmark.address = detailView.address
                bookmark.notes = detailView.notes
                bookmark.category = detailView.category
            }
            return bookmark
        }

        fun updateBookmark(bookmarkView: DetailView) {

            GlobalScope.launch {

                val bookmark = bookmarkViewToBookmark(bookmarkView)

                bookmark?.let { bookmarkRepo.updateBookmark(it) }
            }
        }

    fun getCategoryResourceId(category: String): Int?{
        return bookmarkRepo.getCategoryResourceId(category)
    }

    fun getCategories():List<String>{
        return bookmarkRepo.categories
    }
    }

