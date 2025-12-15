package me.evseeva.polyclinic

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Проверяем, есть ли данные пациента
        val dbHelper = DatabaseHelper(this)
        val patient = dbHelper.getPatient()

        if (patient == null) {
            // Если нет данных пациента, переходим к регистрации
            startActivity(Intent(this, PatientRegistrationActivity::class.java))
            finish()
            return
        }

        // Находим кнопки по ID
        val cardAppointment = findViewById<CardView>(R.id.cardAppointment)
        val ticketsButton = findViewById<CardView>(R.id.ticketsButton)
        val accountButton = findViewById<CardView>(R.id.accountButton)
        val exitButton = findViewById<CardView>(R.id.exitButton)

        cardAppointment.setOnClickListener {
            startActivity(Intent(this, SpecialtiesActivity::class.java))
        }
        ticketsButton.setOnClickListener {
            startActivity(Intent(this, MyTicketsActivity::class.java))
        }
        accountButton.setOnClickListener {
            startActivity(Intent(this, PatientRegistrationActivity::class.java))
        }
        exitButton.setOnClickListener {
            exitProcess(0)
        }

        Toast.makeText(
            this,
            "Добро пожаловать, ${patient.fullName}!",
            Toast.LENGTH_SHORT
        ).show()
    }

}