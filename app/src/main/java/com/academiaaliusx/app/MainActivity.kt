package com.academiaaliusx.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
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
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);AaxApiClient.initialize(applicationContext);setContent{AAXNativeApp()}}}

@Composable private fun AAXNativeApp(){
 val scope=rememberCoroutineScope()
 var user by remember { mutableStateOf<JSONObject?>(null) }
 var page by remember { mutableIntStateOf(0) }
 var authMode by remember { mutableStateOf(false) }
 var registerMode by remember { mutableStateOf(false) }; var recoveryMode by remember { mutableStateOf(false) }
 var nick by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }
 var busy by remember { mutableStateOf(false) }; var notice by remember { mutableStateOf("") }
 var tournaments by remember { mutableStateOf(listOf<JSONObject>()) }
 var profile by remember { mutableStateOf<JSONObject?>(null) }
 fun reload(){
   scope.launch { busy=true; try {
     val me=AaxApiClient.me()
     user=if(me.ok) me.body.optJSONObject("user") else null
     if(user!=null){authMode=false;val t=AaxApiClient.tournaments();if(t.ok){val arr=t.body.optJSONArray("items")?:JSONArray();tournaments=(0 until arr.length()).mapNotNull{arr.optJSONObject(it)}}else notice=t.errorMessage();profile=null}
     else {authMode=true;if(me.status!=401&&notice.isBlank())notice=me.errorMessage()}
   }catch(e:Exception){notice="Error de conexión: "+(e.message?: "desconocido")}finally{busy=false} }
 }
 fun logoutAction(){
   scope.launch {
     busy=true
     var serverConfirmed=false
     try { serverConfirmed=AaxApiClient.logout().ok } catch (_:Exception) { serverConfirmed=false }
     finally {
       AaxApiClient.clearSession()
       user=null;profile=null;page=0;authMode=true;busy=false
       notice=if(serverConfirmed)"Sesión cerrada." else "Sesión cerrada localmente. El servidor no confirmó la salida."
     }
   }
 }
 LaunchedEffect(Unit){reload()}
 var moreNav by remember { mutableStateOf(false) }
 MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, background = Ink, surface = Panel, onBackground = TextMain, onSurface = TextMain, secondary = Color(0xFF438B98))) {
  Scaffold(
   containerColor = Ink,
   bottomBar = {
    if (user != null) {
     Box {
      NavigationBar(containerColor = Ink, tonalElevation = 0.dp) {
       val labels = listOf("Inicio", "Juegos", "Torneos", "Ranking", "Perfil")
       val icons = listOf(Icons.Filled.Home, Icons.Filled.SportsEsports, Icons.Filled.EmojiEvents, Icons.Filled.Leaderboard, Icons.Filled.Person)
       for (idx in 0..4) {
        NavigationBarItem(
         selected = page == idx,
         onClick = { page = idx; if (idx == 4) scope.launch { busy = true; try { profile = AaxApiClient.profile(user?.optString("nick") ?: "").body } finally { busy = false } } },
         icon = { Icon(icons[idx], contentDescription = labels[idx]) },
         label = { Text(labels[idx], maxLines = 1) },
         colors = NavigationBarItemDefaults.colors(selectedIconColor = Ink, selectedTextColor = Cyan, indicatorColor = Cyan, unselectedIconColor = Muted, unselectedTextColor = Muted)
        )
       }
       NavigationBarItem(selected = page >= 5, onClick = { moreNav = true }, icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = "Más") }, label = { Text("Más", maxLines = 1) })
      }
      DropdownMenu(expanded = moreNav, onDismissRequest = { moreNav = false }, modifier = Modifier.background(Panel)) {
       listOf(5 to "Comunidad", 6 to "Ajustes", 7 to "Organización").forEach { (idx, label) ->
        DropdownMenuItem(text = { Text(label) }, onClick = { moreNav = false; page = idx }, leadingIcon = { Icon(listOf(Icons.Filled.People, Icons.Filled.Settings, Icons.Filled.AdminPanelSettings)[idx - 5], contentDescription = label) })
       }
      }
     }
    }
   }
  ) { innerPadding ->
   Column(Modifier.fillMaxSize().padding(innerPadding).background(Ink).padding(horizontal = 20.dp, vertical = 16.dp)) {
    Header()
    Spacer(Modifier.height(20.dp))
    if (user == null) AuthScreen(registerMode, recoveryMode, nick, email, password, { nick = it }, { email = it }, { password = it }, { registerMode = it }, { recoveryMode = it }, busy, notice, { notice = "" }, {
     scope.launch { busy = true; notice = ""; try { val r = if (recoveryMode) AaxApiClient.forgotPassword(email) else if (registerMode) AaxApiClient.register(nick, email, password) else AaxApiClient.login(nick, password); if (r.ok) { notice = if (recoveryMode) "Si la cuenta existe, recibirás instrucciones por correo." else if (registerMode && r.body.optBoolean("verification_required")) "Cuenta creada. Comprueba el correo para verificarla." else "Acceso correcto."; if (!recoveryMode && !(registerMode && r.body.optBoolean("verification_required"))) reload() } else notice = r.errorMessage() } catch (e: Exception) { notice = e.message ?: "Error de conexión" } finally { busy = false } }
    })
    else when (page) {
     0 -> HomeScreen(user!!, tournaments.size, tournaments, { page = 2 }, { page = 4 })
     1 -> GamesScreen(tournaments)
     2 -> TournamentScreen(tournaments, user!!, busy, notice, { notice = it }, { id, joined -> scope.launch { busy = true; try { val r = if (joined) AaxApiClient.leaveTournament(id) else AaxApiClient.joinTournament(id); if (r.ok) { notice = if (joined) "Inscripción cancelada." else "Inscripción confirmada."; reload() } else notice = r.errorMessage() } catch (e: Exception) { notice = e.message ?: "Error" } finally { busy = false } } })
     3 -> RankingsScreen(tournaments)
     4 -> ProfileScreen(user!!, profile, busy, notice, { scope.launch { busy = true; try { val r=AaxApiClient.profile(user?.optString("nick") ?: "");profile=if(r.ok)r.body else null;if(!r.ok)notice=r.errorMessage() } catch (e: Exception) { notice = e.message ?: "Error" } finally { busy = false } } }, ::logoutAction)
     5 -> CommunityScreen()
     6 -> SettingsScreen(user!!, ::logoutAction)
     else -> OrganizationScreen(user!!)
    }
    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Cyan)
    if (notice.isNotBlank()) Text(notice, color = if (notice.contains("Error") || notice.contains("No se pudo")) Color(0xFFFFB86C) else Cyan, modifier = Modifier.padding(top = 12.dp))
   }
  }
 }
}
@Composable private fun Header(){Row(verticalAlignment=Alignment.CenterVertically){Text("AAX",color=Ink,fontWeight=FontWeight.Black,fontSize=18.sp,modifier=Modifier.padding(12.dp).background(Cyan, androidx.compose.foundation.shape.CutCornerShape(8.dp)));Column{Text("ACADEMIA ALIUS X",fontWeight=FontWeight.Black,letterSpacing=2.sp);Text("COMUNIDAD · JUEGO · COMPETICIÓN",color=Muted,fontSize=10.sp)}}}
@Composable private fun AuthScreen(reg:Boolean,recovery:Boolean,nick:String,email:String,password:String,onNick:(String)->Unit,onEmail:(String)->Unit,onPassword:(String)->Unit,onMode:(Boolean)->Unit,onRecovery:(Boolean)->Unit,busy:Boolean,notice:String,clear:()->Unit,submit:()->Unit){
 Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally){
 Text(if(recovery)"Recuperar contraseña" else if(reg)"Crear cuenta" else "Acceder a Alius X",fontSize=28.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(16.dp))
 OutlinedTextField(if(reg||recovery)email else nick,if(reg||recovery)onEmail else onNick,label={Text(if(reg||recovery)"Correo electrónico" else "Usuario o correo")},singleLine=true,modifier=Modifier.fillMaxWidth())
 if(reg||!recovery){Spacer(Modifier.height(8.dp));if(reg)OutlinedTextField(nick,onNick,label={Text("Nick")},singleLine=true,modifier=Modifier.fillMaxWidth());OutlinedTextField(password,onPassword,label={Text("Contraseña")},singleLine=true,modifier=Modifier.fillMaxWidth())}
 Spacer(Modifier.height(16.dp));Button(onClick=submit,enabled=!busy&&(if(recovery)email.contains("@") else nick.isNotBlank()&&password.length>=8&&(!reg||email.contains("@"))),modifier=Modifier.fillMaxWidth().height(52.dp)){Text(if(recovery)"Enviar instrucciones" else if(reg)"Registrarme" else "Iniciar sesión")}
 if(!recovery&&!reg)TextButton(onClick={onRecovery(true);clear()}){Text("¿Olvidaste tu contraseña?")}
 TextButton(onClick={onMode(!reg);onRecovery(false);clear()}){Text(if(reg)"Ya tengo cuenta · Entrar" else "Crear una cuenta")}
 if(recovery)TextButton(onClick={onRecovery(false);clear()}){Text("Volver al acceso")}
 Text("Contraseña: mínimo 8 caracteres",color=Muted,fontSize=12.sp)
 }}
