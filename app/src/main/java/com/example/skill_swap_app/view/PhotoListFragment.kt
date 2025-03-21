package com.example.skill_swap_app.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skill_swap_app.R
import com.example.skill_swap_app.restapi.Photo
import com.example.skill_swap_app.restapi.PhotoAdapter
import com.example.skill_swap_app.restapi.RetrofitInstance
import com.example.skill_swap_app.restapi.UnsplashApiService
import com.example.skill_swap_app.restapi.UnsplashSearchResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PhotoListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhotoAdapter
    private var currentQuery = "jobs"
    private val apiService = RetrofitInstance.retrofitInstance.create(UnsplashApiService::class.java)
    private var currentPage = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_list, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PhotoAdapter(emptyList()) { selectedPhoto -> onPhotoSelected(selectedPhoto) }
        recyclerView.adapter = adapter

        val searchEditText: EditText = view.findViewById(R.id.search_edittext)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentQuery = s.toString()
                if (currentQuery.isNotEmpty()) {
                    loadPhotos(currentQuery)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        loadPhotos(currentQuery)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    loadMorePhotos()
                }
            }
        })

        return view
    }

    private fun loadPhotos(query: String) {
        currentPage = 1
        apiService.searchPhotos(query, currentPage, 30).enqueue(object : Callback<UnsplashSearchResponse> {
            override fun onResponse(call: Call<UnsplashSearchResponse>, response: Response<UnsplashSearchResponse>) {
                if (response.isSuccessful) {
                    val photoList = response.body()?.results
                    photoList?.let {
                        adapter.updatePhotos(it)
                        currentPage++
                    }
                }
            }

            override fun onFailure(call: Call<UnsplashSearchResponse>, t: Throwable) {}
        })
    }

    private fun loadMorePhotos() {
        apiService.searchPhotos(currentQuery, currentPage, 30).enqueue(object : Callback<UnsplashSearchResponse> {
            override fun onResponse(call: Call<UnsplashSearchResponse>, response: Response<UnsplashSearchResponse>) {
                if (response.isSuccessful) {
                    val photoList = response.body()?.results
                    photoList?.let {
                        adapter.updatePhotos(it)
                        currentPage++
                    }
                }
            }

            override fun onFailure(call: Call<UnsplashSearchResponse>, t: Throwable) {}
        })
    }

    private fun onPhotoSelected(photo: Photo) {
        val previousFragment = arguments?.getString("previousFragment", "AddPostFragment")
        val bundle = Bundle().apply {
            putString("selectedImageUrl", photo.urls.regular)
            putString("previousFragment", previousFragment)
            putString("firestoreId", arguments?.getString("firestoreId")) // ✅ שמור את ה-ID של הפוסט
        }

        Log.d("PhotoListFragment", "Navigating back to $previousFragment with image: ${photo.urls.regular}")

        if (previousFragment == "EditPostFragment") {
            findNavController().navigate(R.id.action_photoListFragment_to_editPostFragment, bundle)
        } else {
            findNavController().navigate(R.id.action_photoListFragment_to_addPostFragment, bundle)
        }
    }


}
