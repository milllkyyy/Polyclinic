package me.evseeva.polyclinic

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "clinic.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_PATIENTS = "patients"
        const val COLUMN_ID = "id"
        const val COLUMN_FULL_NAME = "full_name"
        const val COLUMN_GENDER = "gender"
        const val COLUMN_AGE = "age"
        const val TABLE_TICKETS = "tickets"
        const val COLUMN_TICKET_ID = "ticket_id"
        const val COLUMN_DOCTOR_SPECIALTY = "doctor_specialty"
        const val COLUMN_DAY_OF_WEEK = "day_of_week"
        const val COLUMN_TIME_SLOT = "time_slot"
        const val COLUMN_DATE = "date"
        const val COLUMN_IS_BOOKED = "is_booked"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createPatientsTable = """
            CREATE TABLE $TABLE_PATIENTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FULL_NAME TEXT NOT NULL,
                $COLUMN_GENDER TEXT NOT NULL,
                $COLUMN_AGE INTEGER NOT NULL
            )
        """.trimIndent()

        val createTicketsTable = """
            CREATE TABLE $TABLE_TICKETS (
                $COLUMN_TICKET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DOCTOR_SPECIALTY TEXT NOT NULL,
                $COLUMN_DAY_OF_WEEK TEXT NOT NULL,
                $COLUMN_TIME_SLOT TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_IS_BOOKED INTEGER DEFAULT 0
            )
        """.trimIndent()

        db.execSQL(createPatientsTable)
        db.execSQL(createTicketsTable)

        initializeTestData(db)
    }

    private fun initializeTestData(db: SQLiteDatabase) {
        val specialties = listOf("Терапевт", "Хирург", "Кардиолог", "Невролог", "Офтальмолог")
        val daysOfWeek = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница")
        val timeSlots = listOf("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00")

        for (specialty in specialties) {
            for (day in daysOfWeek) {
                for (time in timeSlots) {
                    val values = ContentValues().apply {
                        put(COLUMN_DOCTOR_SPECIALTY, specialty)
                        put(COLUMN_DAY_OF_WEEK, day)
                        put(COLUMN_TIME_SLOT, time)
                        put(COLUMN_DATE, getNextDateForDay(day))
                        put(COLUMN_IS_BOOKED, 0)
                    }
                    db.insert(TABLE_TICKETS, null, values)
                }
            }
        }
    }

    private fun getNextDateForDay(dayOfWeek: String): String {
        val calendar = java.util.Calendar.getInstance()
        val daysOfWeekMap = mapOf(
            "Понедельник" to java.util.Calendar.MONDAY,
            "Вторник" to java.util.Calendar.TUESDAY,
            "Среда" to java.util.Calendar.WEDNESDAY,
            "Четверг" to java.util.Calendar.THURSDAY,
            "Пятница" to java.util.Calendar.FRIDAY
        )

        val targetDay = daysOfWeekMap[dayOfWeek] ?: return ""

        while (calendar.get(java.util.Calendar.DAY_OF_WEEK) != targetDay) {
            calendar.add(java.util.Calendar.DAY_OF_WEEK, 1)
        }

        return "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH)+1}-${calendar.get(java.util.Calendar.DAY_OF_MONTH)}"
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PATIENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TICKETS")
        onCreate(db)
    }

    fun addPatient(fullName: String, gender: String, age: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, fullName)
            put(COLUMN_GENDER, gender)
            put(COLUMN_AGE, age)
        }
        return db.insert(TABLE_PATIENTS, null, values)
    }

    fun getPatient(): Patient? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_PATIENTS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_ID DESC",
            "1"
        )

        return if (cursor.moveToFirst()) {
            val patient = Patient(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
                gender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)),
                age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE))
            )
            cursor.close()
            patient
        } else {
            cursor.close()
            null
        }
    }

    fun getTicketsBySpecialty(specialty: String): List<Ticket> {
        val db = this.readableDatabase
        val tickets = mutableListOf<Ticket>()

        val selection = "$COLUMN_DOCTOR_SPECIALTY = ?"
        val selectionArgs = arrayOf(specialty)

        val cursor = db.query(
            TABLE_TICKETS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COLUMN_DAY_OF_WEEK, $COLUMN_TIME_SLOT"
        )

        while (cursor.moveToNext()) {
            val ticket = Ticket(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TICKET_ID)),
                doctorSpecialty = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOCTOR_SPECIALTY)),
                dayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY_OF_WEEK)),
                timeSlot = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME_SLOT)),
                date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                isBooked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_BOOKED)) == 1
            )
            tickets.add(ticket)
        }
        cursor.close()
        return tickets
    }

    fun bookTicket(ticketId: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_BOOKED, 1)
        }

        val whereClause = "$COLUMN_TICKET_ID = ?"
        val whereArgs = arrayOf(ticketId.toString())

        return db.update(TABLE_TICKETS, values, whereClause, whereArgs) > 0
    }

    fun getPatientTickets(): List<Ticket> {
        val db = this.readableDatabase
        val tickets = mutableListOf<Ticket>()

        val selection = "$COLUMN_IS_BOOKED = ?"
        val selectionArgs = arrayOf("1")

        val cursor = db.query(
            TABLE_TICKETS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COLUMN_DATE DESC, $COLUMN_TIME_SLOT"
        )

        while (cursor.moveToNext()) {
            val ticket = Ticket(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TICKET_ID)),
                doctorSpecialty = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOCTOR_SPECIALTY)),
                dayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY_OF_WEEK)),
                timeSlot = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME_SLOT)),
                date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                isBooked = true
            )
            tickets.add(ticket)
        }
        cursor.close()
        return tickets
    }

    fun getDoctorSpecialties(): List<String> {
        val db = this.readableDatabase
        val specialties = mutableSetOf<String>()

        val cursor = db.rawQuery("SELECT DISTINCT $COLUMN_DOCTOR_SPECIALTY FROM $TABLE_TICKETS", null)

        while (cursor.moveToNext()) {
            specialties.add(cursor.getString(0))
        }
        cursor.close()

        return specialties.toList()
    }
}

data class Patient(
    val id: Int,
    val fullName: String,
    val gender: String,
    val age: Int
)

data class Ticket(
    val id: Int,
    val doctorSpecialty: String,
    val dayOfWeek: String,
    val timeSlot: String,
    val date: String,
    val isBooked: Boolean
)