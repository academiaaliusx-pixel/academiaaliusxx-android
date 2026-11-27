package com.academiaaliusx.app

import android.content.Context
import androidx.activity.compose.BackHandler
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.UUID

private val sampleEvents = listOf(
    LocalTournament("bx-local", "Beyblade X — torneo de muestra"),
    LocalTournament("pokemon-local", "Pokémon — torneo de muestra"),
    LocalTournament("ie-local", "Inazuma Eleven — torneo de muestra")
)

private enum class AaxTab(val label: String) { HOME("Inicio"), TOURNAMENTS("Torneos"), COMMUNITY("Comunidad"), PROFILE("Perfil") }

@Composable
fun NativeAppScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("aax-native-local", Context.MODE_PRIVATE) }
    var tab by remember { mutableStateOf(AaxTab.HOME) }
    var playerName by remember { mutableStateOf(PlayerProfileRules.displayName(prefs.getString("name", PlayerProfileRules.DEFAULT_NAME) ?: "")) }
    var games by remember { mutableStateOf(PlayerProfileRules.normalizeGames(prefs.getStringSet("games", emptySet())?.toSet() ?: emptySet())) }
    var created by remember { mutableStateOf(LocalTournamentRules.decodeEvents(prefs.getStringSet("created", emptySet())?.toSet() ?: emptySet())) }
    var registrations by remember { mutableStateOf(prefs.getStringSet("registrations", emptySet())?.toSet() ?: emptySet()) }
    var query by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("all") }
    var selectedEvent by remember { mutableStateOf<LocalTournament?>(null) }
    var createDialog by remember { mutableStateOf(false) }
    var editEvent by remember { mutableStateOf<LocalTournament?>(null) }
    var pendingDeletion by remember { mutableStateOf<LocalTournament?>(null) }
    var pendingBackupRestore by remember { mutableStateOf<LocalBackupPayload?>(null) }
    var profileDialog by remember { mutableStateOf(false) }
    var backupDialog by remember { mutableStateOf(false) }
    var backupText by remember { mutableStateOf("") }
    var backupMessage by remember { mutableStateOf("") }

    BackHandler(enabled = selectedEvent != null) { selectedEvent = null }

    val events = remember(created) { sampleEvents + created }
    val createdIds = remember(created) { created.map { it.id }.toSet() }
    val visible = remember(events, registrations, createdIds, query, mode) {
        LocalTournamentRules.visibleEvents(events, registrations, createdIds, query, mode)
    }

    fun persist() {
        prefs.edit().putString("name", PlayerProfileRules.displayName(playerName))
            .putStringSet("games", games.toSet())
            .putStringSet("created", LocalTournamentRules.encodeEvents(created).toSet())
            .putStringSet("registrations", registrations.toSet()).apply()
    }
    fun updateName(value: String) { playerName = PlayerProfileRules.sanitizeName(value); persist() }
    fun toggleGame(game: String, enabled: Boolean) { games = PlayerProfileRules.updateGame(games, game, enabled); persist() }
    fun toggleRegistration(id: String) { registrations = if (id in registrations) registrations - id else registrations + id; persist() }
    fun addEvent(title: String) {
        val id = "local-" + UUID.randomUUID().toString()
        created = LocalTournamentRules.add(created, id, title)
        persist()
    }
    fun changeEvent(id: String, title: String) { created = LocalTournamentRules.edit(created, id, title); persist() }
    fun deleteEvent(id: String) { created = LocalTournamentRules.remove(created, id); registrations = registrations - id; persist() }
    fun shareEvent(event: LocalTournament) {
        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, LocalTournamentRules.shareText(event.title, event.id in registrations, event.id in createdIds)) }
        context.startActivity(Intent.createChooser(intent, "Compartir torneo"))
    }
    fun openSite(path: String = "") {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://academiaaliusx.com$path"))) }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(windowInsets = WindowInsets.navigationBars) {
                AaxTab.entries.forEach { item ->
                    val icon = when(item) { AaxTab.HOME -> Icons.Filled.Home; AaxTab.TOURNAMENTS -> Icons.Filled.EmojiEvents; AaxTab.COMMUNITY -> Icons.Filled.Groups; AaxTab.PROFILE -> Icons.Filled.Person }
                    NavigationBarItem(selected = tab == item, onClick = { tab = item }, modifier = Modifier.testTag("nav-${item.name.lowercase()}"), icon = { Icon(icon, contentDescription = item.label) }, label = { Text(item.label) })
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when(tab) {
                AaxTab.HOME -> HomeTab(playerName, games, events.size, registrations.size, onTournaments = { tab = AaxTab.TOURNAMENTS }, onProfile = { tab = AaxTab.PROFILE }, openSite = { openSite() })
                 AaxTab.TOURNAMENTS -> selectedEvent?.let { event ->
                    LocalTournamentDetailScreen(event, event.id in registrations, event.id in createdIds, { selectedEvent = null }, { toggleRegistration(event.id) }, { shareEvent(event) }, { editEvent = event; selectedEvent = null }, { pendingDeletion = event })
                } ?: TournamentTab(visible, registrations, createdIds, query, mode, { query = it }, { mode = it }, { createDialog = true }, { selectedEvent = it }, { toggleRegistration(it) }, { shareEvent(it) }, { editEvent = it }, { id -> pendingDeletion = created.firstOrNull { event -> event.id == id } })
                AaxTab.COMMUNITY -> CommunityTab(openSite = { openSite("/comunidad") })
                AaxTab.PROFILE -> ProfileTab(playerName, games, { profileDialog = true }, { backupDialog = true })
            }
        }
    }

    if (createDialog) TitleDialog("Crear torneo local", "", "Crear", { createDialog = false }) { title -> addEvent(title); createDialog = false }
    editEvent?.let { event -> TitleDialog("Editar torneo", event.title, "Guardar", { editEvent = null }) { title -> changeEvent(event.id, title); editEvent = null } }
    if(profileDialog) {
        var draft by remember(playerName) { mutableStateOf(playerName) }
        AlertDialog(onDismissRequest = { profileDialog = false }, title = { Text("Editar perfil local") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(draft, { draft = PlayerProfileRules.cleanNameInput(it) }, modifier = Modifier.testTag("profile-name-input"), label = { Text("Nombre visible") }, singleLine = true, supportingText = { Text("Máximo ${PlayerProfileRules.MAX_NAME_LENGTH} caracteres") })
                Text("Juegos favoritos")
                PlayerProfileRules.availableGames.forEach { game -> Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = game in games, onCheckedChange = { toggleGame(game, it) }); Text(game) } }
            }
        }, confirmButton = { TextButton(onClick = { updateName(draft); profileDialog = false }, modifier = Modifier.testTag("profile-save")) { Text("Guardar") } }, dismissButton = { TextButton(onClick = { profileDialog = false }) { Text("Cancelar") } })
    }
    if(backupDialog) {
        AlertDialog(onDismissRequest = { backupDialog = false }, title = { Text("Copia local de datos") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Exporta o pega una copia JSON. Importar sustituye tus datos locales actuales.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(backupText, { backupText = it }, label = { Text("JSON de copia") }, modifier = Modifier.fillMaxWidth().heightIn(min=160.dp), minLines=5)
                if(backupMessage.isNotBlank()) Text(backupMessage, color = MaterialTheme.colorScheme.primary)
            }
        }, confirmButton = {
            TextButton(onClick = {
                backupText = LocalBackupRules.json(playerName, games, registrations, created)
                backupMessage = "Copia generada. Selecciona y copia el texto."
            }) { Text("Generar") }
        }, dismissButton = {
            Row {
                TextButton(onClick = { try { pendingBackupRestore=LocalBackupRules.parse(backupText); backupMessage="Copia válida. Revisa el resumen antes de restaurar."; } catch(e: Exception) { backupMessage=e.message ?: "Copia inválida" } }) { Text("Revisar importación") }
                TextButton(onClick = { backupDialog = false; backupMessage = "" }) { Text("Cerrar") }
            }
        })
    }

    pendingDeletion?.let { event ->
        AlertDialog(onDismissRequest = { pendingDeletion = null }, title = { Text("Eliminar torneo local") }, text = {
            Text("¿Quieres eliminar \"${event.title}\" de este dispositivo? Esta acción no se puede deshacer.")
        }, confirmButton = { TextButton(onClick = { deleteEvent(event.id); selectedEvent = null; pendingDeletion = null }, modifier = Modifier.testTag("tournament-delete-confirm")) { Text("Eliminar") } }, dismissButton = { TextButton(onClick = { pendingDeletion = null }) { Text("Cancelar") } })
    }

    pendingBackupRestore?.let { data ->
        val preview = LocalBackupRules.preview(data)
        AlertDialog(onDismissRequest = { pendingBackupRestore = null }, title = { Text("Confirmar restauración") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Se sustituirán los datos locales actuales por los datos de la copia.")
                Text("Nombre: ${preview.playerName}")
                Text("Juegos favoritos: ${preview.gamesCount}")
                Text("Inscripciones locales: ${preview.registrationsCount}")
                Text("Torneos creados: ${preview.createdTournamentsCount}")
            }
        }, confirmButton = { TextButton(onClick = { playerName=data.playerName; games=data.games; registrations=data.registrations; created=data.createdTournaments; persist(); backupMessage="Restauración completada."; pendingBackupRestore=null }) { Text("Restaurar") } }, dismissButton = { TextButton(onClick = { pendingBackupRestore=null }) { Text("Cancelar") } })
    }
}

