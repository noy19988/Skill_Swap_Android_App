package com.example.skill_swap_app.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.skill_swap_app.R
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // הגדרת עיצוב זמני לפני טעינת ה- UI
        setTheme(R.style.Theme_Skill_Swap_App)

        setContentView(R.layout.activity_main)

        // הגדרת Toolbar כ-ActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // אתחול FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // בדיקה אם המשתמש מחובר לפני טעינת הניווט
        checkUserAuthentication()
    }

    private fun checkUserAuthentication() {
        val user = auth.currentUser

        // חיבור NavHostFragment – רק אחרי הבדיקה
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)

        if (user != null) {
            // המשתמש מחובר → מעבר ישיר לפיד
            navController.navigate(R.id.feedFragment)
        }

        // הצגת ה- UI רק אחרי הבדיקה
        findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
