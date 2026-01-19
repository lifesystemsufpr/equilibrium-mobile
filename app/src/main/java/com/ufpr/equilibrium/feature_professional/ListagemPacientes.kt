package com.ufpr.equilibrium.feature_professional

// ListagemPacientes.kt
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.ufpr.equilibrium.R
import com.ufpr.equilibrium.feature_paciente.PacienteAdapter
import com.ufpr.equilibrium.network.PessoasAPI
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import com.ufpr.equilibrium.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.UUID

// ---- MODELOS alinhados ao JSON -------------------

data class Meta (
    val total: Int? = null,
    val page: Int? = null,
    val pageSize: Int? = null,
    val lastPage: Int? = null
)

data class PacienteModelList(
    val id: String?,
    val birthday: String?,
    val weight: Int?,       
    val height: Int?,       
    val zipCode: String?,
    val street: String?,
    val number: String?,
    val complement: String?,
    val neighborhood: String?,
    val socio_economic_level: String?,
    val scholarship: String?,
    val city: String?,
    val state: String?,
    val cpf: String?,
    val fullName: String?,
    val gender: String? = null,
    val role: String? = null,
    val phone: String? = null,
    val updatedAt: String? = null,
    val active: Boolean? = null
)

// Seu modelo de domínio usado pela UI/Adapter


// ---------------------------------------------------

@AndroidEntryPoint
class ListagemPacientes : AppCompatActivity() {
    @Inject lateinit var pessoasAPI: PessoasAPI
    private lateinit var recyclerView: RecyclerView
    private lateinit var pacienteAdapter: PacienteAdapter
    private val pacientes = mutableListOf<com.ufpr.equilibrium.domain.model.Patient>()
    
    // Variáveis de controle de paginação
    private var currentPage = 1
    private var isLoading = false
    private var hasMorePages = true
    private val pageSize = 20 // Tamanho da página
    private var currentFilter = "" // Rastreia o filtro atual para preservá-lo durante paginação
    private var isSearching = false // Indica se está em modo de busca
    private var searchCall: Call<PacientesEnvelope>? = null // Call da busca para poder cancelar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listagem_pacientes)

