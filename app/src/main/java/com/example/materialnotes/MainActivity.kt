package com.example.materialnotes

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class Note(
    val id: Long,
    val title: String = "",
    val body: String = "",
    val colorIndex: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isTrashed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

private enum class NotesSection(val label: String) {
    Notes("Notes"),
    Pinned("Pinned"),
    Archive("Archive"),
    Trash("Trash")
}

private object NoteStorage {
    private const val PREFS = "material_notes_data"
    private const val NOTES_KEY = "notes"

    fun load(context: Context): List<Note> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(NOTES_KEY, "[]") ?: "[]"
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(
                        Note(
                            id = item.optLong("id", System.currentTimeMillis() + index),
                            title = item.optString("title"),
                            body = item.optString("body"),
                            colorIndex = item.optInt("colorIndex", 0).coerceIn(0, 7),
                            isPinned = item.optBoolean("isPinned", false),
                            isArchived = item.optBoolean("isArchived", false),
                            isTrashed = item.optBoolean("isTrashed", false),
                            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, notes: List<Note>) {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(
                JSONObject()
                    .put("id", note.id)
                    .put("title", note.title)
                    .put("body", note.body)
                    .put("colorIndex", note.colorIndex)
                    .put("isPinned", note.isPinned)
                    .put("isArchived", note.isArchived)
                    .put("isTrashed", note.isTrashed)
                    .put("createdAt", note.createdAt)
                    .put("updatedAt", note.updatedAt)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(NOTES_KEY, array.toString())
            .apply()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialNotesApp()
        }
    }
}

@Composable
private fun MaterialNotesApp() {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("material_notes_settings", Context.MODE_PRIVATE)
    }

    var notes by remember { mutableStateOf(NoteStorage.load(context)) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var themeMode by remember { mutableStateOf(preferences.getString("theme_mode", "system") ?: "system") }
    var dynamicColor by remember { mutableStateOf(preferences.getBoolean("dynamic_color", true)) }

    val saveNote: (Note) -> Unit = { edited ->
        val timestamp = System.currentTimeMillis()
        val existing = notes.firstOrNull { it.id == edited.id }
        val stored = edited.copy(
            createdAt = existing?.createdAt ?: edited.createdAt,
            updatedAt = timestamp
        )
        notes = (notes.filterNot { it.id == stored.id } + stored)
        NoteStorage.save(context, notes)
        editingNote = null
    }

    val updateNote: (Note) -> Unit = { changed ->
        notes = notes.map { if (it.id == changed.id) changed.copy(updatedAt = System.currentTimeMillis()) else it }
        NoteStorage.save(context, notes)
    }

    MaterialNotesTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
        if (editingNote != null) {
            NoteEditorScreen(
                note = editingNote!!,
                onSave = saveNote,
                onDismiss = { editingNote = null },
                onMoveToTrash = { note ->
                    updateNote(note.copy(isTrashed = true, isArchived = false, isPinned = false))
                    editingNote = null
                }
            )
        } else {
            NotesHomeScreen(
                notes = notes,
                onCreate = {
                    editingNote = Note(id = System.currentTimeMillis())
                },
                onEdit = { editingNote = it },
                onUpdateNote = updateNote,
                onRestore = { note -> updateNote(note.copy(isTrashed = false, isArchived = false)) },
                onDeleteForever = { note ->
                    notes = notes.filterNot { it.id == note.id }
                    NoteStorage.save(context, notes)
                },
                themeMode = themeMode,
                dynamicColor = dynamicColor,
                onThemeModeChange = {
                    themeMode = it
                    preferences.edit().putString("theme_mode", it).apply()
                },
                onDynamicColorChange = {
                    dynamicColor = it
                    preferences.edit().putBoolean("dynamic_color", it).apply()
                }
            )
        }
    }
}

@Composable
private fun MaterialNotesTheme(
    themeMode: String,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesHomeScreen(
    notes: List<Note>,
    onCreate: () -> Unit,
    onEdit: (Note) -> Unit,
    onUpdateNote: (Note) -> Unit,
    onRestore: (Note) -> Unit,
    onDeleteForever: (Note) -> Unit,
    themeMode: String,
    dynamicColor: Boolean,
    onThemeModeChange: (String) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit
) {
    var section by remember { mutableStateOf(NotesSection.Notes) }
    var searchExpanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var gridMode by remember { mutableStateOf(true) }
    var showAppearance by remember { mutableStateOf(false) }

    val visibleNotes = remember(notes, section, query) {
        notes.asSequence()
            .filter { note ->
                when (section) {
                    NotesSection.Notes -> !note.isArchived && !note.isTrashed
                    NotesSection.Pinned -> note.isPinned && !note.isArchived && !note.isTrashed
                    NotesSection.Archive -> note.isArchived && !note.isTrashed
                    NotesSection.Trash -> note.isTrashed
                }
            }
            .filter {
                query.isBlank() ||
                    it.title.contains(query, ignoreCase = true) ||
                    it.body.contains(query, ignoreCase = true)
            }
            .sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
            .toList()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            when (section) {
                                NotesSection.Notes -> "Material Notes"
                                NotesSection.Pinned -> "Pinned"
                                NotesSection.Archive -> "Archive"
                                NotesSection.Trash -> "Trash"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            searchExpanded = !searchExpanded
                            if (!searchExpanded) query = ""
                        }) {
                            Icon(
                                if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (searchExpanded) "Close search" else "Search notes"
                            )
                        }
                        if (section != NotesSection.Trash) {
                            IconButton(onClick = { gridMode = !gridMode }) {
                                Icon(
                                    if (gridMode) Icons.Default.ViewAgenda else Icons.Default.GridView,
                                    contentDescription = if (gridMode) "Switch to list" else "Switch to grid"
                                )
                            }
                        }
                        IconButton(onClick = { showAppearance = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Appearance settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                if (searchExpanded) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                        placeholder = { Text("Search your notes") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NotesSection.values().forEach { item ->
                        FilterChip(
                            selected = section == item,
                            onClick = { section = item },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (section != NotesSection.Trash) {
                ExtendedFloatingActionButton(
                    onClick = onCreate,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New note") }
                )
            }
        }
    ) { innerPadding ->
        if (visibleNotes.isEmpty()) {
            EmptyNotesState(
                section = section,
                searching = query.isNotBlank(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyVerticalGrid(
                columns = if (gridMode && section != NotesSection.Trash) GridCells.Adaptive(190.dp) else GridCells.Fixed(1),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visibleNotes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onOpen = { onEdit(note) },
                        onTogglePin = { onUpdateNote(note.copy(isPinned = !note.isPinned)) },
                        onArchive = {
                            onUpdateNote(note.copy(isArchived = !note.isArchived, isPinned = false))
                        },
                        onMoveToTrash = {
                            onUpdateNote(note.copy(isTrashed = true, isArchived = false, isPinned = false))
                        },
                        onRestore = { onRestore(note) },
                        onDeleteForever = { onDeleteForever(note) }
                    )
                }
            }
        }
    }

    if (showAppearance) {
        AppearanceDialog(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            onThemeModeChange = onThemeModeChange,
            onDynamicColorChange = onDynamicColorChange,
            onDismiss = { showAppearance = false }
        )
    }
}

@Composable
private fun EmptyNotesState(
    section: NotesSection,
    searching: Boolean,
    modifier: Modifier = Modifier
) {
    val (headline, supporting) = when {
        searching -> "No results" to "Try another search term."
        section == NotesSection.Trash -> "Trash is empty" to "Deleted notes will appear here."
        section == NotesSection.Archive -> "Nothing archived" to "Archive notes to keep them out of your main list."
        section == NotesSection.Pinned -> "Nothing pinned yet" to "Pin important notes to find them quickly."
        else -> "Your notes live here" to "Tap New note to capture an idea, a reminder, or anything worth keeping."
    }

    Box(modifier = modifier.padding(28.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.NoteAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(54.dp)
            )
            Text(headline, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                supporting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onOpen: () -> Unit,
    onTogglePin: () -> Unit,
    onArchive: () -> Unit,
    onMoveToTrash: () -> Unit,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val background = noteCardColor(note.colorIndex)

    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(modifier = Modifier.padding(start = 15.dp, top = 13.dp, end = 9.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = note.title.ifBlank { "Untitled note" },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.isPinned && !note.isTrashed) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Note options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (note.isTrashed) {
                            DropdownMenuItem(
                                text = { Text("Restore") },
                                leadingIcon = { Icon(Icons.Default.RestoreFromTrash, null) },
                                onClick = { menuExpanded = false; onRestore() }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete forever") },
                                leadingIcon = { Icon(Icons.Default.DeleteForever, null) },
                                onClick = { menuExpanded = false; confirmDelete = true }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text(if (note.isPinned) "Unpin" else "Pin") },
                                leadingIcon = { Icon(Icons.Default.PushPin, null) },
                                onClick = { menuExpanded = false; onTogglePin() }
                            )
                            DropdownMenuItem(
                                text = { Text(if (note.isArchived) "Unarchive" else "Archive") },
                                leadingIcon = { Icon(Icons.Default.Archive, null) },
                                onClick = { menuExpanded = false; onArchive() }
                            )
                            DropdownMenuItem(
                                text = { Text("Move to trash") },
                                leadingIcon = { Icon(Icons.Default.Delete, null) },
                                onClick = { menuExpanded = false; onMoveToTrash() }
                            )
                        }
                    }
                }
            }

            if (note.body.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    note.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (note.title.isBlank()) 7 else 5,
                    overflow = TextOverflow.Ellipsis
                )
            } else if (note.title.isBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "No additional text",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatDate(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                if (note.isArchived) {
                    Text("Archived", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (note.isTrashed) {
                    Text("In trash", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete permanently?") },
            text = { Text("This note will be deleted and cannot be restored.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDeleteForever() }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AppearanceDialog(
    themeMode: String,
    dynamicColor: Boolean,
    onThemeModeChange: (String) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Appearance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Theme", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                listOf("system" to "System default", "light" to "Light", "dark" to "Dark").forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeModeChange(value) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = themeMode == value, onClick = { onThemeModeChange(value) })
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dynamic color", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Use colors from your device wallpaper",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = dynamicColor, onCheckedChange = onDynamicColorChange)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditorScreen(
    note: Note,
    onSave: (Note) -> Unit,
    onDismiss: () -> Unit,
    onMoveToTrash: (Note) -> Unit
) {
    var title by remember(note.id) { mutableStateOf(note.title) }
    var body by remember(note.id) { mutableStateOf(note.body) }
    var colorIndex by remember(note.id) { mutableStateOf(note.colorIndex) }
    var pinned by remember(note.id) { mutableStateOf(note.isPinned) }

    fun finishEditing() {
        if (title.isBlank() && body.isBlank()) {
            onDismiss()
        } else {
            onSave(
                note.copy(
                    title = title.trim(),
                    body = body,
                    colorIndex = colorIndex,
                    isPinned = pinned
                )
            )
        }
    }

    BackHandler { finishEditing() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (note.title.isBlank() && note.body.isBlank()) "New note" else "Edit note",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { finishEditing() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!note.isTrashed) {
                        IconButton(onClick = { pinned = !pinned }) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = if (pinned) "Unpin note" else "Pin note",
                                tint = if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (title.isNotBlank() || body.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onMoveToTrash(
                                    note.copy(
                                        title = title.trim(),
                                        body = body,
                                        colorIndex = colorIndex,
                                        isPinned = false
                                    )
                                )
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Move note to trash")
                        }
                    }
                    IconButton(onClick = { finishEditing() }) {
                        Icon(Icons.Default.Check, contentDescription = "Save note")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title") },
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                singleLine = false,
                maxLines = 3,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                placeholder = { Text("Start writing...") },
                textStyle = MaterialTheme.typography.bodyLarge,
                minLines = 10,
                maxLines = 30,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(22.dp))
            Text("Note color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (index in 0..7) {
                    val swatch = noteCardColor(index)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(swatch)
                            .clickable { colorIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        if (colorIndex == index) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected color",
                                tint = if (swatch.luminance() < 0.5f) Color.White else Color.Black,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "Edited " + formatDate(note.updatedAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun noteCardColor(index: Int): Color {
    val lightColors = listOf(
        MaterialTheme.colorScheme.surfaceContainerLow,
        Color(0xFFFFF0E8),
        Color(0xFFFFF5D9),
        Color(0xFFE9F4EA),
        Color(0xFFE7F2FA),
        Color(0xFFF0EAF8),
        Color(0xFFFBE8EF),
        Color(0xFFE8F2EF)
    )
    val darkColors = listOf(
        MaterialTheme.colorScheme.surfaceContainerLow,
        Color(0xFF3E302B),
        Color(0xFF3C3523),
        Color(0xFF29392D),
        Color(0xFF273743),
        Color(0xFF342D40),
        Color(0xFF402D35),
        Color(0xFF293A36)
    )
    val palette = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) darkColors else lightColors
    return palette[index.coerceIn(0, palette.lastIndex)]
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