@Composable
private fun HomeTab(name: String, games: Set<String>, eventCount: Int, registrationCount: Int, onTournaments: () -> Unit, onProfile: () -> Unit, openSite: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Academia Alius X", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Hola, ${PlayerProfileRules.displayName(name)}", style = MaterialTheme.typography.titleLarge)
        StatusNote("Modo nativo local. Los datos de muestra no están publicados ni sincronizados.")
        ElevatedCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tu actividad", style = MaterialTheme.typography.titleMedium)
                Text("$eventCount torneos visibles · $registrationCount inscripciones locales")
                Text(if(games.isEmpty()) "Aún no has elegido juegos." else games.sorted().joinToString(" · "))
                Button(onClick=onTournaments) { Text("Explorar torneos") }
                OutlinedButton(onClick=onProfile) { Text("Completar perfil") }
            }
        }
        ElevatedCard { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Academia Alius X online", style = MaterialTheme.typography.titleMedium); Text("La web oficial contiene la actividad real y funciones que aún no están verificadas en la app nativa."); OutlinedButton(onClick=openSite) { Text("Abrir sitio oficial") } } }
    }
}

@Composable
private fun TournamentTab(visible: List<LocalTournament>, registrations: Set<String>, createdIds: Set<String>, query: String, mode: String, setQuery: (String)->Unit, setMode:(String)->Unit, create:()->Unit, open:(LocalTournament)->Unit, toggle:(String)->Unit, share:(LocalTournament)->Unit, edit:(LocalTournament)->Unit, delete:(String)->Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal=16.dp, vertical=12.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween) { Text("Torneos", style=MaterialTheme.typography.headlineMedium, fontWeight=FontWeight.Bold); Button(onClick=create, modifier = Modifier.testTag("tournament-create")) { Icon(Icons.Filled.Add, null); Text("Crear") } }
        StatusNote("Listado local/de muestra. No consulta ni publica torneos online.")
        OutlinedTextField(query, setQuery, modifier=Modifier.fillMaxWidth(), label={Text("Buscar torneo")}, leadingIcon={Icon(Icons.Filled.Search, null)}, singleLine=true)
        Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) {
            listOf("all" to "Todos","available" to "Disponibles","registered" to "Inscritos","created" to "Creados").forEach { (key,label) ->
                FilterChip(selected=mode==key,onClick={setMode(key)},label={Text(label)})
            }
        }
        Text(LocalTournamentRules.resultSummary(visible.size), style=MaterialTheme.typography.labelLarge)
        if(visible.isEmpty()) { Box(Modifier.fillMaxWidth().weight(1f), contentAlignment=Alignment.Center) { Text("No hay coincidencias. Prueba otra búsqueda o crea un torneo local.") } }
        else LazyColumn(modifier=Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(8.dp)) {
            items(visible, key={it.id}) { event ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(event.title, style=MaterialTheme.typography.titleMedium, fontWeight=FontWeight.SemiBold)
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { AssistChip(onClick={open(event)}, label={Text(if(event.id in createdIds) "Local" else "Muestra") }); if(event.id in registrations) AssistChip(onClick={open(event)}, label={Text("Inscrito local")}) }
                        Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) { Button(onClick={open(event)}, modifier = Modifier.testTag("tournament-details-${event.id}")) { Text("Detalles") }; TextButton(onClick={toggle(event.id)}) { Text(if(event.id in registrations) "Retirar" else "Inscribirse") }; IconButton(onClick={share(event)} ) { Icon(Icons.Filled.Share, contentDescription="Compartir torneo") } }
                        if(event.id in createdIds) Row { TextButton(onClick={edit(event)}) { Icon(Icons.Filled.Edit, null); Text("Editar") }; TextButton(onClick={delete(event.id)}) { Icon(Icons.Filled.Delete, null); Text("Eliminar") } }
                    }
                }
            }
        }
    }
}



