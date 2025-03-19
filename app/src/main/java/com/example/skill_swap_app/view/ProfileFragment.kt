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
import com.google.firebase.firestore.FirebaseFirestore
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
    ): View {
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
        cloudinaryManager = CloudinaryManager(requireContext())

        val deleteButton: Button = view.findViewById(R.id.delete_button)
        deleteButton.setOnClickListener { deleteUser() }

        val logoutButton: Button = view.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener { logoutUser() }

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userEmail = firebaseUser?.email

        if (userEmail != null) {
            getUserFromFirestore(userEmail)
        } else {
            Toast.makeText(requireContext(), "User email not found!", Toast.LENGTH_SHORT).show()
        }

        editButton.setOnClickListener {
            enableEditing()
        }

        uploadImageButton.setOnClickListener { openImagePicker() }

        updateButton.setOnClickListener {
            val updatedUsername = usernameEditText.text.toString().trim()
            val updatedPhone = phoneEditText.text.toString().trim()

            user?.let {
                val finalUsername = if (updatedUsername.isNotEmpty()) updatedUsername else it.username
                val finalPhone = if (updatedPhone.isNotEmpty()) updatedPhone else it.phone
                val finalImageUrl = selectedImageUri?.toString() ?: it.profileImageUrl

                updateUserInFirestore(it.email, finalUsername, finalPhone, finalImageUrl)
            }
        }

        return view
    }

    private fun enableEditing() {
        usernameTextView.visibility = View.GONE
        phoneTextView.visibility = View.GONE
        usernameEditText.visibility = View.VISIBLE
        phoneEditText.visibility = View.VISIBLE
        updateButton.visibility = View.VISIBLE
        uploadImageButton.visibility = View.VISIBLE

        // ✅ הפיכת השדות לניתנים לעריכה
        usernameEditText.isFocusableInTouchMode = true
        usernameEditText.isFocusable = true
        usernameEditText.isEnabled = true

        phoneEditText.isFocusableInTouchMode = true
        phoneEditText.isFocusable = true
        phoneEditText.isEnabled = true
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
                uploadImageToCloudinary(uri)
            }
        }
    }


    private fun getUserFromFirestore(userEmail: String) {
        FirebaseFirestore.getInstance().collection("users").document(userEmail).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""

                    user = User(username = username, email = userEmail, phone = phone, profileImageUrl = profileImageUrl)

                    // עדכון ממשק המשתמש
                    usernameTextView.text = username
                    phoneTextView.text = phone
                    emailTextView.text = userEmail

                    if (profileImageUrl.isNotEmpty()) {
                        Glide.with(requireContext()).load(profileImageUrl).into(profileImageView)
                    }

                    // שמירת הנתונים ב-SharedPreferences כדי שלא יאבדו בחידוש של האפליקציה
                    val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("username", username)
                        putString("phone", phone)
                        putString("profileImageUrl", profileImageUrl)
                        apply()
                    }

                    // עדכון הנתונים גם ב-Room
                    saveUserToRoom(user!!)
                } else {
                    Log.e("ProfileFragment", "User not found in Firestore")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Failed to fetch user from Firestore", e)
            }
    }

    private fun saveUserToRoom(user: User) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val existingUser = db.userDao().getUserByEmail(user.email)
                if (existingUser == null) {
                    db.userDao().insertUser(user) // הכנסת משתמש חדש
                    Log.d("ProfileFragment", "User inserted into Room: ${user.email}")
                } else {
                    db.userDao().updateUserProfile(existingUser.id, user.username, user.phone, user.profileImageUrl ?: "")
                    Log.d("ProfileFragment", "User updated in Room: ${user.email}")
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error saving user to Room", e)
            }
        }
    }


    private fun uploadImageToCloudinary(uri: Uri) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val uploadedUrl = cloudinaryManager.uploadImage(uri)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (!uploadedUrl.isNullOrEmpty()) {
                        updateUserInFirestore(user?.email ?: return@withContext, user?.username ?: "", user?.phone ?: "", uploadedUrl)
                    } else {
                        Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("Cloudinary", "Upload failed", e)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Upload failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUserInFirestore(email: String, username: String, phone: String, imageUrl: String?) {
        val firestore = FirebaseFirestore.getInstance()
        val userMap = mutableMapOf<String, Any>()

        if (username.isNotEmpty()) userMap["username"] = username
        if (phone.isNotEmpty()) userMap["phone"] = phone
        if (!imageUrl.isNullOrEmpty()) userMap["profileImageUrl"] = imageUrl

        firestore.collection("users").document(email)
            .update(userMap)
            .addOnSuccessListener {
                getUserFromFirestore(email)  // ✅ עדכון הנתונים מהשרת
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show() // ✅ הודעה למשתמש
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun deleteUser() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Account")
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.")

        builder.setPositiveButton("Yes") { _, _ ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    user?.let {
                        db.userDao().deleteUserById(it.id)  // מחיקת המשתמש מ-Room
                        FirebaseFirestore.getInstance().collection("users").document(it.email).delete() // מחיקת המשתמש מ-Firestore

                        FirebaseAuth.getInstance().currentUser?.delete() // מחיקת המשתמש מה-Authentication
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "User deleted successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error deleting user", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        val alertDialog = builder.create()
        alertDialog.show()
    }


    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

    companion object {
        private const val IMAGE_PICKER_REQUEST_CODE = 2001
    }
}
