package com.example.skill_swap_app.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.databinding.FragmentFavoritesBinding

class MyItemRecyclerViewAdapter_favorites(
    private val values: List<Post>  // רשימה של פוסטים אהובים
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter_favorites.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentFavoritesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.descriptionTextView.text = item.description
        holder.skillLevelTextView.text = item.skillLevel
        holder.phoneNumberTextView.text = item.phoneNumber
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentFavoritesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val descriptionTextView: TextView = binding.descriptionTextView
        val skillLevelTextView: TextView = binding.skillLevelTextView
        val phoneNumberTextView: TextView = binding.phoneNumberTextView
    }
}
