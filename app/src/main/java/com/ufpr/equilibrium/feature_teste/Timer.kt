package com.ufpr.equilibrium.feature_teste

import com.ufpr.equilibrium.feature_ftsts.FtstsInstruction
import android.annotation.SuppressLint
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.ufpr.equilibrium.R
import com.ufpr.equilibrium.feature_professional.HomeProfissional
import com.ufpr.equilibrium.feature_professional.ListagemPacientes
import com.ufpr.equilibrium.network.RetrofitClient
import com.ufpr.equilibrium.network.Teste
import com.ufpr.equilibrium.utils.PacienteManager
import com.ufpr.equilibrium.utils.SessionManager
import com.ufpr.equilibrium.utils.RoleHelpers
import com.ufpr.equilibrium.utils.ErrorMessages
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

class Timer : AppCompatActivity(), SensorEventListener, TextToSpeech.OnInitListener {

    private lateinit var timerTextView: TextView
    private lateinit var title: TextView
    private lateinit var pauseButton: Button
    private lateinit var loadingOverlay: View
    private lateinit var sensorManager: SensorManager

    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var linearAceleration: Sensor? = null

    private val running = AtomicBoolean(false)
    private var startTime: Long = 0L

    // 60 Hz

    private val frequency = 1_000_000 / 60

    // filas de leitura brutas
    private val accelQueue = ConcurrentLinkedQueue<JSONObject>()
    private val gyroQueue = ConcurrentLinkedQueue<JSONObject>()
    private val linearQueue = ConcurrentLinkedQueue<JSONObject>()

    // resultado mesclado (apenas ACC + GYRO)
    private val result = Collections.synchronizedList(mutableListOf<JSONObject>())

    private val coroutineScope = CoroutineScope(Dispatchers.Default)


    // ======= buffers antigos mantidos (se forem usados pela sua classificação) =======
    private val ax = mutableListOf<Float>()
    private val ay = mutableListOf<Float>()
    private val az = mutableListOf<Float>()
    private val lx = mutableListOf<Float>()
    private val ly = mutableListOf<Float>()
    private val lz = mutableListOf<Float>()
    private val gx = mutableListOf<Float>()
    private val gy = mutableListOf<Float>()
    private val gz = mutableListOf<Float>()
    private val ma = mutableListOf<Float>()
    private val ml = mutableListOf<Float>()
    private val mg = mutableListOf<Float>()
    private var results: FloatArray? = null
    private val N_SAMPLES = 100

    // ========= 5TSTS (contador progressivo) =========
    // ★ removido countdown fixo; agora contamos para cima
    private var elapsedMs = 0L
    private var timerJob: Job? = null
    private var paused = false

    private var repetitions = 0
    private var lastRepTimestamp = 0L
    private var lastStandPeakTs = 0L
    private var sittingLikely = true

    private var lastGyroY = 0.0
    private var lastLinearZ = 0.0

    // ========= SLS Peak Detector (sem thresholds) =========
    private lateinit var slsPeakDetector: SlsPeakDetector

    private var timeDisplay = ""
    private lateinit var typeTeste: String

