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

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu)

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_hamburger -> {
                    showPopupMenu(toolbar)
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

        popupMenu.menu.add("User Profile")
        popupMenu.menu.add("My Posts")
        popupMenu.menu.add("Favorites")


        popupMenu.setOnMenuItemClickListener { item ->
            when (item.title) {
                "User Profile" -> {
                    findNavController().navigate(R.id.userProfileFragment)
                    true
                }
                "My Posts" -> {
                    findNavController().navigate(R.id.myPostsFragment)
                    true


                }
                "Favorites" -> {
                    findNavController().navigate(R.id.favoritesFragment)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}
