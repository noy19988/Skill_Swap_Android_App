package com.example.skill_swap_app.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.skill_swap_app.R
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController

class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        // חיבור Toolbar
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu)

        // הגדרת לחיצה על Hamburger Menu
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_hamburger -> {
                    showPopupMenu(toolbar) // פותח PopupMenu
                    true
                }
                else -> false
            }
        }

        return view
    }

    private fun showPopupMenu(anchor: View) {
        val popupMenu = PopupMenu(requireContext(), anchor)

        popupMenu.gravity = android.view.Gravity.END

        // הוספת פריטים לתפריט
        popupMenu.menu.add("User Profile")
        popupMenu.menu.add("My Posts")
        popupMenu.menu.add("Courses Recommendations")
        popupMenu.menu.add("Favorites")


        // טיפול בלחיצה על פריטים
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.title) {
                "User Profile" -> {
                    findNavController().navigate(R.id.userProfileFragment) // ניווט ל-UserProfileFragment
                    true
                }
                "My Posts" -> {
                    findNavController().navigate(R.id.myPostsFragment) // ניווט ל-MyPostsFragment
                    true
                }
                "Courses Recommendations" -> {
                    findNavController().navigate(R.id.coursesRecommendationsFragment) // ניווט ל-coursesRecommendationsFragment
                    true
                }
                "Favorites" -> {
                    findNavController().navigate(R.id.favoritesFragment) // ניווט ל-FavoritesFragment
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}
