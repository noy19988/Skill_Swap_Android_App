package com.example.skill_swap_app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skill_swap_app.R
import com.example.skill_swap_app.databinding.FragmentFeedBinding
import com.example.skill_swap_app.databinding.ItemPostBinding
import com.example.skill_swap_app.model.Post

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyItemRecyclerViewAdapter_feed(
    private val values: List<Post>  // רשימה של פוסטים
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter_feed.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate את ה-XML של פריט
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // קבלת המידע של הפוסט
        val item = values[position]
        holder.descriptionTextView.text = item.description
        holder.skillLevelTextView.text = item.skillLevel
        holder.phoneNumberTextView.text = item.phoneNumber

        // הצגת התמונה אם יש
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        val descriptionTextView: TextView = binding.descriptionTextView
        val skillLevelTextView: TextView = binding.skillLevelTextView
        val phoneNumberTextView: TextView = binding.phoneNumberTextView
        val imageView: ImageView = binding.imageView
    }
}

