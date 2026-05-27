package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MedicineEntity::class, PrescriptionEntity::class, ReportEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MedicalDatabase : RoomDatabase() {
    abstract fun medicalDao(): MedicalDao

    companion object {
        @Volatile
        private var INSTANCE: MedicalDatabase? = null

        fun getDatabase(context: Context): MedicalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalDatabase::class.java,
                    "mediscript_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
