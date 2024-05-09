package com.example.plants



import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plants.adapter.VegetablesAdapter
import com.example.plants.databinding.ActivityVegetablesBinding
import com.example.plants.model.Vegetable
import android.content.Intent
import android.util.Log
import android.widget.Button

class VegetablesActivity : AppCompatActivity(), VegetablesAdapter.OnVegetableClickListener {

    private lateinit var binding: ActivityVegetablesBinding
    private lateinit var dbHelper: DBHelper
    private lateinit var vegetablesAdapter: VegetablesAdapter

    private val addVegetableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                displayVegetables()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVegetablesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHelperProvider.getDBHelper(this)
        vegetablesAdapter = VegetablesAdapter(dbHelper, this)

        binding.recyclerViewVegetables.apply {
            layoutManager = LinearLayoutManager(this@VegetablesActivity)
            adapter = vegetablesAdapter
        }

        binding.addButton.setOnClickListener {
            val intent = Intent(this, AddVegetableActivity::class.java)
            addVegetableLauncher.launch(intent)
        }
        displayVegetables()
    }

    override fun onResume() {
        super.onResume()
        displayVegetables()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }

    private fun displayVegetables() {
        val vegetables = dbHelper.getAllVegetables()
        vegetablesAdapter.submitList(vegetables)
    }

    override fun onFavoriteClicked(vegetable: Vegetable) {
        val isFavorite = dbHelper.isVegetableFavorite(vegetable.id)
        val newIsFavorite = !isFavorite
        dbHelper.updateVegetableIsFavorite(vegetable.id, newIsFavorite)
        updateStarColor(vegetable, newIsFavorite)
        if (newIsFavorite) {
            moveVegetableToTop(vegetable)
        } else {
            moveVegetableToBottom(vegetable)
        }
    }

    private fun moveVegetableToTop(vegetable: Vegetable) {
        val position = vegetablesAdapter.currentList.indexOf(vegetable)
        if (position > 0) {
            val updatedList = vegetablesAdapter.currentList.toMutableList()
            updatedList.removeAt(position)
            updatedList.add(0, vegetable)
            vegetablesAdapter.submitList(updatedList)
        }
    }

    private fun moveVegetableToBottom(vegetable: Vegetable) {
        val position = vegetablesAdapter.currentList.indexOf(vegetable)
        if (position < vegetablesAdapter.currentList.size - 1) {
            val updatedList = vegetablesAdapter.currentList.toMutableList()
            updatedList.removeAt(position)
            updatedList.add(vegetable)
            vegetablesAdapter.submitList(updatedList)
        }
    }

    private fun updateStarColor(vegetable: Vegetable, isFavorite: Boolean) {
        val position = vegetablesAdapter.currentList.indexOf(vegetable)
        val viewHolder = binding.recyclerViewVegetables.findViewHolderForAdapterPosition(position)
        if (viewHolder is VegetablesAdapter.ViewHolder) {
            val starImageResource = if (isFavorite) {
                android.R.drawable.btn_star_big_on
            } else {
                android.R.drawable.btn_star_big_off
            }
            viewHolder.imageViewFavorite.setImageResource(starImageResource)
        }
    }

    override fun onVegetableClicked(vegetable: Vegetable) {
        Log.d("VegetablesActivity", "onVegetableClicked called")
        val intent = Intent(this, CalendarActivity::class.java)
        startActivity(intent)
    }

    override fun onVegetableLongClicked(vegetable: Vegetable) {
        showDeleteConfirmationDialog(vegetable)
    }

    @SuppressLint("SetTextI18n")
    private fun showDeleteConfirmationDialog(vegetable: Vegetable) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_confirmation, null)
        val textViewMessage = dialogView.findViewById<TextView>(R.id.textViewMessage)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonOk = dialogView.findViewById<Button>(R.id.buttonOk)

        textViewMessage.text = "Вы уверены, что хотите удалить ${vegetable.name}?"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonOk.setOnClickListener {
            dbHelper.deleteVegetable(vegetable)
            displayVegetables()
            dialog.dismiss()
        }

        dialog.show()
    }
}
