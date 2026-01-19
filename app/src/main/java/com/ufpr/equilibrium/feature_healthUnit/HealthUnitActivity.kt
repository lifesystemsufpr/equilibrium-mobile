package com.ufpr.equilibrium.feature_healthUnit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ufpr.equilibrium.feature_teste.Contagem
import com.ufpr.equilibrium.R
import com.ufpr.equilibrium.network.PessoasAPI
import com.ufpr.equilibrium.utils.SessionManager
import com.ufpr.equilibrium.utils.ErrorMessages
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class HealthUnitActivity : AppCompatActivity() {
    @Inject lateinit var pessoasAPI: PessoasAPI

    private lateinit var spinner: Spinner
    private lateinit var btnConfirmar: Button

    private var healthUnitList: List<HealthUnit> = listOf()
    private var selectedUnitId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_unit)

        spinner = findViewById(R.id.spinner_unidades)
        btnConfirmar = findViewById(R.id.btn_confirmar)

        handlerHealthUnits()

        btnConfirmar.setOnClickListener {
            if (selectedUnitId != null) {

                val cpf = intent.getStringExtra("cpf");
                val teste = intent.getStringExtra("teste");

                val intent = Intent(this, Contagem::class.java)

                println(selectedUnitId)

                intent.putExtra("id_unidade", selectedUnitId)

                intent.putExtra("cpf",cpf)

                intent.putExtra("teste", teste)

                startActivity(intent)

                setResult(RESULT_OK, intent)
                finish()

            } else {
                Toast.makeText(this, getString(R.string.error_select_health_unit), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handlerHealthUnits() {
        val call = pessoasAPI.getHealthUnit()

        call.enqueue(object : Callback<HealthUnitEnvelope> {
            override fun onResponse(
                call: Call<HealthUnitEnvelope>,
                response: Response<HealthUnitEnvelope>
            ) {

                if (response.isSuccessful && response.body() != null) {
                    healthUnitList = response.body()!!.data

                    val nomes = healthUnitList.map { it.name }

                    val adapter = ArrayAdapter(
                        this@HealthUnitActivity,
                        android.R.layout.simple_spinner_item,
                        nomes
                    )

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter

                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                        override fun onItemSelected (
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long

                        ) {
                            selectedUnitId = healthUnitList[position].id

                            println(selectedUnitId)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            selectedUnitId = null
                        }
                    }

                } else {
                    val msg = ErrorMessages.forHttpStatus(this@HealthUnitActivity, response.code())
                    Toast.makeText(this@HealthUnitActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<HealthUnitEnvelope>, t: Throwable) {
                Log.e("API", "Falha na requisição", t)
                Toast.makeText(this@HealthUnitActivity, getString(R.string.error_network), Toast.LENGTH_SHORT).show()
            }
        })
    }
}
