package com.example.skill_swap_app.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDatabase
import com.example.skill_swap_app.utils.CloudinaryManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditPostFragment : Fragment() {

    private lateinit var descriptionEditText: EditText
    private lateinit var skillLevelSpinner: Spinner
    private lateinit var phoneNumberEditText: EditText
    private lateinit var updatePostButton: Button
    private lateinit var uploadImageButton: Button
    private lateinit var uploadImageFromUnsplashButton: Button
    private lateinit var selectedImageView: ImageView
    private lateinit var cloudinaryManager: CloudinaryManager
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null
    private var selectedImageUrl: String? = null
    private var postId: Int = 0
    private var firestoreId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            postId = it.getInt("postId")
        }
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
        updatePostButton = view.findViewById(R.id.post_button)
        uploadImageButton = view.findViewById(R.id.upload_image_button)
        uploadImageFromUnsplashButton = view.findViewById(R.id.upload_image_from_unsplash_button)
        selectedImageView = view.findViewById(R.id.selected_image_view)
        progressBar = view.findViewById(R.id.progressBar)

        cloudinaryManager = CloudinaryManager(requireContext())

        updatePostButton.text = "Update Post"

        loadPostDetails()

        uploadImageButton.setOnClickListener {
            openImagePicker()
        }

        uploadImageFromUnsplashButton.setOnClickListener {
            findNavController().navigate(R.id.action_photoListFragment_to_addPostFragment)
        }

        updatePostButton.setOnClickListener {
            updatePost()
        }

        return view
    }

    private fun loadPostDetails() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = PostDatabase.getDatabase(requireContext())
            val post = db.postDao().getPostById(postId)

            withContext(Dispatchers.Main) {
                post?.let {
                    descriptionEditText.setText(it.description)
                    phoneNumberEditText.setText(it.phoneNumber)
                    firestoreId = it.firestoreId
                    selectedImageUrl = it.imageUrl

                    Glide.with(requireContext()).load(it.imageUrl).into(selectedImageView)

                    val skillLevels = resources.getStringArray(R.array.skill_levels)
                    skillLevelSpinner.setSelection(skillLevels.indexOf(it.skillLevel))
                }
            }
        }
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

            Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show()
            uploadImageToCloudinary(selectedImageUri!!)
        }
    }

    private fun uploadImageToCloudinary(uri: Uri) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val uploadedUrl = cloudinaryManager.uploadImage(uri)
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                if (!uploadedUrl.isNullOrEmpty()) {
                    selectedImageUrl = uploadedUrl
                    Glide.with(requireContext()).load(uploadedUrl).into(selectedImageView)
                    Toast.makeText(requireContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updatePost() {
        val description = descriptionEditText.text.toString()
        val skillLevel = skillLevelSpinner.selectedItem.toString()
        val phoneNumber = phoneNumberEditText.text.toString()

        if (description.isEmpty() || phoneNumber.isEmpty() || selectedImageUrl.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields and upload an image", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = PostDatabase.getDatabase(requireContext())
            val post = Post(
                id = postId,
                description = description,
                skillLevel = skillLevel,
                phoneNumber = phoneNumber,
                imageUrl = selectedImageUrl!!,
                userId = db.postDao().getPostById(postId)?.userId ?: 0,
                firestoreId = firestoreId
            )

            db.postDao().updatePost(post)

            firestoreId?.let {
                FirebaseFirestore.getInstance().collection("posts").document(it)
                    .update(
                        "description", description,
                        "skillLevel", skillLevel,
                        "phoneNumber", phoneNumber,
                        "imageUrl", selectedImageUrl!!
                    )
                    .addOnSuccessListener {
                        Log.d("EditPostFragment", "Post updated in Firestore")
                    }
                    .addOnFailureListener {
                        Log.e("EditPostFragment", "Failed to update Firestore: ${it.message}")
                    }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Post updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_editPostFragment_to_feedFragment)
            }
        }
    }

    companion object {
        private const val IMAGE_PICKER_REQUEST_CODE = 1001
    }
}
