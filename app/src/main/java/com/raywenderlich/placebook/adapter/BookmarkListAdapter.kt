package com.raywenderlich.placebook.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.databinding.BookmarkItemBinding
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel

 class BookmarkListAdapter(
    private var bookmarkData: List<MapsViewModel.BookmarkMarkerView>?,
    private val mapsActivity: MapsActivity
):RecyclerView.Adapter<BookmarkListAdapter.BookmarkViewHolder>() {

   class BookmarkViewHolder(
        val binding: BookmarkItemBinding,
        private val mapsActivity: MapsActivity
    ):RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                val bookmarkView = binding.root.tag as? MapsViewModel.BookmarkMarkerView
                Log.d("do not give up", "${itemView.tag}")
                mapsActivity.moveToBookmark(bookmarkView?:MapsViewModel.BookmarkMarkerView(null))
            }
        }
    }

     fun setBookmarkData(bookmarks:List<MapsViewModel.BookmarkMarkerView>){
         Log.d("do not give up", "i am at setBookmarkData")
        bookmarkData = bookmarks
         notifyDataSetChanged()
     }

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
         Log.d("do not give up", "i am at onCreateViewHolder")
         val layoutInflater = LayoutInflater.from(parent.context)
         val binding = BookmarkItemBinding.inflate(layoutInflater,parent,false)
         return BookmarkViewHolder(binding,mapsActivity)
     }

     override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
            bookmarkData?.let { list ->
                val bookmarkViewData = list[position]
                Log.d("do not give up", "i am at onBindViewHolder $position")
                holder.binding.root.tag = bookmarkViewData
                holder.binding.bookmarkData = bookmarkViewData
                holder.binding.bookmarkIcon.setImageResource(R.drawable.girl)


            }
     }

     override fun getItemCount(): Int {
         Log.d("do not give up", "i am at getItemCount ${bookmarkData?.size}.")
         return bookmarkData?.size ?: 0
     }


 }