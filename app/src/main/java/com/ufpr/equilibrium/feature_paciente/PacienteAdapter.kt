package com.ufpr.equilibrium.feature_paciente

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ufpr.equilibrium.feature_ftsts.FtstsInstruction
import com.ufpr.equilibrium.feature_questionnaire.QuestionnaireActivity
import com.ufpr.equilibrium.R
import com.ufpr.equilibrium.domain.model.Patient

/**
 * Adapter for displaying patients in a RecyclerView.
 * Refactored to use domain models and delegate actions via callbacks.
 * No longer makes direct API calls - this is now the responsibility of the ViewModel.
 */
class PacienteAdapter(
    private val context: Context,
    private var pacientes: List<Patient> = emptyList(),
    private val onPatientSelected: (Patient) -> Unit = {}
) : RecyclerView.Adapter<PacienteAdapter.PacienteViewHolder>() {

    private var pacientesFiltrados: List<Patient> = pacientes

    class PacienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nome: TextView = itemView.findViewById(R.id.tv_nome_paciente)
        val info: TextView = itemView.findViewById(R.id.tv_info_paciente)
        val btn5sts: MaterialButton = itemView.findViewById(R.id.btn_5sts)
        val btnIvcf20: MaterialButton = itemView.findViewById(R.id.btn_ivcf20)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PacienteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_paciente, parent, false)
        return PacienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: PacienteViewHolder, position: Int) {
        val paciente = pacientesFiltrados[position]

        holder.nome.text = paciente.fullName
        holder.info.text = "CPF: ${paciente.cpf}"

        // Botão 30sSTS - Mostra dialog de confirmação
        holder.btn5sts.setOnClickListener {
            showPatientConfirmationDialog(paciente)
        }
        
        // Botão IVCF-20 - Navega diretamente para o questionário
        holder.btnIvcf20.setOnClickListener {
            // Salva o ID do paciente
            onPatientSelected(paciente)
            
            // Navega para a activity do questionário
            val intent = Intent(context, QuestionnaireActivity::class.java).apply {
                putExtra("paciente_id", paciente.id.toString())
                putExtra("paciente_name", paciente.fullName)
                putExtra("paciente_cpf", paciente.cpf)
                putExtra("paciente_age", paciente.age)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = pacientesFiltrados.size

    /**
     * Show confirmation dialog before starting test.
     */
    private fun showPatientConfirmationDialog(paciente: Patient) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_participant_info, null)
        
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.dialog_cancel)
        val ivClose = dialogView.findViewById<ImageView>(R.id.dialog_close)
        val btnConfirm = dialogView.findViewById<MaterialButton>(R.id.dialog_confirm)
        val tvParticipantName = dialogView.findViewById<TextView>(R.id.dialog_name)
        val tvParticipantSubInfo = dialogView.findViewById<TextView>(R.id.dialog_subinfo)
        
        tvParticipantName.text = paciente.fullName
        tvParticipantSubInfo.text = "Idade: ${paciente.age}   CPF: ${paciente.cpf}"
        
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        ivClose.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            // Notify callback that patient was selected
            onPatientSelected(paciente)
            
            // Navigate to test instruction screen
            val intent = Intent(context, FtstsInstruction::class.java).apply {
                putExtra("paciente_id", paciente.id.toString())
                putExtra("paciente_name", paciente.fullName)
                putExtra("paciente_cpf", paciente.cpf)
                putExtra("paciente_age", paciente.age)
            }
            dialog.dismiss()
            context.startActivity(intent)
        }

        dialog.show()
    }

    /**
     * Update the full patient list.
     */
    fun atualizarLista(novaLista: List<Patient>) {
        this.pacientes = novaLista
        this.pacientesFiltrados = novaLista
        notifyDataSetChanged()
    }

    /**
     * Filter patients by CPF or name.
     * Accepts partial matches for both fields.
     */
    fun filtrar(query: String) {
        val normalizedQuery = query.trim().lowercase()
        pacientesFiltrados = if (normalizedQuery.isBlank()) {
            pacientes
        } else {
            pacientes.filter { paciente ->
                // Filter by CPF (remove formatting)
                val cpfDigits = query.filter { it.isDigit() }
                val matchesCpf = cpfDigits.isNotEmpty() && 
                                 paciente.cpf.filter(Char::isDigit).contains(cpfDigits)
                
                // Filter by name (case-insensitive)
                val matchesName = paciente.fullName.lowercase().contains(normalizedQuery)
                
                matchesCpf || matchesName
            }
        }
        notifyDataSetChanged()
    }

    /**
     * @deprecated Use filtrar() instead. Kept for backward compatibility.
     */
    @Deprecated("Use filtrar() instead", ReplaceWith("filtrar(cpf)"))
    fun filtrarPorCpf(cpf: String) = filtrar(cpf)
}
