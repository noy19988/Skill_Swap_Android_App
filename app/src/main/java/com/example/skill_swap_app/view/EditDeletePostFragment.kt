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

    private val IMAGE_REQUEST_CODE = 1001 // קוד בקשת התמונות

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_delete_post, container, false)

        // קבלת ה-`postId` מה-bundle
        postId = arguments?.getInt("postId") ?: 0

        // אתחול של הדאטה בייס
        db = PostDatabase.getDatabase(requireContext())

        descriptionEditText = view.findViewById(R.id.description_edittext)
        skillLevelSpinner = view.findViewById(R.id.skill_level_spinner)
        phoneNumberEditText = view.findViewById(R.id.phone_number_edittext)
        postButton = view.findViewById(R.id.post_button)
        deleteButton = view.findViewById(R.id.delete_button)
        selectedImageView = view.findViewById(R.id.selected_image_view)
        uploadImageButton = view.findViewById(R.id.upload_image_button)

        // נטען את הפוסט ממסד הנתונים
        loadPostData(postId)

        // כפתור עדכון פוסט
        postButton.setOnClickListener {
            updatePost()
        }

        // כפתור מחיקת פוסט
        deleteButton.setOnClickListener {
            deletePost()
        }

        // כפתור העלאת תמונה
        uploadImageButton.setOnClickListener {
            uploadImage()
        }

        return view
    }

    // שליפת נתוני הפוסט
    private fun loadPostData(postId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val post = db.postDao().getPostById(postId) // שליפת הפוסט לפי ה-ID
            post?.let {
                activity?.runOnUiThread {
                    // מילוי השדות עם המידע של הפוסט
                    descriptionEditText.setText(it.description)
                    phoneNumberEditText.setText(it.phoneNumber)

                    Log.d("EditDeletePostFragment", "Skill Level: ${it.skillLevel}")

                    // הנחת רמת מיומנות ב-Spinner
                    val skillLevelPosition = getSkillLevelPosition(it.skillLevel)
                    skillLevelSpinner.setSelection(skillLevelPosition)

                    // הצגת התמונה בעזרת Glide
                    Glide.with(this@EditDeletePostFragment)
                        .load(it.imageUrl) // הנח את ה-URL של התמונה
                        .into(selectedImageView)

                    // אם התמונה קיימת, הגדר את ה-`tag` של התמונה
                    selectedImageView.tag = it.imageUrl
                }
            } ?: run {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Post not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // קבלת מיקום רמת המיומנות ב-Spinner
    private fun getSkillLevelPosition(skillLevel: String): Int {
        return when (skillLevel) {
            "Slightly" -> 0
            "Good" -> 1
            "Expert" -> 2
            else -> 0
        }
    }

    // עדכון הפוסט
    private fun updatePost() {
        val updatedDescription = descriptionEditText.text.toString()
        val updatedPhoneNumber = phoneNumberEditText.text.toString()
        val updatedSkillLevel = skillLevelSpinner.selectedItem.toString()

        // אם ה-`tag` של התמונה הוא null, השתמש במחרוזת ריקה או ערך ברירת מחדל
        val updatedImageUrl = selectedImageView.tag?.toString() ?: ""

        // שליפת ה-`userId` הנוכחי של הפוסט
        lifecycleScope.launch(Dispatchers.IO) {
            val post = db.postDao().getPostById(postId)
            post?.let {
                val userId = it.userId  // שמירת ה-`userId` המקורי
                val updatedPost = Post(
                    id = postId,
                    description = updatedDescription,
                    skillLevel = updatedSkillLevel,
                    phoneNumber = updatedPhoneNumber,
                    imageUrl = updatedImageUrl,
                    userId = userId  // שימור ה-`userId` המקורי
                )

                // עדכון הפוסט במסד הנתונים
                db.postDao().updatePost(updatedPost)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Post updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
    }

    // מחיקת פוסט
    private fun deletePost() {
        lifecycleScope.launch(Dispatchers.IO) {
            db.postDao().deletePost(postId)
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    // העלאת תמונה מהגלריה
    private fun uploadImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    // טיפול בתוצאה מהגלריה או המצלמה
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE) {
            val imageUri: Uri? = data?.data
            selectedImageView.setImageURI(imageUri)

            // שמור את ה-URI של התמונה
            val updatedImageUrl = imageUri.toString() // עדכן את ה-URL של התמונה
            selectedImageView.tag = updatedImageUrl // שמור את ה-URL בתג
        }
    }
}
