package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val frequency: String,
    val timing: String,
    val days: Int,
    val isCompletedToday: Boolean = false,
    val dosesTaken: Int = 0,
    val dosesNeededPerDay: Int = 1,
    val historyJson: String = "[]", // List of timestamps/dates taken
    val isMissed: Boolean = false,
    val reminderTimes: String = "08:00", // comma-separated
    val instructions: String = "After food"
)

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientName: String,
    val doctorName: String,
    val date: String,
    val diagnosis: String,
    val medicinesJson: String, // Stringified list of structured medicines
    val signatureSvg: String = "", // Base64 signature path or sketch coords
    val voiceTranscript: String = "",
    val doctorNotes: String = ""
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // e.g., Blood Test, X-Ray, etc.
    val date: String,
    val fileSize: String,
    val notes: String = "",
    val aiSummary: String = "", // AI generated explanation
    val base64Bytes: String = "" // Mock bytes or local base64/uri for mock displays
)
