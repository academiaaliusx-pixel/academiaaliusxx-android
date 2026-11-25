package com.academiaaliusx.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF10131C)
private val Panel = Color(0xFF1A2030)
private val Cyan = Color(0xFF42D7F5)
private val Muted = Color(0xFF9BA7BC)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { AAXApp() } }
}

@Composable fun AAXApp() {
    var selected by remember { mutableStateOf(0) }
    val labels = listOf("Inicio", "Comunidad", "Torneos", "Perfil")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Groups, Icons.Filled.EmojiEvents, Icons.Filled.Person)
    MaterialTheme(colorScheme = darkColorScheme(primary=Cyan, background=Ink, surface=Panel)) {
        Scaffold(containerColor=Ink, bottomBar={
            NavigationBar(containerColor=Panel) {
                labels.forEachIndexed { index, label -> NavigationBarItem(selected=index==selected, onClick={selected=index}, icon={Icon(icons[index],label)}, label={Text(label)}, colors=NavigationBarItemDefaults.colors(selectedIconColor=Cyan, selectedTextColor=Cyan, indicatorColor=Color(0xFF29394A), unselectedTextColor=Muted)) }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=20.dp).padding(top=24.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).background(Cyan, RoundedCornerShape(14.dp)), contentAlignment=Alignment.Center) { Text("AX", color=Ink, fontWeight=FontWeight.Black, fontSize=17.sp) }
                    Spacer(Modifier.width(12.dp)); Column { Text("ACADEMIA ALIUS X", fontWeight=FontWeight.Black, letterSpacing=1.2.sp); Text("COMUNIDAD · JUEGO · COMPETICIÓN", color=Muted, fontSize=10.sp, letterSpacing=.5.sp) }
                }
                Spacer(Modifier.height(28.dp))
                when(selected) {
                    0 -> HomeScreen()
                    1 -> SectionScreen("Comunidad", "Encuentra jugadores, conecta y crea tu círculo.", "La comunidad será el punto de encuentro de jugadores de Academia Alius X.")
                    2 -> SectionScreen("Torneos", "Competiciones, clasificaciones y resultados.", "Aquí reuniremos el calendario, las inscripciones y el seguimiento de torneos.")
                    else -> SectionScreen("Perfil", "Tu identidad dentro de Academia Alius X.", "El perfil nativo incluirá tus datos, juegos, equipo y actividad.")
                }
            }
        }
    }
}

@Composable private fun HomeScreen() {
    Text("TU UNIVERSO GAMER", color=Cyan, fontSize=12.sp, fontWeight=FontWeight.Bold, letterSpacing=1.5.sp)
    Spacer(Modifier.height(8.dp)); Text("Bienvenido a\nAcademia Alius X", fontSize=32.sp, fontWeight=FontWeight.Black, lineHeight=36.sp)
    Spacer(Modifier.height(10.dp)); Text("Todo tu mundo competitivo, en un solo lugar.", color=Muted)
    Spacer(Modifier.height(24.dp))
    FeatureCard("Comunidad", "Jugadores, amistades y equipos", Icons.Filled.Groups)
    FeatureCard("Torneos", "Competiciones y resultados", Icons.Filled.EmojiEvents)
    FeatureCard("Tus juegos", "Beyblade X · Pokémon · Inazuma Eleven", Icons.Filled.SportsEsports)
    FeatureCard("Mensajes", "Conecta con otros miembros", Icons.Filled.Chat)
    Spacer(Modifier.height(18.dp)); Text("PRIMERA VERSIÓN NATIVA", color=Muted, fontSize=11.sp, letterSpacing=1.sp)
}

@Composable private fun FeatureCard(title:String, subtitle:String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(Modifier.fillMaxWidth().padding(bottom=12.dp), colors=CardDefaults.cardColors(containerColor=Panel), shape=RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(Color(0xFF243445),RoundedCornerShape(14.dp)), contentAlignment=Alignment.Center){ Icon(icon,null,tint=Cyan) }
            Spacer(Modifier.width(14.dp)); Column { Text(title,fontWeight=FontWeight.Bold,fontSize=16.sp); Text(subtitle,color=Muted,fontSize=12.sp) }
        }
    }
}

@Composable private fun SectionScreen(title:String, subtitle:String, description:String) {
    Text(title,fontSize=30.sp,fontWeight=FontWeight.Black); Spacer(Modifier.height(8.dp)); Text(subtitle,color=Cyan)
    Spacer(Modifier.height(22.dp)); Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(20.dp)) { Column(Modifier.padding(20.dp)) { Text(description,color=Muted); Spacer(Modifier.height(12.dp)); Text("En construcción",color=Cyan,fontWeight=FontWeight.Bold) } }
}
