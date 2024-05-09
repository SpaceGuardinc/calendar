package com.example.plants.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.plants.R

class WorkTypeAdapter(
    context: Context,
    private val workTypes: List<String>,
    private val spinner: Spinner
) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, workTypes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        view.setTextColor(ContextCompat.getColor(context, R.color.black))
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        view.setTextColor(ContextCompat.getColor(context, R.color.black))
        return view
    }

    fun setOnItemSelectedListener(listener: AdapterView.OnItemSelectedListener) {
        spinner.onItemSelectedListener = listener
    }
}
