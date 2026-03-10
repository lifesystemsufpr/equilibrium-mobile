package com.ufpr.equilibrium.feature_professional

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import com.ufpr.equilibrium.MainActivity
import java.util.Calendar
import com.ufpr.equilibrium.R
import com.ufpr.equilibrium.utils.SessionManager

class HomeProfissional : AppCompatActivity() {

    private lateinit var  tvName: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_home_profissional)
        
        // Ajusta o padding superior do layout para respeitar a status bar
        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootLayout)?.let { rootLayout ->
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(top = systemBars.top)
                insets
            }
        }

        val builder = AlertDialog.Builder(this)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        
        // Ajusta a margem do container que envolve o BottomNavigationView (include CardView)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val parentView = view.parent
            if (parentView is android.view.View) {
                parentView.updateLayoutParams<androidx.constraintlayout.widget.ConstraintLayout.LayoutParams> {
                    bottomMargin = systemBars.bottom
                }
            } else {
                @Suppress("UNCHECKED_CAST")
                (view.layoutParams as? android.view.ViewGroup.MarginLayoutParams)?.let {
                    it.bottomMargin = systemBars.bottom
                    view.layoutParams = it
                }
            }
            insets
        }

        builder.setTitle("Deseja sair do aplicativo ?")

        builder.setPositiveButton("Sim") { dialog, which ->
            SessionManager.clearSession()
            startActivity(Intent(this@HomeProfissional, MainActivity::class.java))
        }

        builder.setNegativeButton("Não") { dialog, which ->

        }

        tvName = findViewById(R.id.tvUserName)

        SessionManager.user?.let { user ->
            val firstName = user.fullName
                .trim()
                .split(Regex("\\s+"))
                .firstOrNull()
                ?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
                }
                ?: "Usuário"

            tvName.text = firstName

        } ?: run {
            tvName.text = "Usuário"
        }
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                builder.show()
            }
        })

        bottomNav.setOnNavigationItemSelectedListener { item -> 
            when (item.itemId) {

                R.id.nav_add -> {
                    startActivity(Intent(this@HomeProfissional, ListagemPacientes::class.java))
                }


                R.id.nav_add_user -> {
                    startActivity(Intent(this@HomeProfissional, CadastroPacienteActivity::class.java))
                }

                // questionnaire feature removed
            }

            true
        }

        // Initialize dashboard cards and chart with sample data
        try {
            val tvTotal = findViewById<TextView>(R.id.tvTotalValue)
            val tvMasculino = findViewById<TextView>(R.id.tvMasculinoValue)
            val tvFeminino = findViewById<TextView>(R.id.tvFemininoValue)
            val tvMediaGlobal = findViewById<TextView>(R.id.tvMediaGlobalValue)
            val tvMediaIndividual = findViewById<TextView>(R.id.tvMediaIndividualValue)

            // sample values — replace with real data
            tvTotal.text = "100"
            tvMasculino.text = "50"
            tvFeminino.text = "50"
            tvMediaGlobal.text = "100"
            tvMediaIndividual.text = "120"

            // ── Bar chart ──
            val barChart = findViewById<BarChart>(R.id.barChartMonthly)

            val monthlyValues = listOf(12f, 18f, 22f, 35f, 48f, 60f, 78f, 72f, 55f, 40f, 28f, 16f)
            val entries = ArrayList<BarEntry>()
            for (i in monthlyValues.indices) {
                entries.add(BarEntry(i.toFloat(), monthlyValues[i]))
            }

            val currentMonth = Calendar.getInstance().get(Calendar.MONTH).coerceIn(0, 11)

            val dataSet = BarDataSet(entries, "Avaliações Mensais")
            val colors = mutableListOf<Int>()
            for (i in monthlyValues.indices) {
                colors.add(
                    if (i == currentMonth) resources.getColor(R.color.blue, null)
                    else resources.getColor(R.color.blue_light, null)
                )
            }
            dataSet.colors = colors
            dataSet.setDrawValues(false)

            barChart.description.isEnabled = false
            barChart.axisRight.isEnabled = false
            barChart.legend.isEnabled = false
            barChart.setTouchEnabled(false)
            barChart.setPinchZoom(false)

            val yAxis = barChart.axisLeft
            yAxis.setDrawGridLines(true)
            yAxis.gridColor = android.graphics.Color.parseColor("#EEEEEE")
            yAxis.gridLineWidth = 0.8f
            yAxis.setDrawAxisLine(false)
            yAxis.setDrawLabels(false)
            yAxis.axisMinimum = 0f

            val xAxis = barChart.xAxis
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawLabels(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Jan","Fev","Mar","Abr","Mai","Jun","Jul","Ago","Set","Out","Nov","Dez"))
            xAxis.granularity = 1f
            xAxis.labelCount = 12
            xAxis.textSize = 10f
            xAxis.textColor = android.graphics.Color.parseColor("#666666")

            val barData = BarData(dataSet)
            barData.barWidth = 0.45f
            barChart.data = barData
            barChart.setFitBars(true)
            barChart.setExtraOffsets(4f, 0f, 4f, 0f)
            barChart.animateY(700)
            barChart.invalidate()
        } catch (t: Throwable) {
            android.util.Log.e("HomeProfissional", "Error initializing chart", t)
        }

        // Hamburger menu (icMenu) -> mostra opções: Alterar dados, Sair
        val menuBtn = findViewById<ImageView>(R.id.icMenu)
        menuBtn?.setOnClickListener { v ->
            val popup = PopupMenu(this, v)
            popup.menuInflater.inflate(R.menu.menu_hamburger_professional, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_logout -> {
                        builder.show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
}
