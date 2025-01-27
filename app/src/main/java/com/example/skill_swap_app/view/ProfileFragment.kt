package com.example.skill_swap_app.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skill_swap_app.R
import com.example.skill_swap_app.model.AppDatabase
import com.example.skill_swap_app.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: EditText
    private lateinit var updateButton: Button
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    private lateinit var db: AppDatabase

    private var user: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        usernameTextView = view.findViewById(R.id.usernameTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        phoneTextView = view.findViewById(R.id.phoneTextView)
        updateButton = view.findViewById(R.id.update_button)
        editButton = view.findViewById(R.id.edit_button)
        deleteButton = view.findViewById(R.id.delete_button)

        // מאתחלים את Room Database
        db = AppDatabase.getDatabase(requireContext())
        // שליפת המידע מה-Room
        getUserFromRoom()

        // כפתור Edit – מאפשר עריכה של שם המשתמש והטלפון
        editButton.setOnClickListener {
            phoneTextView.isEnabled = true
            usernameTextView.isEnabled = true
            updateButton.visibility = View.VISIBLE // מציג את כפתור ה-Update
        }

        // כפתור Update – מעדכן את המשתמש
        updateButton.setOnClickListener {
            val updatedPhone = phoneTextView.text.toString()
            val updatedUsername = usernameTextView.text.toString()

            if (updatedPhone.isNotEmpty() && updatedUsername.isNotEmpty()) {
                user?.let {
                    it.username = updatedUsername
                    it.phone = updatedPhone
                    updateUser(it.id, updatedUsername, updatedPhone) // עדכון פרטי המשתמש
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // כפתור Delete – מוחק את המשתמש ומחזיר למסך התחברות
        deleteButton.setOnClickListener {
            user?.let {
                deleteUser(it) // מחיקת המשתמש
            }
        }

        return view
    }

    // פונקציה לשליפת המשתמש ממסד הנתונים (Room)
    private fun getUserFromRoom() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // כאן תעשה שליפת המשתמש מהמייל או ה-ID
                user = db.userDao().getUserByEmail("user@example.com") // תחליף במייל של המשתמש
                activity?.runOnUiThread {
                    // אחרי ששלפת את המשתמש, עדכן את ה-UI
                    user?.let {
                        usernameTextView.text = it.username
                        emailTextView.text = it.email
                        phoneTextView.setText(it.phone)
                        println("Username from Room: ${it.username}") // הדפסת שם משתמש לשם בדיקה
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // פונקציה לעדכון המשתמש
    private fun updateUser(userId: Int, updatedUsername: String, updatedPhone: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.userDao().updateUser(updatedUsername, updatedPhone, userId) // עדכון פרטי המשתמש
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "User updated successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error updating user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // פונקציה למחיקת המשתמש
    private fun deleteUser(user: User) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.userDao().deleteUserById(user.id) // מחיקת המשתמש לפי ה-ID
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "User deleted successfully", Toast.LENGTH_SHORT).show()
                    // ניווט למסך Login אחרי מחיקה
                    findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error deleting user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
