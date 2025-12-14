package me.evseeva.polyclinic

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class SpecialtiesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specialties)

        val dbHelper = DatabaseHelper(this)
        val specialties = dbHelper.getDoctorSpecialties()

        val listView = findViewById<ListView>(R.id.lvSpecialties)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, specialties)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedSpecialty = specialties[position]
            val intent = Intent(this, ScheduleActivity::class.java).apply {
                putExtra("SPECIALTY", selectedSpecialty)
            }
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}