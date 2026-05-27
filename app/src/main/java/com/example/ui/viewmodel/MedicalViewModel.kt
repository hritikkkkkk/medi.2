package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.repository.MedicalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "ai"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class StructuredMedDraft(
    val name: String,
    val dosage: String,
    val frequency: String,
    val timing: String,
    val days: Int
)

class MedicalViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MedicalDatabase.getDatabase(application)
    private val repository = MedicalRepository(database.medicalDao())

    // --- Core Navigation & Session States ---
    val isSplashCompleted = MutableStateFlow(false)
    val isLandingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted = MutableStateFlow(false)
    val isLoggedIn = MutableStateFlow(false)
    val isDoctorMode = MutableStateFlow(false)
    
    // Bottom tabs: 0 for Dashboard/Patients, 1 for Tracker/Prescribe, 2 for AI Chat, 3 for Reports/History, 4 for Profile
    val selectedPatientTab = MutableStateFlow(0)
    val selectedDoctorTab = MutableStateFlow(0)

    // --- Observable Data from DB ---
    val medicines: StateFlow<List<MedicineEntity>> = repository.allMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prescriptions: StateFlow<List<PrescriptionEntity>> = repository.allPrescriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<ReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Doctor AI Voice Prescription States ---
    val isListening = MutableStateFlow(false)
    val voiceTranscript = MutableStateFlow("")
    val isGeneratingPrescription = MutableStateFlow(false)
    val structuredMedsDraft = MutableStateFlow<List<StructuredMedDraft>>(emptyList())
    
    val patientNameInput = MutableStateFlow("")
    val doctorNameInput = MutableStateFlow("Dr. Dev Anand")
    val diagnosisInput = MutableStateFlow("")
    val doctorNotesInput = MutableStateFlow("")
    val draftSignature = MutableStateFlow("")

    // --- Patient UI Interactive States ---
    val isSimplifying = MutableStateFlow(false)
    val simplifiedExplanation = MutableStateFlow("")
    val selectedMedicineForSimplify = MutableStateFlow<MedicineEntity?>(null)

    // --- Diagnostic Report Action States ---
    val isUploadingReport = MutableStateFlow(false)
    val uploadNotesInput = MutableStateFlow("")
    val uploadCategoryInput = MutableStateFlow("Blood Test")
    val uploadNameInput = MutableStateFlow("")
    val reportExplanation = MutableStateFlow("")

    // --- Chat Session States ---
    val chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ai",
                content = "Hello! I am **MediScript AI**, your virtual clinical partner. You can ask me to explain a side effect, coordinate a scheduling priority, or write down a plan. How can I assist you today?"
            )
        )
    )
    val chatInputText = MutableStateFlow("")
    val isChatLoading = MutableStateFlow(false)

    // --- Mock Seeding on App Launch ---
    init {
        viewModelScope.launch {
            // Seed sample medicines and data for full working feel directly out-of-the-box!
            medicines.take(1).collect { existing ->
                if (existing.isEmpty()) {
                    repository.addMedicine(
                        MedicineEntity(
                            name = "Pantoprazole",
                            dosage = "40 mg",
                            frequency = "Once Daily",
                            timing = "Before Food",
                            days = 14,
                            isCompletedToday = false,
                            dosesTaken = 3,
                            dosesNeededPerDay = 1,
                            reminderTimes = "07:30",
                            instructions = "Eat 30 minutes before morning coffee"
                        )
                    )
                    repository.addMedicine(
                        MedicineEntity(
                            name = "Metoprolol Tartrate",
                            dosage = "25 mg",
                            frequency = "Twice Daily",
                            timing = "After Food",
                            days = 30,
                            isCompletedToday = true,
                            dosesTaken = 10,
                            dosesNeededPerDay = 2,
                            reminderTimes = "08:30,20:30",
                            instructions = "Slowly balances your heart pressure"
                        )
                    )
                    repository.addMedicine(
                        MedicineEntity(
                            name = "Cetirizine HCl",
                            dosage = "10 mg",
                            frequency = "At Bedtime",
                            timing = "At Bedtime",
                            days = 7,
                            isCompletedToday = false,
                            dosesTaken = 2,
                            dosesNeededPerDay = 1,
                            reminderTimes = "21:30",
                            instructions = "May cause mild nighttime drowsiness"
                        )
                    )

                    // Seed a sample prescription
                    repository.addPrescription(
                        PrescriptionEntity(
                            patientName = "Ritik Rajput",
                            doctorName = "Dr. Dev Anand",
                            date = getCurrentDateString(),
                            diagnosis = "Seasonal Rhinopharyngitis and Acid Reflux",
                            medicinesJson = """[{"name":"Pantoprazole","dosage":"40 mg","frequency":"Once Daily","timing":"Before Food","days":14},{"name":"Cetirizine HCl","dosage":"10 mg","frequency":"At Bedtime","timing":"At Bedtime","days":7}]""",
                            signatureSvg = "DrDevAnand_Sign_Enc",
                            voiceTranscript = "Patient Ritik has Seasonal Rhinopharyngitis with some acid reflux of moderate severity. prescribing pantoprazole forty milligrams once daily before food for two weeks, and cetirizine ten milligrams at bedtime daily for seven days.",
                            doctorNotes = "Advised to drink abundant lukewarm liquids and avoid eating deep oily items."
                        )
                    )

                    // Seed a sample report
                    repository.addReport(
                        ReportEntity(
                            name = "Annual Lipid Health Chart",
                            category = "Cardiology",
                            date = "2026-05-15",
                            fileSize = "2.2 MB",
                            notes = "Cholesterol levels are moderately elevated, triglyceride profile requires attention.",
                            aiSummary = "📈 **Lipid Profile Summary**\nYour lipid screen shows a Total Cholesterol of 230 mg/dL, which is slightly above ideal levels. Triglycerides are also mildly active.\n\n- 🌱 **Wellness Focus**: Increase high-fiber foods (cereals, barley) and start a 25-minute fast paced daily walking activity.\n- 🥤 **Advisory**: Restrict saturated fats and monitor levels in 3 months.",
                            base64Bytes = "lipid_mock"
                        )
                    )
                }
            }
        }
    }

    // --- Action Handlers ---

    fun toggleMode() {
        isDoctorMode.value = !isDoctorMode.value
    }

    fun completeOnboarding() {
        isOnboardingCompleted.value = true
    }

    fun markMedicineTaken(medicine: MedicineEntity) {
        viewModelScope.launch {
            val updated = medicine.copy(
                isCompletedToday = true,
                dosesTaken = medicine.dosesTaken + 1
            )
            repository.addMedicine(updated)
        }
    }

    fun resetMedicineDaily() {
        viewModelScope.launch {
            // Simulates daily reset of checkbox for testing interaction
            val currentList = medicines.value
            currentList.forEach { med ->
                repository.addMedicine(med.copy(isCompletedToday = false))
            }
        }
    }

    fun addCustomMedicine(name: String, Dosage: String, freq: String, timing: String, daysCount: Int, reminders: String, details: String) {
        viewModelScope.launch {
            val med = MedicineEntity(
                name = name,
                dosage = Dosage,
                frequency = freq,
                timing = timing,
                days = daysCount,
                reminderTimes = reminders,
                instructions = details,
                dosesNeededPerDay = if (freq.contains("Twice")) 2 else if (freq.contains("Thrice")) 3 else 1
            )
            repository.addMedicine(med)
        }
    }

    fun deleteMedicine(id: Int) {
        viewModelScope.launch {
            repository.deleteMedicine(id)
        }
    }

    // --- Voice Dictation & Parse Operations ---

    fun startListeningSimulated() {
        isListening.value = true
        voiceTranscript.value = ""
    }

    fun stopListeningAndParse(presetText: String? = null) {
        isListening.value = false
        val finalTranscript = presetText ?: "Paracetamol 650 mg twice daily for five days after food, plus Pantoprazole 40 mg once daily before food for seven days."
        voiceTranscript.value = finalTranscript
        
        viewModelScope.launch {
            isGeneratingPrescription.value = true
            val structuredJson = repository.generateStructuredPrescription(finalTranscript)
            
            val drafts = mutableListOf<StructuredMedDraft>()
            try {
                val array = JSONArray(structuredJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    drafts.add(
                        StructuredMedDraft(
                            name = obj.optString("name", "Unknown"),
                            dosage = obj.optString("dosage", "N/A"),
                            frequency = obj.optString("frequency", "Once Daily"),
                            timing = obj.optString("timing", "After Food"),
                            days = obj.optInt("days", 5)
                        )
                    )
                }
            } catch (e: Exception) {
                // If parse failure occurs, fallback gracefully to clean split
                drafts.add(StructuredMedDraft("Paracetamol", "650 mg", "Twice Daily", "After Food", 5))
            }
            structuredMedsDraft.value = drafts
            isGeneratingPrescription.value = false
        }
    }

    fun clearVoiceState() {
        voiceTranscript.value = ""
        structuredMedsDraft.value = emptyList()
    }

    fun commitVoicePrescription() {
        viewModelScope.launch {
            val medsJson = JSONArray().apply {
                structuredMedsDraft.value.forEach { draft ->
                    put(org.json.JSONObject().apply {
                        put("name", draft.name)
                        put("dosage", draft.dosage)
                        put("frequency", draft.frequency)
                        put("timing", draft.timing)
                        put("days", draft.days)
                    })
                }
            }.toString()

            val p = PrescriptionEntity(
                patientName = patientNameInput.value.ifBlank { "Unassigned Patient" },
                doctorName = doctorNameInput.value,
                date = getCurrentDateString(),
                diagnosis = diagnosisInput.value.ifBlank { "General Assessment" },
                medicinesJson = medsJson,
                signatureSvg = draftSignature.value.ifBlank { "DrDevAnand_Auto_Sign" },
                voiceTranscript = voiceTranscript.value,
                doctorNotes = doctorNotesInput.value
            )

            // Add the prescription
            repository.addPrescription(p)

            // Automatically register medicines into the patient's tracker system if patient matches
            if (patientNameInput.value.lowercase().contains("ritik") || patientNameInput.value.isBlank()) {
                structuredMedsDraft.value.forEach { m ->
                    repository.addMedicine(
                        MedicineEntity(
                            name = m.name,
                            dosage = m.dosage,
                            frequency = m.frequency,
                            timing = m.timing,
                            days = m.days,
                            isCompletedToday = false,
                            instructions = "Prescribed by Dr. Dev Anand on " + getCurrentDateString()
                        )
                    )
                }
            }

            // Reset inputs
            patientNameInput.value = ""
            diagnosisInput.value = ""
            doctorNotesInput.value = ""
            draftSignature.value = ""
            clearVoiceState()
            
            // Navigate to Prescriptions history lists
            selectedDoctorTab.value = 3
        }
    }

    // --- Interactive Explanations ---

    fun requestMedicineExplanation(medicine: MedicineEntity) {
        selectedMedicineForSimplify.value = medicine
        isSimplifying.value = true
        simplifiedExplanation.value = ""
        
        viewModelScope.launch {
            val formatted = "${medicine.name} ${medicine.dosage} - ${medicine.frequency}, ${medicine.timing} for ${medicine.days} days. Instructions: ${medicine.instructions}"
            val res = repository.simplifyPrescription(formatted)
            simplifiedExplanation.value = res
            isSimplifying.value = false
        }
    }

    fun clearExplanation() {
        selectedMedicineForSimplify.value = null
        simplifiedExplanation.value = ""
    }

    // --- Report Management Operations ---

    fun processReportUpload() {
        val name = uploadNameInput.value.ifBlank { "Diagnostic Lab report" }
        val category = uploadCategoryInput.value
        val notes = uploadNotesInput.value.ifBlank { "Standard diagnostic scan results" }
        
        isUploadingReport.value = true
        reportExplanation.value = ""

        viewModelScope.launch {
            val summary = repository.analyseReport(category, notes)
            val r = ReportEntity(
                name = name,
                category = category,
                date = getCurrentDateString(),
                fileSize = "1.5 MB",
                notes = notes,
                aiSummary = summary,
                base64Bytes = "custom_scanned_mock"
            )
            repository.addReport(r)
            
            // Clear inputs
            uploadNameInput.value = ""
            uploadNotesInput.value = ""
            isUploadingReport.value = false
            
            // Navigate to report list tab
            selectedPatientTab.value = 3
        }
    }

    fun deleteReport(id: Int) {
        viewModelScope.launch {
            repository.deleteReport(id)
        }
    }

    // --- Chat Health Assistant ---

    fun sendChatMessage() {
        val prompt = chatInputText.value.trim()
        if (prompt.isEmpty()) return

        val userMsg = ChatMessage(sender = "user", content = prompt)
        chatMessages.value = chatMessages.value + userMsg
        chatInputText.value = ""
        isChatLoading.value = true

        viewModelScope.launch {
            val reply = repository.askChatQuestion(prompt)
            val aiMsg = ChatMessage(sender = "ai", content = reply)
            chatMessages.value = chatMessages.value + aiMsg
            isChatLoading.value = false
        }
    }

    // --- Utility Methods ---
    private fun getCurrentDateString(): String {
        val s = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return s.format(Date())
    }
}
