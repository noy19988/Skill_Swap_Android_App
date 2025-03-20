package com.example.skill_swap_app.view

import android.app.Activity
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
    private var originalPost: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            firestoreId = it.getString("firestoreId")
            Log.d("EditPostFragment", "onCreate - Received firestoreId: $firestoreId")
        }

        Log.d("EditPostFragment", "onCreate - firestoreId: $firestoreId")
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

        // ✅ בדיקה אם יש תמונה חדשה מ-PhotoListFragment
        val newImageUrl = arguments?.getString("selectedImageUrl")
        if (!newImageUrl.isNullOrEmpty()) {
            selectedImageUrl = newImageUrl
            Glide.with(this).load(selectedImageUrl).into(selectedImageView)
            Log.d("EditPostFragment", "✅ Loaded new image from Unsplash: $selectedImageUrl")
        }
        Log.d("EditPostFragment", "onViewCreated - selectedImageUrl: $selectedImageUrl")


        // ✅ אם זו הפעם הראשונה שנטען הפוסט – טען נתונים מה-Firestore
        if (originalPost == null) {
            firestoreId = arguments?.getString("firestoreId") ?: firestoreId
            loadPostDetails()
        } else {
            restorePreviousData()
        }

        uploadImageButton.setOnClickListener {
            openImagePicker()
        }

        uploadImageFromUnsplashButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("previousFragment", "EditPostFragment")
                putString("firestoreId", firestoreId) // 🔥 כדי שנחזור עם אותו ID
            }
            findNavController().navigate(R.id.action_editPostFragment_to_photoListFragment, bundle)
        }

        updatePostButton.setOnClickListener {
            updatePost()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newImageUrl = arguments?.getString("selectedImageUrl")
        if (!newImageUrl.isNullOrEmpty()) {
            selectedImageUrl = newImageUrl
            Glide.with(this).load(selectedImageUrl).into(selectedImageView)
            Log.d("EditPostFragment", "Loaded new image from Unsplash: $selectedImageUrl")
            arguments?.remove("selectedImageUrl")
        }
    }




    private fun restorePreviousData() {
        descriptionEditText.setText(originalPost?.description)
        phoneNumberEditText.setText(originalPost?.phoneNumber)

        selectedImageUrl = selectedImageUrl ?: originalPost?.imageUrl
        Glide.with(requireContext()).load(selectedImageUrl).into(selectedImageView)

        val skillLevels = resources.getStringArray(R.array.skill_levels)
        skillLevelSpinner.setSelection(skillLevels.indexOf(originalPost?.skillLevel ?: ""))
    }



    private fun loadPostDetails() {
        if (firestoreId.isNullOrEmpty()) {
            Log.e("EditPostFragment", "No firestoreId received, cannot load post")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val postRef = firestore.collection("posts").document(firestoreId!!)

        postRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        originalPost = post
                        originalPost = post.copy(firestoreId = firestoreId) // ✅ מעדכן את האובייקט כולו

                        descriptionEditText.setText(post.description)
                        phoneNumberEditText.setText(post.phoneNumber)
                        selectedImageUrl = post.imageUrl

                        Glide.with(requireContext()).load(post.imageUrl).into(selectedImageView)

                        Log.d("EditPostFragment", "Loaded post from Firestore: Desc=${post.description}, FirestoreID=${firestoreId}") // הדפסת firestoreId

                        val skillLevels = resources.getStringArray(R.array.skill_levels)
                        skillLevelSpinner.setSelection(skillLevels.indexOf(post.skillLevel))
                    }
                } else {
                    Log.e("EditPostFragment", "Post not found in Firestore, ID: $firestoreId")
                }
            }
    }


    private fun openImagePicker() {
        Log.d("EditPostFragment", "Opening image picker...")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("EditPostFragment", "onActivityResult called - requestCode: $requestCode, resultCode: $resultCode")

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMAGE_PICKER_REQUEST_CODE -> {
                    val uri = data?.data
                    Log.d("EditPostFragment", "Image selected: URI=$uri")

                    if (uri != null) {
                        selectedImageUri = data?.data
                        selectedImageView.setImageURI(selectedImageUri)
                        selectedImageUrl = null // מבטל את ה-URL הקודם אם היה
                        uploadImageToCloudinary(selectedImageUri!!)
                    } else {
                        Log.e("EditPostFragment", "Image selection failed - URI is null")
                    }
                }
            }
        } else {
            Log.w("EditPostFragment", "Image selection canceled")
        }
    }


    private fun uploadImageToCloudinary(uri: Uri) {
        Log.d("EditPostFragment", "Uploading image to Cloudinary: URI=$uri")

        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1️⃣ יצירת קובץ זמני במטמון
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val tempFile = File(requireContext().cacheDir, "temp_uploaded_image.jpg")
                tempFile.outputStream().use { output -> inputStream?.copyTo(output) }

                val fileUri = Uri.fromFile(tempFile)

                Log.d("EditPostFragment", "Converted URI to File: $fileUri, Exists: ${tempFile.exists()}, Size: ${tempFile.length()} bytes")

                // 2️⃣ שליחת הקובץ לקלאודינרי
                val uploadedUrl = cloudinaryManager.uploadImage(fileUri)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (!uploadedUrl.isNullOrEmpty()) {
                        selectedImageUrl = uploadedUrl
                        Glide.with(requireContext()).load(uploadedUrl).into(selectedImageView)
                        Log.d("EditPostFragment", "✅ Image uploaded successfully: URL=$uploadedUrl")
                    } else {
                        Log.e("EditPostFragment", "❌ Failed to upload image to Cloudinary - Response was empty")
                        Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("EditPostFragment", "❌ Cloudinary upload error: ${e.localizedMessage}", e)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Upload error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }










    private fun getRealPathFromURI(uri: Uri): String? {
        val contentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex("_data")
                if (index != -1) {
                    return it.getString(index)
                }
            }
        }

        // ניסיון נוסף להשיג נתיב אם הדרך הקודמת נכשלה
        return uri.path
    }



    private fun updatePost() {
        Log.d("EditPostFragment", "updatePost() called!")

        if (originalPost == null) {
            Log.e("EditPostFragment", "updatePost - No original post found, cannot update.")
            return
        }

        if (firestoreId.isNullOrEmpty()) {
            Log.e("EditPostFragment", "updatePost - firestoreId is null or empty, cannot update Firestore.")
        }

        val newDescription = descriptionEditText.text.toString()
        val newSkillLevel = skillLevelSpinner.selectedItem.toString()
        val newPhoneNumber = phoneNumberEditText.text.toString()

        // ✅ עדכון originalPost עם selectedImageUrl החדש
        originalPost = originalPost!!.copy(imageUrl = selectedImageUrl ?: originalPost!!.imageUrl)
        Log.d("EditPostFragment", "updatePost - originalPost imageUrl: ${originalPost!!.imageUrl}")

        val updatedPost = originalPost!!.copy(
            description = newDescription.ifEmpty { originalPost!!.description },
            skillLevel = newSkillLevel.ifEmpty { originalPost!!.skillLevel },
            phoneNumber = newPhoneNumber.ifEmpty { originalPost!!.phoneNumber },
            imageUrl = originalPost!!.imageUrl
        )

        lifecycleScope.launch(Dispatchers.IO) {
            val db = PostDatabase.getDatabase(requireContext())
            db.postDao().updatePost(updatedPost)

            if (!firestoreId.isNullOrEmpty()) {
                FirebaseFirestore.getInstance().collection("posts").document(firestoreId!!)
                    .update(
                        "description", updatedPost.description,
                        "skillLevel", updatedPost.skillLevel,
                        "phoneNumber", updatedPost.phoneNumber,
                        "imageUrl", updatedPost.imageUrl
                    )
                    .addOnSuccessListener {
                        Log.d("EditPostFragment", "updatePost - Post updated in Firestore")
                    }
                    .addOnFailureListener {
                        Log.e("EditPostFragment", "updatePost - Failed to update Firestore: ${it.message}")
                    }
            } else {
                Log.e("EditPostFragment", "updatePost - Firestore update skipped due to null firestoreId.")
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
