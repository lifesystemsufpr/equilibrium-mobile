package com.ufpr.equilibrium.feature_questionnaire

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ufpr.equilibrium.R


// Item selado para representar tanto cabeçalhos quanto questões
sealed class QuestionnaireItem {
    data class Header(val groupId: String, val title: String) : QuestionnaireItem()
    data class QuestionItem(val question: Question) : QuestionnaireItem()
}

class QuestionnaireAdapter(
    private val questions: List<Question>,
    private val onAnswerChanged: (questionId: String, selectedIndex: Int, score: Int, note: String?) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_QUESTION = 1
    }

    // Lista mesclada de cabeçalhos e questões
    private val items: List<QuestionnaireItem> = buildItemList()

    private fun buildItemList(): List<QuestionnaireItem> {
        val result = mutableListOf<QuestionnaireItem>()
        var lastGroupId: String? = null

        questions.forEach { question ->
            // Adicionar cabeçalho se for um grupo novo
            if (question.groupId != null && question.groupId != lastGroupId) {
                // Usa o groupName diretamente da questão (vem da API)
                val groupTitle = question.groupName ?: question.groupId
                result.add(QuestionnaireItem.Header(question.groupId, groupTitle))
                lastGroupId = question.groupId
            }
            result.add(QuestionnaireItem.QuestionItem(question))
        }

        return result
    }

    // guarda estado local - agora usa String (UUID) como chave
    private val selectedIndices = mutableMapOf<String, Int>().apply {
        questions.forEach { put(it.id, -1) }
    }
    private val notes = mutableMapOf<String, String?>()


    // ViewHolders
    inner class QuestionVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuestion: TextView = view.findViewById(R.id.tvQuestion)
        val rgOptions: RadioGroup = view.findViewById(R.id.rgOptions)
        val etNote: EditText = view.findViewById(R.id.etNote)
    }

    inner class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvGroupName: TextView = view.findViewById(R.id.tvGroupName)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is QuestionnaireItem.Header -> VIEW_TYPE_HEADER
            is QuestionnaireItem.QuestionItem -> VIEW_TYPE_QUESTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_group_header, parent, false)
                HeaderVH(v)
            }
            VIEW_TYPE_QUESTION -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_question, parent, false)
                QuestionVH(v)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is QuestionnaireItem.Header -> {
                (holder as HeaderVH).tvGroupName.text = item.title
            }
            is QuestionnaireItem.QuestionItem -> {
                bindQuestion(holder as QuestionVH, item.question)
            }
        }
    }

    private fun bindQuestion(holder: QuestionVH, q: Question) {
        // Encontrar índice da questão na lista original (para numeração)
        val questionIndex = questions.indexOf(q) + 1
        holder.tvQuestion.text = "$questionIndex. ${q.text}"

        // Remover listener temporariamente para evitar triggers indesejados
        holder.rgOptions.setOnCheckedChangeListener(null)
        
        // limpar RadioGroup atual (reuso de views)
        holder.rgOptions.removeAllViews()

        q.options.forEachIndexed { idx, opt ->
            val rb = RadioButton(holder.itemView.context).apply {
                id = View.generateViewId()
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = opt.text
                // armazenar o score/índice como tag para recuperar depois
                tag = Pair(idx, opt.score)
                isClickable = true
                isFocusable = true
            }
            holder.rgOptions.addView(rb)
        }

        // visibilidade campo de nota
        holder.etNote.visibility = if (q.allowNote) View.VISIBLE else View.GONE
        holder.etNote.hint = q.groupName ?: ""
        
        // Remover TextWatcher anterior se existir (usando tag)
        val oldWatcher = holder.etNote.tag as? android.text.TextWatcher
        if (oldWatcher != null) {
            holder.etNote.removeTextChangedListener(oldWatcher)
        }
        
        // Definir texto sem disparar evento
        holder.etNote.setText(notes[q.id] ?: "")

        // restaurar seleção se houver (ANTES de configurar listener)
        val prevSelected = selectedIndices[q.id] ?: -1
        android.util.Log.d("QuestionnaireAdapter", "Binding question ${q.id.take(8)}: prevSelected=$prevSelected, childCount=${holder.rgOptions.childCount}")
        
        if (prevSelected >= 0 && prevSelected < holder.rgOptions.childCount) {
            val rb = holder.rgOptions.getChildAt(prevSelected) as RadioButton
            rb.isChecked = true
            android.util.Log.d("QuestionnaireAdapter", "  -> Restored selection at index $prevSelected")
        } else {
            holder.rgOptions.clearCheck()
            android.util.Log.d("QuestionnaireAdapter", "  -> Cleared check (no previous selection)")
        }

        // AGORA configurar listener de seleção (DEPOIS de restaurar estado)
        holder.rgOptions.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == -1) {
                // nenhuma seleção
                android.util.Log.d("QuestionnaireAdapter", "Question ${q.id.take(8)}: selection cleared (checkedId=-1)")
                selectedIndices[q.id] = -1
                onAnswerChanged(q.id, -1, 0, holder.etNote.text?.toString())
            } else {
                val rb = group.findViewById<RadioButton>(checkedId)
                val (index, score) = rb.tag as Pair<Int, Int>
                android.util.Log.d("QuestionnaireAdapter", "Question ${q.id.take(8)}: selected index=$index, score=$score")
                selectedIndices[q.id] = index
                onAnswerChanged(q.id, index, score, holder.etNote.text?.toString())
            }
        }

        // Criar e armazenar novo TextWatcher
        val newWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                notes[q.id] = s?.toString()
                val sel = selectedIndices[q.id] ?: -1
                val score = if (sel >= 0) q.options[sel].score else 0
                onAnswerChanged(q.id, sel, score, notes[q.id])
            }
        }
        
        // Salvar watcher na tag e adicionar ao EditText
        holder.etNote.tag = newWatcher
        holder.etNote.addTextChangedListener(newWatcher)

        // permitir clicar no card para abrir opções (usabilidade)
        holder.itemView.setOnClickListener { holder.rgOptions.performClick() }
    }

    override fun getItemCount(): Int = items.size
}
