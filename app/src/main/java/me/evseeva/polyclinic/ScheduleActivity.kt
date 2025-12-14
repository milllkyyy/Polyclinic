package me.evseeva.polyclinic

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ScheduleActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var specialty: String
    private lateinit var tableLayout: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        dbHelper = DatabaseHelper(this)
        specialty = intent.getStringExtra("SPECIALTY") ?: ""
        tableLayout = findViewById(R.id.tableSchedule)

        val btnBack = findViewById<Button>(R.id.btnBack)

        supportActionBar?.title = "Расписание: $specialty"

        loadSchedule()

        btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun loadSchedule() {
        // Очищаем таблицу перед загрузкой новых данных
        tableLayout.removeAllViews()

        val tickets = dbHelper.getTicketsBySpecialty(specialty)

        // Группируем по дням недели
        val daysOfWeek = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница")
        val timeSlots = listOf("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00")

        // Создаем заголовок с днями недели
        val headerRow = TableRow(this)

        // Первая ячейка заголовка (пустая для времени)
        val emptyHeader = TextView(this).apply {
            text = "Время"
            setTextColor(Color.BLACK)
            setBackgroundColor(resources.getColor(R.color.table_header, theme))
            setPadding(16, 16, 16, 16)
            gravity = android.view.Gravity.CENTER
        }
        headerRow.addView(emptyHeader)

        // Заголовки дней недели
        daysOfWeek.forEach { day ->
            val dayHeader = TextView(this).apply {
                text = day
                setTextColor(Color.BLACK)
                setBackgroundColor(resources.getColor(R.color.table_header, theme))
                setPadding(16, 16, 16, 16)
                gravity = android.view.Gravity.CENTER
                textSize = 12f
            }
            headerRow.addView(dayHeader)
        }
        tableLayout.addView(headerRow)

        // Создаем строки с временными слотами
        timeSlots.forEach { time ->
            val row = TableRow(this)

            // Ячейка времени
            val timeCell = TextView(this).apply {
                text = time
                setTextColor(Color.BLACK)
                setBackgroundColor(Color.LTGRAY)
                setPadding(16, 16, 16, 16)
                gravity = android.view.Gravity.CENTER
            }
            row.addView(timeCell)

            // Ячейки для каждого дня
            daysOfWeek.forEach { day ->
                val ticketForSlot = tickets.find { it.dayOfWeek == day && it.timeSlot == time }
                val cell = TextView(this).apply {
                    text = if (ticketForSlot?.isBooked == true) "Занято" else "Свободно"
                    setTextColor(resources.getColor(R.color.table_cell_text, theme))
                    setBackgroundColor(
                        if (ticketForSlot?.isBooked == true)
                            resources.getColor(R.color.table_cell_busy, theme)
                        else
                            resources.getColor(R.color.table_cell_free, theme)
                    )
                    setPadding(16, 16, 16, 16)
                    gravity = android.view.Gravity.CENTER

                    // Добавляем возможность бронирования только для свободных слотов
                    if (ticketForSlot?.isBooked == false) {
                        setOnClickListener {
                            bookTicket(ticketForSlot.id, time, day)
                        }
                    } else {
                        isClickable = false
                    }
                }
                row.addView(cell)
            }

            tableLayout.addView(row)
        }
    }

    private fun bookTicket(ticketId: Int, time: String, day: String) {
        val success = dbHelper.bookTicket(ticketId)
        if (success) {
            Toast.makeText(this, "Талон успешно забронирован!\n$day $time", Toast.LENGTH_LONG).show()
            loadSchedule() // Полностью перерисовываем расписание
        } else {
            Toast.makeText(this, "Ошибка при бронировании", Toast.LENGTH_SHORT).show()
        }
    }
}