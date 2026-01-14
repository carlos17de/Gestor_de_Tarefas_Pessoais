// build.gradle.kts (Project: Gestor_de_Tarefas_Pessoais)
plugins {
    // Usamos os IDs diretos para evitar erros de nomes
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

    // O plugin do Google Services (Firebase)
    id("com.google.gms.google-services") version "4.4.1" apply false
}