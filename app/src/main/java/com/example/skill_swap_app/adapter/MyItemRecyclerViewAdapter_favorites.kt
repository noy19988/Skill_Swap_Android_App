package com.example.skill_swap_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.Post
import com.google.firebase.firestore.FirebaseFirestore

class MyItemRecyclerViewAdapter_favorites(
    private val values: List<Post>,
    private val context: Context
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter_favorites.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutInflater.from(parent.context).inflate(R.layout.fragment_favorites, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        holder.descriptionTextView.text = item.description
        holder.skillLevelTextView.text = item.skillLevel
        holder.phoneNumberTextView.text = item.phoneNumber

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(holder.imageView)

        loadProfileImage(item.userId, holder.profileImageView)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: View) : RecyclerView.ViewHolder(binding) {
        val descriptionTextView: TextView = binding.findViewById(R.id.descriptionTextView)
        val skillLevelTextView: TextView = binding.findViewById(R.id.skillLevelTextView)
        val phoneNumberTextView: TextView = binding.findViewById(R.id.phoneNumberTextView)
        val imageView: ImageView = binding.findViewById(R.id.postImageView)
        val profileImageView: ImageView = binding.findViewById(R.id.profileImageView) // ✅ הוספת תמונת פרופיל
    }

    private fun loadProfileImage(userId: Int, imageView: ImageView) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").whereEqualTo("id", userId).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDocument = documents.documents[0]
                    val profileImageUrl = userDocument.getString("profileImageUrl")
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.default_profile_picture)
                            .error(R.drawable.default_profile_picture)
                            .circleCrop()
                            .into(imageView)
                    } else {
                        imageView.setImageResource(R.drawable.default_profile_picture)
                    }
                } else {
                    imageView.setImageResource(R.drawable.default_profile_picture)
                }
            }
            .addOnFailureListener {
                imageView.setImageResource(R.drawable.default_profile_picture)
            }
    }
}