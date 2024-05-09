// AddVegetableActivity.kt
package com.example.plants

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.plants.databinding.ActivityAddVegetableBinding

class AddVegetableActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddVegetableBinding
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddVegetableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dbHelper = DBHelper.getInstance(this)


        binding.buttonSave.setOnClickListener {
            val vegetableName = binding.editTextVegetableName.text.toString()
            dbHelper.insertVegetable(vegetableName)
            finish()
        }
    }
}
