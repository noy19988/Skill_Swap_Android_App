package com.example.skill_swap_app.adapter

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.Course

class UdemyCoursesAdapter(private val courses: List<Course>) :
    RecyclerView.Adapter<UdemyCoursesAdapter.CourseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.bind(course)
    }

    override fun getItemCount(): Int = courses.size

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.courseTitle)
        private val priceTextView: TextView = itemView.findViewById(R.id.coursePrice)
        private val imageView: ImageView = itemView.findViewById(R.id.courseImage)

        fun bind(course: Course) {
            titleTextView.text = course.title
            priceTextView.text = "Price: ${course.price}"

            Glide.with(itemView.context)
                .load(course.image_480x270)
                .placeholder(R.drawable.placeholder_image)
                .into(imageView)

            itemView.setOnClickListener {
                var courseUrl = course.url

                if (!courseUrl.startsWith("https://")) {
                    courseUrl = "https://www.udemy.com$courseUrl"
                }

                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(courseUrl))
                    itemView.context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(itemView.context, "Error opening course link", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}

