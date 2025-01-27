package com.example.skill_swap_app.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddPostFragment : Fragment() {

    private lateinit var descriptionEditText: EditText
    private lateinit var skillLevelSpinner: Spinner
    private lateinit var phoneNumberEditText: EditText
    private lateinit var postButton: Button
    private lateinit var uploadImageButton: Button
    private lateinit var selectedImageView: ImageView
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // הוספת MenuFragment
        val menuFragment = MenuFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.menu_fragment_container, menuFragment)
            .commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)

        descriptionEditText = view.findViewById(R.id.description_edittext)
        skillLevelSpinner = view.findViewById(R.id.skill_level_spinner)
        phoneNumberEditText = view.findViewById(R.id.phone_number_edittext)
        postButton = view.findViewById(R.id.post_button)
        uploadImageButton = view.findViewById(R.id.upload_image_button)
        selectedImageView = view.findViewById(R.id.selected_image_view)

        // הגדרת כפתור העלאת התמונה
        uploadImageButton.setOnClickListener {
            openImagePicker()
        }

        // כפתור לשליחת הפוסט
        postButton.setOnClickListener {
            val description = descriptionEditText.text.toString()
            val skillLevel = skillLevelSpinner.selectedItem.toString()
            val phoneNumber = phoneNumberEditText.text.toString()

            if (description.isNotEmpty() && phoneNumber.isNotEmpty()) {
                // במקרה הזה, אם התמונה לא נבחרה, אנחנו לא נשלח כתובת התמונה, אלא ניתן כתובת דמה
                val imageUrl = selectedImageUri?.toString() ?: "image_url"  // תוכל להחיל את הכתובת של התמונה כאן

                val post = Post(description = description, skillLevel = skillLevel, phoneNumber = phoneNumber, imageUrl = imageUrl)

                // יצירת פוסט במסד נתונים
                insertPost(post)

                Toast.makeText(requireContext(), "Post created successfully", Toast.LENGTH_SHORT).show()

                // ניווט חזרה ל-FeedFragment
                findNavController().navigate(R.id.action_addPostFragment_to_feedFragment)
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // פתיחת file explorer לבחור תמונה
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }

    // קבלה של התמונה שנבחרה והצגת התמונה ב-ImageView
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICKER_REQUEST_CODE) {
            selectedImageUri = data?.data // קבלת ה-URI של התמונה
            selectedImageView.setImageURI(selectedImageUri) // הצגת התמונה ב-ImageView
            Toast.makeText(requireContext(), "Image selected successfully", Toast.LENGTH_SHORT).show()
        }
    }

    // הוספת הפוסט למסד נתונים
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
