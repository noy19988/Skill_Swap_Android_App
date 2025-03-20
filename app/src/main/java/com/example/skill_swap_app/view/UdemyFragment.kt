package com.example.skill_swap_app.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skill_swap_app.R
import com.example.skill_swap_app.adapter.UdemyCoursesAdapter
import com.example.skill_swap_app.model.Course
import com.example.skill_swap_app.repository.UdemyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UdemyFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var udemyRepository: UdemyRepository
    private lateinit var coursesAdapter: UdemyCoursesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_udemy, container, false)

        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)
        recyclerView = view.findViewById(R.id.recyclerViewCourses)
        progressBar = view.findViewById(R.id.progressBar)

        udemyRepository = UdemyRepository(requireContext())

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        coursesAdapter = UdemyCoursesAdapter(emptyList()) // אתחל רשימה ריקה בהתחלה
        recyclerView.adapter = coursesAdapter

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchCourses(query)
            }
        }

        return view
    }

    private fun searchCourses(query: String) {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            val response = udemyRepository.fetchCourses(query)

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                if (response != null && response.results.isNotEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    coursesAdapter = UdemyCoursesAdapter(response.results)
                    recyclerView.adapter = coursesAdapter
                }
            }
        }
    }
}
