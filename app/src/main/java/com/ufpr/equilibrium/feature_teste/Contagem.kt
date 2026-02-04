package com.ufpr.equilibrium.feature_teste

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.ufpr.equilibrium.R
import java.util.Locale

class Contagem : AppCompatActivity(), TextToSpeech.OnInitListener, SensorEventListener {

    private var textToSpeech: TextToSpeech? = null
    private lateinit var viewPager: ViewPager2
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

    // Contagem de 10 até 1 e depois "JÁ!"
    private val countdownList = listOf("7","6","5","4","3","2","1","JÁ!")
    private var countdownRunning = false
    
    // Gerenciamento de sensores
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var sensorsStarted = false
    
    // Frequência de 60 Hz
    private val frequency = 1_000_000 / 60

    companion object {
        private const val INTRO_UTTERANCE_ID = "INTRO_TTS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contagem)

        textToSpeech = TextToSpeech(this, this)
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = ContagemAdapter(countdownList)
        
        // Inicializar SensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Não inicia a contagem aqui; esperamos o TTS inicial terminar.
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale("pt", "BR")

            // Ouvir quando a fala introdutória terminar para então iniciar a contagem
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    if (utteranceId == INTRO_UTTERANCE_ID) {
                        runOnUiThread { startCountdown() }
                    }
                }
                override fun onError(utteranceId: String?) {
                    // Em caso de erro no TTS, ainda assim começamos a contagem
                    if (utteranceId == INTRO_UTTERANCE_ID) {
                        runOnUiThread { startCountdown() }
                    }
                }
            })

            // Fala introdutória antes da contagem de 10 s
            speakText(
                "Sete segundos para começar o teste! Faça os ajustes necessários",
                INTRO_UTTERANCE_ID
            )
        }
    }

    private fun startCountdown() {
        if (countdownRunning) return
        countdownRunning = true
        currentPage = 0

        // Exibe e fala a cada 1 segundo
        handler.post(object : Runnable {
            override fun run() {
                if (currentPage < countdownList.size) {
                    viewPager.setCurrentItem(currentPage, true)

                    val text = countdownList[currentPage]
                    speakText(text)
                    
                    // Iniciar sensores quando chegar a 4 segundos (currentPage == 3)
                    // Lista: ["7","6","5","4","3","2","1","JÁ!"]
                    // Index:   0   1   2   3   4   5   6   7
                    if (currentPage == 3 && !sensorsStarted) {
                        startSensorCollection()
                        sensorsStarted = true
                    }

                    currentPage++

                    // "JÁ!" fica menos tempo (300 ms) e já troca de tela
                    val delay = if (currentPage == countdownList.size) 300L else 1000L
                    handler.postDelayed(this, delay)
                } else {
                    startTimerActivity()
                }
            }
        })
    }

    private fun speakText(text: String, utteranceId: String? = null) {
        // Usamos QUEUE_FLUSH para garantir que cada fala substitua a anterior
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId ?: System.nanoTime().toString())
    }

    private fun startTimerActivity() {
        val teste = intent.getStringExtra("teste")
        val selectedUnitId = intent.getStringExtra("id_unidade")

        val newIntent = Intent(this, Timer::class.java).apply {
            putExtra("teste", teste)
            putExtra("id_unidade", selectedUnitId)
        }

        startActivity(newIntent)
        finish()
    }

    private fun startSensorCollection() {
        sensorManager.registerListener(this, accelerometer, frequency)
        sensorManager.registerListener(this, gyroscope, frequency)
    }
    
    private fun stopSensorCollection() {
        if (sensorsStarted) {
            try {
                sensorManager.unregisterListener(this)
            } catch (_: Exception) { }
            sensorsStarted = false
        }
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        // Os dados dos sensores não precisam ser processados aqui
        // Este método existe apenas para permitir o "warm up" dos sensores
        // O Timer.kt irá processar os dados quando a Activity for iniciada
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Não é necessário processar mudanças de precisão nesta tela
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        // Não paramos os sensores aqui para permitir continuidade com Timer.kt
        // O Timer.kt registrará seus próprios listeners enquanto os sensores continuam ativos
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }
}



