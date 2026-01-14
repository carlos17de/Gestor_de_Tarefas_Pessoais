package pt.ipca.a31482.gestortarefas

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Repositório responsável por todas as operações de dados (Firebase Auth e Firestore)
class TaskRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // Verifica se existe um utilizador autenticado
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // Realiza o login assíncrono com email e password
    suspend fun login(email: String, pass: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Regista um novo utilizador no Firebase Auth
    suspend fun register(email: String, pass: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, pass).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Termina a sessão do utilizador
    fun logout() {
        auth.signOut()
    }

    // Retorna o UID do utilizador atual
    fun getUserId(): String = auth.currentUser?.uid ?: ""

    // Obtém o fluxo de dados em tempo real da coleção de tarefas
    fun getTasks(): Flow<List<Tarefa>> = callbackFlow {
        val userId = getUserId()
        if (userId.isEmpty()) {
            close()
            return@callbackFlow
        }

        // Listener que reage a alterações na base de dados
        val subscription = db.collection("tarefas")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val tarefas = snapshot.documents.mapNotNull { doc ->
                        val tarefa = doc.toObject(Tarefa::class.java)
                        // Mapeamento do ID do documento para o objeto local
                        tarefa?.apply { id = doc.id }
                    }
                    // Ordena por prioridade (Decrescente) antes de enviar para a UI
                    trySend(tarefas.sortedByDescending { it.prioridade })
                }
            }
        awaitClose { subscription.remove() }
    }

    // Adiciona um novo documento à coleção
    fun addTask(titulo: String, prioridade: Int) {
        val userId = getUserId()
        if (userId.isNotEmpty()) {
            val ref = db.collection("tarefas").document()
            val novaTarefa = Tarefa(ref.id, titulo, false, prioridade, userId)
            ref.set(novaTarefa)
        }
    }

    // Atualiza o estado de conclusão da tarefa
    fun toggleTask(tarefa: Tarefa) {
        if (tarefa.id.isNotEmpty()) {
            db.collection("tarefas").document(tarefa.id)
                .update("isConcluida", !tarefa.isConcluida)
        }
    }

    // Remove a tarefa da base de dados
    fun deleteTask(tarefa: Tarefa) {
        if (tarefa.id.isNotEmpty()) {
            db.collection("tarefas").document(tarefa.id).delete()
        }
    }
}