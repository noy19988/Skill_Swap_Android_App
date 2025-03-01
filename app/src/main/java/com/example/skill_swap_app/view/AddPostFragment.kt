package com.example.skill_swap_app.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDatabase
import com.example.skill_swap_app.model.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddPostFragment : Fragment() {

    private lateinit var descriptionEditText: EditText
    private lateinit var skillLevelSpinner: Spinner
    private lateinit var phoneNumberEditText: EditText
    private lateinit var postButton: Button
    private lateinit var uploadImageButton: Button
    private lateinit var uploadImageFromUnsplashButton: Button
    private lateinit var selectedImageView: ImageView
    private var selectedImageUri: Uri? = null
    private var selectedImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)

        val actionBar: ActionBar? = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        descriptionEditText = view.findViewById(R.id.description_edittext)
        skillLevelSpinner = view.findViewById(R.id.skill_level_spinner)
        phoneNumberEditText = view.findViewById(R.id.phone_number_edittext)
        postButton = view.findViewById(R.id.post_button)
        uploadImageButton = view.findViewById(R.id.upload_image_button)
        uploadImageFromUnsplashButton = view.findViewById(R.id.upload_image_from_unsplash_button)
        selectedImageView = view.findViewById(R.id.selected_image_view)

        arguments?.getString("selectedImageUrl")?.let {
            selectedImageUrl = it
            Glide.with(this).load(it).into(selectedImageView)
        }

        uploadImageButton.setOnClickListener {
            openImagePicker()
        }

        uploadImageFromUnsplashButton.setOnClickListener {
            findNavController().navigate(R.id.action_addPostFragment_to_photoListFragment)
        }

        postButton.setOnClickListener {
            val description = descriptionEditText.text.toString()
            val skillLevel = skillLevelSpinner.selectedItem.toString()
            val phoneNumber = phoneNumberEditText.text.toString()

            if (description.isNotEmpty() && phoneNumber.isNotEmpty()) {
                val imageUrl = selectedImageUrl ?: selectedImageUri?.toString() ?: "image_url"

                val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                val userEmail = sharedPreferences.getString("user_email", null)

                if (!userEmail.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        val db = AppDatabase.getDatabase(requireContext())
                        val user = db.userDao().getUserByEmail(userEmail)
                        var userId = 0
                        user?.let {
                            userId = it.id
                            Log.d("AddPostFragment", "User ID: $userId")

                            val post = Post(
                                description = description,
                                skillLevel = skillLevel,
                                phoneNumber = phoneNumber,
                                imageUrl = imageUrl,
                                userId = userId
                            )

                            insertPost(post)

                            Toast.makeText(requireContext(), "Post created successfully", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_addPostFragment_to_feedFragment)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigate(R.id.action_addPostFragment_to_feedFragment)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICKER_REQUEST_CODE) {
            selectedImageUri = data?.data
            selectedImageView.setImageURI(selectedImageUri)
            selectedImageUrl = null
            Toast.makeText(requireContext(), "Image selected successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertPost(post: Post) {
        GlobalScope.launch {
            val db = PostDatabase.getDatabase(requireContext())
            db.postDao().insertPost(post)
        }
    }

    companion object {
        private const val IMAGE_PICKER_REQUEST_CODE = 1001
    }
}