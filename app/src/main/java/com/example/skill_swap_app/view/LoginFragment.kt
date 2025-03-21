package com.example.skill_swap_app.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.skill_swap_app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = FirebaseAuth.getInstance()

        val emailInput: EditText = view.findViewById(R.id.email_input)
        val passwordInput: EditText = view.findViewById(R.id.password_input)
        val loginButton: Button = view.findViewById(R.id.login_button)
        val signUpButton: Button = view.findViewById(R.id.sign_up_button)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Email and Password are required", Toast.LENGTH_SHORT).show()
            } else {
                progressBar.visibility = View.VISIBLE

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            val loggedInUserEmail = auth.currentUser?.email
                            Log.d("LoginFragment", "Login successful! User email: $loggedInUserEmail")

                            if (loggedInUserEmail != null) {
                                saveUserEmailToSharedPreferences(loggedInUserEmail)
                                fetchUserDataFromFirestore(loggedInUserEmail)
                            } else {
                                Log.e("LoginFragment", "Error: FirebaseAuth returned null email")
                                Toast.makeText(context, "Failed to retrieve user email", Toast.LENGTH_SHORT).show()
                            }

                            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_loginFragment_to_feedFragment)
                        } else {
                            Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            Log.e("LoginFragment", "Login error", task.exception)
                        }
                    }
            }
        }

        signUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }

    private fun saveUserEmailToSharedPreferences(email: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_email", email)
        editor.apply()

        val savedEmail = sharedPreferences.getString("user_email", "NOT FOUND")
        Log.d("LoginFragment", "User email saved in SharedPreferences: $savedEmail")
    }

    private fun fetchUserDataFromFirestore(email: String) {
        FirebaseFirestore.getInstance().collection("users").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("LoginFragment", "User data found in Firestore: ${document.data}")

                    val username = document.getString("username") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""

                    val sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("username", username)
                    editor.putString("phone", phone)
                    editor.putString("profileImageUrl", profileImageUrl)
                    editor.apply()

                    Log.d("LoginFragment", "User data saved in SharedPreferences")
                } else {
                    Log.e("LoginFragment", "User not found in Firestore")
                }
            }
            .addOnFailureListener { e ->
                Log.e("LoginFragment", "Failed to fetch user from Firestore", e)
            }
    }
}
