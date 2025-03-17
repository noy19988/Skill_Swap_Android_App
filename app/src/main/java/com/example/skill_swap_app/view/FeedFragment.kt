package com.example.skill_swap_app.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skill_swap_app.R
import com.example.skill_swap_app.adapter.MyItemRecyclerViewAdapter_feed
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedFragment : Fragment() {

    private var columnCount = 1
    private lateinit var spinner: Spinner
    private lateinit var searchView: SearchView

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

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }

        loadPosts(spinner.selectedItem.toString()) { posts ->
            if (posts.isEmpty()) {
                recyclerView.visibility = View.GONE
            } else {
                recyclerView.visibility = View.VISIBLE
                recyclerView.adapter = MyItemRecyclerViewAdapter_feed(posts, PostDatabase.getDatabase(requireContext()).postDao())
            }
        }

        val addPostButton: ImageButton = view.findViewById(R.id.add_post_button)
        addPostButton.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_addPostFragment)
        }

        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                loadPosts(spinner.selectedItem.toString()) { posts ->
                    recyclerView.adapter = MyItemRecyclerViewAdapter_feed(posts, PostDatabase.getDatabase(requireContext()).postDao())
                }
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

    private fun loadPosts(skillLevel: String, callback: (List<Post>) -> Unit) {
        lifecycleScope.launch {
            val posts = withContext(Dispatchers.IO) {
                val db = PostDatabase.getDatabase(requireContext())
                if (skillLevel == "All") {
                    db.postDao().getAllPosts()
                } else {
                    db.postDao().getPostsBySkillLevel(skillLevel)
                }
            }
            callback(posts)
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
            recyclerView.adapter = MyItemRecyclerViewAdapter_feed(posts, PostDatabase.getDatabase(requireContext()).postDao())
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
