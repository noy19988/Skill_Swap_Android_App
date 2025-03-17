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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.AppDatabase
import com.example.skill_swap_app.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        auth = FirebaseAuth.getInstance()
        db = AppDatabase.getDatabase(requireContext())

        val usernameInput: EditText = view.findViewById(R.id.username_input)
        val emailInput: EditText = view.findViewById(R.id.email_input)
        val passwordInput: EditText = view.findViewById(R.id.password_input)
        val phoneInput: EditText = view.findViewById(R.id.phone_input)
        val registerButton: Button = view.findViewById(R.id.register_button)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)

        val toolbar: MaterialToolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp() // חזרה לעמוד הקודם
        }

        registerButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                progressBar.visibility = View.VISIBLE
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                val user = User(username = username, email = email, phone = phone)
                                val userId = db.userDao().insertUser(user).toInt()

                                Log.d("RegisterFragment", "User saved: $user (ID: $userId)")

                                val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putInt("user_id", userId)
                                editor.putString("user_email", email)
                                editor.apply()

                                // ✅ שמירה גם בפיירסטור עם ה-ID
                                saveUserToFirestore(username, email, phone, userId)
                            }

                            Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                            requireActivity().onBackPressed()
                        } else {
                            Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            Log.e("RegisterFragment", "Registration error", task.exception)
                        }
                    }
            }
        }

        return view
    }

    private fun saveUserToFirestore(username: String, email: String, phone: String, userId: Int) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        val userMap = hashMapOf(
            "username" to username,
            "email" to email,
            "phone" to phone,
            "profileImageUrl" to "", // אפשרות להוסיף תמונת פרופיל מאוחר יותר
            "id" to userId // ✅ הוספת ה-ID
        )

        firestore.collection("users").document(email)
            .set(userMap)
            .addOnSuccessListener {
                Log.d("RegisterFragment", "User added to Firestore: $email (ID: $userId)")
            }
            .addOnFailureListener { e ->
                Log.e("RegisterFragment", "Failed to add user to Firestore", e)
            }
    }
}