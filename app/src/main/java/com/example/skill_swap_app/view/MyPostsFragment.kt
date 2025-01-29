package com.example.skill_swap_app.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skill_swap_app.R
import com.example.skill_swap_app.adapter.MyItemRecyclerViewAdapter_my_posts
import com.example.skill_swap_app.model.AppDatabase
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyPostsFragment : Fragment() {
    private var columnCount = 1
    private lateinit var db1: PostDatabase
    private lateinit var db2: AppDatabase
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
        db1 = PostDatabase.getDatabase(requireContext())
        db2 = AppDatabase.getDatabase(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_posts_list, container, false)

        // Retrieve user email from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        userEmail = sharedPreferences.getString("user_email", null)

        Log.d("MyPostsFragment", "User Email: $userEmail")  // לוג לצפייה ב-Email של המשתמש

        getUserPosts(userEmail)

        return view
    }

    private fun getUserPosts(userEmail: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val user = db2.userDao().getUserByEmail(userEmail.orEmpty())
                if (user != null) {
                    Log.d("MyPostsFragment", "User ID: ${user.id}")  // לוג לצפייה ב-userId

                    // Fetch the posts of this user from the database
                    val userPosts = db1.postDao().getPostsByUserId(user.id)
                    Log.d("MyPostsFragment", "Found ${userPosts.size} posts for this user")  // לוג לצפייה במספר הפוסטים

                    activity?.runOnUiThread {
                        val recyclerView: RecyclerView = requireView().findViewById(R.id.list)
                        recyclerView.layoutManager = LinearLayoutManager(requireContext())
                        recyclerView.adapter = MyItemRecyclerViewAdapter_my_posts(userPosts)
                    }
                } else {
                    Log.d("MyPostsFragment", "No user found with email: $userEmail")  // לוג אם לא נמצא משתמש
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error retrieving posts", Toast.LENGTH_SHORT).show()
                }
                Log.e("MyPostsFragment", "Error retrieving posts", e)  // לוג של טעות אם יש
            }
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
