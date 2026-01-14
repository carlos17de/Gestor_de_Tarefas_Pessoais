package pt.ipca.a31482.gestortarefas

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel responsável pela lógica de negócio e gestão de estado da UI
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository()
    private val context = application.applicationContext

    // Estados observáveis pelo Jetpack Compose
    var isLoggedIn by mutableStateOf(repository.isUserLoggedIn())
    var loginError by mutableStateOf("")

    // StateFlow para manter a lista de tarefas reativa e atualizada
    private val _tarefas = MutableStateFlow<List<Tarefa>>(emptyList())
    val tarefas: StateFlow<List<Tarefa>> = _tarefas

    init {
        // Se o utilizador já estiver logado ao abrir a app, inicia a escuta de dados
        if (isLoggedIn) {
            startListeningToTasks()
        }
    }

    // Subscreve o fluxo de dados do repositório (Firestore)
    private fun startListeningToTasks() {
        viewModelScope.launch {
            repository.getTasks().collect { lista ->
                _tarefas.value = lista
            }
        }
    }

    // Lógica de Login
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            if (repository.login(email, pass)) {
                isLoggedIn = true
                loginError = ""
                startListeningToTasks()
            } else {
                loginError = "Erro no login."
            }
        }
    }

    // Lógica de Registo
    fun register(email: String, pass: String) {
        viewModelScope.launch {
            if (email.isNotBlank() && pass.isNotBlank()) {
                if (repository.register(email, pass)) {
                    isLoggedIn = true
                    startListeningToTasks()
                } else {
                    loginError = "Erro ao registar."
                }
            }
        }
    }

    // Terminar Sessão e limpar estado local
    fun logout() {
        repository.logout()
        isLoggedIn = false
        _tarefas.value = emptyList()
    }

    // Adiciona tarefa e dispara notificação local
    fun adicionarTarefa(titulo: String, prioridade: Int) {
        repository.addTask(titulo, prioridade)

        // Feedback imediato ao utilizador via notificação Heads-up
        NotificationUtils.showNotification(context, "Nova Tarefa", "Prioridade: $prioridade - $titulo")
    }

    // Alterna o estado de conclusão da tarefa
    fun alternarEstado(tarefa: Tarefa) {
        repository.toggleTask(tarefa)
    }

    // Remove a tarefa permanentemente
    fun apagarTarefa(tarefa: Tarefa) {
        repository.deleteTask(tarefa)
    }
}