    // ========= TTS =========
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false
    private var lastSpokenSecond: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@Timer, FtstsInstruction::class.java))
                finish()
            }
        })

        timerTextView = findViewById(R.id.timerTextView)
        title = findViewById(R.id.title)
        pauseButton = findViewById(R.id.pauseButton)
        loadingOverlay = findViewById(R.id.loading_overlay)

        typeTeste = "TTSTS"
        title.text = "30sSTS"

        val refreshBtn = findViewById<ImageView>(R.id.refresh)


        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        linearAceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        // Inicializar detector SLS baseado em picos
        slsPeakDetector = SlsPeakDetector(
            onCycleComplete = { count ->
                Log.d("SLS", "Ciclo $count detectado!")
                runOnUiThread {
                    repetitions = count
                }
            }
        )

        
        //   - enquanto rodando: "Pausar" -> pausa o teste
        //   - pausado: "Enviar" -> envia ou navega
        pauseButton.text = "Pausar"
        pauseButton.isEnabled = true

      pauseButton.setOnClickListener {

    // Caso esteja rodando → pausa manual
    if (!paused && running.get()) {
        paused = true
        stopTimerAndSensors()
        speak("Teste pausado")
        pauseButton.text = "Enviar"
        return@setOnClickListener
    }

    // Se o botão estiver em modo "Enviar"
    if (paused) {

        // Profissional → pedir confirmação
        if (RoleHelpers.isHealthProfessional()) {
            showSendConfirmation(
                onConfirm = { postData() },
                onCancel = { }
            )
            
        } else {
            // Paciente → navega direto
            val intent = Intent(this@Timer, TestResult::class.java)
            intent.putExtra("time", timeDisplay.safeTime())

            intent.putExtra("teste", typeTeste)
            startActivity(intent)
        }
    }
}

        refreshBtn.setOnClickListener {
            val wasRunning = running.get() && !paused
            if (wasRunning) {
                // pause collection while waiting confirmation
                stopTimerAndSensors()
                paused = true
                runOnUiThread { pauseButton.text = "Enviar" }
            }
            showResetConfirmation (
                onConfirm = { resetTimer() },
                onCancel = { if (wasRunning) resumeTimerAndSensors() }
            )
        }


        textToSpeech = TextToSpeech(this, this)

        startTimerAndSensors()
    }

    // ====== TTS ======
    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) textToSpeech?.language = Locale("pt", "BR")
    }

    private fun speak(text: String) {
        if (ttsReady) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, System.nanoTime().toString())
        }
    }

    // ====== Timer/Sensores ======
    private fun startTimerAndSensors() {
        startTime = System.currentTimeMillis()
        // start protocol countdown at 30 seconds
        elapsedMs = 30_000L
        lastSpokenSecond = -1
        paused = false
        running.set(true)

        startSensorCollection()
        startCountdown()
    }

    private fun stopTimerAndSensors() {
        running.set(false)
        try {
            sensorManager.unregisterListener(this)
        } catch (_: Exception) { }
        timerJob?.cancel()
    }

    private fun resetTimer() {
        stopTimerAndSensors()

        result.clear()
        accelQueue.clear()
        gyroQueue.clear()
        linearQueue.clear()

        repetitions = 0
        lastRepTimestamp = 0L
        lastStandPeakTs = 0L
        sittingLikely = true
        lastSpokenSecond = -1
        elapsedMs = 0L
        
        // Resetar detector SLS
        slsPeakDetector.reset()
        paused = false
        timerTextView.text = "00:30"
        pauseButton.text = "Pausar"

        finish()
        startActivity(Intent(this@Timer, FtstsInstruction::class.java))
    }
    private fun startCountdown() {
        // if elapsedMs is zero or negative, initialize to protocol duration
        if (elapsedMs <= 0L) elapsedMs = 30_000L

        timerJob = coroutineScope.launch(Dispatchers.Main) {
            while (running.get() && elapsedMs >= 0) {

                timerTextView.text = formatTime(elapsedMs)
                timeDisplay = formatTime(elapsedMs)

                delay(200)
                elapsedMs -= 200

                // Quando zerar → finaliza o teste automaticamente
                if (elapsedMs <= 0) {
                    running.set(false)
                    stopTimerAndSensors()
                    
                    // ★ Imprimir resultado final do detector SLS
                    slsPeakDetector.printFinalSummary()
                    repetitions = slsPeakDetector.getCycleCount()
                    
                    // ensure UI shows zeroed time
                    runOnUiThread {
                        timerTextView.text = "00:00"
                        timeDisplay = "00:00"
                        
                        // Mostrar dialog com resultado de ciclos SLS
                        showCycleResultDialog(slsPeakDetector.getCycleCount())
                    }
                    speak("Teste concluído. ${slsPeakDetector.getCycleCount()} repetições.")

                    // fluxo ao terminar automaticamente
                    if (RoleHelpers.isHealthProfessional()) {
                        // mark as paused and change button to "Enviar" so the user must
                        // explicitly click to confirm sending (alert will be shown on click)
                        paused = true
                        // ensure UI update happens on main thread
                        runOnUiThread {
                            pauseButton.text = "Enviar"
                        }

                    } else {
                        val intent = Intent(this@Timer, TestResult::class.java)
                        intent.putExtra("time", "00:30")  // tempo total do protocolo
                        intent.putExtra("repetitions", repetitions)
                        intent.putExtra("teste", typeTeste)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun resumeTimerAndSensors() {
        // resume sensors and countdown using the current elapsedMs
        if (running.get()) return
        running.set(true)
        paused = false
        startSensorCollection()
        startCountdown()
    }


    @SuppressLint("DefaultLocale")
    private fun formatTime(ms: Long): String {
        val clamped = if (ms < 0) 0 else ms
        val seconds = (clamped / 1000) % 60
        val minutes = (clamped / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun startSensorCollection() {
        sensorManager.registerListener(this, accelerometer, frequency)
        sensorManager.registerListener(this, gyroscope, frequency)
        sensorManager.registerListener(this, linearAceleration, frequency)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!running.get()) return

        val timestampStr = formatTimestamp()
        val sensorData = floatArrayOf(event.values[0], event.values[1], event.values[2])

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelQueue.add(JSONObject().apply {
                    put("time", timestampStr)
                    put("x", sensorData[0].toDouble())
                    put("y", sensorData[1].toDouble())
                    put("z", sensorData[2].toDouble())
                })
            }

            Sensor.TYPE_GYROSCOPE -> {
                gyroQueue.add(JSONObject().apply {
                    put("time", timestampStr)
                    put("x", sensorData[0].toDouble())
                    put("y", sensorData[1].toDouble())
                    put("z", sensorData[2].toDouble())
                })
                lastGyroY = sensorData[1].toDouble()
                
                // Processar no detector SLS baseado em picos (usa gyroY para celular de pé)
                slsPeakDetector.processSample(
                    gyroValue = sensorData[1].toDouble(),  // gyroY - celular de pé com tela no peito
                    timestampMs = System.currentTimeMillis()
                )
            }
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                linearQueue.add(JSONObject().apply {
                    put("time", timestampStr)
                    put("x", sensorData[0].toDouble())
                    put("y", sensorData[1].toDouble())
                    put("z", sensorData[2].toDouble())
                })
                lastLinearZ = sensorData[2].toDouble()
            }
        }

        tryMergeSensorData()
    }



    private fun tryMergeSensorData() {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        while (true) {
            val acc = accelQueue.peek() ?: return
            val gyr = gyroQueue.peek() ?: return

            val accDateStr = acc.optString("time", "")
            val accDate = sdf.parse(accDateStr)
            if (accDate == null) { accelQueue.poll(); continue }

            val gyrDateStr = gyr.optString("time", "")
            val gyrDate = sdf.parse(gyrDateStr)
            if (gyrDate == null) { gyroQueue.poll(); continue }

            val diff = kotlin.math.abs(accDate.time - gyrDate.time)

            when {
                diff <= 25 -> {
                    val merged = JSONObject().apply {
                        put("timestamp", accDateStr)                // campo que o backend valida
                        put("accel_x", acc.optDouble("x"))
                        put("accel_y", acc.optDouble("y"))
                        put("accel_z", acc.optDouble("z"))
                        put("gyro_x",  gyr.optDouble("x"))
                        put("gyro_y",  gyr.optDouble("y"))
                        put("gyro_z",  gyr.optDouble("z"))
                    }

                    accelQueue.poll()
                    gyroQueue.poll()
                    result.add(merged)
                }
                accDate.before(gyrDate) -> accelQueue.poll()
                else -> gyroQueue.poll()
            }
        }
    }

    private fun formatTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun isoUtc(dateMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(dateMillis))
    }

    private fun postData() {
        loadingOverlay.visibility = View.VISIBLE
        
        // Validate that we have a valid patient ID before proceeding
        val patientUuid = PacienteManager.uuid
        if (patientUuid == null) {
            loadingOverlay.visibility = View.GONE
            val errorMsg = "Erro: ID do paciente não encontrado. Por favor, selecione o paciente novamente."
            Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
            speak(errorMsg)
            Log.e("Timer", "Patient UUID is null - cannot submit test")
            return
        }
        
        val api = RetrofitClient.instancePessoasAPI

        // ✅ Converte para SensorDataPoint (tipo seguro) em vez de Map<String, Any>
        // O result já contém dados mesclados de accel + gyro pelo tryMergeSensorData()
        val sensorList: List<com.ufpr.equilibrium.network.SensorDataPoint> = result.mapNotNull { json ->
            try {
                // Get timestamp as ISO string
                val timestampStr = json.optString("timestamp", "")
                if (timestampStr.isEmpty()) {
                    Log.w("Timer", "Skipping sensor point: missing timestamp")
                    return@mapNotNull null
                }
                
                // Extract merged accelerometer and gyroscope data
                com.ufpr.equilibrium.network.SensorDataPoint(
                    timestamp = timestampStr,  // Keep as ISO 8601 string for API
                    accel_x = json.optDouble("accel_x", 0.0),
                    accel_y = json.optDouble("accel_y", 0.0),
                    accel_z = json.optDouble("accel_z", 0.0),
                    gyro_x = json.optDouble("gyro_x", 0.0),
                    gyro_y = json.optDouble("gyro_y", 0.0),
                    gyro_z = json.optDouble("gyro_z", 0.0)
                )
            } catch (e: Exception) {
                Log.e("Timer", "Error converting sensor data point", e)
                null
            }
        }

        // ★ totalTime agora é o tempo decorrido do 5TSTS
        val total = timeDisplay.safeTime()

        val teste = Teste (
            type = "TTSTS",
            participantId = patientUuid.toString(),  // Now we know this is a valid UUID
            healthProfessionalId = SessionManager.user?.id.toString(),
            healthcareUnitId = intent.getStringExtra("id_unidade"),
            date = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()),
            totalTime = total,                    // ★ usa o tempo real
            sensorData = sensorList,              // ✅ Lista tipada, não Map
            time_init = isoUtc(startTime),
            time_end = isoUtc(System.currentTimeMillis())

        )

        Log.d("Timer", "Sending test with ${sensorList.size} sensor data points")

        println(teste.sensorData)

        if (RoleHelpers.isHealthProfessional()) {
            val call = api.postTestes(teste, "Bearer ${SessionManager.token}")
            call.enqueue(object : retrofit2.Callback<Teste> {
                override fun onResponse(call: Call<Teste>, response: Response<Teste>) {
                    loadingOverlay.visibility = View.GONE
                    
                    if (response.isSuccessful) {

                        val onConfirm = Intent(this@Timer, ListagemPacientes::class.java)
                        val onCancel = Intent(this@Timer, HomeProfissional::class.java)

                        speak("Teste enviado com sucesso!")

                        showOnSuccessConfirmation (
                            onConfirm = {
                                startActivity(onConfirm)
                                finish()
                            },
                            onCancel = {
                                startActivity(onCancel)
                                finish()
                            }
                        )

                    } else {
                        val msg = ErrorMessages.forHttpStatus(this@Timer, response.code())
                        speak(msg)
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Teste>, t: Throwable) {
                    loadingOverlay.visibility = View.GONE
                    Log.e("Erro", "Falha ao enviar o teste", t)
                    val msg = getString(R.string.error_network)
                    speak(msg)
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Show confirmation dialog before sending the test to server
    private fun showSendConfirmation(onConfirm: () -> Unit, onCancel: () -> Unit) {
        runOnUiThread {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@Timer)
                .setTitle(getString(R.string.confirm_send_title).takeIf { it.isNotEmpty() } ?: "Enviar resultado")
                .setMessage(getString(R.string.confirm_send_message).takeIf { it.isNotEmpty() } ?: "Deseja enviar os dados do teste para o servidor?")
                .setPositiveButton(getString(R.string.yes).takeIf { it.isNotEmpty() } ?: "Enviar") { _, _ ->
                    onConfirm()
                }
                .setNegativeButton(getString(R.string.no).takeIf { it.isNotEmpty() } ?: "Cancelar") { dialog, _ ->
                    dialog.dismiss()
                    onCancel()
                }
            builder.show()
        }
    }

    // Show confirmation dialog before resetting/restarting the test
    private fun showResetConfirmation(onConfirm: () -> Unit, onCancel: () -> Unit) {
        runOnUiThread {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@Timer)
                .setTitle("Reiniciar teste")
                .setMessage("Deseja reiniciar o teste? Todos os dados coletados serão perdidos.")
                .setPositiveButton("Reiniciar") { _, _ ->
                    onConfirm()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                    onCancel()
                }
            builder.show()
        }
    }


    private fun showOnSuccessConfirmation(onConfirm: () -> Unit, onCancel: () -> Unit) {
        runOnUiThread {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@Timer)
                .setTitle("Teste enviado com sucesso!")
                .setPositiveButton("Novo teste") { _, _ ->
                    onConfirm()
                }
                .setNegativeButton("Voltar ao início") { dialog, _ ->
                    dialog.dismiss()
                    onCancel()
                }
            builder.show()
        }
    }

    // Dialog para mostrar resultado de ciclos SLS ao final do teste
    private fun showCycleResultDialog( cycleCount: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this@Timer)
            .setTitle("Resultado do Teste 30sSTS")
            .setMessage("Total de ciclos sentar-levantar-sentar detectados:\n\n" +
                    "🔄 $cycleCount ciclos completos")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("📥 Baixar CSV") { dialog, _ ->
                exportCsvData()
                dialog.dismiss()
            }
            .setCancelable(false)

        builder.show()
    }

    // Exporta os dados do sensor para arquivo CSV
    private fun exportCsvData() {
        try {
            // Criar nome do arquivo com timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val patientName = PacienteManager.nome ?: "Paciente"
            val fileName = "30sSTS_${patientName.replace(" ", "_")}_$timestamp.csv"
            
            // Criar diretório se não existir
            val exportDir = File(getExternalFilesDir(null), "Exportacoes")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val csvFile = File(exportDir, fileName)
            
            // Escrever CSV
            FileWriter(csvFile).use { writer ->
                // Cabeçalho
                writer.append("timestamp,accel_x,accel_y,accel_z,gyro_x,gyro_y,gyro_z\n")
                
                // Dados
                result.forEach { json ->
                    writer.append(convertJsonToCsv(json))
                }
            }
            
            // Compartilhar arquivo
            shareCsvFile(csvFile)
            
            Toast.makeText(
                this,
                "CSV exportado com sucesso!\n${csvFile.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
            
            speak("CSV exportado com sucesso")
            
        } catch (e: Exception) {
            Log.e("Timer", "Erro ao exportar CSV", e)
            Toast.makeText(
                this,
                "Erro ao exportar CSV: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            speak("Erro ao exportar CSV")
        }
    }
    
    // Compartilha o arquivo CSV usando o sistema de compartilhamento do Android
    private fun shareCsvFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Dados do Teste 30sSTS")
                putExtra(Intent.EXTRA_TEXT, "Dados do sensor coletados durante o teste 30sSTS")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(shareIntent, "Compartilhar CSV"))
            
        } catch (e: Exception) {
            Log.e("Timer", "Erro ao compartilhar CSV", e)
            Toast.makeText(
                this,
                "Erro ao compartilhar arquivo",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    // útil para debug/export
    private fun convertJsonToCsv(json: JSONObject): String {
        fun getS(vararg keys: String): String {
            for (k in keys) if (json.has(k) && !json.isNull(k)) return json.optString(k)
            return ""
        }
        fun getD(vararg keys: String): Double {
            for (k in keys) if (json.has(k) && !json.isNull(k)) return json.optDouble(k, 0.0)
            return 0.0
        }

        val ts  = getS("timestamp", "time")
        val ax  = getD("accel_x", "x")
        val ay  = getD("accel_y", "y")
        val az  = getD("accel_z", "z")
        val gx  = getD("gyro_x", "x")
        val gy  = getD("gyro_y", "y")
        val gz  = getD("gyro_z", "z")

        return "\n$ts,$ax,$ay,$az,$gx,$gy,$gz"
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    // ===== Helpers =====
    private fun String.safeTime(): String {
        // garante "MM:SS" mesmo se timeDisplay estiver vazio
        return if (this.matches(Regex("\\d{2}:\\d{2}"))) this else "00:00"
    }
}
