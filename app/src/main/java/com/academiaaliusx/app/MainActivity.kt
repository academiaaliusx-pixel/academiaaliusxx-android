package com.academiaaliusx.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONArray

private val Ink=Color(0xFF07151D); private val Panel=Color(0xFF102630); private val Cyan=Color(0xFF20C5C8); private val Muted=Color(0xFF91AAB2); private val Edge=Color(0xFF28505C); private val TextMain=Color(0xFFEAF3F5)
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{AAXNativeApp()}}}

@Composable private fun AAXNativeApp(){
 val scope=rememberCoroutineScope()
 var user by remember { mutableStateOf<JSONObject?>(null) }
 var page by remember { mutableIntStateOf(0) }
 var authMode by remember { mutableStateOf(false) }
 var registerMode by remember { mutableStateOf(false) }
 var nick by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }
 var busy by remember { mutableStateOf(false) }; var notice by remember { mutableStateOf("") }
 var tournaments by remember { mutableStateOf(listOf<JSONObject>()) }
 var profile by remember { mutableStateOf<JSONObject?>(null) }
 fun reload(){
   scope.launch { busy=true; try {
     val me=AaxApiClient.me(); user=me.body.optJSONObject("user")
     if(user!=null){authMode=false;val t=AaxApiClient.tournaments();val arr=t.body.optJSONArray("items")?:JSONArray(); tournaments=(0 until arr.length()).mapNotNull{arr.optJSONObject(it)}; profile=null}
     else {authMode=true}
   }catch(e:Exception){notice="Error de conexión: "+(e.message?: "desconocido")}finally{busy=false} }
 }
 LaunchedEffect(Unit){reload()}
 MaterialTheme(colorScheme=darkColorScheme(primary=Cyan,background=Ink,surface=Panel,onBackground=TextMain,onSurface=TextMain,secondary=Color(0xFF438B98))){Scaffold(containerColor=Ink,bottomBar={if(user!=null)NavigationBar(containerColor=Ink, tonalElevation=0.dp){listOf("Inicio","Juegos","Torneos","Ranking","Perfil","Comunidad","Ajustes","Organización").forEachIndexed{idx,label->NavigationBarItem(colors=NavigationBarItemDefaults.colors(selectedIconColor=Ink,selectedTextColor=Cyan,indicatorColor=Cyan,unselectedIconColor=Muted,unselectedTextColor=Muted),selected=page==idx,onClick={page=idx;if(idx==4)scope.launch{busy=true;try{profile=AaxApiClient.profile(user?.optString("nick")?:"").body}finally{busy=false}}},icon={Icon(listOf(Icons.Filled.Home,Icons.Filled.SportsEsports,Icons.Filled.EmojiEvents,Icons.Filled.Leaderboard,Icons.Filled.Person,Icons.Filled.People,Icons.Filled.Settings,Icons.Filled.AdminPanelSettings)[idx],label)},label={Text(label)})}}}){pad->
 Column(Modifier.fillMaxSize().padding(pad).background(Ink).padding(horizontal=20.dp,vertical=16.dp)){
 Header()
 Spacer(Modifier.height(20.dp))
 if(user==null) AuthScreen(registerMode,nick,email,password,{nick=it},{email=it},{password=it},{registerMode=it},busy,notice,{notice=""},{ 
   scope.launch{busy=true;notice="";try{val r=if(registerMode)AaxApiClient.register(nick,email,password) else AaxApiClient.login(nick,password);if(r.ok){notice=if(registerMode&&r.body.optBoolean("verification_required"))"Cuenta creada. Comprueba el correo para verificarla." else "Acceso correcto.";reload()}else notice=r.body.optString("error","No se pudo completar la operación.")}catch(e:Exception){notice=e.message?: "Error de conexión"}finally{busy=false}}
 })
 else when(page){
  0->HomeScreen(user!!,tournaments.size,tournaments,{page=2},{page=4})
  1->GamesScreen(tournaments)
  2->TournamentScreen(tournaments,user!!,busy,notice,{notice=it},{id,joined->scope.launch{busy=true;try{val r=if(joined)AaxApiClient.leaveTournament(id)else AaxApiClient.joinTournament(id);if(r.ok){notice=if(joined)"Inscripción cancelada." else "Inscripción confirmada.";reload()}else notice=r.body.optString("error","Error")}catch(e:Exception){notice=e.message?: "Error"}finally{busy=false}}})
  3->RankingsScreen(tournaments)
  4->ProfileScreen(user!!,profile,busy,notice,{scope.launch{busy=true;try{profile=AaxApiClient.profile(user?.optString("nick")?:"").body}catch(e:Exception){notice=e.message?: "Error"}finally{busy=false}}},{scope.launch{busy=true;try{val r=AaxApiClient.logout();if(r.ok){user=null;profile=null;authMode=true;page=0;notice="Sesión cerrada."}else notice="No se pudo cerrar sesión"}finally{busy=false}}})
  5->CommunityScreen()
  6->SettingsScreen(user!!,{scope.launch{busy=true;try{val r=AaxApiClient.logout();if(r.ok){user=null;profile=null;page=0;notice="Sesión cerrada."}}finally{busy=false}}})
  else->OrganizationScreen(user!!)
 }
 if(busy)LinearProgressIndicator(Modifier.fillMaxWidth(),color=Cyan)
 if(notice.isNotBlank())Text(notice,color=if(notice.contains("Error")||notice.contains("No se pudo"))Color(0xFFFFB86C)else Cyan,modifier=Modifier.padding(top=12.dp))
 }
}}}
@Composable private fun Header(){Row(verticalAlignment=Alignment.CenterVertically){Text("AX",color=Ink,fontWeight=FontWeight.Black,fontSize=18.sp,modifier=Modifier.padding(12.dp).background(Cyan, androidx.compose.foundation.shape.CutCornerShape(8.dp)));Column{Text("ACADEMIA ALIUS X",fontWeight=FontWeight.Black,letterSpacing=2.sp);Text("COMUNIDAD · JUEGO · COMPETICIÓN",color=Muted,fontSize=10.sp)}}}
@Composable private fun AuthScreen(reg:Boolean,nick:String,email:String,password:String,onNick:(String)->Unit,onEmail:(String)->Unit,onPassword:(String)->Unit,onMode:(Boolean)->Unit,busy:Boolean,notice:String,clear:()->Unit,submit:()->Unit){
 Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally){Text(if(reg)"Crear cuenta" else "Acceder a Alius X",fontSize=28.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(16.dp));OutlinedTextField(nick,onNick,label={Text(if(reg)"Nick" else "Usuario o correo")},singleLine=true,colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=Cyan,unfocusedBorderColor=Edge,cursorColor=Cyan),modifier=Modifier.fillMaxWidth());if(reg){Spacer(Modifier.height(8.dp));OutlinedTextField(email,onEmail,label={Text("Correo electrónico")},singleLine=true,modifier=Modifier.fillMaxWidth())};Spacer(Modifier.height(8.dp));OutlinedTextField(password,onPassword,label={Text("Contraseña")},singleLine=true,modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(16.dp));Button(onClick=submit,shape=androidx.compose.foundation.shape.CutCornerShape(10.dp),colors=ButtonDefaults.buttonColors(containerColor=Cyan,contentColor=Ink),enabled=!busy&&nick.isNotBlank()&&password.length>=8&&( !reg||email.isNotBlank()),modifier=Modifier.fillMaxWidth().height(52.dp)){Text(if(reg)"Registrarme" else "Iniciar sesión")};TextButton(onClick={onMode(!reg);clear()}){Text(if(reg)"Ya tengo cuenta · Entrar" else "Crear una cuenta")};Text("Contraseña: mínimo 8 caracteres",color=Muted,fontSize=12.sp)}
}
@Composable private fun HomeScreen(user:JSONObject,count:Int,tournaments:List<JSONObject>,toTournaments:()->Unit,toProfile:()->Unit){
 Column(Modifier.verticalScroll(rememberScrollState())){Text("▰  TU UNIVERSO GAMER",color=Cyan,fontSize=13.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp);Spacer(Modifier.height(8.dp));Text("¡Bienvenido, "+user.optString("nick")+"!",fontSize=30.sp,fontWeight=FontWeight.Black,lineHeight=34.sp);Spacer(Modifier.height(10.dp));Text("Ahora conectado a Academia Alius X.",color=Muted,fontSize=17.sp);Spacer(Modifier.height(20.dp));Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=androidx.compose.foundation.shape.CutCornerShape(14.dp),border=androidx.compose.foundation.BorderStroke(1.dp,Edge)){Column(Modifier.padding(18.dp)){Text(count.toString(),fontSize=32.sp,color=Cyan,fontWeight=FontWeight.Black);Text("Torneos disponibles/registrados en el servidor")}};Spacer(Modifier.height(12.dp));Button(onClick=toTournaments,shape=androidx.compose.foundation.shape.CutCornerShape(12.dp),colors=ButtonDefaults.buttonColors(containerColor=Cyan,contentColor=Ink),modifier=Modifier.fillMaxWidth().height(54.dp)){Icon(Icons.Filled.EmojiEvents,null);Spacer(Modifier.width(8.dp));Text("Explorar torneos online")};Spacer(Modifier.height(8.dp));OutlinedButton(onClick=toProfile,shape=androidx.compose.foundation.shape.CutCornerShape(12.dp),border=androidx.compose.foundation.BorderStroke(1.dp,Edge),modifier=Modifier.fillMaxWidth().height(52.dp)){Icon(Icons.Filled.Person,null);Spacer(Modifier.width(8.dp));Text("Ver mi perfil")}}
}
@Composable private fun TournamentScreen(items:List<JSONObject>,user:JSONObject,busy:Boolean,notice:String,onNotice:(String)->Unit,onAction:(String,Boolean)->Unit){
 Column(Modifier.fillMaxSize()){Text("TORNEOS ONLINE",fontSize=28.sp,fontWeight=FontWeight.Black,letterSpacing=1.5.sp);Text("Datos consultados desde el servidor",color=Cyan);Spacer(Modifier.height(12.dp));if(items.isEmpty())Text("No hay torneos disponibles o la respuesta está vacía.",color=Muted)
 LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp)){items(items){t->val id=t.optString("id");val title=t.optString("name",t.optString("title","Torneo"));val joined=t.optInt("joined",0)>0;val status=t.optString("status");Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=androidx.compose.foundation.shape.CutCornerShape(14.dp),border=androidx.compose.foundation.BorderStroke(1.dp,Edge)){Column(Modifier.padding(16.dp)){Text(title,fontWeight=FontWeight.Bold,fontSize=18.sp);Spacer(Modifier.height(4.dp));Text(listOf(t.optString("game"),t.optString("date"),status).filter{it.isNotBlank()}.joinToString(" · "),color=Muted,fontSize=12.sp);Spacer(Modifier.height(6.dp));Text("Participantes: "+t.optString("n","?")+(if(t.has("max_players"))"/"+t.optString("max_players")else""),color=Muted);Button(onClick={onAction(id,joined)},enabled=!busy&&status=="open",modifier=Modifier.fillMaxWidth()){Text(if(joined)"Cancelar inscripción" else "Inscribirme")}}}}}}
}
@Composable private fun ProfileScreen(user:JSONObject,profile:JSONObject?,busy:Boolean,notice:String,refresh:()->Unit,logout:()->Unit){
 Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())){Text("Perfil",fontSize=28.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(10.dp));Card(colors=CardDefaults.cardColors(containerColor=Panel)){Column(Modifier.padding(18.dp)){Text(profile?.optString("nick")?:user.optString("nick"),fontSize=22.sp,fontWeight=FontWeight.Bold,color=Cyan);Text("ID público: "+(profile?.optString("public_id")?: "—"),color=Muted);Text("Perfil y estadísticas sincronizados",color=Muted);val games=profile?.optJSONArray("games");if(games!=null){Spacer(Modifier.height(12.dp));Text("Estadísticas por juego",fontWeight=FontWeight.Bold);for(i in 0 until games.length()){val g=games.optJSONObject(i)?:continue;Text(g.optString("game")+" · "+g.optString("pts")+" puntos · "+g.optString("rank")+"º")}}}};Spacer(Modifier.height(12.dp));Button(onClick=refresh,enabled=!busy,modifier=Modifier.fillMaxWidth()){Text("Actualizar perfil")};Spacer(Modifier.height(8.dp));OutlinedButton(onClick=logout,enabled=!busy,modifier=Modifier.fillMaxWidth()){Text("Cerrar sesión")}}
}

