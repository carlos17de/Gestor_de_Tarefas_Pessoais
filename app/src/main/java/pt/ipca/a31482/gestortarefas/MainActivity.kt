package pt.ipca.a31482.gestortarefas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

// Definição de cores da aplicação
val BluePrimary = Color(0xFF1565C0)
val BlueDark = Color(0xFF0D47A1)
val WhiteCard = Color(0xFFFFFFFF)
val BackgroundGrey = Color(0xFFF0F2F5)
val TimerColor = Color(0xFFE53935)

// Cores associadas à prioridade das tarefas
val PriorityHigh = Color(0xFFFFEBEE)
val PriorityMedium = Color(0xFFFFF8E1)
val PriorityLow = Color(0xFFE8F5E9)
val PriorityHighStroke = Color(0xFFEF5350)
val PriorityMediumStroke = Color(0xFFFFCA28)
val PriorityLowStroke = Color(0xFF66BB6A)

// Activity principal
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(primary = BluePrimary, background = BackgroundGrey)
            ) {
                AppNavigation()
            }
        }
    }
}

// Navegação entre Ecrã de Login e Aplicação Principal
@Composable
fun AppNavigation() {
    val viewModel: TaskViewModel = viewModel()
    Crossfade(targetState = viewModel.isLoggedIn, label = "LoginSwitch") { loggedIn ->
        if (loggedIn) EcraPrincipal(viewModel) else EcraAutenticacao(viewModel)
    }
}

// Estrutura principal com Menu Inferior
@Composable
fun EcraPrincipal(viewModel: TaskViewModel) {
    var ecrAtual by remember { mutableIntStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = WhiteCard) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Tarefas") },
                    selected = ecrAtual == 0,
                    onClick = { ecrAtual = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                    label = { Text("Modo Foco") },
                    selected = ecrAtual == 1,
                    onClick = { ecrAtual = 1 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (ecrAtual == 0) EcraTarefas(viewModel) else EcraFoco()
        }
    }
}

