package pt.ipca.a31482.gestortarefas

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlin.random.Random

object NotificationUtils {


    private const val CHANNEL_ID = "canal_prioridade_maxima_v2"
    private const val CHANNEL_NAME = "Notificações Urgentes"

    fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Configurar o Canal para ALTA IMPORTÂNCIA (Heads-up)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH //
            ).apply {
                description = "Canal para alertas de tarefas"
                enableVibration(true)
                enableLights(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500) // Vibra: Zzz... Zzz...
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Verificar Permissões
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return // Se não tiver permissão, sai sem fazer nada
            }
        }

        // 3. Criar Intent para abrir a App ao clicar na notificação
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Construir a Notificação
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)   // Som + Luzes
            .setVibrate(longArrayOf(0, 500, 200, 500))     // Forçar vibração
            .setContentIntent(pendingIntent)               // Abre a app ao clicar
            .setAutoCancel(true)

        notificationManager.notify(Random.nextInt(), builder.build())
    }
}