@Composable
private fun LocalTournamentDetailScreen(
    event: LocalTournament,
    registered: Boolean,
    created: Boolean,
    onBack: () -> Unit,
    onToggleRegistration: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().testTag("tournament-detail-screen").verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("tournament-detail-back")) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver a torneos") }
            Text("Detalle de torneo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(event.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(LocalTournamentRules.detailSourceLabel(created), style = MaterialTheme.typography.labelLarge)
                Divider()
                Text(LocalTournamentRules.detailRegistrationState(registered), style = MaterialTheme.typography.titleMedium)
                Text(
                    "Esta ficha contiene únicamente información local o de demostración. No hay fechas, plazas, reglas, resultados ni organizador confirmados por el servidor.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("ID local: ${event.id}", style = MaterialTheme.typography.bodySmall)
            }
        }
        Button(onClick = onToggleRegistration, modifier = Modifier.fillMaxWidth().testTag("tournament-registration-toggle")) {
            Text(LocalTournamentRules.detailRegistrationLabel(registered))
        }
        OutlinedButton(onClick = onShare, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Compartir ficha")
        }
        if (created) {
            OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Editar torneo local")
            }
            TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth().testTag("tournament-detail-delete")) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Eliminar torneo local")
            }
        }
        StatusNote("Las inscripciones, modificaciones y eliminaciones de esta ficha solo afectan a este dispositivo.")
    }
}

