package com.raywenderlich.placebook.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.raywenderlich.placebook.model.Bookmark

@Database(entities = [Bookmark::class], version = 3)

 abstract class PlaceBookDatabase: RoomDatabase() {

     abstract fun BookmarkDao(): BookmarkDao

     companion object{

         private var instance: PlaceBookDatabase? = null

         fun getInstance(context: Context): PlaceBookDatabase{
             if(instance == null){
                 instance = Room.databaseBuilder(context.applicationContext,
                     PlaceBookDatabase::class.java,
                 "Bookmark").fallbackToDestructiveMigration()
                     .build()

             }
             return instance as PlaceBookDatabase
         }
     }
}