@Composable private fun HomeScreen(user:JSONObject,count:Int,tournaments:List<JSONObject>,toTournaments:()->Unit,toProfile:()->Unit){
 Column(Modifier.verticalScroll(rememberScrollState())){Text("▰  TU UNIVERSO GAMER",color=Cyan,fontSize=13.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp);Spacer(Modifier.height(8.dp));Text("¡Bienvenido, "+user.optString("nick")+"!",fontSize=30.sp,fontWeight=FontWeight.Black,lineHeight=34.sp);Spacer(Modifier.height(10.dp));Text("Ahora conectado a Academia Alius X.",color=Muted,fontSize=17.sp);Spacer(Modifier.height(20.dp));Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=androidx.compose.foundation.shape.CutCornerShape(14.dp),border=androidx.compose.foundation.BorderStroke(1.dp,Edge)){Column(Modifier.padding(18.dp)){Text(count.toString(),fontSize=32.sp,color=Cyan,fontWeight=FontWeight.Black);Text("Torneos disponibles/registrados en el servidor")}};Spacer(Modifier.height(12.dp));Button(onClick=toTournaments,shape=androidx.compose.foundation.shape.CutCornerShape(12.dp),colors=ButtonDefaults.buttonColors(containerColor=Cyan,contentColor=Ink),modifier=Modifier.fillMaxWidth().height(54.dp)){Icon(Icons.Filled.EmojiEvents,null);Spacer(Modifier.width(8.dp));Text("Explorar torneos online")};Spacer(Modifier.height(8.dp));OutlinedButton(onClick=toProfile,shape=androidx.compose.foundation.shape.CutCornerShape(12.dp),border=androidx.compose.foundation.BorderStroke(1.dp,Edge),modifier=Modifier.fillMaxWidth().height(52.dp)){Icon(Icons.Filled.Person,null);Spacer(Modifier.width(8.dp));Text("Ver mi perfil")}}
}
@Composable private fun TournamentScreen(items:List<JSONObject>,user:JSONObject,busy:Boolean,notice:String,onNotice:(String)->Unit,onAction:(String,Boolean)->Unit){
 var query by remember { mutableStateOf("") }
 var filter by remember { mutableStateOf(TournamentListFilter.ALL) }
 var selected by remember { mutableStateOf<JSONObject?>(null) }
 val visible=TournamentListRules.filter(items,query,filter)
 Column(Modifier.fillMaxSize()){
  Text("TORNEOS ONLINE",fontSize=28.sp,fontWeight=FontWeight.Black,letterSpacing=1.5.sp)
  Text("Datos consultados desde el servidor",color=Cyan)
  Spacer(Modifier.height(12.dp))
  OutlinedTextField(query,{query=it},label={Text("Buscar por torneo, juego o estado")},singleLine=true,modifier=Modifier.fillMaxWidth())
  if(query.isNotBlank()) TextButton(onClick={query=""}){Text("Borrar búsqueda")}
  Row(horizontalArrangement=Arrangement.spacedBy(6.dp),modifier=Modifier.fillMaxWidth()){
   FilterChip(selected=filter==TournamentListFilter.ALL,onClick={filter=TournamentListFilter.ALL},label={Text("Todos")})
   FilterChip(selected=filter==TournamentListFilter.OPEN,onClick={filter=TournamentListFilter.OPEN},label={Text("Abiertos")})
   FilterChip(selected=filter==TournamentListFilter.JOINED,onClick={filter=TournamentListFilter.JOINED},label={Text("Inscritos")})
  }
  Text(visible.size.toString()+" de "+items.size+" torneos",color=Muted,modifier=Modifier.padding(vertical=6.dp))
  if(items.isEmpty()) Text("No hay torneos en la respuesta del servidor.",color=Muted)
  else if(visible.isEmpty()) Text("No hay torneos que coincidan. Prueba a borrar la búsqueda o cambiar el filtro.",color=Muted)
  LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(12.dp)){
   items(visible){t->
    val id=t.optString("id");val title=TournamentListRules.title(t);val joined=TournamentListRules.isJoined(t);val status=t.optString("status")
    Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=androidx.compose.foundation.shape.CutCornerShape(14.dp),border=androidx.compose.foundation.BorderStroke(1.dp,Edge)){
     Column(Modifier.padding(16.dp)){
      Text(title,fontWeight=FontWeight.Bold,fontSize=18.sp)
      Spacer(Modifier.height(4.dp))
      Text(listOf(t.optString("game"),t.optString("date"),status).filter{it.isNotBlank()}.joinToString(" · "),color=Muted,fontSize=12.sp)
      Spacer(Modifier.height(6.dp))
      Text("Participantes: "+t.optString("n","?")+(if(t.has("max_players"))"/"+t.optString("max_players")else""),color=Muted)
      TextButton(onClick={selected=t},modifier=Modifier.fillMaxWidth()){Text("Ver detalles")}
      Button(onClick={onAction(id,joined)},enabled=!busy&&TournamentListRules.isOpen(t),modifier=Modifier.fillMaxWidth()){Text(if(joined)"Cancelar inscripción" else "Inscribirme")}
     }
    }
   }
  }
 }
 selected?.let { t ->
  AlertDialog(
   onDismissRequest={selected=null},
   containerColor=Panel,
   title={Text(TournamentListRules.title(t),fontWeight=FontWeight.Bold)},
   text={
    Column(Modifier.heightIn(max=420.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
     val fields=listOf("game" to "Juego","status" to "Estado","date" to "Fecha","n" to "Participantes","max_players" to "Máximo de jugadores","description" to "Descripción","location" to "Ubicación","format" to "Formato","url" to "Enlace")
     fields.forEach{(key,label)->val value=t.optString(key).trim();if(value.isNotEmpty()&&value!="null"){Text(label,fontWeight=FontWeight.SemiBold,color=Cyan);Text(value)}}
     Text("Estos detalles proceden únicamente de los datos recibidos en el listado.",color=Muted,fontSize=12.sp)
    }
   },
   confirmButton={TextButton(onClick={selected=null}){Text("Cerrar")}}
  )
 }
}
@Composable private fun ProfileScreen(user:JSONObject,profile:JSONObject?,busy:Boolean,notice:String,refresh:()->Unit,logout:()->Unit){
 val scope=rememberCoroutineScope();var msg by remember{mutableStateOf("")}
 val frameInfo=profile?.optJSONObject("avatarFrame");val frames=frameInfo?.optJSONArray("frames");val unlockedIds=frameInfo?.optJSONArray("unlocked")
 Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())){Text("PERFIL",fontSize=28.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(10.dp));AaxPanel{Text(profile?.optString("nick")?:user.optString("nick"),fontSize=22.sp,fontWeight=FontWeight.Bold,color=Cyan);Text("ID público: "+(profile?.optString("public_id")?: "—"),color=Muted);Text("Perfil y estadísticas",color=Muted);Text("Puntos: "+(frameInfo?.optString("points")?: "—"),color=Cyan)}
 val games=profile?.optJSONArray("games");if(games!=null){Text("Estadísticas por juego",fontWeight=FontWeight.Bold);for(i in 0 until games.length()){val g=games.optJSONObject(i)?:continue;AaxPanel{Text(g.optString("game"),fontWeight=FontWeight.Bold);Text(g.optString("pts")+" puntos · "+g.optString("rank")+"º puesto",color=Cyan)}}}
 if(frames!=null){Text("Marcos de avatar",fontWeight=FontWeight.Bold);Text("Selecciona un marco desbloqueado.",color=Muted);for(i in 0 until frames.length()){val fr=frames.optJSONObject(i)?:continue;val id=fr.optString("id");val unlocked=(0 until (unlockedIds?.length()?:0)).any{unlockedIds?.optString(it)==id};AaxPanel{Text(fr.optString("name",id),fontWeight=FontWeight.Bold);Text(if(unlocked)"Desbloqueado" else "Bloqueado",color=if(unlocked)Cyan else Muted);Button(enabled=unlocked&&!busy,onClick={scope.launch{try{val r=AaxApiClient.setAvatarFrame(id);msg=if(r.ok)"Marco actualizado"else r.errorMessage()}catch(e:Exception){msg=e.message?: "Error"}}}){Text("Equipar")}}}}
 if(msg.isNotBlank())Text(msg,color=Cyan);Button(onClick=refresh,enabled=!busy,modifier=Modifier.fillMaxWidth()){Text("Actualizar perfil")};OutlinedButton(onClick=logout,enabled=!busy,modifier=Modifier.fillMaxWidth()){Text("Cerrar sesión")}}
}
@Composable private fun GamesScreen(tournaments:List<JSONObject>){
 val catalog=listOf("Beyblade X","Inazuma Eleven","Pokémon","Bakugan","Yo-kai Watch")
 var selected by remember{mutableStateOf(catalog.first())}
 var query by remember{mutableStateOf("")}
 val filtered=catalog.filter{it.contains(query,true)}
 val gameTournaments=tournaments.filter{it.optString("game").equals(selected,true)}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){
 Text("JUEGOS",fontSize=28.sp,fontWeight=FontWeight.Black);Text("Catálogo de la comunidad AAX",color=Cyan)
 OutlinedTextField(query,{query=it},label={Text("Buscar juego")},modifier=Modifier.fillMaxWidth())
 filtered.forEach{g->Card(colors=CardDefaults.cardColors(containerColor=if(g==selected)Color(0xFF183C46) else Panel),border=androidx.compose.foundation.BorderStroke(1.dp,if(g==selected)Cyan else Edge),modifier=Modifier.fillMaxWidth().padding(vertical=5.dp)){Column(Modifier.padding(14.dp)){Text(g,fontSize=20.sp,fontWeight=FontWeight.Bold);Text(tournaments.count{it.optString("game").equals(g,true)}.toString()+" torneos en el listado",color=Muted);TextButton(onClick={selected=g}){Text(if(selected==g)"Seleccionado" else "Ver juego")}}}}
 Divider(color=Edge);Text("Torneos de "+selected,fontWeight=FontWeight.Bold,fontSize=20.sp)
 if(gameTournaments.isEmpty())Text("No hay torneos de este juego en la respuesta del servidor.",color=Muted)
 gameTournaments.forEach{t->AaxPanel{Text(t.optString("name",t.optString("title","Torneo")),fontWeight=FontWeight.Bold);Text(t.optString("status")+" · "+t.optString("date"),color=Muted)}}
 }
}
@Composable private fun RankingsScreen(tournaments:List<JSONObject>){
 val scope=rememberCoroutineScope();var game by remember{mutableStateOf(tournaments.firstOrNull()?.optString("game")?: "Beyblade X")};var rows by remember{mutableStateOf(listOf<JSONObject>())};var podiums by remember{mutableStateOf(listOf<JSONObject>())};var loading by remember{mutableStateOf(false)};var showPodiums by remember{mutableStateOf(false)}
 fun load(){scope.launch{loading=true;try{rows=arr(AaxApiClient.standings(game).body,"items");podiums=arr(AaxApiClient.podiums().body,"items")}catch(_:Exception){}finally{loading=false}}}
 Column(Modifier.fillMaxSize()){Text("RANKINGS",fontSize=28.sp,fontWeight=FontWeight.Black);OutlinedTextField(game,{game=it},label={Text("Juego")},modifier=Modifier.fillMaxWidth());Row{TextButton(onClick={showPodiums=false;load()}){Text("Clasificación")};TextButton(onClick={showPodiums=true;load()}){Text("Podios recientes")}};Button(onClick={load()},enabled=!loading,modifier=Modifier.fillMaxWidth()){Text("Actualizar")};if(loading)LinearProgressIndicator(Modifier.fillMaxWidth())
 if(showPodiums)LazyColumn{items(podiums){r->AaxPanel{Text(r.optString("name"),fontWeight=FontWeight.Bold);Text(r.optString("game")+" · "+r.optString("date"),color=Muted);Text("#"+r.optString("position")+" · "+r.optString("nick"),color=Cyan)}}}
 else LazyColumn{items(rows){r->AaxPanel{Text(r.optString("display_nick",r.optString("nick")),fontWeight=FontWeight.Bold);Text(r.optString("pts")+" puntos · "+r.optString("wins")+" victorias · "+r.optString("n")+" resultados",color=Cyan)}}}}
 LaunchedEffect(game){load()}
}
@Composable private fun CommunityScreen(){
 val scope=rememberCoroutineScope()
 var query by remember{mutableStateOf("")};var users by remember{mutableStateOf(listOf<JSONObject>())};var friends by remember{mutableStateOf(listOf<JSONObject>())};var pending by remember{mutableStateOf(listOf<JSONObject>())}
 var teams by remember{mutableStateOf(listOf<JSONObject>())};var myTeam by remember{mutableStateOf<JSONObject?>(null)};var teamRequests by remember{mutableStateOf(listOf<JSONObject>())};var myRequests by remember{mutableStateOf(listOf<JSONObject>())}
 var teamQuery by remember{mutableStateOf("")};var teamName by remember{mutableStateOf("")};var teamTag by remember{mutableStateOf("")};var teamGame by remember{mutableStateOf("Beyblade X")};var teamDesc by remember{mutableStateOf("")}
 var msg by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)}
 fun loadAll(){scope.launch{busy=true;try{val f=AaxApiClient.friends().body;friends=arr(f,"friends");pending=arr(f,"pending");val t=AaxApiClient.teams().body;teams=arr(t,"teams");val mt=AaxApiClient.myTeam().body;myTeam=mt.optJSONObject("team");if(myTeam!=null)teamRequests=arr(AaxApiClient.teamRequests().body,"requests")else teamRequests=emptyList();myRequests=arr(AaxApiClient.myTeamRequests().body,"requests")}catch(e:Exception){msg=e.message?: "Error de conexión"}finally{busy=false}}}
 fun act(block:suspend()->AaxApiClient.ApiResult,success:String){scope.launch{busy=true;try{val r=block();if(r.ok){msg=success;loadAll()}else msg=r.errorMessage()}catch(e:Exception){msg=e.message?: "Error"}finally{busy=false}}}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){Text("COMUNIDAD",fontSize=28.sp,fontWeight=FontWeight.Black);Text("Jugadores, amistades y equipos",color=Cyan);Button(onClick={loadAll()},enabled=!busy,modifier=Modifier.fillMaxWidth()){Text("Actualizar comunidad")}
 if(busy)LinearProgressIndicator(Modifier.fillMaxWidth());if(msg.isNotBlank())Text(msg,color=Muted)
 Text("Buscar jugadores",fontWeight=FontWeight.Bold);OutlinedTextField(query,{query=it},label={Text("Nick o ID público")},modifier=Modifier.fillMaxWidth());Button(enabled=!busy&&query.length>=2,onClick={scope.launch{busy=true;try{users=arr(AaxApiClient.socialSearch(query).body,"users")}catch(e:Exception){msg=e.message?: "Error"}finally{busy=false}}},modifier=Modifier.fillMaxWidth()){Text("Buscar")};users.forEach{u->AaxPanel{Text(u.optString("nick"),fontWeight=FontWeight.Bold);Text("@"+u.optString("public_id"),color=Muted);Button(enabled=!busy,onClick={act({AaxApiClient.friendRequest(u.optString("id"))},"Solicitud enviada")}){Text("Añadir amigo")};Button(enabled=!busy,onClick={act({AaxApiClient.blockUser(u.optString("id"))},"Jugador bloqueado")}){Text("Bloquear")}}}
 Text("Solicitudes recibidas",fontWeight=FontWeight.Bold);pending.forEach{p->AaxPanel{Text(p.optString("nick"),fontWeight=FontWeight.Bold);Row{Button(onClick={act({AaxApiClient.friendAccept(p.optString("id"))},"Amistad aceptada")}){Text("Aceptar")};TextButton(onClick={act({AaxApiClient.friendAction(p.optString("id"),"reject")},"Solicitud rechazada")}){Text("Rechazar")}}}}
 Text("Amigos",fontWeight=FontWeight.Bold);friends.forEach{fr->AaxPanel{Text(fr.optString("nick",fr.optString("user_nick")),fontWeight=FontWeight.Bold);Text(fr.optString("public_id"),color=Muted);TextButton(onClick={act({AaxApiClient.friendAction(fr.optString("id"),"remove")},"Amigo eliminado")}){Text("Eliminar")}}}
 Divider(color=Edge);Text("EQUIPOS",fontSize=24.sp,fontWeight=FontWeight.Black)
 if(myTeam!=null){AaxPanel{Text("MI EQUIPO · "+myTeam!!.optString("name"),fontWeight=FontWeight.Bold,fontSize=20.sp,color=Cyan);Text("["+myTeam!!.optString("tag")+"] · "+myTeam!!.optString("game"));Text(myTeam!!.optString("description"),color=Muted);TextButton(onClick={act({AaxApiClient.teamManage(JSONObject().put("team_id",myTeam!!.optString("id")).put("action","leave"))},"Has salido del equipo")}){Text("Abandonar equipo")};teamRequests.forEach{rq->Row(verticalAlignment=Alignment.CenterVertically){Text(rq.optString("nick"),Modifier.weight(1f));TextButton(onClick={act({AaxApiClient.teamRespond(rq.optString("id"),true)},"Solicitud aceptada")}){Text("Aceptar")};TextButton(onClick={act({AaxApiClient.teamRespond(rq.optString("id"),false)},"Solicitud rechazada")}){Text("Rechazar")}}}}}
 else{Text("Crear equipo",fontWeight=FontWeight.Bold);OutlinedTextField(teamName,{teamName=it},label={Text("Nombre")},modifier=Modifier.fillMaxWidth());OutlinedTextField(teamTag,{teamTag=it},label={Text("Tag")},modifier=Modifier.fillMaxWidth());OutlinedTextField(teamGame,{teamGame=it},label={Text("Juego")},modifier=Modifier.fillMaxWidth());OutlinedTextField(teamDesc,{teamDesc=it},label={Text("Descripción")},modifier=Modifier.fillMaxWidth());Button(enabled=!busy&&teamName.isNotBlank()&&teamTag.isNotBlank(),onClick={act({AaxApiClient.teamCreate(JSONObject().put("name",teamName).put("tag",teamTag).put("game",teamGame).put("description",teamDesc))},"Equipo creado")},modifier=Modifier.fillMaxWidth()){Text("Crear equipo")}}
 Text("Buscar equipos",fontWeight=FontWeight.Bold);OutlinedTextField(teamQuery,{teamQuery=it},label={Text("Nombre o juego")},modifier=Modifier.fillMaxWidth());Button(onClick={scope.launch{busy=true;try{teams=arr(AaxApiClient.teams().body,"teams")}catch(e:Exception){msg=e.message?: "Error"}finally{busy=false}}},enabled=!busy){Text("Actualizar equipos")};teams.forEach{t->AaxPanel{Text(t.optString("name"),fontWeight=FontWeight.Bold);Text("["+t.optString("tag")+"] · "+t.optString("game"),color=Cyan);Text(t.optString("description"),color=Muted);Text("Miembros: "+t.optString("size","—"));Button(enabled=!busy&&myTeam==null,onClick={act({AaxApiClient.teamJoin(t.optString("id"))},"Solicitud enviada al equipo")}){Text("Solicitar entrar")}}}
 Text("Mis solicitudes de equipo",fontWeight=FontWeight.Bold);myRequests.forEach{rq->AaxPanel{Text(rq.optString("name",rq.optString("team_name")));TextButton(onClick={act({AaxApiClient.teamRequestCancel(rq.optString("id"))},"Solicitud cancelada")}){Text("Cancelar")}}}
 }
 LaunchedEffect(Unit){loadAll()}
}
private fun arr(o:JSONObject,key:String):List<JSONObject>{val a=o.optJSONArray(key)?:return emptyList();return (0 until a.length()).mapNotNull{a.optJSONObject(it)}}
@Composable private fun SettingsScreen(user:JSONObject,logout:()->Unit){
 val scope=rememberCoroutineScope();var blocked by remember{mutableStateOf(listOf<JSONObject>())};var busy by remember{mutableStateOf(false)};var msg by remember{mutableStateOf("")};var oldPass by remember{mutableStateOf("")};var newPass by remember{mutableStateOf("")};var deletePass by remember{mutableStateOf("")}
 fun load(){scope.launch{busy=true;try{blocked=arr(AaxApiClient.blocks().body,"blocks")}catch(e:Exception){msg=e.message?: "Error"}finally{busy=false}}}
 Column(Modifier.verticalScroll(rememberScrollState())){Text("AJUSTES",fontSize=28.sp,fontWeight=FontWeight.Black);AaxPanel{Text("Cuenta",fontWeight=FontWeight.Bold);Text(user.optString("nick"));Text("Sesión y seguridad de cuenta")}
 Text("Cambiar contraseña",fontWeight=FontWeight.Bold);OutlinedTextField(oldPass,{oldPass=it},label={Text("Contraseña actual")},modifier=Modifier.fillMaxWidth());OutlinedTextField(newPass,{newPass=it},label={Text("Nueva contraseña (8-100)")},modifier=Modifier.fillMaxWidth());Button(enabled=!busy&&oldPass.isNotBlank()&&newPass.length>=8,onClick={scope.launch{busy=true;try{val r=AaxApiClient.changePassword(oldPass,newPass);msg=if(r.ok)"Contraseña actualizada."else r.errorMessage()}catch(e:Exception){msg=e.message?: "Error"}finally{busy=false}}},modifier=Modifier.fillMaxWidth()){Text("Actualizar contraseña")}
 Divider(color=Edge);Text("Usuarios bloqueados",fontWeight=FontWeight.Bold);Button(onClick={load()},enabled=!busy,modifier=Modifier.fillMaxWidth()){Text("Cargar bloqueos")};if(busy)LinearProgressIndicator();blocked.forEach{b->AaxPanel{Text(b.optString("nick",b.optString("user_nick")),fontWeight=FontWeight.Bold);TextButton(onClick={scope.launch{try{val r=AaxApiClient.blockAction(b.optString("user_id",b.optString("id")),"unblock");msg=if(r.ok)"Usuario desbloqueado"else r.errorMessage();load()}catch(e:Exception){msg=e.message?: "Error"}}}){Text("Desbloquear")}}}
 Divider(color=Edge);Text("Eliminar cuenta",fontWeight=FontWeight.Bold,color=Color(0xFFFF8A80));Text("Esta acción elimina permanentemente la cuenta y los datos asociados.",color=Muted);OutlinedTextField(deletePass,{deletePass=it},label={Text("Confirma con tu contraseña")},modifier=Modifier.fillMaxWidth());Button(enabled=!busy&&deletePass.isNotBlank(),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF9D3434)),onClick={scope.launch{busy=true;try{val r=AaxApiClient.deleteAccount(deletePass);if(r.ok){msg="Cuenta eliminada.";logout()}else msg=r.errorMessage()}catch(e:Exception){msg=e.message?: "Error"}finally{busy=false}}},modifier=Modifier.fillMaxWidth()){Text("Eliminar mi cuenta")}
 if(msg.isNotBlank())Text(msg,color=Cyan);OutlinedButton(onClick=logout,modifier=Modifier.fillMaxWidth()){Text("Cerrar sesión")}}
 LaunchedEffect(Unit){load()}
}
@Composable private fun OrganizationScreen(user:JSONObject){
 val scope=rememberCoroutineScope();var busy by remember{mutableStateOf(false)};var result by remember{mutableStateOf("")};var ok by remember{mutableStateOf(false)}
 Column(Modifier.verticalScroll(rememberScrollState())){Text("ORGANIZACIÓN",fontSize=28.sp,fontWeight=FontWeight.Black);if(user.optString("role")!="admin"){AaxPanel{Text("Área de organización",fontWeight=FontWeight.Bold);Text("Las funciones administrativas están disponibles solo para cuentas autorizadas.",color=Muted)}}else{AaxPanel{Text("Panel de organizador",fontWeight=FontWeight.Bold,fontSize=20.sp,color=Cyan);Text("Integración segura con Challonge a través de Cloudflare. Las credenciales nunca se almacenan en el APK.");Button(enabled=!busy,onClick={scope.launch{busy=true;result="";try{val r=AaxApiClient.challongeTest();ok=r.ok;result=if(r.ok)"Conexión verificada. Torneos consultados: "+r.body.optString("tournaments_checked","—") else r.errorMessage() }catch(e:Exception){result=e.message?: "Error"}finally{busy=false}}},modifier=Modifier.fillMaxWidth()){Text("Comprobar Challonge")};if(busy)LinearProgressIndicator();if(result.isNotBlank())Text(result,color=if(ok)Cyan else Color(0xFFFFB86C))}}}
}
@Composable private fun AaxPanel(content:@Composable ColumnScope.()->Unit){Card(colors=CardDefaults.cardColors(containerColor=Panel),shape=androidx.compose.foundation.shape.CutCornerShape(12.dp),border=androidx.compose.foundation.BorderStroke(1.dp,Edge),modifier=Modifier.fillMaxWidth().padding(vertical=6.dp)){Column(Modifier.padding(16.dp),content=content)}}
