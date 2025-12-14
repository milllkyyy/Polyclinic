package me.evseeva.polyclinic

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dbHelper = DatabaseHelper(this)
        val patient = dbHelper.getPatient()

        if (patient == null) {
            startActivity(Intent(this, PatientRegistrationActivity::class.java))
            finish()
            return
        }

        // Настройка кнопок
        findViewById<Button>(R.id.btnBookAppointment).setOnClickListener {
            startActivity(Intent(this, SpecialtiesActivity::class.java))
        }

        findViewById<Button>(R.id.btnTickets).setOnClickListener {
            startActivity(Intent(this, MyTicketsActivity::class.java))
        }

        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, PatientRegistrationActivity::class.java))
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}