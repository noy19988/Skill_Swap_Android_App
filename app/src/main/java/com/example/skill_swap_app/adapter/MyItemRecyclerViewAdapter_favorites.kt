package com.example.skill_swap_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.Post

class MyItemRecyclerViewAdapter_favorites(
    private val values: List<Post>  // רשימה של פוסטים אהובים
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter_favorites.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutInflater.from(parent.context).inflate(R.layout.fragment_favorites, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        // ✅ הצגת פרטי הפוסט
        holder.descriptionTextView.text = item.description
        holder.skillLevelTextView.text = item.skillLevel
        holder.phoneNumberTextView.text = item.phoneNumber

        // ✅ טעינת תמונה עם Glide
        Glide.with(holder.itemView.context)
            .load(item.imageUrl) // ה-URL של התמונה
            .placeholder(R.drawable.placeholder_image) // תמונה זמנית בזמן טעינה
            .error(R.drawable.placeholder_image) // תמונה במקרה של שגיאה
            .into(holder.imageView)
    }



    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: View) : RecyclerView.ViewHolder(binding) {
        val descriptionTextView: TextView = binding.findViewById(R.id.descriptionTextView)
        val skillLevelTextView: TextView = binding.findViewById(R.id.skillLevelTextView)
        val phoneNumberTextView: TextView = binding.findViewById(R.id.phoneNumberTextView)
        val imageView: ImageView = binding.findViewById(R.id.postImageView) // ✅ וידוא שהתמונה מוצגת
    }
}
