package com.example.skill_swap_app.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skill_swap_app.R
import com.example.skill_swap_app.adapter.MyItemRecyclerViewAdapter_my_posts
import com.example.skill_swap_app.model.AppDatabase
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyPostsFragment : Fragment() {

    private lateinit var db1: PostDatabase
    private lateinit var db2: AppDatabase
    private var userEmail: String? = null
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db1 = PostDatabase.getDatabase(requireContext())
        db2 = AppDatabase.getDatabase(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_posts_list, container, false)
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        userEmail = sharedPreferences.getString("user_email", null)

        Log.d("MyPostsFragment", "User Email: $userEmail")
        loadPostsFromFirestore(view)

        return view
    }

    private fun loadPostsFromFirestore(view: View) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val user = db2.userDao().getUserByEmail(userEmail.orEmpty())
                if (user == null) {
                    showToast("User not found locally")
                    return@launch
                }

                firestore.collection("posts")
                    .whereEqualTo("userId", user.id)
                    .get()
                    .addOnSuccessListener { result ->
                        val posts = result.documents.mapNotNull { doc ->
                            try {
                                Post(
                                    id = 0, // Firestore לא מחזיר ID של Room
                                    description = doc.getString("description") ?: "",
                                    skillLevel = doc.getString("skillLevel") ?: "",
                                    phoneNumber = doc.getString("phoneNumber") ?: "",
                                    imageUrl = doc.getString("imageUrl") ?: "",
                                    userId = doc.getLong("userId")?.toInt() ?: 0,
                                    firestoreId = doc.id
                                )
                            } catch (e: Exception) {
                                Log.e("MyPostsFragment", "Error parsing Firestore post", e)
                                null
                            }
                        }

                        updateRecyclerView(view, posts)
                    }
                    .addOnFailureListener { error ->
                        Log.e("MyPostsFragment", "Firestore failed: ${error.message}")
                        loadPostsFromRoom(view, user.id) // fallback
                    }

            } catch (e: Exception) {
                Log.e("MyPostsFragment", "Error loading posts", e)
                showToast("Error loading posts")
            }
        }
    }

    private fun loadPostsFromRoom(view: View, userId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val posts = db1.postDao().getPostsByUserId(userId)
                withContext(Dispatchers.Main) {
                    updateRecyclerView(view, posts)
                }
            } catch (e: Exception) {
                Log.e("MyPostsFragment", "Room fallback failed", e)
                showToast("Failed to load posts")
            }
        }
    }

    private fun updateRecyclerView(view: View, posts: List<Post>) {
        val recyclerView: RecyclerView = view.findViewById(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = MyItemRecyclerViewAdapter_my_posts(posts)
    }

    private fun showToast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            MyPostsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
