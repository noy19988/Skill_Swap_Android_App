package com.example.skill_swap_app.adapter

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skill_swap_app.R
import com.example.skill_swap_app.databinding.ItemPostBinding
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDao
import com.example.skill_swap_app.model.PostDatabase
import com.example.skill_swap_app.utils.CloudinaryManager
import com.google.firebase.auth.FirebaseAuth
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
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

        Log.d("FeedAdapter", "Binding post: ID=${item.id}, FirestoreID=${item.firestoreId}, Desc=${item.description}")

        holder.descriptionTextView.text = item.description
        holder.skillLevelTextView.text = item.skillLevel
        holder.phoneNumberTextView.text = item.phoneNumber

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.imageView)

        holder.mainCheckBox.setOnCheckedChangeListener(null)
        holder.mainCheckBox.isChecked = item.favoritedByUsers.contains(userEmail)

        holder.mainCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!item.favoritedByUsers.contains(userEmail)) {
                    item.favoritedByUsers = item.favoritedByUsers + userEmail
                }
            } else {
                item.favoritedByUsers = item.favoritedByUsers.filter { it != userEmail }
            }

            Log.d("FeedAdapter", "Updating post ${item.firestoreId} - favoritedByUsers: ${item.favoritedByUsers}")
            updateFavoriteStatusInFirestore(item)
        }
        Glide.with(holder.itemView.context).clear(holder.profileImageView)
        loadProfileImage(item.userId, holder.profileImageView, holder.itemView.context)

        if (item.userId == getCurrentUserId(holder.itemView.context)) {
            holder.optionsButton.visibility = View.VISIBLE
            holder.optionsButton.setOnClickListener {
                val popup = PopupMenu(holder.itemView.context, holder.optionsButton)
                popup.menuInflater.inflate(R.menu.post_options_menu, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_delete -> {
                            val context = holder.itemView.context

                            AlertDialog.Builder(context)
                                .setTitle("Confirm Deletion")
                                .setMessage("Are you sure you want to delete this post?")
                                .setPositiveButton("Yes") { _, _ ->
                                    Log.d("FeedAdapter", "Deleting post: id=${item.id}, firestoreId=${item.firestoreId}")

                                    if (!item.firestoreId.isNullOrEmpty()) {
                                        deletePostFromFirestore(item.firestoreId)
                                        deletePostFromRoom(item.firestoreId, holder.itemView.context)

                                        if (item.imageUrl.startsWith("gs://") || item.imageUrl.startsWith("https://firebasestorage.googleapis.com/")) {
                                            deletePostFromStorage(item.imageUrl)
                                        } else {
                                            deleteImageFromCloudinary(item.imageUrl, context)
                                        }

                                        values.removeAt(position)
                                        notifyItemRemoved(position)

                                        Toast.makeText(context, "Post deleted successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Log.e("FeedAdapter", "firestoreId is null or empty for post ${item.id}")
                                        Toast.makeText(context, "Failed to delete post. firestoreId is missing.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                            true
                        }
                        R.id.action_edit -> {
                            val bundle = Bundle()
                            bundle.putString("firestoreId", item.firestoreId)
                            Log.d("FeedAdapter", "Navigating to EditPostFragment with firestoreId=${item.firestoreId}")

                            val navController = Navigation.findNavController(holder.itemView)
                            navController.navigate(R.id.action_feedFragment_to_editPostFragment, bundle)

                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        } else {
            holder.optionsButton.visibility = View.GONE
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
        val optionsButton: ImageButton = binding.optionsButton
    }

    private fun getCurrentUserId(context: Context): Int? {
        val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1).takeIf { it != -1 }
    }

    private fun updateFavoriteStatusInFirestore(post: Post) {
        val firestore = FirebaseFirestore.getInstance()
        val postRef = firestore.collection("posts").document(post.firestoreId ?: return)

        Log.d("FeedAdapter", "Updating post in Firestore: ${post.firestoreId}, New favoritedByUsers: ${post.favoritedByUsers}")

        postRef.update("favoritedByUsers", post.favoritedByUsers)
            .addOnSuccessListener {
                Log.d("FeedAdapter", "Post updated successfully in Firestore: ${post.firestoreId}")
            }
            .addOnFailureListener { exception ->
                Log.e("FeedAdapter", "Error updating post in Firestore", exception)
            }
    }

    private fun loadProfileImage(userId: Int, imageView: ImageView, context: Context) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users")
            .whereEqualTo("id", userId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDocument = documents.documents[0]
                    val profileImageUrl = userDocument.getString("profileImageUrl")

                    if (!profileImageUrl.isNullOrEmpty()) {
                        // ✅ נטען את התמונה מה-URL של המשתמש
                        Glide.with(context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.default_profile_picture)
                            .error(R.drawable.default_profile_picture)
                            .into(imageView)
                    } else {
                        // 🟡 אם אין תמונה בפרופיל - נטען ברירת מחדל
                        imageView.setImageResource(R.drawable.default_profile_picture)
                    }
                } else {
                    // ❌ אם לא נמצא משתמש בכלל עם ה-ID הזה
                    imageView.setImageResource(R.drawable.default_profile_picture)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FeedAdapter", "Error loading profile image", exception)
                imageView.setImageResource(R.drawable.default_profile_picture)
            }
    }






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


    private fun deletePostFromRoom(firestoreId: String, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = PostDatabase.getDatabase(context)
                db.postDao().deletePostByFirestoreId(firestoreId)
                Log.d("FeedAdapter", "Post deleted from Room DB: $firestoreId")
            } catch (e: Exception) {
                Log.e("FeedAdapter", "Error deleting post from Room DB", e)
            }
        }
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