// Ecrã de Listagem e Criação de Tarefas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcraTarefas(viewModel: TaskViewModel) {
    val listaTarefas by viewModel.tarefas.collectAsState()
    var textoNovaTarefa by remember { mutableStateOf("") }
    var prioridadeSel by remember { mutableIntStateOf(1) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {}

    // Pedir permissões de notificação no Android 13+
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("As Minhas Tarefas", color = Color.White) },
                actions = { IconButton(onClick = { viewModel.logout() }) { Icon(Icons.Default.ExitToApp, "Sair", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BluePrimary)
            )
        },
        containerColor = BackgroundGrey
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxSize()) {

            // Área de criação de nova tarefa
            Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = WhiteCard)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = textoNovaTarefa, onValueChange = { textoNovaTarefa = it }, placeholder = { Text("Nova tarefa...") },
                            modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent)
                        )
                        FloatingActionButton(
                            onClick = {
                                if (textoNovaTarefa.isNotBlank()) {
                                    viewModel.adicionarTarefa(textoNovaTarefa, prioridadeSel)
                                    textoNovaTarefa = ""
                                    prioridadeSel = 1
                                }
                            },
                            containerColor = BluePrimary, contentColor = Color.White, modifier = Modifier.size(48.dp)
                        ) { Icon(Icons.Default.Add, "Add") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Prioridade:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        SeletorPrioridade(cor = PriorityLowStroke, texto = "Baixa", selecionado = prioridadeSel == 1) { prioridadeSel = 1 }
                        Spacer(modifier = Modifier.width(8.dp))
                        SeletorPrioridade(cor = PriorityMediumStroke, texto = "Média", selecionado = prioridadeSel == 2) { prioridadeSel = 2 }
                        Spacer(modifier = Modifier.width(8.dp))
                        SeletorPrioridade(cor = PriorityHighStroke, texto = "Alta", selecionado = prioridadeSel == 3) { prioridadeSel = 3 }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Lista de tarefas
            LazyColumn {
                items(listaTarefas) { tarefa ->
                    val (corFundo, corBorda) = when(tarefa.prioridade) {
                        3 -> Pair(PriorityHigh, PriorityHighStroke)
                        2 -> Pair(PriorityMedium, PriorityMediumStroke)
                        else -> Pair(PriorityLow, PriorityLowStroke)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, if(tarefa.isConcluida) Color.Transparent else corBorda, RoundedCornerShape(12.dp))
                            .clickable { viewModel.alternarEstado(tarefa) },
                        colors = CardDefaults.cardColors(containerColor = if (tarefa.isConcluida) Color(0xFFE0E0E0) else corFundo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

                            // Indicador visual de estado
                            Icon(
                                imageVector = if (tarefa.isConcluida) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (tarefa.isConcluida) Color.Green else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )

                            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(
                                    text = tarefa.titulo,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textDecoration = if(tarefa.isConcluida) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                    color = if(tarefa.isConcluida) Color.Gray else Color.Black
                                )
                                if (!tarefa.isConcluida) {
                                    Text(
                                        text = when(tarefa.prioridade) { 3->"URGENTE"; 2->"MÉDIA"; else->"NORMAL" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if(tarefa.prioridade==3) Color.Red else Color.Gray
                                    )
                                }
                            }

                            // Partilhar tarefa
                            IconButton(onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Tarefa: ${tarefa.titulo}")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            }) {
                                Icon(Icons.Outlined.Share, "Partilhar", tint = BluePrimary)
                            }

                            // Apagar tarefa
                            IconButton(onClick = { viewModel.apagarTarefa(tarefa) }) {
                                Icon(Icons.Default.Delete, "Apagar", tint = Color(0xFFE57373))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Ecrã do Temporizador (Modo Foco)
@Composable
fun EcraFoco() {
    var tempoRestante by remember { mutableLongStateOf(25 * 60L) }
    var aCorrer by remember { mutableStateOf(false) }

    LaunchedEffect(aCorrer, tempoRestante) {
        if (aCorrer && tempoRestante > 0) { delay(1000L); tempoRestante-- } else if (tempoRestante == 0L) aCorrer = false
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGrey), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Modo Foco", style = MaterialTheme.typography.headlineMedium, color = BlueDark, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp).clip(CircleShape).background(WhiteCard)) {
            CircularProgressIndicator(progress = { tempoRestante / (25 * 60f) }, modifier = Modifier.size(250.dp), color = TimerColor, strokeWidth = 8.dp)
            val min = tempoRestante / 60; val seg = tempoRestante % 60
            Text(text = String.format("%02d:%02d", min, seg), style = MaterialTheme.typography.displayLarge, color = BlueDark, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row {
            Button(onClick = { aCorrer = !aCorrer }, colors = ButtonDefaults.buttonColors(containerColor = if (aCorrer) Color.Gray else BluePrimary), modifier = Modifier.height(50.dp)) {
                Icon(if (aCorrer) Icons.Default.Pause else Icons.Default.PlayArrow, null); Spacer(modifier = Modifier.width(8.dp)); Text(if (aCorrer) "Pausar" else "Iniciar")
            }
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedButton(onClick = { aCorrer = false; tempoRestante = 25 * 60L }, modifier = Modifier.height(50.dp)) { Text("Reset") }
        }
    }
}

// Componente para seleção de prioridade
@Composable
fun SeletorPrioridade(cor: Color, texto: String, selecionado: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selecionado, onClick = onClick, label = { Text(texto) }, leadingIcon = { Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(cor)) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = cor.copy(alpha = 0.2f), selectedLabelColor = Color.Black))
}

// Gestão de Ecrãs de Autenticação
@Composable
fun EcraAutenticacao(viewModel: TaskViewModel) {
    var isRegistering by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BlueDark, BluePrimary))), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.padding(24.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WhiteCard)) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = if (isRegistering) Icons.Filled.PersonAdd else Icons.Filled.Lock, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = if (isRegistering) "Criar Conta" else "Bem-vindo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = BlueDark)
                Spacer(modifier = Modifier.height(24.dp))
                if (isRegistering) FormularioRegisto(viewModel) { isRegistering = false } else FormularioLogin(viewModel) { isRegistering = true }
            }
        }
    }
}

// Formulário de Login
@Composable
fun FormularioLogin(viewModel: TaskViewModel, onGoToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    InputBonito(email, { email = it }, "Email", Icons.Outlined.Person)
    Spacer(modifier = Modifier.height(16.dp))
    InputBonito(pass, { pass = it }, "Password", Icons.Outlined.Lock, isPassword = true)
    if (viewModel.loginError.isNotBlank()) { Spacer(modifier = Modifier.height(8.dp)); Text(viewModel.loginError, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = { viewModel.login(email, pass) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)) { Text("ENTRAR", fontSize = 16.sp) }
    Spacer(modifier = Modifier.height(16.dp))
    TextButton(onClick = onGoToRegister) { Text("Não tens conta? Regista-te", color = BlueDark) }
}

// Formulário de Registo
@Composable
fun FormularioRegisto(viewModel: TaskViewModel, onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    InputBonito(email, { email = it }, "Novo Email", Icons.Outlined.Person)
    Spacer(modifier = Modifier.height(16.dp))
    InputBonito(pass, { pass = it }, "Nova Password", Icons.Outlined.Lock, isPassword = true)
    if (viewModel.loginError.isNotBlank()) { Spacer(modifier = Modifier.height(8.dp)); Text(viewModel.loginError, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = { viewModel.register(email, pass) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = BlueDark)) { Text("CRIAR CONTA", fontSize = 16.sp) }
    Spacer(modifier = Modifier.height(16.dp))
    TextButton(onClick = onBack) { Text("Voltar ao Login", color = BluePrimary) }
}

// Componente de Input personalizado
@Composable
fun InputBonito(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isPassword: Boolean = false) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, leadingIcon = { Icon(icon, contentDescription = null, tint = BluePrimary) }, visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
}