@Composable
private fun CommunityTab(openSite: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Comunidad", style=MaterialTheme.typography.headlineMedium, fontWeight=FontWeight.Bold)
        StatusNote("La comunidad nativa requiere contratos API y autorización verificados. No mostramos datos ficticios.")
        ElevatedCard { Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("Funciones pendientes de integración segura", style=MaterialTheme.typography.titleMedium)
            Text("Búsqueda de jugadores, solicitudes, bloqueos, equipos y mensajes se habilitarán cuando se verifiquen las rutas y permisos del backend.")
            Button(onClick=openSite) { Text("Abrir comunidad oficial") }
        } }
    }
}

@Composable
private fun ProfileTab(name: String, games: Set<String>, edit:()->Unit, backup:()->Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Perfil", style=MaterialTheme.typography.headlineMedium, fontWeight=FontWeight.Bold)
        ElevatedCard { Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) { Text(PlayerProfileRules.displayName(name), style=MaterialTheme.typography.titleLarge); Text(if(games.isEmpty()) "Sin juegos favoritos" else games.sorted().joinToString(" · ")); OutlinedButton(onClick=edit, modifier = Modifier.testTag("profile-edit")) { Icon(Icons.Filled.Edit, null); Text("Editar perfil") } } }
        ElevatedCard { Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) { Text("Tus datos están guardados en este dispositivo.", style=MaterialTheme.typography.bodyMedium); Text("No son un perfil online ni se sincronizan con la web."); Button(onClick=backup) { Text("Importar / exportar copia") } } }
    }
}

@Composable
private fun TitleDialog(title: String, initial: String, action: String, cancel:()->Unit, save:(String)->Unit) {
    var value by remember(initial) { mutableStateOf(initial) }
    AlertDialog(onDismissRequest=cancel, title={Text(title)}, text={ Column { OutlinedTextField(value,{value=LocalTournamentRules.cleanTitleInput(it)},modifier=Modifier.testTag("title-dialog-input"),label={Text("Título")},singleLine=true); Text("${value.length}/${LocalTournamentRules.MAX_TITLE_LENGTH}",style=MaterialTheme.typography.labelSmall) } }, confirmButton={ TextButton(onClick={save(value)},modifier=Modifier.testTag("title-dialog-save"),enabled=LocalTournamentRules.validTitle(value)) { Text(action) } }, dismissButton={TextButton(onClick=cancel){Text("Cancelar")}})
}

@Composable
private fun StatusNote(text: String) { Surface(color=MaterialTheme.colorScheme.secondaryContainer, shape=MaterialTheme.shapes.medium) { Text(text, Modifier.padding(12.dp), style=MaterialTheme.typography.bodySmall) } }
