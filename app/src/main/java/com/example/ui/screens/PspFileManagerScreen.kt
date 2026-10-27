package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PspSoundEngine
import com.example.model.PspStorageItem
import com.example.model.PspStorageManager

@Composable
fun PspFileManagerScreen(
    soundEngine: PspSoundEngine,
    onBack: () -> Unit,
    onLaunchGameOrFile: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var currentSubPath by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<PspStorageItem>>(emptyList()) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Reload directory items on path change
    fun refreshItems() {
        PspStorageManager.initializePspFileSystem(context)
        items = PspStorageManager.listDirectoryItems(context, currentSubPath)
    }

    LaunchedEffect(currentSubPath) {
        refreshItems()
    }

    // Auto-dismiss toast
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(2000)
            toastMessage = null
        }
    }

    val currentFolderDisplay = if (currentSubPath.isEmpty()) "PSP" else currentSubPath.substringAfterLast("/")
    val displayPath = if (currentSubPath.isEmpty()) ".../Pictures/PSP" else ".../Pictures/PSP/$currentSubPath"

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("psp_file_manager_screen"),
        containerColor = Color(0xFF1B1410), // Authentic dark brown ZArchiver background
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    soundEngine.playConfirm()
                    showCreateFolderDialog = true
                },
                containerColor = Color(0xFFE58052), // Orange FAB from screenshot
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .testTag("create_folder_fab")
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Crear carpeta",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Bar matching ZArchiver screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B2019))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        soundEngine.playCancel()
                        if (currentSubPath.isNotEmpty()) {
                            currentSubPath = currentSubPath.substringBeforeLast("/", "")
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier.testTag("fm_back_btn")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentFolderDisplay,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = displayPath,
                        color = Color(0xFFAAAAAA),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }

                IconButton(onClick = { soundEngine.playConfirm() }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White)
                }

                IconButton(onClick = { soundEngine.playConfirm() }) {
                    Icon(Icons.Default.ViewList, contentDescription = "Modo lista", tint = Color.White)
                }

                IconButton(onClick = {
                    soundEngine.playConfirm()
                    refreshItems()
                    toastMessage = "Carpetas PSP verificadas en almacenamiento"
                }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = Color.White)
                }
            }

            // Sync Banner info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF38291F))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Almacenamiento Memory Stick™ activo",
                    color = Color(0xFFFFB74D),
                    fontSize = 11.sp
                )
                Text(
                    text = "${items.size} elementos",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }

            // File & Folder List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp)
            ) {
                // Parent folder navigation row ".." (green up arrow in folder)
                if (currentSubPath.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    soundEngine.playCancel()
                                    currentSubPath = currentSubPath.substringBeforeLast("/", "")
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE58022)), // Orange folder
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⬆", color = Color(0xFF66BB6A), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "..",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                items(items, key = { it.relativePath }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (item.isDirectory) {
                                    soundEngine.playConfirm()
                                    currentSubPath = item.relativePath
                                } else {
                                    soundEngine.playConfirm()
                                    toastMessage = "Archivo: ${item.name}"
                                    onLaunchGameOrFile(item.name)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Folder / File Icon matching orange ZArchiver look
                        if (item.isDirectory) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE58022)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF3E4A59)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = Color.Cyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = item.sizeText,
                                color = Color(0xFFAAAAAA),
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = item.dateText,
                            color = Color(0xFF888888),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Live notification toast
        toastMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2E2218))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = msg, color = Color(0xFFFFB74D), fontSize = 12.sp)
                }
            }
        }

        // Create Folder Dialog
        if (showCreateFolderDialog) {
            AlertDialog(
                onDismissRequest = { showCreateFolderDialog = false },
                title = { Text("Nueva carpeta", color = Color.White) },
                text = {
                    Column {
                        Text(
                            "Crear carpeta en $displayPath",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            placeholder = { Text("Nombre de la carpeta") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFE58022)
                            ),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newFolderName.isNotBlank()) {
                            PspStorageManager.createFolder(context, currentSubPath, newFolderName)
                            soundEngine.playConfirm()
                            newFolderName = ""
                            showCreateFolderDialog = false
                            refreshItems()
                        }
                    }) {
                        Text("Crear", color = Color(0xFFE58022), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateFolderDialog = false }) {
                        Text("Cancelar", color = Color.LightGray)
                    }
                },
                containerColor = Color(0xFF241A14)
            )
        }
    }
}
