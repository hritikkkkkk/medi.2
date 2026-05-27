package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalDao {
    @Query("SELECT * FROM medicines ORDER BY id DESC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicineById(id: Int)

    @Query("SELECT * FROM prescriptions ORDER BY id DESC")
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity)

    @Query("SELECT * FROM reports ORDER BY id DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReportById(id: Int)
}
