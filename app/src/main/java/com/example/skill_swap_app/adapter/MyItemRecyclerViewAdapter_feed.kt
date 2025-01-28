package com.example.skill_swap_app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skill_swap_app.R
import com.example.skill_swap_app.databinding.ItemPostBinding
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyItemRecyclerViewAdapter_feed(
    private val values: List<Post>,  // רשימה של פוסטים
    private val postDao: PostDao    // אובייקט של ה-DAO
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter_feed.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate את ה-XML של פריט
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.descriptionTextView.text = item.description
        holder.skillLevelTextView.text = item.skillLevel
        holder.phoneNumberTextView.text = item.phoneNumber

        // הצגת התמונה אם יש
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.imageView)

        // הצגת מצב הצ'קבוקס
        holder.mainCheckBox.isChecked = item.isFavorite

        // עדכון מצב הצ'קבוקס במודל וב-Room
        holder.mainCheckBox.setOnCheckedChangeListener { _, isChecked ->
            item.isFavorite = isChecked
            GlobalScope.launch {
                postDao.updatePost(item)  // עדכון ה-Room עם המידע החדש
            }
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        val descriptionTextView: TextView = binding.descriptionTextView
        val skillLevelTextView: TextView = binding.skillLevelTextView
        val phoneNumberTextView: TextView = binding.phoneNumberTextView
        val imageView: ImageView = binding.imageView
        val mainCheckBox: CheckBox = binding.root.findViewById(R.id.mainCheckBox)
    }
}
