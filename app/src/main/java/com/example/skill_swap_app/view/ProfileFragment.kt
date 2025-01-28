package com.example.skill_swap_app.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.AppDatabase
import com.example.skill_swap_app.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var usernameTextView: TextView
    private lateinit var usernameEditText: EditText
    private lateinit var phoneTextView: TextView
    private lateinit var phoneEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var editButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var db: AppDatabase
    private lateinit var emailTextView: TextView

    private var user: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize views
        usernameTextView = view.findViewById(R.id.usernameTextView)
        usernameEditText = view.findViewById(R.id.usernameEditText)
        phoneTextView = view.findViewById(R.id.phoneTextView)
        phoneEditText = view.findViewById(R.id.phoneEditText)
        updateButton = view.findViewById(R.id.update_button)
        editButton = view.findViewById(R.id.edit_button)
        emailTextView = view.findViewById(R.id.emailTextView)
//        progressBar = view.findViewById(R.id.progressBar)

        db = AppDatabase.getDatabase(requireContext())
        val deleteButton: Button = view.findViewById(R.id.delete_button)
        deleteButton.setOnClickListener {
            deleteUser() // קריאה לפונקציית מחיקת המשתמש
        }

        val logoutButton: Button = view.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            logoutUser() // קריאה לפונקציה שתנתק את המשתמש ותחזיר אותו למסך ההתחברות
        }

        // Retrieve user info from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("user_email", null)

        // Retrieve user data from the database
        getUserFromRoom(userEmail)

        // Set Edit button click listener
        editButton.setOnClickListener {
            // Enable editing of username and phone
            usernameTextView.visibility = View.GONE
            phoneTextView.visibility = View.GONE
            usernameEditText.visibility = View.VISIBLE
            phoneEditText.visibility = View.VISIBLE
            updateButton.visibility = View.VISIBLE
        }

        // Set Update button click listener
        updateButton.setOnClickListener {
            val updatedUsername = usernameEditText.text.toString()
            val updatedPhone = phoneEditText.text.toString()

            if (updatedUsername.isNotEmpty() && updatedPhone.isNotEmpty()) {
                user?.let {
                    it.username = updatedUsername
                    it.phone = updatedPhone
                    updateUser(it.id, updatedUsername, updatedPhone)
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun logoutUser() {
        // נתקים את המשתמש מהפיירבייס
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        // נוודא שהמשתמש יועבר למסך ה-Login
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment) // פעולה של ניווט למסך ההתחברות
    }


    private fun deleteUser() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // מחיקת המשתמש ממסד הנתונים
                user?.let {
                    db.userDao().deleteUserById(it.id) // מחיקת המשתמש לפי ID
                }

                // מחיקת המשתמש מ-Firebase
                val currentUser = FirebaseAuth.getInstance().currentUser
                currentUser?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // אם הצלחנו למחוק מהפיירבייס
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "User deleted successfully", Toast.LENGTH_SHORT).show()
                            // ניווט למסך התחברות אחרי מחיקה
                            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
                        }
                    } else {
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Error deleting user from Firebase", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error deleting user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getUserFromRoom(userEmail: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                userEmail?.let {
                    user = db.userDao().getUserByEmail(it)
                }
                activity?.runOnUiThread {
                    user?.let {
                        // Display user details as text
                        usernameTextView.text = it.username
                        phoneTextView.text = it.phone
                        emailTextView.text = it.email

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUser(userId: Int, updatedUsername: String, updatedPhone: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.userDao().updateUser(updatedUsername, updatedPhone, userId)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "User updated successfully", Toast.LENGTH_SHORT).show()
                    usernameTextView.text = updatedUsername
                    phoneTextView.text = updatedPhone

                    // Hide edit fields and show updated text
                    usernameTextView.visibility = View.VISIBLE
                    phoneTextView.visibility = View.VISIBLE
                    usernameEditText.visibility = View.GONE
                    phoneEditText.visibility = View.GONE
                    updateButton.visibility = View.GONE
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error updating user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

