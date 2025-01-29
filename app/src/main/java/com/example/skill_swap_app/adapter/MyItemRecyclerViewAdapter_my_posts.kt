package com.example.skill_swap_app.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.Post
import com.bumptech.glide.Glide


class MyItemRecyclerViewAdapter_my_posts(
    private val values: List<Post> // פריטי הפוסט
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter_my_posts.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutInflater.from(parent.context).inflate(R.layout.fragment_my_posts, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        // הצגת הנתונים בעזרת ה-ViewHolder
        holder.descriptionTextView.text = item.description
        holder.skillLevelTextView.text = item.skillLevel
        holder.phoneNumberTextView.text = item.phoneNumber

        // הצגת התמונה בעזרת Glide (נדרש להוסיף dependency ל-Glide אם זה לא קיים)
        Glide.with(holder.itemView.context)
            .load(item.imageUrl) // נסה לשים את ה-URL של התמונה פה
            .into(holder.imageView)

        // הגדרת OnClickListener למעבר לעמוד EditDeletePostFragment

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("postId", item.id) // העברת ה-ID של הפוסט
            holder.itemView.findNavController().navigate(R.id.action_myPostsFragment_to_editDeletePostFragment, bundle)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: View) : RecyclerView.ViewHolder(binding) {
        val descriptionTextView: TextView = binding.findViewById(R.id.descriptionTextView)
        val skillLevelTextView: TextView = binding.findViewById(R.id.skillLevelTextView)
        val phoneNumberTextView: TextView = binding.findViewById(R.id.phoneNumberTextView)
        val imageView: ImageView = binding.findViewById(R.id.imageView)
    }
}
