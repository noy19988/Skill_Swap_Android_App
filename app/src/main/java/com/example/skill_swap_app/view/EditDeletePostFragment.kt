package com.example.skill_swap_app.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDatabase
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditDeletePostFragment : Fragment() {

    private var postId: Int = 0
    private lateinit var db: PostDatabase
    private lateinit var descriptionEditText: EditText
    private lateinit var skillLevelSpinner: Spinner
    private lateinit var phoneNumberEditText: EditText
    private lateinit var postButton: Button
    private lateinit var deleteButton: Button
    private lateinit var selectedImageView: ImageView
    private lateinit var uploadImageButton: Button

    private val IMAGE_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_delete_post, container, false)

        postId = arguments?.getInt("postId") ?: 0

        db = PostDatabase.getDatabase(requireContext())

        descriptionEditText = view.findViewById(R.id.description_edittext)
        skillLevelSpinner = view.findViewById(R.id.skill_level_spinner)
        phoneNumberEditText = view.findViewById(R.id.phone_number_edittext)
        postButton = view.findViewById(R.id.post_button)
        deleteButton = view.findViewById(R.id.delete_button)
        selectedImageView = view.findViewById(R.id.selected_image_view)
        uploadImageButton = view.findViewById(R.id.upload_image_button)

        loadPostData(postId)

        postButton.setOnClickListener {
            updatePost()
        }

        deleteButton.setOnClickListener {
            deletePost()
        }

        uploadImageButton.setOnClickListener {
            uploadImage()
        }

        return view
    }

    private fun loadPostData(postId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val post = db.postDao().getPostById(postId)
            post?.let {
                activity?.runOnUiThread {
                    descriptionEditText.setText(it.description)
                    phoneNumberEditText.setText(it.phoneNumber)

                    Log.d("EditDeletePostFragment", "Skill Level: ${it.skillLevel}")

                    val skillLevelPosition = getSkillLevelPosition(it.skillLevel)
                    skillLevelSpinner.setSelection(skillLevelPosition)

                    Glide.with(this@EditDeletePostFragment)
                        .load(it.imageUrl)
                        .into(selectedImageView)

                    selectedImageView.tag = it.imageUrl
                }
            } ?: run {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Post not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getSkillLevelPosition(skillLevel: String): Int {
        return when (skillLevel) {
            "Slightly" -> 0
            "Good" -> 1
            "Expert" -> 2
            else -> 0
        }
    }

    private fun updatePost() {
        val updatedDescription = descriptionEditText.text.toString()
        val updatedPhoneNumber = phoneNumberEditText.text.toString()
        val updatedSkillLevel = skillLevelSpinner.selectedItem.toString()

        val updatedImageUrl = selectedImageView.tag?.toString() ?: ""

        lifecycleScope.launch(Dispatchers.IO) {
            val post = db.postDao().getPostById(postId)
            post?.let {
                val userId = it.userId
                val updatedPost = Post(
                    id = postId,
                    description = updatedDescription,
                    skillLevel = updatedSkillLevel,
                    phoneNumber = updatedPhoneNumber,
                    imageUrl = updatedImageUrl,
                    userId = userId
                )

                db.postDao().updatePost(updatedPost)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Post updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun deletePost() {
        lifecycleScope.launch(Dispatchers.IO) {
            db.postDao().deletePost(postId)
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun uploadImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE) {
            val imageUri: Uri? = data?.data
            selectedImageView.setImageURI(imageUri)

            val updatedImageUrl = imageUri.toString()
            selectedImageView.tag = updatedImageUrl
        }
    }
}