@Composable private fun GamesScreen(tournaments:List<JSONObject>){Column(Modifier.verticalScroll(rememberScrollState())){Text("JUEGOS",fontSize=28.sp,fontWeight=FontWeight.Black);Text("Universos activos en la comunidad",color=Cyan);val games=tournaments.map{it.optString("game")}.filter{it.isNotBlank()}.distinct();(games.ifEmpty{listOf("Beyblade X","Inazuma Eleven","Pokémon","Bakugan","Yo-kai Watch")}).forEach{g->AaxPanel{Text(g,fontSize=20.sp,fontWeight=FontWeight.Bold);Text("Torneos y actividad",color=Muted)}}}}
@Composable private fun RankingsScreen(tournaments:List<JSONObject>){var game by remember{mutableStateOf(tournaments.firstOrNull()?.optString("game")?: "Beyblade X")};var rows by remember{mutableStateOf(listOf<JSONObject>())};var loading by remember{mutableStateOf(false)};LaunchedEffect(game){loading=true;try{val a=AaxApiClient.standings(game).body.optJSONArray("items");rows=if(a==null)emptyList()else(0 until a.length()).mapNotNull{a.optJSONObject(it)}}catch(_:Exception){}finally{loading=false}};Column(Modifier.fillMaxSize()){Text("RANKINGS",fontSize=28.sp,fontWeight=FontWeight.Black);OutlinedTextField(game,{game=it},label={Text("Juego")},modifier=Modifier.fillMaxWidth());if(loading)LinearProgressIndicator();LazyColumn{items(rows){r->AaxPanel{Text(r.optString("display_nick",r.optString("nick")),fontWeight=FontWeight.Bold);Text(r.optString("pts")+" puntos · "+r.optString("wins")+" victorias",color=Cyan)}}}}}
@Composable private fun CommunityScreen(){val scope=rememberCoroutineScope();var query by remember{mutableStateOf("")};var users by remember{mutableStateOf(listOf<JSONObject>())};var friends by remember{mutableStateOf(listOf<JSONObject>())};var pending by remember{mutableStateOf(listOf<JSONObject>())};var msg by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)};fun load(){scope.launch{busy=true;try{val j=AaxApiClient.friends().body;val a=j.optJSONArray("friends");friends=if(a==null)emptyList()else(0 until a.length()).mapNotNull{a.optJSONObject(it)};val p=j.optJSONArray("pending");pending=if(p==null)emptyList()else(0 until p.length()).mapNotNull{p.optJSONObject(it)}}catch(e:Exception){msg=e.message?: "Error"}finally{busy=false}}};Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){Text("COMUNIDAD",fontSize=28.sp,fontWeight=FontWeight.Black);Text("Jugadores, amistades y solicitudes",color=Cyan);OutlinedTextField(query,{query=it},label={Text("Nick o ID público")},modifier=Modifier.fillMaxWidth());Button(enabled=!busy&&query.length>=2,onClick={scope.launch{busy=true;try{val a=AaxApiClient.socialSearch(query).body.optJSONArray("users");users=if(a==null)emptyList()else(0 until a.length()).mapNotNull{a.optJSONObject(it)}}catch(e:Exception){msg=e.message?: "Error"}finally{busy=false}}},modifier=Modifier.fillMaxWidth()){Text("Buscar jugadores")};Button(enabled=!busy,onClick={load()},modifier=Modifier.fillMaxWidth()){Text("Actualizar amigos")};if(busy)LinearProgressIndicator(Modifier.fillMaxWidth());if(msg.isNotBlank())Text(msg,color=Muted);Text("Resultados",fontWeight=FontWeight.Bold);users.forEach{u->AaxPanel{Text(u.optString("nick"),fontWeight=FontWeight.Bold);Text("@"+u.optString("public_id"),color=Muted);Button(enabled=!busy,onClick={scope.launch{try{val r=AaxApiClient.friendRequest(u.optString("id"));msg=r.body.optString("error","Solicitud enviada");load()}catch(e:Exception){msg=e.message?: "Error"}}}){Text("Añadir amigo")}}};Text("Solicitudes recibidas",fontWeight=FontWeight.Bold);pending.forEach{p->AaxPanel{Text(p.optString("nick"),fontWeight=FontWeight.Bold);Button(onClick={scope.launch{try{AaxApiClient.friendAccept(p.optString("id"));load()}catch(e:Exception){msg=e.message?: "Error"}}}){Text("Aceptar")};Button(onClick={scope.launch{try{AaxApiClient.friendAction(p.optString("id"),"reject");load()}catch(e:Exception){msg=e.message?: "Error"}}}){Text("Rechazar")}}};Text("Amigos",fontWeight=FontWeight.Bold);friends.forEach{fr->AaxPanel{Text(fr.optString("nick",fr.optString("user_nick")),fontWeight=FontWeight.Bold);Text(fr.optString("public_id"),color=Muted)}};LaunchedEffect(Unit){load()}}}
@Composable private fun SettingsScreen(user:JSONObject,logout:()->Unit){Column(Modifier.verticalScroll(rememberScrollState())){Text("AJUSTES",fontSize=28.sp,fontWeight=FontWeight.Black);AaxPanel{Text("Cuenta",fontWeight=FontWeight.Bold);Text(user.optString("nick"));Text("Configuración de cuenta y sesión")};Button(onClick=logout,modifier=Modifier.fillMaxWidth()){Text("Cerrar sesión")}}}
@Composable private fun OrganizationScreen(user:JSONObject){Column(Modifier.verticalScroll(rememberScrollState())){Text("ORGANIZACIÓN",fontSize=28.sp,fontWeight=FontWeight.Black);AaxPanel{Text(if(user.optString("role")=="admin")"Panel de organizador" else "Área restringida",fontWeight=FontWeight.Bold);Text("Los controles administrativos de la web aún requieren integración.")}}}
@Composable private fun AaxPanel(content:@Composable ColumnScope.()->Unit){Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=androidx.compose.foundation.shape.CutCornerShape(12.dp),border=androidx.compose.foundation.BorderStroke(1.dp,Edge),modifier=Modifier.fillMaxWidth().padding(vertical=6.dp)){Column(Modifier.padding(16.dp),content=content)}}
