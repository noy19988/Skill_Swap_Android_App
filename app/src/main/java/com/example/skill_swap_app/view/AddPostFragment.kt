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
import com.example.skill_swap_app.model.AppDatabase
import com.example.skill_swap_app.utils.CloudinaryManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AddPostFragment : Fragment() {

    private lateinit var descriptionEditText: EditText
    private lateinit var skillLevelSpinner: Spinner
    private lateinit var phoneNumberEditText: EditText
    private lateinit var postButton: Button
    private lateinit var uploadImageButton: Button
    private lateinit var uploadImageFromUnsplashButton: Button
    private lateinit var selectedImageView: ImageView
    private lateinit var cloudinaryManager: CloudinaryManager
    private lateinit var progressBar: ProgressBar

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
        progressBar = view.findViewById(R.id.progressBar)

        cloudinaryManager = CloudinaryManager(requireContext())

        arguments?.getString("selectedImageUrl")?.let {
            selectedImageUrl = it
            Glide.with(this).load(it).into(selectedImageView)

            lifecycleScope.launch {
                downloadAndUploadImage(it)
            }
        }



        uploadImageButton.setOnClickListener {
            openImagePicker()
        }

        uploadImageFromUnsplashButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("previousFragment", "AddPostFragment")
            }
            findNavController().navigate(R.id.action_addPostFragment_to_photoListFragment, bundle)
        }

        postButton.setOnClickListener {
            createPost()
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
            Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show()

            uploadImageToCloudinary(selectedImageUri!!)
        }
    }


    private suspend fun downloadAndUploadImage(imageUrl: String) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = java.net.URL(imageUrl).openStream()
                val file = File(requireContext().cacheDir, "temp_image.jpg")
                file.outputStream().use { output -> inputStream.copyTo(output) }

                val imageUri = Uri.fromFile(file)

                withContext(Dispatchers.Main) {
                    uploadImageToCloudinary(imageUri)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to download image", Toast.LENGTH_SHORT).show()
                }
            }
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



    private fun savePostToFirestore(postId: Int) { // שינוי: קבלת postId
        val firestore = FirebaseFirestore.getInstance()
        lifecycleScope.launch(Dispatchers.IO){
            val db = PostDatabase.getDatabase(requireContext())
            val post = db.postDao().getPostById(postId)
            withContext(Dispatchers.Main){
                if(post != null){
                    val postMap = hashMapOf(
                        "description" to post.description,
                        "skillLevel" to post.skillLevel,
                        "phoneNumber" to post.phoneNumber,
                        "imageUrl" to post.imageUrl,
                        "userId" to post.userId
                    )

                    firestore.collection("posts")
                        .add(postMap)
                        .addOnSuccessListener { documentReference ->
                            val firestoreId = documentReference.id
                            Log.d("AddPostFragment", "Firestore post created with id: $firestoreId")
                            Toast.makeText(context, "Post uploaded to Firestore!", Toast.LENGTH_SHORT).show()

                            updatePostInRoomWithFirestoreId(postId, firestoreId)
                            Log.d("AddPostFragment", "Updated Room post with firestoreId: $firestoreId")
                            findNavController().navigate(R.id.action_addPostFragment_to_feedFragment)

                        }
                        .addOnFailureListener {
                            Log.e("AddPostFragment", "Failed to upload post to Firestore: ${it.message}")
                            Toast.makeText(context, "Failed to upload post to Firestore", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }


    private fun updatePostInRoomWithFirestoreId(postId: Int, firestoreId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = PostDatabase.getDatabase(requireContext())
                val post = db.postDao().getPostById(postId)

                if (post != null) {
                    val updatedPost = post.copy(firestoreId = firestoreId)
                    db.postDao().updatePost(updatedPost)
                    Log.d("AddPostFragment", "Updated Room post with firestoreId: $firestoreId")
                } else {
                    Log.e("AddPostFragment", "Post with id $postId not found in Room")
                }
            } catch (e: Exception) {
                Log.e("AddPostFragment", "Error updating Room post: ${e.message}")
            }
        }
    }




    private fun createPost() {
        val description = descriptionEditText.text.toString()
        val skillLevel = skillLevelSpinner.selectedItem.toString()
        val phoneNumber = phoneNumberEditText.text.toString()

        if (description.isEmpty() || phoneNumber.isEmpty() || selectedImageUrl.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields and upload an image", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("user_email", null)

        if (!userEmail.isNullOrEmpty()) {
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(requireContext())
                val user = db.userDao().getUserByEmail(userEmail)
                user?.let {
                    val post = Post(
                        description = description,
                        skillLevel = skillLevel,
                        phoneNumber = phoneNumber,
                        imageUrl = selectedImageUrl!!,
                        userId = it.id
                    )
                    val postId = withContext(Dispatchers.IO) {
                        PostDatabase.getDatabase(requireContext()).postDao().insertPost(post)
                    }
                    Log.d("AddPostFragment", "Post created in Room with id: $postId")
                    savePostToFirestore(postId.toInt())
                }
            }
        } else {
            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
        }
    }


    private fun insertPost(post: Post) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = PostDatabase.getDatabase(requireContext())
            db.postDao().insertPost(post)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Post created successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_addPostFragment_to_feedFragment)
            }
        }
    }

    companion object {
        private const val IMAGE_PICKER_REQUEST_CODE = 1001
    }
}