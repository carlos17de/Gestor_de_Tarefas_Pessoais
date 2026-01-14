package pt.ipca.a31482.gestortarefas

data class Tarefa(
    var id: String = "",
    var titulo: String = "",
    var isConcluida: Boolean = false,
    var prioridade: Int = 1, //
    var userId: String = ""
)