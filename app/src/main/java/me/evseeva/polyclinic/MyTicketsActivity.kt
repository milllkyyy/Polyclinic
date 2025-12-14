package me.evseeva.polyclinic

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MyTicketsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tickets)

        val dbHelper = DatabaseHelper(this)
        val tickets = dbHelper.getPatientTickets()

        val tvNoTickets = findViewById<TextView>(R.id.tvNoTickets)
        val listView = findViewById<ListView>(R.id.lvTickets)
        val btnBack = findViewById<Button>(R.id.btnBack)

        if (tickets.isEmpty()) {
            tvNoTickets.visibility = TextView.VISIBLE
            listView.visibility = ListView.GONE
        } else {
            tvNoTickets.visibility = TextView.GONE
            listView.visibility = ListView.VISIBLE

            val ticketStrings = tickets.map { ticket ->
                "${ticket.doctorSpecialty}\n${ticket.dayOfWeek}, ${ticket.date} ${ticket.timeSlot}"
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ticketStrings)
            listView.adapter = adapter
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}