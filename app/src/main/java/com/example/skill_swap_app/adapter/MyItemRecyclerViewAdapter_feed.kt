package com.example.skill_swap_app.adapter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skill_swap_app.R
import com.example.skill_swap_app.databinding.ItemPostBinding
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDao
import com.example.skill_swap_app.utils.CloudinaryManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyItemRecyclerViewAdapter_feed(
    private val values: MutableList<Post>,
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

        holder.mainCheckBox.setOnCheckedChangeListener(null)

        holder.mainCheckBox.isChecked = item.isFavorite && item.favoritedByUserId == currentUserId

        holder.mainCheckBox.setOnCheckedChangeListener { _, isChecked ->
            item.isFavorite = isChecked
            item.favoritedByUserId = if (isChecked) currentUserId else null

            Log.d("FeedAdapter", "Updating post ${item.id} - isFavorite: $isChecked, favoritedByUserId: ${item.favoritedByUserId}")

            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("posts").document(item.firestoreId ?: "default_id")
                .update("isFavorite", item.isFavorite, "favoritedByUserId", item.favoritedByUserId)
                .addOnSuccessListener {
                    Log.d("FeedAdapter", "Post updated in Firestore: ${item.firestoreId}")
                }
                .addOnFailureListener { exception ->
                    Log.e("FeedAdapter", "Error updating post in Firestore", exception)
                }

        }
        loadProfileImage(item.userId, holder.profileImageView, holder.itemView.context)

        // הוספת תפריט אפשרויות
        if (item.userId == currentUserId) {
            holder.optionsButton.visibility = android.view.View.VISIBLE
            holder.optionsButton.setOnClickListener {
                val popup = PopupMenu(holder.itemView.context, holder.optionsButton)
                popup.menuInflater.inflate(R.menu.post_options_menu, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_delete -> {
                            val context = holder.itemView.context

                            // יצירת דיאלוג אישור מחיקה
                            android.app.AlertDialog.Builder(context)
                                .setTitle("Confirm Deletion")
                                .setMessage("Are you sure you want to delete this post?")
                                .setPositiveButton("Yes") { _, _ ->
                                    Log.d("FeedAdapter", "Deleting post: id=${item.id}, firestoreId=${item.firestoreId}")

                                    if (!item.firestoreId.isNullOrEmpty()) {
                                        deletePostFromFirestore(item.firestoreId)
                                        /*
                                                                                deletePostFromRoom(item.id)
                                        */
                                        if (item.imageUrl.startsWith("gs://") || item.imageUrl.startsWith("https://firebasestorage.googleapis.com/")) {
                                            deletePostFromStorage(item.imageUrl)
                                        } else {
                                            deleteImageFromCloudinary(item.imageUrl, context)
                                        }

                                        // מחיקה מהרשימה ועדכון ה-RecyclerView
                                        values.removeAt(position)
                                        notifyItemRemoved(position)

                                        // הצגת הודעה למשתמש שהמחיקה הצליחה
                                        Toast.makeText(context, "Post deleted successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Log.e("FeedAdapter", "firestoreId is null or empty for post ${item.id}")
                                        Toast.makeText(context, "Failed to delete post. firestoreId is missing.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .setNegativeButton("Cancel", null) // אם המשתמש לוחץ על ביטול - לא עושה כלום
                                .show()

                            true
                        }
                        R.id.action_edit -> {
                            val bundle = Bundle()
                            bundle.putInt("postId", item.id)

                            val navController = androidx.navigation.Navigation.findNavController(holder.itemView)
                            navController.navigate(R.id.action_feedFragment_to_editPostFragment, bundle)

                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        } else {
            holder.optionsButton.visibility = android.view.View.GONE
        }
    }


    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        val descriptionTextView: TextView = binding.descriptionTextView
        val skillLevelTextView: TextView = binding.skillLevelTextView
        val phoneNumberTextView: TextView = binding.phoneNumberTextView
        val imageView: ImageView = binding.imageView
        val mainCheckBox: CheckBox = binding.root.findViewById(R.id.mainCheckBox)
        val profileImageView: ImageView = binding.profileImageView
        val optionsButton: android.widget.ImageButton = binding.optionsButton
    }

    private fun getCurrentUserId(context: Context): Int? {
        val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1).takeIf { it != -1 }
    }

    private fun loadProfileImage(userId: Int, imageView: ImageView, context: Context) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").whereEqualTo("id", userId).get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    val userDocument = documents.documents[0]
                    val profileImageUrl = userDocument.getString("profileImageUrl")
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Log.d("FeedAdapter", "Loading profile image from: $profileImageUrl")
                        Glide.with(context)
                            .load(profileImageUrl)
                            .into(imageView)
                            .onLoadFailed(ContextCompat.getDrawable(context, R.drawable.default_profile_picture))
                    } else {
                        Log.w("FeedAdapter", "Profile image URL is empty for user: $userId")
                        imageView.setImageResource(R.drawable.default_profile_picture)
                    }
                } else {
                    Log.w("FeedAdapter", "User document not found for user: $userId")
                    imageView.setImageResource(R.drawable.default_profile_picture)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FeedAdapter", "Error loading profile image", exception)
                imageView.setImageResource(R.drawable.default_profile_picture)
            }
    }

    /*private fun deletePostFromRoom(postId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            postDao.deletePost(postId)
        }
    }*/


    private fun deletePostFromStorage(imageUrl: String) {
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        storageReference.delete()
            .addOnSuccessListener {
                Log.d("FeedAdapter", "Post image deleted from Storage: $imageUrl")
            }
            .addOnFailureListener { exception ->
                Log.e("FeedAdapter", "Error deleting post image from Storage", exception)
            }
    }

    private fun deleteImageFromCloudinary(imageUrl: String, context: Context) {
        val publicId = extractPublicIdFromUrl(imageUrl)
        if (publicId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    CloudinaryManager(context).deleteImage(publicId)
                    Log.d("FeedAdapter", "Image deleted from Cloudinary: $publicId")
                } catch (e: Exception) {
                    Log.e("FeedAdapter", "Error deleting image from Cloudinary", e)
                }
            }
        }
    }

    private fun extractPublicIdFromUrl(imageUrl: String): String? {
        val regex = Regex("/([^/]+)\\.[^.]+$")
        val matchResult = regex.find(imageUrl)
        return matchResult?.groupValues?.get(1)
    }

    private fun deletePostFromFirestore(postId: String) {
        FirebaseFirestore.getInstance().collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                Log.d("FeedAdapter", "Post deleted from Firestore: $postId")
            }
            .addOnFailureListener { exception ->
                Log.e("FeedAdapter", "Error deleting post from Firestore", exception)
            }
    }
}