package com.example.skill_swap_app.adapter

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyItemRecyclerViewAdapter_feed(
    private val values: List<Post>,
    private val postDao: PostDao
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter_feed.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        holder.descriptionTextView.text = item.description
        holder.skillLevelTextView.text = item.skillLevel
        holder.phoneNumberTextView.text = item.phoneNumber

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.imageView)

        val currentUserId = getCurrentUserId(holder.itemView.context) ?: 0

        holder.mainCheckBox.setOnCheckedChangeListener(null) // מניעת קריאה חוזרת

        // ✅ הצ'קבוקס מסומן רק אם המשתמש הנוכחי סימן אותו כמועדף
        holder.mainCheckBox.isChecked = item.isFavorite && item.favoritedByUserId == currentUserId

        holder.mainCheckBox.setOnCheckedChangeListener { _, isChecked ->
            item.isFavorite = isChecked
            item.favoritedByUserId = if (isChecked) currentUserId else null  // ✅ שמירת המשתמש שסימן

            Log.d("FeedAdapter", "Updating post ${item.id} - isFavorite: $isChecked, favoritedByUserId: ${item.favoritedByUserId}")

            CoroutineScope(Dispatchers.IO).launch {
                postDao.updatePost(item)
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

    // ❗ פונקציה שמביאה את ה-ID של המשתמש המחובר מה- SharedPreferences
    private fun getCurrentUserId(context: Context): Int? {
        val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1).takeIf { it != -1 }
    }
}