        recyclerView = findViewById(R.id.rv_pacientes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cria o adapter com callback para salvar o ID do paciente selecionado
        pacienteAdapter = PacienteAdapter(
            context = this, 
            pacientes = pacientes,
            onPatientSelected = { patient ->
                // Salva o ID e nome do paciente no PacienteManager quando ele é selecionado
                com.ufpr.equilibrium.utils.PacienteManager.uuid = patient.id
                com.ufpr.equilibrium.utils.PacienteManager.nome = patient.fullName
                android.util.Log.d("ListagemPacientes", "Paciente selecionado: ${patient.fullName}, ID: ${patient.id}")
            }
        )
        recyclerView.adapter = pacienteAdapter

        // Adiciona scroll listener para paginação
        setupScrollListener()

        // Carrega a primeira página
        getPacientes(1, isFirstLoad = true)

        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton("Sim") { _, _ ->
            SessionManager.clearSession()
            startActivity(Intent(this@ListagemPacientes, HomeProfissional::class.java))
        }
        builder.setNegativeButton("Não") { _, _ -> }

        val searchInput = findViewById<TextInputEditText>(R.id.searchInput)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val filterText = s.toString()
                currentFilter = filterText
                
                // Primeiro tenta filtrar localmente
                pacienteAdapter.filtrarPorCpf(filterText)
                
                // Se o filtro tem 11 dígitos (CPF completo) e não encontrou localmente, busca no servidor
                val cpfDigits = filterText.filter { it.isDigit() }
                if (cpfDigits.length == 11) {
                    // Verifica se o CPF completo está na lista local (correspondência exata)
                    val foundLocally = pacientes.any { 
                        it.cpf.filter { char -> char.isDigit() } == cpfDigits 
                    }
                    if (!foundLocally && !isSearching) {
                        searchPacienteByCpf(cpfDigits)
                    }
                } else {
                    // Cancela busca anterior se o CPF não está completo
                    searchCall?.cancel()
                    isSearching = false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                intent = Intent(this@ListagemPacientes, HomeProfissional::class.java)
                startActivity(intent)
            }
        })
    }

    private fun setupScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                if (layoutManager != null) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    
                    // Carrega mais itens quando está próximo do final (últimos 5 itens)
                    if (!isLoading && hasMorePages) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                            && firstVisibleItemPosition >= 0) {
                            loadNextPage()
                        }
                    }
                }
            }
        })
    }

    private fun loadNextPage() {
        if (!isLoading && hasMorePages) {
            currentPage++
            getPacientes(currentPage, isFirstLoad = false)
        }
    }

    private fun searchPacienteByCpf(cpf: String) {
        // Cancela busca anterior se houver
        searchCall?.cancel()
        isSearching = true
        
        searchCall = pessoasAPI.getParticipants(cpf = cpf, page = 1, pageSize = 1)
        
        searchCall?.enqueue(object : Callback<PacientesEnvelope> {
            override fun onResponse(
                call: Call<PacientesEnvelope>,
                response: Response<PacientesEnvelope>
            ) {
                isSearching = false
                
                if (response.isSuccessful) {
                    val envelope = response.body()
                    val listaApi = envelope?.data.orEmpty()
                    val lista = listaApi.map { it.toDomain() }
                    
                    if (lista.isNotEmpty()) {
                        // Adiciona o paciente encontrado à lista se ainda não estiver
                        val pacienteEncontrado = lista.first()
                        val jaExiste = pacientes.any { it.cpf.filter { it.isDigit() } == cpf }
                        
                        if (!jaExiste) {
                            pacientes.add(pacienteEncontrado)
                            // Ordena alfabeticamente por nome
                            pacientes.sortBy { it.fullName }
                            pacienteAdapter.atualizarLista(pacientes.toList())
                            pacienteAdapter.filtrarPorCpf(currentFilter)
                        }
                    }
                }
            }
            
            override fun onFailure(call: Call<PacientesEnvelope>, t: Throwable) {
                isSearching = false
                // Silenciosamente falha - não mostra erro para não incomodar o usuário
                // Só loga se não foi cancelado
                val wasCanceled = t.message?.contains("Canceled", ignoreCase = true) == true
                if (!wasCanceled) {
                    Log.d("Busca", "Paciente não encontrado no servidor para CPF: $cpf")
                }
            }
        })
    }

    private fun getPacientes(page: Int, isFirstLoad: Boolean = false) {
        if (isLoading) return
        
        isLoading = true
        
        val call = pessoasAPI.getParticipants(page = page, pageSize = pageSize)

        call.enqueue(object : Callback<PacientesEnvelope> {
            override fun onResponse(
                call: Call<PacientesEnvelope>,
                response: Response<PacientesEnvelope>
            ) {
                isLoading = false
                
                if (response.isSuccessful) {
                    val envelope = response.body()
                    val listaApi = envelope?.data.orEmpty()
                    val lista = listaApi.map { it.toDomain() }
                    
                    // Atualiza informações de paginação
                    val meta = envelope?.meta
                    val lastPage = meta?.lastPage ?: currentPage
                    hasMorePages = currentPage < lastPage

                    if (isFirstLoad) {
                        // Primeira carga: limpa e adiciona
                        pacientes.clear()
                        pacientes.addAll(lista)
                    } else {
                        // Carregamento subsequente: adiciona à lista existente
                        pacientes.addAll(lista)
                    }
                    
                    // Ordena alfabeticamente por nome
                    pacientes.sortBy { it.fullName }
                    
                    // Atualiza o adapter com a lista completa e reaplica o filtro se houver
                    pacienteAdapter.atualizarLista(pacientes.toList())
                    if (currentFilter.isNotEmpty()) {
                        pacienteAdapter.filtrarPorCpf(currentFilter)
                    }
                } else {
                    // Em caso de erro, volta a página anterior
                    if (!isFirstLoad) {
                        currentPage--
                    }

                    Toast.makeText (
                        applicationContext,
                        "Erro ao buscar pacientes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PacientesEnvelope>, t: Throwable) {
                isLoading = false
                // Em caso de falha, volta a página anterior
                if (!isFirstLoad) {
                    currentPage--
                }
                Log.e("Erro", "Falha ao buscar pacientes", t)
                Toast.makeText(
                    applicationContext,
                    "Falha na conexão. Tente novamente.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // --------- MAPPER SIMPLES e SEGURO (sem user) ----------------

    private fun PacienteModelList.toDomain(): com.ufpr.equilibrium.domain.model.Patient = 
        com.ufpr.equilibrium.domain.model.Patient(
            id       = parseUuidOrFromCpf(id, cpf),
            fullName = fullName.orEmpty(),
            cpf      = cpf.orEmpty(),
            phone    = phone.orEmpty(),
            birthDate = birthday.orEmpty(),
            gender   = gender.orEmpty(),
            age      = parseAgeFromIso(birthday),
            education = scholarship,
            socioeconomicLevel = socio_economic_level,
            weight   = weight,
            height   = normalizeHeight(height),
            hasFallHistory = false,  // ajuste quando houver origem real
            address  = if (!zipCode.isNullOrBlank() && !street.isNullOrBlank()) {
                com.ufpr.equilibrium.domain.model.Address(
                    zipCode = zipCode,
                    street = street,
                    number = number?.toIntOrNull() ?: 0,
                    complement = complement,
                    neighborhood = neighborhood.orEmpty(),
                    city = city.orEmpty(),
                    state = state.orEmpty(),
                    stateCode = state.orEmpty()
                )
            } else null
        )

    private fun parseUuidOrFromCpf(idStr: String?, cpf: String?): UUID {
        // tenta o id; se não der, gera UUID determinístico pelo CPF; fallback random
        parseUuid(idStr)?.let { return it }
        if (!cpf.isNullOrBlank()) return UUID.nameUUIDFromBytes(cpf.toByteArray())
        return UUID.randomUUID()
    }

    private fun parseUuid(idStr: String?): UUID? = try {
        if (idStr.isNullOrBlank()) null else UUID.fromString(idStr)
    } catch (_: Exception) { null }

    private fun parseAgeFromIso(iso: String?): Int {
        return try {
            if (iso.isNullOrBlank()) return 0
            // Ex.: "1953-07-15T00:00:00.000Z"
            val instant = Instant.parse(iso)
            val birth = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            Period.between(birth, LocalDate.now()).years
        } catch (_: Exception) {
            // Tenta "yyyy-MM-dd" simples, se vier assim algum dia
            try {
                val birth = LocalDate.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE)
                Period.between(birth, LocalDate.now()).years
            } catch (_: Exception) { 0 }
        }
    }

    private fun normalizeHeight(h: Int?): Int {
        val v = h ?: 0
        // Seus dados mostram 175/172 (cm) e 1 (provável metro).
        // Heurística: >= 100 => já em cm; 1..3 => metros -> cm; senão retorna como está.
        return when {
            v >= 100 -> v
            v in 1..3 -> v * 100
            else -> v
        }
    }
}
