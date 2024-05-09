// VegetablesAdapter.kt
package com.example.plants.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.plants.R
import com.example.plants.model.Vegetable
import com.example.plants.DBHelper

class VegetablesAdapter(private val dbHelper: DBHelper, private val listener: OnVegetableClickListener) :
    ListAdapter<Vegetable, VegetablesAdapter.ViewHolder>(VegetableDiffCallback()) {

    interface OnVegetableClickListener {
        fun onFavoriteClicked(vegetable: Vegetable)
        fun onVegetableClicked(vegetable: Vegetable)
        fun onVegetableLongClicked(vegetable: Vegetable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vegetable, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentVegetable = getItem(position)
        holder.textViewVegetableName.text = currentVegetable.name
        val isFavorite = dbHelper.isVegetableFavorite(currentVegetable.id)
        val starImageResource = if (isFavorite) {
            android.R.drawable.btn_star_big_on
        } else {
            android.R.drawable.btn_star_big_off
        }
        holder.imageViewFavorite.setImageResource(starImageResource)

        holder.imageViewFavorite.setOnClickListener {
            listener.onFavoriteClicked(currentVegetable)
        }

        holder.textViewVegetableName.setOnClickListener {
            listener.onVegetableClicked(currentVegetable)
        }

        holder.textViewVegetableName.setOnLongClickListener {
            listener.onVegetableLongClicked(currentVegetable)
            true
        }

        holder.itemView.setOnLongClickListener {
            listener.onVegetableLongClicked(currentVegetable)
            true
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewVegetableName: TextView = itemView.findViewById(R.id.textViewVegetableName)
        val imageViewFavorite: ImageView = itemView.findViewById(R.id.imageViewFavorite)
    }

    private class VegetableDiffCallback : DiffUtil.ItemCallback<Vegetable>() {
        override fun areItemsTheSame(oldItem: Vegetable, newItem: Vegetable): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Vegetable, newItem: Vegetable): Boolean {
            return oldItem == newItem
        }
    }
}
