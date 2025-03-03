package com.example.skill_swap_app.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.AppDatabase
import com.example.skill_swap_app.model.User
import com.example.skill_swap_app.utils.CloudinaryManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var usernameTextView: TextView
    private lateinit var usernameEditText: EditText
    private lateinit var phoneTextView: TextView
    private lateinit var phoneEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var editButton: Button
    private lateinit var emailTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var db: AppDatabase
    private lateinit var cloudinaryManager: CloudinaryManager
    private var user: User? = null
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileImageView = view.findViewById(R.id.profile_image_view)
        uploadImageButton = view.findViewById(R.id.upload_image_button)
        usernameTextView = view.findViewById(R.id.usernameTextView)
        usernameEditText = view.findViewById(R.id.usernameEditText)
        phoneTextView = view.findViewById(R.id.phoneTextView)
        phoneEditText = view.findViewById(R.id.phoneEditText)
        updateButton = view.findViewById(R.id.update_button)
        editButton = view.findViewById(R.id.edit_button)
        emailTextView = view.findViewById(R.id.emailTextView)
        progressBar = view.findViewById(R.id.progressBar)

        db = AppDatabase.getDatabase(requireContext())
        cloudinaryManager = CloudinaryManager(requireContext()) // יצירת מופע CloudinaryManager

        val deleteButton: Button = view.findViewById(R.id.delete_button)
        deleteButton.setOnClickListener { deleteUser() }

        val logoutButton: Button = view.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener { logoutUser() }

        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("user_email", null)

        getUserFromRoom(userEmail)

        editButton.setOnClickListener {
            usernameTextView.visibility = View.GONE
            phoneTextView.visibility = View.GONE
            usernameEditText.visibility = View.VISIBLE
            phoneEditText.visibility = View.VISIBLE
            updateButton.visibility = View.VISIBLE
            uploadImageButton.visibility = View.VISIBLE  // הצגת כפתור העלאת תמונה
        }

        uploadImageButton.setOnClickListener { openImagePicker() }

        updateButton.setOnClickListener {
            val updatedUsername = usernameEditText.text.toString().trim()
            val updatedPhone = phoneEditText.text.toString().trim()

            user?.let {
                val finalUsername = if (updatedUsername.isNotEmpty()) updatedUsername else it.username
                val finalPhone = if (updatedPhone.isNotEmpty()) updatedPhone else it.phone
                val finalImageUrl = selectedImageUri?.toString() ?: it.profileImageUrl // ✅ אם לא נבחרה תמונה חדשה, נשמור את הישנה

                updateUser(it.id, finalUsername, finalPhone, finalImageUrl) // ✅ עדכון ב-Room
                updateUserInFirestore(it.email, finalUsername, finalPhone, finalImageUrl) // ✅ עדכון בפיירסטור
            }
        }



        return view
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
            selectedImageUri?.let { uri ->
                profileImageView.setImageURI(uri)
                uploadImageToCloudinary(uri) // קריאה לפונקציה להעלאת התמונה ל-Cloudinary
            }
        }
    }

    private fun uploadImageToCloudinary(uri: Uri) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("Cloudinary", "Starting upload for: $uri")
                val uploadedUrl = cloudinaryManager.uploadImage(uri)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (!uploadedUrl.isNullOrEmpty()) {
                        Log.d("Cloudinary", "Upload success: $uploadedUrl")
                        selectedImageUri = Uri.parse(uploadedUrl) // ✅ שמירה זמנית של ה-URL
                        Glide.with(requireContext()).load(uploadedUrl).into(profileImageView)
                        Toast.makeText(requireContext(), "Image uploaded successfully! Don't forget to save.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("Cloudinary", "Upload failed with error", e)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Upload failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun updateUserInFirestore(email: String, username: String, phone: String, imageUrl: String?) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        val userMap = mutableMapOf<String, Any>()

        if (username.isNotEmpty()) userMap["username"] = username
        if (phone.isNotEmpty()) userMap["phone"] = phone
        if (!imageUrl.isNullOrEmpty()) userMap["profileImageUrl"] = imageUrl

        if (userMap.isNotEmpty()) {
            firestore.collection("users").document(email)
                .update(userMap)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile updated in Firestore!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update Firestore", Toast.LENGTH_SHORT).show()
                }
        }
    }





    private fun getUserFromRoom(userEmail: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                userEmail?.let { user = db.userDao().getUserByEmail(it) }
                activity?.runOnUiThread {
                    user?.let {
                        usernameTextView.text = it.username
                        phoneTextView.text = it.phone
                        emailTextView.text = it.email

                        if (!it.profileImageUrl.isNullOrEmpty()) {
                            Glide.with(requireContext()).load(it.profileImageUrl).into(profileImageView)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUser(userId: Int, updatedUsername: String, updatedPhone: String, imageUrl: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.userDao().updateUserProfile(userId, updatedUsername, updatedPhone, imageUrl ?: "")

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "User updated successfully", Toast.LENGTH_SHORT).show()
                    usernameTextView.text = updatedUsername
                    phoneTextView.text = updatedPhone
                    Glide.with(requireContext()).load(imageUrl).into(profileImageView) // ✅ עדכון תמונת הפרופיל
                    usernameTextView.visibility = View.VISIBLE
                    phoneTextView.visibility = View.VISIBLE
                    usernameEditText.visibility = View.GONE
                    phoneEditText.visibility = View.GONE
                    updateButton.visibility = View.GONE
                    uploadImageButton.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error updating user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUserProfileImage(userId: Int, imageUrl: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.userDao().updateUserProfileImage(userId, imageUrl)
                activity?.runOnUiThread {
                    Glide.with(requireContext()).load(imageUrl).into(profileImageView)
                    Toast.makeText(requireContext(), "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error updating profile image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

    private fun deleteUser() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                user?.let { db.userDao().deleteUserById(it.id) }
                FirebaseAuth.getInstance().currentUser?.delete()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "User deleted successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error deleting user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val IMAGE_PICKER_REQUEST_CODE = 2001
    }
}
