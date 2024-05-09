package com.example.plants

import android.app.DownloadManager.Request
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.plants.databinding.ActivityMainBinding
import org.json.JSONObject

const val API_KEY = "a82854cdb0024ff2963131405241904"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
            //binding.bGet.setOnClickListener{
            getResult()
        //}

        binding.buttonVegetables.setOnClickListener {
            val intent = Intent(this, VegetablesActivity::class.java)
            startActivity(intent)
        }

        binding.buttonFruits.setOnClickListener {
            val intent = Intent(this, FruitsActivity::class.java)
            startActivity(intent)
        }

        binding.buttonTrees.setOnClickListener {
            val intent = Intent(this, TreesActivity::class.java)
            startActivity(intent)
        }

        binding.buttonFlowers.setOnClickListener {
            val intent = Intent(this, FlowersActivity::class.java)
            startActivity(intent)
        }
    }
    private fun getResult() {
        val url = buildString {
            append("https://api.weatherapi.com/v1/current.json")
            append("?key=$API_KEY&q=Moscow&aqi=no")
        }
        val query = Volley.newRequestQueue(this)
        val request = StringRequest(url,
            { response ->
                val obj = JSONObject(response)
                val temp = obj.getJSONObject("current")
                val condition = temp.getJSONObject("condition")

                val temperature = temp.getString("temp_c")
                val conditionText = condition.getString("text")

                val temperatureTextView = findViewById<TextView>(R.id.temperatureTextView)
                val conditionTextView = findViewById<TextView>(R.id.conditionTextView)

                temperatureTextView.text = "$temperature°C"
                conditionTextView.text = "$conditionText"
            },
            {
                Log.d("MyLog", "Volley error: $it")
            }
        )
        query.add(request)
    }

}