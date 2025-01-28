package com.example.skill_swap_app.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.Post
import com.example.skill_swap_app.model.PostDatabase
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_delete_post, container, false)

        // קבלת ה-`postId` מהניווט
        postId = arguments?.getInt("postId") ?: 0

        // אתחול של הדאטה בייס
        db = PostDatabase.getDatabase(requireContext())

        descriptionEditText = view.findViewById(R.id.description_edittext)
        skillLevelSpinner = view.findViewById(R.id.skill_level_spinner)
        phoneNumberEditText = view.findViewById(R.id.phone_number_edittext)
        postButton = view.findViewById(R.id.post_button)
        deleteButton = view.findViewById(R.id.delete_button)

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

        return view
    }

    private fun loadPostData(postId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val post = db.postDao().getPostsByUserId(postId).firstOrNull()
            post?.let {
                activity?.runOnUiThread {
                    descriptionEditText.setText(it.description)
                    phoneNumberEditText.setText(it.phoneNumber)
                }
            }
        }
    }

    private fun updatePost() {
        val updatedDescription = descriptionEditText.text.toString()
        val updatedPhoneNumber = phoneNumberEditText.text.toString()
        val updatedSkillLevel = skillLevelSpinner.selectedItem.toString()

        val post = Post(id = postId, description = updatedDescription, skillLevel = updatedSkillLevel, phoneNumber = updatedPhoneNumber, userId = 0)

        lifecycleScope.launch(Dispatchers.IO) {
            db.postDao().updatePost(post)
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Post updated successfully", Toast.LENGTH_SHORT).show()
                // חזור לדף הפוסטים
                findNavController().navigateUp()
            }
        }
    }

    private fun deletePost() {
        lifecycleScope.launch(Dispatchers.IO) {
            db.postDao().deletePost(postId)
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show()
                // חזור לדף הפוסטים
                findNavController().navigateUp()
            }
        }
    }
}
