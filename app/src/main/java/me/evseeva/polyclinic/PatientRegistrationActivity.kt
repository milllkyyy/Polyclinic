package me.evseeva.polyclinic

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PatientRegistrationActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_registration)

        dbHelper = DatabaseHelper(this)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val etAge = findViewById<EditText>(R.id.etAge)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val existingPatient = dbHelper.getPatient()
        existingPatient?.let {
            etFullName.setText(it.fullName)
            etAge.setText(it.age.toString())

            val genderButtonId = when (it.gender) {
                "Мужской" -> R.id.rbMale
                "Женский" -> R.id.rbFemale
                else -> -1
            }
            if (genderButtonId != -1) {
                rgGender.check(genderButtonId)
            }
        }

        btnSave.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val ageText = etAge.text.toString().trim()
            val selectedGenderId = rgGender.checkedRadioButtonId

            if (fullName.isEmpty() || ageText.isEmpty() || selectedGenderId == -1) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageText.toIntOrNull()
            if (age == null || age <= 0 || age > 150) {
                Toast.makeText(this, "Введите корректный возраст", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gender = when (selectedGenderId) {
                R.id.rbMale -> "Мужской"
                R.id.rbFemale -> "Женский"
                else -> ""
            }

            dbHelper.addPatient(fullName, gender, age)

            Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}