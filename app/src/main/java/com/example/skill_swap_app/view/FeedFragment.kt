package com.example.skill_swap_app.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skill_swap_app.R
import com.example.skill_swap_app.adapter.MyItemRecyclerViewAdapter_feed
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FeedFragment : Fragment() {

    private var columnCount = 1

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

        // חיבור RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }

        // מביא את הפוסטים אם קיימים
        loadPosts { posts ->
            if (posts.isEmpty()) {
                // אם אין פוסטים, להסתיר את ה-RecycleView או להציג הודעה
                recyclerView.visibility = View.GONE
                // הוספת הודעה שאין פוסטים, אם תרצה
            } else {
                // אם יש פוסטים, להציג אותם ב-RecycleView
                recyclerView.visibility = View.VISIBLE
                recyclerView.adapter = MyItemRecyclerViewAdapter_feed(posts)
            }
        }

        // כפתור להוספת פוסט
        val addPostButton: ImageButton = view.findViewById(R.id.add_post_button)
        addPostButton.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_addPostFragment)
        }

        return view
    }

    private fun loadPosts(callback: (List<Post>) -> Unit) {
        GlobalScope.launch {
            val db = PostDatabase.getDatabase(requireContext())
            val posts = db.postDao().getAllPosts()
            callback(posts)
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
