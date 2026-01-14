# Relatório de Projeto: Gestor de Tarefas & Produtividade (Cloud)
**Unidade Curricular:** Computação Móvel
**Instituição:** Instituto Politécnico do Cávado e do Ave (IPCA)
**Docente:** Lourenço Gomes
**Data:** 14 de Janeiro de 2026

**Alunos:**
* **Carlos Gomes** (31482)
* **Pedro Leiras** (31479)

---

## 1. Introdução
Este projeto consiste no desenvolvimento de uma aplicação móvel nativa para Android, desenvolvida na linguagem **Kotlin**. O objetivo inicial foi criar uma ferramenta de gestão de tarefas ("To-Do List"), que evoluiu para um **Assistente de Produtividade Pessoal**.

O projeto distingue-se pela utilização de tecnologias modernas (Android Modern Development), nomeadamente a interface declarativa **Jetpack Compose**, arquitetura **MVVM** e persistência de dados na **Cloud (Firebase)**. Além da gestão de tarefas, a aplicação integra ferramentas de foco (Timer Pomodoro) e funcionalidades sociais (Partilha).

## 2. Arquitetura e Padrões de Desenho
A aplicação segue rigorosamente o padrão **MVVM (Model-View-ViewModel)**:

* **Model (Dados):**
  * `Tarefa.kt`: Data class que define a estrutura do objeto, incluindo agora o campo de `prioridade` (Int).
  * `TaskRepository.kt`: Responsável por todas as operações de I/O com o **Firebase Firestore** e **Auth**.
* **ViewModel (Lógica):**
  * `TaskViewModel.kt`: Gere o estado da UI (`StateFlow`), a lógica do temporizador e comunica com o repositório.
* **View (Interface):**
  * `MainActivity.kt`: Interface 100% em **Jetpack Compose**. Implementa navegação por abas (Bottom Navigation) entre a lista de tarefas e o modo foco.

## 3. Tecnologias e Bibliotecas
* **Linguagem:** Kotlin.
* **UI:** Jetpack Compose (Material Design 3) com navegação `Scaffold`.
* **Backend:** Google Firebase (Firestore & Authentication).
* **Concorrência:** Kotlin Coroutines & Flows (para atualizações em tempo real e gestão do Timer).
* **Intents:** Intents Implícitos para partilha de conteúdo.
* **Notificações:** Android Notification Manager (Canais de Alta Prioridade).

## 4. Funcionalidades Implementadas

### A. Gestão de Tarefas (Cloud CRUD & Prioridades)
A aplicação permite um ciclo de vida avançado das tarefas:
* **Priorização:** O utilizador pode classificar tarefas como **Baixa** (Verde), **Média** (Amarela) ou **Alta** (Vermelha).
* **Ordenação Automática:** A lista é ordenada automaticamente, exibindo as tarefas urgentes no topo.
* **Feedback Visual:** As cores dos cartões adaptam-se dinamicamente à prioridade da tarefa.
* **Sincronização:** Utilização de `SnapshotListeners` para atualizações em tempo real.

### B. Modo Foco (Pomodoro Timer)

[Image of Pomodoro Technique Timer]

Foi implementado um módulo de produtividade dedicado:
* Um ecrã separado com um temporizador de 25 minutos.
* Animação circular de progresso (`CircularProgressIndicator`).
* Controlos de **Iniciar**, **Pausar** e **Reset**, geridos através de `LaunchedEffect` para garantir a precisão da contagem decrescente.

### C. Funcionalidades Nativas
* **Notificações "Heads-Up":** Alertas visuais de alta prioridade ao criar tarefas, com gestão de permissões para Android 13+.
* **Partilha Social (Intents):** Capacidade de partilhar o conteúdo de uma tarefa com outras aplicações (WhatsApp, Email, SMS) através de um `Intent.ACTION_SEND`.

### D. Autenticação
Sistema robusto de Login e Registo com validação de campos e persistência de sessão via Firebase Auth.

## 5. Modelo de Dados (NoSQL)
Os dados são armazenados no Firestore na coleção `tarefas`. Estrutura atualizada:

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | String | Identificador único do documento. |
| `titulo` | String | Descrição da tarefa. |
| `isConcluida`| Boolean | Estado da tarefa. |
| `prioridade` | Int | Nível de urgência (1=Baixa, 2=Média, 3=Alta). |
| `userId` | String | ID do utilizador (Foreign Key lógica). |

## 6. Dificuldades e Soluções
1.  **Gestão de Estado do Timer:** Implementar um cronómetro que atualiza a UI a cada segundo sem bloquear a thread principal exigiu o uso correto de `LaunchedEffect` e `delay` do Kotlin Coroutines.
2.  **Permissões Android 13:** A implementação das notificações exigiu o pedido explícito de permissão `POST_NOTIFICATIONS` em tempo de execução.
3.  **UI Dinâmica:** Criar componentes que mudam de cor e ícone baseados na prioridade e estado da tarefa (riscado/não riscado) obrigou a uma gestão cuidada do estado no Jetpack Compose.

## 7. Conclusão
O projeto resultou numa aplicação completa e pronta para uso real. A evolução de uma lista simples para um gestor com **Prioridades** e **Modo Foco** demonstra a capacidade de integrar lógica complexa, design moderno e serviços Cloud numa única solução móvel.