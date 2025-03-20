package com.example.skill_swap_app.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skill_swap_app.R
import com.example.skill_swap_app.adapter.MyItemRecyclerViewAdapter_feed
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedFragment : Fragment() {

    private var columnCount = 1
    private lateinit var spinner: Spinner
    private lateinit var searchView: SearchView
    private lateinit var profileImageViewHeader: ImageView
    private lateinit var recyclerView: RecyclerView
    private var firestoreListener: ListenerRegistration? = null

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
        val view = inflater.inflate(R.layout.fragment_feed_list, container, false)
        profileImageViewHeader = view.findViewById(R.id.profile_image_view_header)
        loadCurrentUserProfileImage()

        profileImageViewHeader.setOnClickListener {
        }

        val menuFragment = MenuFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.menu_fragment_container, menuFragment)
            .commit()

        spinner = view.findViewById(R.id.spinner)
        val skillLevels = arrayOf("All", "Beginner", "Intermediate", "Expert")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, skillLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        searchView = view.findViewById(R.id.searchView)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }

        setupFirestoreListener(spinner.selectedItem.toString())

        val addPostButton: ImageButton = view.findViewById(R.id.add_post_button)
        addPostButton.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_addPostFragment)
        }

        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                setupFirestoreListener(spinner.selectedItem.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPosts(newText, recyclerView)
                return true
            }
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        firestoreListener?.remove()
    }

    private fun setupFirestoreListener(skillLevel: String) {
        firestoreListener?.remove()
        val firestore = FirebaseFirestore.getInstance()
        val query = if (skillLevel == "All") {
            firestore.collection("posts")
        } else {
            firestore.collection("posts").whereEqualTo("skillLevel", skillLevel)
        }

        firestoreListener = query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e("FeedFragment", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val posts = snapshots.documents.map { document ->
                    val userIdValue = document.get("userId")
                    val userId = when (userIdValue) {
                        is Number -> userIdValue.toInt()
                        is String -> userIdValue.toIntOrNull() ?: 0
                        else -> 0
                    }

                    val favoritedByUsers = (document.get("favoritedByUsers") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()

                    Post(
                        id = 0,
                        description = document.getString("description") ?: "",
                        skillLevel = document.getString("skillLevel") ?: "",
                        phoneNumber = document.getString("phoneNumber") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        userId = userId,
                        favoritedByUsers = favoritedByUsers,
                        firestoreId = document.id
                    )
                }

                Log.d("FeedFragment", "Loaded ${posts.size} posts from Firestore")
                updateRecyclerView(posts)
            }
        }
    }


    private fun updateRecyclerView(posts: List<Post>) {
        if (posts.isEmpty()) {
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = MyItemRecyclerViewAdapter_feed(posts.toMutableList())
        }
    }

    private fun loadCurrentUserProfileImage() {
        val sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("user_email", null)

        if (userEmail != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users").document(userEmail).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val profileImageUrl = document.getString("profileImageUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(profileImageUrl)
                                .into(profileImageViewHeader)
                        } else {
                            profileImageViewHeader.setImageResource(R.drawable.default_profile_picture)
                        }
                    } else {
                        profileImageViewHeader.setImageResource(R.drawable.default_profile_picture)
                    }
                }
                .addOnFailureListener {
                    profileImageViewHeader.setImageResource(R.drawable.default_profile_picture)
                }
        } else {
            profileImageViewHeader.setImageResource(R.drawable.default_profile_picture)
        }
    }

    private fun filterPosts(query: String?, recyclerView: RecyclerView) {
        lifecycleScope.launch {
            val posts = withContext(Dispatchers.IO) {
                val db = PostDatabase.getDatabase(requireContext())
                val allPosts = db.postDao().getAllPosts()
                val filteredPosts = allPosts.filter { it.description.contains(query ?: "", ignoreCase = true) }
                filteredPosts
            }
            recyclerView.adapter = MyItemRecyclerViewAdapter_feed(posts.toMutableList())
        }
    }

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            FeedFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}