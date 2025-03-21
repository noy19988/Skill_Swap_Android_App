package com.example.skill_swap_app.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skill_swap_app.R
import com.example.skill_swap_app.adapter.MyItemRecyclerViewAdapter_favorites
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FavoritesFragment : Fragment() {

    private var columnCount = 1
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites_list, container, false)
        recyclerView = view.findViewById(R.id.list)

        recyclerView.layoutManager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }

        loadFavoritePosts()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadFavoritePosts()
    }

    private fun loadFavoritePosts() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        if (currentUserEmail.isNullOrEmpty()) {
            Log.e("FavoritesFragment", "User email is missing! Cannot load favorite posts.")
            return
        }

        Log.d("FavoritesFragment", "Loading favorite posts for user: $currentUserEmail")

        lifecycleScope.launch {
            val posts = withContext(Dispatchers.IO) {
                val db = PostDatabase.getDatabase(requireContext())
                val roomFavorites = db.postDao().getFavoritePosts(currentUserEmail)
                val firestoreFavorites = fetchFavoritePostsFromFirestore(currentUserEmail)

                Log.d("FavoritesFragment", "Fetched ${firestoreFavorites.size} posts from Firestore")

                (roomFavorites + firestoreFavorites).distinctBy { it.firestoreId }
            }

            Log.d("FavoritesFragment", "Total favorite posts loaded: ${posts.size}")

            if (posts.isEmpty()) {
                Log.w("FavoritesFragment", "No favorite posts found for user: $currentUserEmail")
            }

            recyclerView.adapter = MyItemRecyclerViewAdapter_favorites(posts, requireContext())
        }
    }



    private suspend fun fetchFavoritePostsFromFirestore(userEmail: String): List<Post> {
        val firestore = FirebaseFirestore.getInstance()
        val posts = mutableListOf<Post>()

        Log.d("FavoritesFragment", "Fetching favorite posts for user: $userEmail")

        return try {
            val querySnapshot = firestore.collection("posts")
                .whereArrayContains("favoritedByUsers", userEmail)
                .get().await()

            Log.d("FavoritesFragment", "Found ${querySnapshot.size()} favorite posts in Firestore")

            for (document in querySnapshot) {
                val post = document.toObject(Post::class.java).copy(firestoreId = document.id)
                Log.d("FavoritesFragment", "Post found: ${post.description}, favoritedByUsers: ${post.favoritedByUsers}")
                posts.add(post)
            }

            posts
        } catch (e: Exception) {
            Log.e("FavoritesFragment", "Error fetching favorite posts from Firestore", e)
            emptyList()
        }
    }


    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            FavoritesFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
