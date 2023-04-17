package com.raywenderlich.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.raywenderlich.placebook.db.PlaceBookDatabase
import com.raywenderlich.placebook.model.Bookmark

class BookmarkRepo(context: Context) {

    val db:PlaceBookDatabase = PlaceBookDatabase.getInstance(context)
    val bookmarkDao = db.BookmarkDao()

    fun addBookmark(bookMark:Bookmark): Long?{
      val newId =   bookmarkDao.insertBookmark(bookMark)
        bookMark.id = newId
        return newId
    }

   fun createBookmark(): Bookmark{
       return Bookmark()
   }

    fun getLiveBookmark(bookmarkId: Long):LiveData<Bookmark>{
        return bookmarkDao.loadLiveBookmark(bookmarkId)
    }

    val allBookmarks:LiveData<List<Bookmark>>
    get() = bookmarkDao.loadAll()

    fun updateBookmark(bookmark: Bookmark){
        bookmarkDao.updateBookmark(bookmark)
    }

    fun getBookmark(bookmarkId: Long):Bookmark {
        return bookmarkDao.loadBookmark(bookmarkId)
    }
}