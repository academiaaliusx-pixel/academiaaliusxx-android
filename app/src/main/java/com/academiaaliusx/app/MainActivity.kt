package com.academiaaliusx.app

import android.os.Bundle
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

private val Ink=Color(0xFF10131C); private val Panel=Color(0xFF1A2030); private val Cyan=Color(0xFF42D7F5); private val Muted=Color(0xFF9BA7BC)
class MainActivity:ComponentActivity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState); val prefs=getSharedPreferences("aliusx_local",MODE_PRIVATE); setContent{AAXApp(prefs)} } }
@Composable fun AAXApp(prefs:SharedPreferences){
 var selected by remember { mutableIntStateOf(0) }
 var name by remember { mutableStateOf(PlayerProfileRules.sanitizeName(prefs.getString("name",PlayerProfileRules.DEFAULT_NAME)?:PlayerProfileRules.DEFAULT_NAME)) }
 var games by remember { mutableStateOf(PlayerProfileRules.normalizeGames(prefs.getStringSet("games",setOf("Beyblade X"))?.toSet()?:setOf("Beyblade X"))) }
 var registrations by remember { mutableStateOf(prefs.getStringSet("registrations",emptySet())?.toSet()?:emptySet()) }
 var customEvents by remember { mutableStateOf(LocalTournamentRules.decodeEvents(prefs.getStringSet("custom_events",emptySet())?.toSet()?:emptySet())) }
 val labels=listOf("Inicio","Comunidad","Torneos","Perfil"); val icons=listOf(Icons.Filled.Home,Icons.Filled.Groups,Icons.Filled.EmojiEvents,Icons.Filled.Person)
 MaterialTheme(colorScheme=darkColorScheme(primary=Cyan,background=Ink,surface=Panel)){ Scaffold(containerColor=Ink,bottomBar={NavigationBar(containerColor=Panel){labels.forEachIndexed{ i,l->NavigationBarItem(selected=i==selected,onClick={selected=i},icon={Icon(icons[i],l)},label={Text(l)},colors=NavigationBarItemDefaults.colors(selectedIconColor=Cyan,selectedTextColor=Cyan,indicatorColor=Color(0xFF29394A),unselectedTextColor=Muted))}}}){ p->Column(Modifier.fillMaxSize().padding(p).padding(horizontal=18.dp).padding(top=20.dp).verticalScroll(rememberScrollState())){Header();Spacer(Modifier.height(24.dp));when(selected){0->Home(PlayerProfileRules.displayName(name)){selected=it};1->Community(games);2->Tournaments(registrations,customEvents,{id->val updated=if(id in registrations) registrations-id else registrations+id;registrations=updated;prefs.edit().putStringSet("registrations",updated.toSet()).apply()},{title->val updated=LocalTournamentRules.add(customEvents,"local-"+java.util.UUID.randomUUID().toString(),title);customEvents=updated;prefs.edit().putStringSet("custom_events",LocalTournamentRules.encodeEvents(updated).toSet()).apply()},{id->val updatedEvents=LocalTournamentRules.remove(customEvents,id);customEvents=updatedEvents;prefs.edit().putStringSet("custom_events",LocalTournamentRules.encodeEvents(updatedEvents).toSet()).apply();val updatedRegistrations=registrations-id;registrations=updatedRegistrations;prefs.edit().putStringSet("registrations",updatedRegistrations.toSet()).apply()});else->Profile(name,games,{value->name=PlayerProfileRules.sanitizeName(value);prefs.edit().putString("name",name).apply()},{value->games=PlayerProfileRules.normalizeGames(value);prefs.edit().putStringSet("games",games.toSet()).apply()})};Spacer(Modifier.height(24.dp))}}}}
