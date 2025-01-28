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
    private lateinit var searchView: SearchView  // הגדרת המשתנה SearchView

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

        // הוספת MenuFragment
        val menuFragment = MenuFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.menu_fragment_container, menuFragment)
            .commit()

        // קישור ל-Spinner
        spinner = view.findViewById(R.id.spinner)
        val skillLevels = arrayOf("All", "Beginner", "Intermediate", "Expert")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, skillLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // קישור ל-SearchView
        searchView = view.findViewById(R.id.searchView)  // קישור ל-SearchView מתוך ה-XML

        // חיבור RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }

        // מביא את הפוסטים עם רמת המיומנות שנבחרה
        loadPosts(spinner.selectedItem.toString()) { posts ->
            if (posts.isEmpty()) {
                recyclerView.visibility = View.GONE
            } else {
                recyclerView.visibility = View.VISIBLE
                recyclerView.adapter = MyItemRecyclerViewAdapter_feed(posts, PostDatabase.getDatabase(requireContext()).postDao())
            }
        }

        // כפתור להוספת פוסט
        val addPostButton: ImageButton = view.findViewById(R.id.add_post_button)
        addPostButton.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_addPostFragment)
        }

        // מאזין לשינוי בסינון רמת המיומנות
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                loadPosts(spinner.selectedItem.toString()) { posts ->
                    recyclerView.adapter = MyItemRecyclerViewAdapter_feed(posts, PostDatabase.getDatabase(requireContext()).postDao())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // טיפול במצב שבו לא נבחר פריט
            }
        })

        // מאזין לשדה החיפוש
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // לא נדרש לבצע פעולה כאשר לוחצים על Enter
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // סינון הפוסטים לפי הטקסט שהוקלד
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
                    db.postDao().getPostsBySkillLevel(skillLevel)  // סינון לפי רמת מיומנות
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
                // סינון הפוסטים לפי טקסט החיפוש
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
