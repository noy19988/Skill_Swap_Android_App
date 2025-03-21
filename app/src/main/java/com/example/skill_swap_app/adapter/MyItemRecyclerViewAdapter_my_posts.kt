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
    private val values: List<Post>
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter_my_posts.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutInflater.from(parent.context).inflate(R.layout.fragment_my_posts, parent, false)
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


        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("postId", item.id)
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