@Composable private fun Header(){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(44.dp).background(Cyan,RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Text("AX",color=Ink,fontWeight=FontWeight.Black,fontSize=17.sp)};Spacer(Modifier.width(12.dp));Column{Text("ACADEMIA ALIUS X",fontWeight=FontWeight.Black,letterSpacing=1.2.sp);Text("COMUNIDAD · JUEGO · COMPETICIÓN",color=Muted,fontSize=10.sp,letterSpacing=.5.sp)}}}
@Composable private fun Home(name:String,navigate:(Int)->Unit){Text("TU UNIVERSO GAMER",color=Cyan,fontSize=12.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp);Spacer(Modifier.height(8.dp));Text("¡Bienvenido, $name!",fontSize=30.sp,fontWeight=FontWeight.Black,lineHeight=36.sp);Spacer(Modifier.height(10.dp));Text("Todo tu mundo competitivo, en un solo lugar.",color=Muted);Spacer(Modifier.height(24.dp));HomeCard("Comunidad","Jugadores y juegos",Icons.Filled.Groups){navigate(1)};HomeCard("Torneos","Competiciones e inscripciones",Icons.Filled.EmojiEvents){navigate(2)};HomeCard("Tu perfil","Personaliza nombre y juegos",Icons.Filled.Person){navigate(3)};HomeCard("Juegos","Beyblade X · Pokémon · Inazuma Eleven",Icons.Filled.SportsEsports){navigate(1)};Text("MVP NATIVO · DATOS LOCALES",color=Muted,fontSize=10.sp,letterSpacing=1.sp)}
@Composable private fun HomeCard(title:String,subtitle:String,icon:androidx.compose.ui.graphics.vector.ImageVector,click:()->Unit){Card(onClick=click,modifier=Modifier.fillMaxWidth().padding(bottom=12.dp),colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(18.dp)){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(44.dp).background(Color(0xFF243445),RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Icon(icon,null,tint=Cyan)};Spacer(Modifier.width(14.dp));Column{Text(title,fontWeight=FontWeight.Bold,fontSize=16.sp);Text(subtitle,color=Muted,fontSize=12.sp)};Spacer(Modifier.weight(1f));Icon(Icons.Filled.ChevronRight,null,tint=Muted)}}}
@Composable private fun SectionTitle(title:String,subtitle:String){Text(title,fontSize=29.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(6.dp));Text(subtitle,color=Cyan);Spacer(Modifier.height(18.dp))}
@Composable private fun Community(games:Set<String>){SectionTitle("Comunidad","Jugadores y juegos");Text("Vista de muestra: aún no consulta jugadores reales.",color=Muted,fontSize=12.sp);Spacer(Modifier.height(12.dp));PlayerProfileRules.availableGames.forEach{g->Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().padding(bottom=10.dp)){Column(Modifier.padding(16.dp)){Text(g,fontWeight=FontWeight.Bold,fontSize=17.sp,color=Cyan);Text(if(g in games)"Seleccionado en tu perfil" else "Explora este espacio",color=Muted,fontSize=12.sp);Text("La búsqueda online y los perfiles se incorporarán con el backend.",fontSize=12.sp)}}}}
@Composable private fun Tournaments(regs:Set<String>,custom:List<LocalTournament>,toggle:(String)->Unit,add:(String)->Unit,remove:(String)->Unit){
 SectionTitle("Torneos","Competiciones")
 Text("Eventos de demostración y torneos creados en este dispositivo.",color=Muted,fontSize=12.sp)
 Spacer(Modifier.height(12.dp))
 var title by remember { mutableStateOf("") }
 var search by remember { mutableStateOf("") }
 var filter by remember { mutableStateOf("all") }
 Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().padding(bottom=16.dp)){
  Column(Modifier.padding(16.dp)){
   Text("Crear torneo local",fontWeight=FontWeight.Bold,fontSize=18.sp,color=Cyan)
   Spacer(Modifier.height(8.dp))
   OutlinedTextField(value=title,onValueChange={title=LocalTournamentRules.cleanTitleInput(it)},label={Text("Nombre del torneo")},placeholder={Text("Ej. Liga del sábado")},modifier=Modifier.fillMaxWidth(),singleLine=true,colors=OutlinedTextFieldDefaults.colors(focusedTextColor=Color.White,unfocusedTextColor=Color.White,focusedBorderColor=Cyan,focusedLabelColor=Cyan))
   Spacer(Modifier.height(10.dp))
   Button(onClick={add(title);title=""},enabled=LocalTournamentRules.validTitle(title),modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Cyan,contentColor=Ink)){Text("Crear evento")}
   Text("Solo se guarda en este móvil; aún no se publica online.",color=Muted,fontSize=11.sp)
  }
 }
 OutlinedTextField(value=search,onValueChange={search=it},label={Text("Buscar torneo")},modifier=Modifier.fillMaxWidth(),singleLine=true,leadingIcon={Icon(Icons.Filled.Search,null)},colors=OutlinedTextFieldDefaults.colors(focusedTextColor=Color.White,unfocusedTextColor=Color.White,focusedBorderColor=Cyan,focusedLabelColor=Cyan))
 Spacer(Modifier.height(10.dp))
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
  FilterChip(selected=filter=="all",onClick={filter="all"},label={Text("Todos")})
  FilterChip(selected=filter=="registered",onClick={filter="registered"},label={Text("Inscritos")})
  FilterChip(selected=filter=="created",onClick={filter="created"},label={Text("Creados")})
 }
 Spacer(Modifier.height(12.dp))
 val builtInEvents=listOf(LocalTournament("bx-local","Beyblade X · Arena abierta"),LocalTournament("pokemon-local","Pokémon · Liga amistosa"),LocalTournament("ie-local","Inazuma Eleven · Copa"))
 val createdIds=custom.map{it.id}.toSet()
 val visible=LocalTournamentRules.visibleEvents(builtInEvents+custom,regs,createdIds,search,filter)
 visible.forEach{event->
  val isCreated=event.id in createdIds
  TournamentCard(event.id,event.title,if(isCreated)"Evento local · solo en este dispositivo" else "Evento de demostración",event.id in regs,onDelete=if(isCreated)({remove(event.id)})else null){toggle(event.id)}
 }
 if(visible.isEmpty()){Text("No hay torneos que coincidan con la búsqueda o el filtro.",color=Muted,fontSize=12.sp)}
}
@Composable private fun TournamentCard(id:String,title:String,subtitle:String,registered:Boolean,onDelete:(()->Unit)?=null,onToggle:()->Unit){
 Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().padding(bottom=12.dp)){
  Column(Modifier.padding(16.dp)){
   Text(title,fontWeight=FontWeight.Bold,fontSize=17.sp)
   Spacer(Modifier.height(5.dp))
   Text(subtitle,color=Muted,fontSize=12.sp)
   Spacer(Modifier.height(12.dp))
   Row(verticalAlignment=Alignment.CenterVertically){Button(onClick=onToggle,colors=ButtonDefaults.buttonColors(containerColor=if(registered)Color(0xFF334455)else Cyan,contentColor=if(registered)Color.White else Ink)){Text(if(registered)"Cancelar inscripción local"else"Apuntarme (local)")};if(onDelete!=null){Spacer(Modifier.width(8.dp));TextButton(onClick=onDelete){Icon(Icons.Filled.Delete,null);Text("Eliminar")}}}
  }
 }
}
@Composable private fun Profile(name:String,games:Set<String>,onName:(String)->Unit,onGames:(Set<String>)->Unit){SectionTitle("Perfil","Tu identidad en Alius X");OutlinedTextField(value=name,onValueChange=onName,label={Text("Nombre de jugador")},modifier=Modifier.fillMaxWidth(),singleLine=true,colors=OutlinedTextFieldDefaults.colors(focusedTextColor=Color.White,unfocusedTextColor=Color.White,focusedBorderColor=Cyan,focusedLabelColor=Cyan));Spacer(Modifier.height(20.dp));Text("Juegos que practicas",fontWeight=FontWeight.Bold,fontSize=18.sp);PlayerProfileRules.availableGames.forEach{g->Row(Modifier.fillMaxWidth().padding(vertical=6.dp),verticalAlignment=Alignment.CenterVertically){Checkbox(checked=g in games,onCheckedChange={c->onGames(PlayerProfileRules.updateGame(games,g,c))},colors=CheckboxDefaults.colors(checkedColor=Cyan));Text(g)}};Spacer(Modifier.height(12.dp));Text("Se guarda localmente. Sin cuenta ni sincronización todavía.",color=Muted,fontSize=12.sp)}
