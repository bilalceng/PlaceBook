package com.raywenderlich.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.db.PlaceBookDatabase
import com.raywenderlich.placebook.model.Bookmark



class BookmarkRepo( var context: Context) {

    val db: PlaceBookDatabase = PlaceBookDatabase.getInstance(context)
    val bookmarkDao = db.BookmarkDao()
    val categoryMap: HashMap<Place.Type, String> = buildCategoryMap()
    val categories: List<String>
        get() = ArrayList(allCategories.keys)
    private var allCategories: HashMap<String, Int> = buildCategories()

    private fun buildCategoryMap(): HashMap<Place.Type, String> {
        return hashMapOf(
            Place.Type.BAKERY to "Restaurant",
            Place.Type.BAR to "Restaurant",
            Place.Type.CAFE to "Restaurant",
            Place.Type.FOOD to "Restaurant",
            Place.Type.RESTAURANT to "Restaurant",
            Place.Type.MEAL_DELIVERY to "Restaurant",
            Place.Type.MEAL_TAKEAWAY to "Restaurant",
            Place.Type.GAS_STATION to "Gas",
            Place.Type.CLOTHING_STORE to "Shopping",
            Place.Type.DEPARTMENT_STORE to "Shopping",
            Place.Type.FURNITURE_STORE to "Shopping",
            Place.Type.GROCERY_OR_SUPERMARKET to "Shopping",
            Place.Type.HARDWARE_STORE to "Shopping",
            Place.Type.HOME_GOODS_STORE to "Shopping",
            Place.Type.JEWELRY_STORE to "Shopping",
            Place.Type.SHOE_STORE to "Shopping",
            Place.Type.SHOPPING_MALL to "Shopping",
            Place.Type.STORE to "Shopping",
            Place.Type.LODGING to "Lodging",
            Place.Type.ROOM to "Lodging"
        )
    }


    fun placeTypeToCategory(key: Place.Type): String {
        var category = "Other"

        if (categoryMap.containsKey(key)) {
            category = categoryMap.get(key).toString()
            return category
        }
        return category
    }

    private fun buildCategories(): HashMap<String, Int> {
        return hashMapOf(
            "Gas" to R.drawable.gas,
            "Shopping" to R.drawable.shopping,
            "Lodging" to R.drawable.hotel,
            "Restaurant" to R.drawable.restaurant,
            "Other" to R.drawable.other
        )

    }

    fun getCategoryResourceId(category: String) =
        if (allCategories[category] != null) allCategories[category] else null

    fun addBookmark(bookMark: Bookmark): Long? {
        val newId = bookmarkDao.insertBookmark(bookMark)
        bookMark.id = newId
        return newId
    }

    fun createBookmark(): Bookmark {
        return Bookmark()
    }

    fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark> {
        return bookmarkDao.loadLiveBookmark(bookmarkId)
    }

    val allBookmarks: LiveData<List<Bookmark>>
        get() = bookmarkDao.loadAll()

    fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }

    fun getBookmark(bookmarkId: Long): Bookmark {
        return bookmarkDao.loadBookmark(bookmarkId)
    }

    fun deleteBookmark(bookmark: Bookmark) {
        bookmark.deleteImage(context)
        bookmarkDao.deleteBookmark(bookmark)
    }
}