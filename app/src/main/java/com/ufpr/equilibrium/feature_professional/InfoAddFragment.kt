package com.ufpr.equilibrium.feature_professional
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.ufpr.equilibrium.R

class InfoAddFragment : Fragment() {

    private lateinit var escolaridade: AutoCompleteTextView
    private lateinit var nivelSocio: AutoCompleteTextView
    private lateinit var peso: EditText
    private lateinit var altura: EditText

    private val escolaridadeMap = mapOf(
        "Nenhuma" to "NONE",
        "Fundamental Incompleto" to "FUNDAMENTAL_INCOMPLETE",
        "Fundamental Completo" to "FUNDAMENTAL_COMPLETE",
        "Ensino Médio Incompleto" to "HIGH_SCHOOL_INCOMPLETE",
        "Ensino Médio Completo" to "HIGH_SCHOOL_COMPLETE",
        "Ensino Superior Incompleto" to "HIGHER_EDUCATION_INCOMPLETE",
        "Ensino Superior Completo" to "HIGHER_EDUCATION_COMPLETE",
        "Pós-graduação" to "POSTGRADUATE",
        "Mestrado" to "MASTERS",
        "Doutorado" to "DOCTORATE"
    )

    private val nivelSocioMap = mapOf(
        "A (acima de R$ 21.000)" to "A",
        "B (R$ 10.800 a R$ 20.999)" to "B",
        "C (R$ 4.800 a R$ 10.799)" to "C",
        "D (R$ 2.400 a R$ 4.799)" to "D",
        "E (até R$ 2.399)" to "E"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_info_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnVoltar = view.findViewById<MaterialButton>(R.id.voltar)
        val btnEnviar = view.findViewById<MaterialButton>(R.id.enviar)
        val viewModel = ViewModelProvider(requireActivity()).get(FormViewModel::class.java)

        escolaridade = view.findViewById(R.id.escolaridade)
        nivelSocio = view.findViewById(R.id.nivelSocioeconomico)
        peso = view.findViewById(R.id.peso)
        altura = view.findViewById(R.id.altura)

        // Adapters para os dropdowns
        val socioAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            nivelSocioMap.keys.toList()
        )
        nivelSocio.setAdapter(socioAdapter)

        val escolaridadeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            escolaridadeMap.keys.toList()
        )
        escolaridade.setAdapter(escolaridadeAdapter)

        // Configuração de máscara / filtros
        peso.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        altura.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        peso.filters = arrayOf(InputFilter.LengthFilter(5))   // Ex: 120.5
        altura.filters = arrayOf(InputFilter.LengthFilter(4)) // Ex: 1.75

        btnVoltar.setOnClickListener {
            val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
            viewPager?.let { if (it.currentItem > 0) it.currentItem = it.currentItem - 1 }
        }

        btnEnviar.setOnClickListener {
            if (validarCampos()) {
                val escolaridadeSelecionada = escolaridade.text.toString().trim()
                val escolaridadeApiValue = escolaridadeMap[escolaridadeSelecionada] ?: ""

                val nivelSocioSelecionado = nivelSocio.text.toString().trim()
                val nivelSocioApiValue = nivelSocioMap[nivelSocioSelecionado] ?: ""

                viewModel.escolaridade.value = escolaridadeApiValue
                viewModel.nivelSocio.value = nivelSocioApiValue
                viewModel.peso.value = peso.text.toString().toInt()
                viewModel.altura.value = altura.text.toString().toInt()

                val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
                viewPager?.let {
                    if (it.currentItem < (it.adapter?.itemCount ?: 1) - 1) it.currentItem = it.currentItem + 1
                }
            }
        }
    }

    // ---------------- Validações ---------------- //

    private fun validarCampos(): Boolean {
        val escolaridadeSelecionada = escolaridade.text.toString().trim()
        val nivelSocioSelecionado = nivelSocio.text.toString().trim()
        val txtPeso = peso.text.toString().replace(",", ".").trim()
        val txtAltura = altura.text.toString().replace(",", ".").trim()

        return when {
            escolaridadeSelecionada.isEmpty() || !escolaridadeMap.containsKey(escolaridadeSelecionada) -> {
                mostrarErro("Selecione uma escolaridade válida")
                false
            }
            nivelSocioSelecionado.isEmpty() || !nivelSocioMap.containsKey(nivelSocioSelecionado) -> {
                mostrarErro("Selecione um nível socioeconômico válido")
                false
            }
            txtPeso.isEmpty() -> {
                mostrarErro("Preencha o peso")
                false
            }
            txtAltura.isEmpty() -> {
                mostrarErro("Preencha a altura")
                false
            }

            else -> true
        }
    }

    private fun isNumeroValido(valor: String, min: Float, max: Float): Boolean {
        return try {
            val numero = valor.toFloat()
            numero in min..max
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun mostrarErro(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }



}
