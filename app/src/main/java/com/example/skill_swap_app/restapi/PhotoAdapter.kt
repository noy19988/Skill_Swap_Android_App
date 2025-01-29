package com.example.skill_swap_app.restapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skill_swap_app.R

// PhotoAdapter
class PhotoAdapter(private var photos: List<Photo>, val onItemSelected: (Photo) -> Unit) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    // פונקציה שמעדכנת את התמונות
    fun updatePhotos(newPhotos: List<Photo>) {
        // אם התמונות החדשות שונות מהקיימות, עדכן
        if (photos != newPhotos) {
            photos = newPhotos
            notifyDataSetChanged() // עדכון ה-RecyclerView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        Glide.with(holder.itemView.context)
            .load(photo.urls.regular)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            onItemSelected(photo) // שליחת התמונה שנבחרה
        }
    }

    override fun getItemCount(): Int = photos.size

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.photo_image)
    }
}
