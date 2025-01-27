package com.example.skill_swap_app.view

import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skill_swap_app.R

class MenuAdapter(
    private val menuItems: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        // יצירת TextView פריט בצורה דינמית
        val textView = TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(32, 24, 32, 24) // Padding פנימי
            textSize = 18f
            setTextColor(Color.BLACK)
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_hamburger_menu, 0, 0, 0) // אייקון בצד שמאל
            compoundDrawablePadding = 16 // מרווח בין האייקון לטקסט
        }
        return MenuViewHolder(textView)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuItems[position]
        (holder.itemView as TextView).apply {
            text = menuItem
            setOnClickListener { onItemClick(menuItem) }
        }
    }

    override fun getItemCount(): Int = menuItems.size

    class MenuViewHolder(itemView: TextView) : RecyclerView.ViewHolder(itemView)
}
