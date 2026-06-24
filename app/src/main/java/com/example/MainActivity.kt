package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

// CameraX, Permissions, and System imports
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview as CameraPreviewX
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.ui.platform.LocalLifecycleOwner

class MainActivity : ComponentActivity() {
    private lateinit var database: LoveDatabase
    private lateinit var repository: LoveRepository
    private lateinit var viewModel: LoveViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room local storage
        database = Room.databaseBuilder(
            applicationContext,
            LoveDatabase::class.java,
            "love_database"
        ).fallbackToDestructiveMigration().build()

        repository = LoveRepository(database.loveDao())

        val sharedPrefs = getSharedPreferences("pure_soul_love_prefs", android.content.Context.MODE_PRIVATE)

        // Initialize ViewModel using manual Factory for simple constructor injection
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LoveViewModel(repository, sharedPrefs) as T
            }
        })[LoveViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val signedInUser by viewModel.signedInUser.collectAsStateWithLifecycle()
                if (signedInUser == null) {
                    LoginScreen(viewModel)
                } else {
                    MainAppScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainAppScreen(viewModel: LoveViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showAboutDialog by remember { mutableStateOf(false) }
    var showCameraView by remember { mutableStateOf(false) }
    var showLoverryGallery by remember { mutableStateOf(false) }

    // Quotes that display randomly at the top for inspiration
    val inspirationalQuotes = listOf(
        "Love is not only romance – it is daily shared responsibilities and mutual respect.",
        "Deep trust is built when your commitments match your action steps, even in small chores.",
        "Listening patiently without preparing an argument is the ultimate gift of care.",
        "A healthy balanced relationship is where both partners invest visual and physical effort.",
        "Loyalty is being transparent in your actions, honest in your words, and gentle in conflict."
    )
    val quoteIndex = remember { mutableStateOf((0..4).random()) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "RELATIONSHIP COMPANION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Pure Soul Love",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                            Text(
                                text = "💖",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { showLoverryGallery = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFE91E63).copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Open Loverry Gallery",
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { showCameraView = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Open Moments Camera",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Elegant Avatar badge featuring you with custom warm borders
                        Image(
                            painter = painterResource(id = R.drawable.img_love_app_logo_new_1782243568349),
                            contentDescription = "User Avatar",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                                .shadow(2.dp, CircleShape)
                                .clickable { showAboutDialog = true },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Inspirational Quote styled inside a supportive Natural Tones card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = inspirationalQuotes[quoteIndex.value],
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "New Quote",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { quoteIndex.value = (0..4).random() }
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .shadow(12.dp)
                    .navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val tabs = listOf(
                    Triple(0, "Pillars", Icons.Default.Spa),
                    Triple(1, "Challenges", Icons.Default.Checklist),
                    Triple(2, "Devotions", Icons.Default.Psychology),
                    Triple(3, "Gratitude", Icons.Default.Favorite),
                    Triple(4, "Social Chat", Icons.Default.Chat),
                    Triple(5, "Try Love", Icons.Default.SportsEsports)
                )
                tabs.forEach { (index, title, icon) ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { viewModel.setTab(index) },
                        icon = { Icon(imageVector = icon, contentDescription = title) },
                        label = { Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_tab_$index")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            when (currentTab) {
                0 -> PillarsScreen()
                1 -> ChallengesScreen(viewModel)
                2 -> DevotionCheckupsScreen(viewModel)
                3 -> GratitudeJarScreen(viewModel)
                4 -> SocialChatHubScreen(viewModel)
                5 -> LoveGameScreen(viewModel)
            }
        }
    }

    if (showAboutDialog) {
        Dialog(onDismissRequest = { showAboutDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Logo Image
                    Image(
                        painter = painterResource(id = R.drawable.img_love_app_logo_new_1782243568349),
                        contentDescription = "Pure Soul Love Splash Brand Mark",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .shadow(4.dp, RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Text(
                        text = "Pure Soul Love",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = (-0.5).sp
                        )
                    )

                    Text(
                        text = "v1.2.0 • Secured Companion Portal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Text(
                        text = "A modern, secure sanctuary built to cultivate conscious habits, transparency, and loyalty between partners. No cloud backups, no metrics – your bond is private and fully yours.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showAboutDialog = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Close Branding Card", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            showAboutDialog = false
                            viewModel.signOutUser()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Log Out",
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Log Out Account", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showCameraView) {
        CameraViewOverlay(viewModel, onDismiss = { showCameraView = false })
    }

    if (showLoverryGallery) {
        LoverryGalleryOverlay(viewModel, onDismiss = { showLoverryGallery = false })
    }
}

// ==================== LOVERRY GALLERY ENGINE ====================
@Composable
fun LoverryGalleryOverlay(viewModel: LoveViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val loverryPhotos by viewModel.loverryPhotos.collectAsStateWithLifecycle()
    var currentSubTab by remember { mutableStateOf(0) } // 0: Gallery, 1: Add Picture

    // Form inputs
    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf("preset_sunset") }

    val presets = listOf(
        Triple("preset_sunset", "Sunset Bliss", R.drawable.img_loverry_sunset_1782244034482),
        Triple("preset_cafe", "Cozy Café", R.drawable.img_loverry_cafe_1782244053493),
        Triple("preset_hands", "Holding Hands", R.drawable.img_loverry_hands_1782244068417),
        Triple("preset_sanctuary", "Pure Sanctuary", R.drawable.img_pure_soul_hero_1782200050790),
        Triple("preset_devotion", "Love Devotion", R.drawable.img_love_hero_banner_1782206379617)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Loverry",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE91E63)
                                )
                            )
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = "Love", tint = Color(0xFFE91E63), modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = "Your private sanctuary of shared pictures",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Loverry")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                        .padding(4.dp)
                ) {
                    listOf("Gallery Grid", "Add Picture").forEachIndexed { index, name ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (currentSubTab == index) Color(0xFFE91E63) else Color.Transparent)
                                .clickable { currentSubTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentSubTab == index) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentSubTab == 0) {
                    // Gallery Grid Tab
                    if (loverryPhotos.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = "Empty Loverry",
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loverry is empty!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Start by saving your moments here using the 'Add Picture' tab or taking beautiful photos using the Moments Camera.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(loverryPhotos) { photo ->
                                val imageResId = when (photo.imagePath) {
                                    "preset_sunset" -> R.drawable.img_loverry_sunset_1782244034482
                                    "preset_cafe" -> R.drawable.img_loverry_cafe_1782244053493
                                    "preset_hands" -> R.drawable.img_loverry_hands_1782244068417
                                    "preset_sanctuary" -> R.drawable.img_pure_soul_hero_1782200050790
                                    "preset_devotion" -> R.drawable.img_love_hero_banner_1782206379617
                                    else -> R.drawable.img_love_app_logo_new_1782243568349
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column {
                                        Image(
                                            painter = painterResource(id = imageResId),
                                            contentDescription = photo.title,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = photo.title,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                )
                                                IconButton(
                                                    onClick = { viewModel.deleteLoverryPhoto(photo) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete picture", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = photo.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Saved on: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(photo.timestamp)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                                TextButton(
                                                    onClick = {
                                                        Toast.makeText(context, "Moment shared with partner securely! 💖", Toast.LENGTH_SHORT).show()
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(14.dp))
                                                        Text("Share", style = MaterialTheme.typography.labelMedium)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Add Picture Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Picture Title") },
                            placeholder = { Text("e.g., Cozy Sunday Morning") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            label = { Text("Love Story/Description") },
                            placeholder = { Text("Write a loving description of this saved memory...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2
                        )

                        Text(
                            text = "Choose Image Preset",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(presets) { preset ->
                                val presetKey = preset.first
                                val presetName = preset.second
                                val presetResId = preset.third
                                val isSelected = selectedPreset == presetKey
                                Card(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .clickable { selectedPreset = presetKey },
                                    border = BorderStroke(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFE91E63) else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Image(
                                            painter = painterResource(id = presetResId),
                                            contentDescription = presetName,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(80.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isSelected) Color(0xFFE91E63).copy(alpha = 0.1f) else Color.Transparent)
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = presetName,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (titleInput.isBlank()) {
                                    Toast.makeText(context, "Please enter a title!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.addLoverryPhoto(titleInput, descInput, selectedPreset)
                                    titleInput = ""
                                    descInput = ""
                                    currentSubTab = 0 // Go back to grid
                                    Toast.makeText(context, "Saved successfully into Loverry! 📸✨", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                                Text("Add to Loverry Gallery", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== SCREEN 1: THE PILLARS OF SOUL LOVE ====================
@Composable
fun PillarsScreen() {
    val scrollState = rememberScrollState()
    var expandedPillar by remember { mutableStateOf<Int?>(null) }
    var commitmentDone = remember { mutableStateMapOf<Int, Boolean>() }

    val pillarsList = listOf(
        PillarData(
            id = 1,
            title = "Trust & Loyalty",
            icon = Icons.Default.Security,
            cardColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            overview = "Unwavering consistency, transparency, and gentle emotional availability.",
            wisdom = "Trust is not built in major milestones, but in daily micro-moments. It grows when you keep your promises, avoid hiding small details, and offer physical and emotional reliability.",
            scenario = "Scenario: Your partner feels anxious due to past emotional letdowns. How do you respond?\n\nReal Action: Reassure them gently, validate their fears with calm, present listening, and establish clear transparency about your plans.",
            commitMsg = "Commit to direct, open calendar and updates transparency"
        ),
        PillarData(
            id = 2,
            title = "Respect & Honest Apologies",
            icon = Icons.Default.WorkspacePremium,
            cardColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            overview = "Value your partner's emotional boundaries and take accountability for friction.",
            wisdom = "Respect is recognizing that your partner's opinions, time, and needs are equal to your own. True love means admitting when you are wrong and saying 'I am sorry' without defensive excuses.",
            scenario = "Scenario: A minor argument escalates because of exhaustion.\n\nReal Action: De-escalate immediately. Take a deep breath, apologize for any harsh tones, and ask, 'How can we solve this together?'",
            commitMsg = "Commit to say 'I am sorry' first if communication gets harsh"
        ),
        PillarData(
            id = 3,
            title = "Active Listening & Connection",
            icon = Icons.Default.Hearing,
            cardColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
            overview = "Listen to understand feelings rather than simply preparing an answer.",
            wisdom = "Empathetic active listening means giving undivided attention – phone face-down – and validating the underlying feelings of your partner without jumping to immediately solve their problems.",
            scenario = "Scenario: Your partner shares a stressful event at their workplace.\n\nReal Action: Put away screens, look into their eyes, and say, 'That sounds very difficult. Tell me more, I'm right here with you.'",
            commitMsg = "Commit to 15 minutes of uninterrupted emotional listening daily"
        ),
        PillarData(
            id = 4,
            title = "Shared Responsibilities",
            icon = Icons.Default.Handshake,
            cardColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
            overview = "Dividing domestic duties, financial planning, and efforts equally.",
            wisdom = "Resentment feeds on unbalanced efforts. Pure Soul Love requires both boys and girls to actively participate in home chores, meal preparation, and cleaning without being prompted.",
            scenario = "Scenario: Both partners return home exhausted, and the kitchen sink is full of dirty dishes.\n\nReal Action: Divide the tasks cleanly or tackle them side-by-side as a team, making it a connection habit rather than a burden.",
            commitMsg = "Commit to complete 1 priority chore without being asked today"
        ),
        PillarData(
            id = 5,
            title = "Mutual Care & Personal Growth",
            icon = Icons.Default.SelfImprovement,
            cardColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
            overview = "Supporting each other's career goals and holding space for silent rest.",
            wisdom = "Healthy commitment does not restrict individuality – it enhances it. Support your partner's professional aspirations, hobbies, and personal boundaries. Lift each other's dreams higher.",
            scenario = "Scenario: Your partner has an important exam or meeting scheduled and feels overwhelmed.\n\nReal Action: Comfort them, relieve them of standard domestic chores temporarily, and say, 'I believe in you, focus on your growth.'",
            commitMsg = "Commit to support 1 career goal or study session of my partner this week"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Visual Banner styled exactly like the HTML Soul Practice card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(1.dp, Color(0xFFF2B8B5), RoundedCornerShape(32.dp))
                    .shadow(2.dp, RoundedCornerShape(32.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_love_hero_banner_1782206379617),
                    contentDescription = "Pure Soul Love Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "TODAY'S SOUL PRACTICE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "The Art of Active Empathy",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "True love is built on listening to what isn't said. Focus on feeling your partner's silence.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f))
                        )
                    }
                    Button(
                        onClick = { expandedPillar = 3 }, // Focuses Pillar 3 (Active Listening)
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(text = "Begin Session", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "The 5 Pillars of Pure Soul Love",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Deep relationships do not survive on romance alone – true companionship requires joint dedication, emotional safety, shared burdens, and active efforts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val pillarsList = listOf(
            PillarData(
                id = 1,
                title = "Trust & Loyalty",
                icon = Icons.Default.Security,
                cardColor = Color(0xFFFFDAD4).copy(alpha = 0.5f),
                overview = "Unwavering consistency, transparency, and gentle emotional availability.",
                wisdom = "Trust is not built in major milestones, but in daily micro-moments. It grows when you keep your promises, avoid hiding small details, and offer physical and emotional reliability.",
                scenario = "Scenario: Your partner feels anxious due to past emotional letdowns. How do you respond?\n\nReal Action: Reassure them gently, validate their fears with calm, present listening, and establish clear transparency about your plans.",
                commitMsg = "Commit to direct, open calendar and updates transparency"
            ),
            PillarData(
                id = 2,
                title = "Respect & Honest Apologies",
                icon = Icons.Default.WorkspacePremium,
                cardColor = Color(0xFFF9DEDC).copy(alpha = 0.5f),
                overview = "Value your partner's emotional boundaries and take accountability for friction.",
                wisdom = "Respect is recognizing that your partner's opinions, time, and needs are equal to your own. True love means admitting when you are wrong and saying 'I am sorry' without defensive excuses.",
                scenario = "Scenario: A minor argument escalates because of exhaustion.\n\nReal Action: De-escalate immediately. Take a deep breath, apologize for any harsh tones, and ask, 'How can we solve this together?'",
                commitMsg = "Commit to say 'I am sorry' first if communication gets harsh"
            ),
            PillarData(
                id = 3,
                title = "Active Listening & Connection",
                icon = Icons.Default.Hearing,
                cardColor = Color(0xFFE8DEF8).copy(alpha = 0.5f),
                overview = "Listen to understand feelings rather than simply preparing an answer.",
                wisdom = "Empathetic active listening means giving undivided attention – phone face-down – and validating the underlying feelings of your partner without jumping to immediately solve their problems.",
                scenario = "Scenario: Your partner shares a stressful event at their workplace.\n\nReal Action: Put away screens, look into their eyes, and say, 'That sounds very difficult. Tell me more, I'm right here with you.'",
                commitMsg = "Commit to 15 minutes of uninterrupted emotional listening daily"
            ),
            PillarData(
                id = 4,
                title = "Shared Responsibilities",
                icon = Icons.Default.Handshake,
                cardColor = Color(0xFFFFDAD4).copy(alpha = 0.4f),
                overview = "Dividing domestic duties, financial planning, and efforts equally.",
                wisdom = "Resentment feeds on unbalanced efforts. Pure Soul Love requires both boys and girls to actively participate in home chores, meal preparation, and cleaning without being prompted.",
                scenario = "Scenario: Both partners return home exhausted, and the kitchen sink is full of dirty dishes.\n\nReal Action: Divide the tasks cleanly or tackle them side-by-side as a team, making it a connection habit rather than a burden.",
                commitMsg = "Commit to complete 1 priority chore without being asked today"
            ),
            PillarData(
                id = 5,
                title = "Mutual Care & Personal Growth",
                icon = Icons.Default.SelfImprovement,
                cardColor = Color(0xFFF9DEDC).copy(alpha = 0.4f),
                overview = "Supporting each other's career goals and holding space for silent rest.",
                wisdom = "Healthy commitment does not restrict individuality – it enhances it. Support your partner's professional aspirations, hobbies, and personal boundaries. Lift each other's dreams higher.",
                scenario = "Scenario: Your partner has an important exam or meeting scheduled and feels overwhelmed.\n\nReal Action: Comfort them, relieve them of standard domestic chores temporarily, and say, 'I believe in you, focus on your growth.'",
                commitMsg = "Commit to support 1 career goal or study session of my partner this week"
            )
        )

        items(pillarsList) { pillar ->
            val isExpanded = expandedPillar == pillar.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedPillar = if (isExpanded) null else pillar.id }
                    .shadow(1.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(pillar.cardColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = pillar.icon,
                                contentDescription = pillar.title,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pillar.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = pillar.overview,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand indication"
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Pure Soul Wisdom",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pillar.wisdom,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = pillar.scenario,
                                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val committed = commitmentDone[pillar.id] == true
                            Button(
                                onClick = { commitmentDone[pillar.id] = !committed },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("commit_button_${pillar.id}")
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (committed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = if (committed) Icons.Default.CheckCircle else Icons.Default.FavoriteBorder,
                                    contentDescription = "Commit Check"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (committed) "Commitment Saved! ❤️" else pillar.commitMsg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PillarData(
    val id: Int,
    val title: String,
    val icon: ImageVector,
    val cardColor: Color,
    val overview: String,
    val wisdom: String,
    val scenario: String,
    val commitMsg: String
)

// ==================== SCREEN 2: DAILY CONNECTION CHALLENGES ====================
@Composable
fun ChallengesScreen(viewModel: LoveViewModel) {
    val goalsList by viewModel.goals.collectAsStateWithLifecycle()
    val completedCount by viewModel.completedGoalsCount.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // High-fidelity progress summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Your Shared Growth Compass",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Balancing domestic responsibilities and trust exercises daily keeps your connection secure.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val totalGoals = goalsList.size
                    val progress = if (totalGoals > 0) completedCount.toFloat() / totalGoals else 0f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Challenges Complete: $completedCount / $totalGoals",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Joint Practices",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Goal", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Joint Goal", fontSize = 12.sp)
                }
            }
        }

        if (goalsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Empty challenges",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Connection Goals Defined",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        } else {
            items(goalsList) { goal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (goal.isCompleted) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = goal.isCompleted,
                            onCheckedChange = { viewModel.toggleGoalCompletion(goal) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("challenge_check_${goal.id}")
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (goal.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = goal.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            AssistChip(
                                onClick = {},
                                label = { Text(goal.pillar, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    labelColor = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }

                        if (goal.isCustom) {
                            IconButton(
                                onClick = { viewModel.deleteGoal(goal) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Goal",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add goal Overlay dialog
    if (showAddDialog) {
        var goalTitle by remember { mutableStateOf("") }
        var goalDesc by remember { mutableStateOf("") }
        var selectedPillar by remember { mutableStateOf("Trust") }
        val pillars = listOf("Trust & Loyalty", "Respect & Care", "Active Listening", "Responsibilities", "Honesty")

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Create Collaborative Goal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )

                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("Task Title (e.g. Wash dishes together)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = goalDesc,
                        onValueChange = { goalDesc = it },
                        label = { Text("Task Description (Define clear execution step)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Belongs to Pillar:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        pillars.forEach { p ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPillar = p }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedPillar == p,
                                    onClick = { selectedPillar = p },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = p, fontSize = 13.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (goalTitle.isNotBlank()) {
                                    viewModel.addCustomGoal(goalTitle, goalDesc, selectedPillar)
                                    showAddDialog = false
                                }
                            }
                        ) {
                            Text("Save Goal")
                        }
                    }
                }
            }
        }
    }
}

// ==================== SCREEN 3: SOUL QUIZZES ====================
@Composable
fun QuizzesScreen() {
    var activeQuizId by remember { mutableStateOf<Int?>(null) }

    val quizzes = listOf(
        QuizMeta(
            id = 1,
            title = "Trust & Loyalty Audit",
            description = "Deals with open communication, physical and device transparency, and emotional reliability.",
            icon = Icons.Default.Security,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        QuizMeta(
            id = 2,
            title = "Responsibilities & Effort Balance",
            description = "Assess equality of household chores, financial burden contribution, and initiative efforts.",
            icon = Icons.Default.Handshake,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        ),
        QuizMeta(
            id = 3,
            title = "Active Listening Verification",
            description = "Measures empathy levels and present behaviors during conflicts and daily concerns sharing.",
            icon = Icons.Default.Hearing,
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        )
    )

    if (activeQuizId == null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Soul Checkups",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Take offline relationship checkups together to review trust levels, equal domestic effort, and communication habits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(quizzes) { quiz ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeQuizId = quiz.id }
                        .shadow(1.dp, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(quiz.color),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = quiz.icon, contentDescription = quiz.title, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = quiz.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = quiz.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = "Go", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    } else {
        ActiveQuizWindow(
            quizId = activeQuizId!!,
            onClose = { activeQuizId = null }
        )
    }
}

data class QuizMeta(val id: Int, val title: String, val description: String, val icon: ImageVector, val color: Color)

@Composable
fun ActiveQuizWindow(quizId: Int, onClose: () -> Unit) {
    val quizTitle = when(quizId) {
        1 -> "Trust & Loyalty Audit"
        2 -> "Responsibilities & Effort Balance"
        else -> "Active Listening Verification"
    }

    val questions = when(quizId) {
        1 -> listOf(
            QuizQuestion(
                "How open are you regarding sharing details of your conversations and plans with your partner?",
                listOf(
                    QuizAnswer("Fully transparent – we speak with perfect openness and respect.", 3),
                    QuizAnswer("I share final schedules but hide a few details to avoid friction.", 2),
                    QuizAnswer("I prefer keeping my plans, conversations, and schedules private.", 1)
                )
            ),
            QuizQuestion(
                "If you receive sudden plans with friends, what action do you take?",
                listOf(
                    QuizAnswer("Consult my partner immediately, showing care for their scheduled day.", 3),
                    QuizAnswer("Inform them right as I am leaving.", 2),
                    QuizAnswer("Go without checking, believing relationships shouldn't limit freedom.", 1)
                )
            ),
            QuizQuestion(
                "When your partner asks a question regarding a doubt or worry, how do you respond?",
                listOf(
                    QuizAnswer("Listen patiently, validate them calmly, and clarify details honestly.", 3),
                    QuizAnswer("Feel minor annoyance but explain it anyway to move on.", 2),
                    QuizAnswer("Turn defensive immediately, bringing up their past insecurities.", 1)
                )
            )
        )
        2 -> listOf(
            QuizQuestion(
                "How are routine daily chores (cooking, cleaning, shopping) divided in your life?",
                listOf(
                    QuizAnswer("Distributed equally – we proactively complete them together.", 3),
                    QuizAnswer("One partner bears the main weight, but the other occasionally assists.", 2),
                    QuizAnswer("We never divide them – we argue about chores constantly.", 1)
                )
            ),
            QuizQuestion(
                "When one partner is clearly exhausted, what does the other do?",
                listOf(
                    QuizAnswer("Proactively takes over all chores and urges the other to rest.", 3),
                    QuizAnswer("Asks 'Do you need help?' but waits for explicit instructions.", 2),
                    QuizAnswer("Tells them to handle their part later, ignoring the exhaustion.", 1)
                )
            ),
            QuizQuestion(
                "Who takes the initiative in organizing connection dates, bills paying, and life planning?",
                listOf(
                    QuizAnswer("We coordinate as a unified team, dividing intellectual effort.", 3),
                    QuizAnswer("One partner carries the design load, while the other agrees.", 2),
                    QuizAnswer("We behave passively, leading to chaotic household planning.", 1)
                )
            )
        )
        else -> listOf(
            QuizQuestion(
                "When your partner is venting about a frustrating workday, what is your first action?",
                listOf(
                    QuizAnswer("Put my phone face down, provide eye contact, and validate their feelings.", 3),
                    QuizAnswer("Listen partially while scrolling or doing something else.", 2),
                    QuizAnswer("Interrupt with logical advice directly, telling them to change jobs.", 1)
                )
            ),
            QuizQuestion(
                "During a conflict, how do you handle their point of view?",
                listOf(
                    QuizAnswer("Summarize what they said: 'I hear that you feel hurt because...', validating them.", 3),
                    QuizAnswer("Wait for them to stop talking just to deliver my counter-argument.", 2),
                    QuizAnswer("Yell over them, dismiss their feelings, or walk out of the room.", 1)
                )
            ),
            QuizQuestion(
                "How often do you hold deep, vulnerable emotional check-ins, asking about their inner heart?",
                listOf(
                    QuizAnswer("Continuously – we check in weekly with sincere care.", 3),
                    QuizAnswer("Rarely – only when a major crisis or argument occurs.", 2),
                    QuizAnswer("Never – we only touch on surface logistics or watch TV.", 1)
                )
            )
        )
    }

    var questionIndex by remember { mutableStateOf(0) }
    var totalScore by remember { mutableStateOf(0) }
    var quizCompleted by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .shadow(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = quizTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                IconButton(onClick = onClose, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Quiz")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!quizCompleted) {
                val q = questions[questionIndex]
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Question Count Indicator
                    Text(text = "Question ${questionIndex + 1} of ${questions.size}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold))

                    Text(text = q.text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        q.answers.forEach { ans ->
                            Button(
                                onClick = {
                                    totalScore += ans.points
                                    if (questionIndex < questions.size - 1) {
                                        questionIndex++
                                    } else {
                                        quizCompleted = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Text(text = ans.text, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 4.dp), fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // Quiz complete screen
                val counselText = when {
                    totalScore >= 8 -> "Golden Pure Love Connection: You have a beautiful, highly developed relationship built on profound trust, equal shared effort, and active listening. Keep nurturing this rare, selfless commitment!"
                    totalScore >= 5 -> "Balanced Growing Love: There is authentic affection and care here! However, daily chores burden, small transparency slip-offs, or active listening blockages are putting strain. Sit down together and trade a chore or communicate feelings deeply."
                    else -> "Soulful Audit Required: Your path needs active restoration. Romance won't fix structural inequality. Prioritize transparent honesty, apologize clearly, divide domestic responsibilities equally, and practice uninterrupted active listening."
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = "Complete", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                    }

                    Text(text = "Audit Complete!", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Compatibility Index Score: ", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "$totalScore / 9", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = counselText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Explore Other Audits")
                    }
                }
            }
        }
    }
}

data class QuizQuestion(val text: String, val answers: List<QuizAnswer>)
data class QuizAnswer(val text: String, val points: Int)

// ==================== SCREEN 4: GRATITUDE JAR & LOVE JOURNAL ====================
@Composable
fun GratitudeJarScreen(viewModel: LoveViewModel) {
    val journalList by viewModel.journalEntries.collectAsStateWithLifecycle()
    var partnerName by remember { mutableStateOf("") }
    var journalContent by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Gratitude") }
    val categories = listOf("Gratitude", "Breakthrough", "Sweet Moment", "Growth Note")
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "The Shared Gratitude Jar",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Document positive memories, breakthroughs after active conversations, and appreciation notes. What made you feel valued today?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = partnerName,
                        onValueChange = { partnerName = it },
                        label = { Text("Partner's Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = journalContent,
                        onValueChange = { journalContent = it },
                        label = { Text("What are you grateful for today?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category chips
                    Text(text = "Note Category:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (partnerName.isNotBlank() && journalContent.isNotBlank()) {
                                viewModel.addJournalEntry(partnerName, journalContent, selectedCategory)
                                journalContent = ""
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("add_journal_button")
                    ) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = "Add journal")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Gratitude Jar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Stored Reflections",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (journalList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = "Empty journal",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Your Gratitude Jar is currently empty.", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(text = "Write a sweet note above to catalog a memory.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(journalList) { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val catIcon = when(entry.category) {
                                    "Gratitude" -> Icons.Default.CardGiftcard
                                    "Breakthrough" -> Icons.Default.BubbleChart
                                    "Sweet Moment" -> Icons.Default.Star
                                    else -> Icons.Default.Book
                                }
                                Icon(
                                    imageVector = catIcon,
                                    contentDescription = entry.category,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = entry.category,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteJournalEntry(entry) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Memory",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${entry.content}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Appreciation for: ${entry.partnerName}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.secondary
                            )

                            // Formatted simplified Date
                            Text(
                                text = "Pure Soul Sanctuary",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== SCREEN 5: AI RELATIONSHIP REPOSITORY ADVISOR (GEMINI) ====================
@Composable
fun AIAdvisorScreen(viewModel: LoveViewModel) {
    val reply by viewModel.advisorReply.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAdvisorLoading.collectAsStateWithLifecycle()
    var questionText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val promptSuggestions = listOf(
        "How can we divide household responsibilities fairly without arguing?",
        "How to rebuild transparent trust after a serious misunderstanding?",
        "What are simple active listening exercises couples can practice daily?",
        "How do we balance career ambitions with continuous organic relationship care?"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Pure Soul AI Companion",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "Seek gentle, neutral, and unbiased relationship perspective. Ask about duties division, communications, active listening, or building loyalty.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Consult the Advisor",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        placeholder = { Text("Explain what friction, communication issue, or goal you are thinking about...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            if (questionText.isNotBlank()) {
                                viewModel.askAdvisor(questionText)
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ask_ai_button")
                            .height(44.dp),
                        enabled = !isLoading
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send Question")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gather Advisor Wisdom", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Reply Space with Animation
        if (reply != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Core Heart",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pure Soul Advice",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.clearAdvisorReply() },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Reply",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = reply ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Caring Suggestion Prompts",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(promptSuggestions) { suggestion ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { questionText = suggestion }
                    .shadow(0.5.dp, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Text(
                    text = suggestion,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==================== NEW SCREEN: DEVOTION CHECKUPS HUB ====================
@Composable
fun DevotionCheckupsScreen(viewModel: LoveViewModel) {
    var selectedSubTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Segmented Control Header
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Love Calculator 💖", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Loyalty Gauge 🛡️", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("Soul Quizzes 🧠", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(modifier = Modifier.weight(1f)) {
            when (selectedSubTab) {
                0 -> LoveCalculatorTab(viewModel)
                1 -> LoyaltyGaugeTab(viewModel)
                2 -> QuizzesScreen() 
            }
        }
    }
}

@Composable
fun LoveCalculatorTab(viewModel: LoveViewModel) {
    val result by viewModel.loveScoreResult.collectAsStateWithLifecycle()
    val isCalculating by viewModel.isCalculatingLoveScore.collectAsStateWithLifecycle()
    
    var yourName by remember { mutableStateOf("") }
    var partnerName by remember { mutableStateOf("") }
    
    var yourZodiac by remember { mutableStateOf("Leo") }
    var partnerZodiac by remember { mutableStateOf("Cancer") }
    
    val zodiacSigns = listOf("Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces")
    
    var showYourZodiacDropdown by remember { mutableStateOf(false) }
    var showPartnerZodiacDropdown by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Affinity Alignment Analyzer",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "Measure your spiritual compatibility based on name vocal frequencies, astrologic planetary traits, and commitment behaviors.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (result == null && !isCalculating) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Calculate Affinities", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        
                        OutlinedTextField(
                            value = yourName,
                            onValueChange = { yourName = it },
                            label = { Text("Your Name") },
                            placeholder = { Text("Enter your full name") },
                            modifier = Modifier.fillMaxWidth().testTag("your_name_input")
                        )
                        
                        // Your Zodiac dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = yourZodiac,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Your Zodiac Sign") },
                                trailingIcon = {
                                    IconButton(onClick = { showYourZodiacDropdown = !showYourZodiacDropdown }) {
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Zodiac")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { showYourZodiacDropdown = !showYourZodiacDropdown }
                            )
                            DropdownMenu(
                                expanded = showYourZodiacDropdown,
                                onDismissRequest = { showYourZodiacDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                zodiacSigns.forEach { sign ->
                                    DropdownMenuItem(
                                        text = { Text(sign) },
                                        onClick = {
                                            yourZodiac = sign
                                            showYourZodiacDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        OutlinedTextField(
                            value = partnerName,
                            onValueChange = { partnerName = it },
                            label = { Text("Partner's Name") },
                            placeholder = { Text("Enter partner's full name") },
                            modifier = Modifier.fillMaxWidth().testTag("partner_name_input")
                        )
                        
                        // Partner Zodiac dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = partnerZodiac,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Partner's Zodiac Sign") },
                                trailingIcon = {
                                    IconButton(onClick = { showPartnerZodiacDropdown = !showPartnerZodiacDropdown }) {
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Zodiac")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { showPartnerZodiacDropdown = !showPartnerZodiacDropdown }
                            )
                            DropdownMenu(
                                expanded = showPartnerZodiacDropdown,
                                onDismissRequest = { showPartnerZodiacDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                zodiacSigns.forEach { sign ->
                                    DropdownMenuItem(
                                        text = { Text(sign) },
                                        onClick = {
                                            partnerZodiac = sign
                                            showPartnerZodiacDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Button(
                            onClick = {
                                if (yourName.isNotBlank() && partnerName.isNotBlank()) {
                                    viewModel.calculateLoveScore(yourName, partnerName, yourZodiac, partnerZodiac)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("calculate_love_score"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = yourName.isNotBlank() && partnerName.isNotBlank()
                        ) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = "Heart")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calculate Pure Love Score", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (isCalculating) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.25f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Pulsator",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp).graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Reading Initials Vibrations...", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Aligning planetary fields & devotion registers...", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            val scoreResult = result!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DIVINE COMPATIBILITY CALCULATED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${scoreResult.yourName} 💖 ${scoreResult.partnerName}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Zodiac Alignment: ${scoreResult.zodiacPair}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${scoreResult.score}%",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Affinity Match",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CompatibilityProgBar("Trust Connection Index", scoreResult.trustScore)
                            CompatibilityProgBar("Communication Frequency Sync", scoreResult.communicationScore)
                            CompatibilityProgBar("Ethereal Chemistry Ratio", scoreResult.chemistryScore)
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Aura Summary", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = scoreResult.description, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = { viewModel.clearLoveScore() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Calculate Another Bond")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompatibilityProgBar(label: String, score: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            Text("$score%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = score.toFloat() / 100f,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        )
    }
}

@Composable
fun LoyaltyGaugeTab(viewModel: LoveViewModel) {
    val loyaltyScore by viewModel.loyaltyScoreResult.collectAsStateWithLifecycle()
    val loyaltyTier by viewModel.loyaltyTier.collectAsStateWithLifecycle()
    val loyaltyReport by viewModel.loyaltyReport.collectAsStateWithLifecycle()
    
    val loyaltyQuestions = listOf(
        QuizQuestion(
            "Your partner asks to borrow your phone to look up a recipe, but your messaging apps are unlocked. What is your standard action?",
            listOf(
                QuizAnswer("Hand it over instantly with open confidence, having absolutely nothing to hide. 🛡️", 3),
                QuizAnswer("Hand it over but stay tense, making sure they only look at the recipe.", 2),
                QuizAnswer("Refuse, claiming device privacy is a strict and unbreakable personal line.", 1)
            )
        ),
        QuizQuestion(
            "You have sudden, spontaneous late-night plans with friends. How do you communicate this?",
            listOf(
                QuizAnswer("Call or message immediately to share details and reassure them. 📞", 3),
                QuizAnswer("Send a brief text right as you arrive at the venue.", 2),
                QuizAnswer("Explain the next morning to avoid disturbing their sleep.", 1)
            )
        ),
        QuizQuestion(
            "An old flame or ex messages you with friendly, curious catch-up questions. What is your response?",
            listOf(
                QuizAnswer("Share the conversation with your partner transparently with complete transparency. 🌸", 3),
                QuizAnswer("Reply politely but delete the chat history to prevent potential friction.", 2),
                QuizAnswer("Keep chatting privately, believing it is purely platonic and there is no harm.", 1)
            )
        ),
        QuizQuestion(
            "Your partner is extremely exhausted and asks you to help out with their share of chores today. How do you respond?",
            listOf(
                QuizAnswer("Complete the work happily with unconditional care and tell them to rest. 🥰", 3),
                QuizAnswer("Help with moderate sighing, reminding them you also had a long day.", 2),
                QuizAnswer("Decline or procrastinate, insisting chore division must remain perfectly equal.", 1)
            )
        ),
        QuizQuestion(
            "You accidentally made a financial or personal mistake that might cause minor worry. What do you do?",
            listOf(
                QuizAnswer("Schedule a talking hour, explain and apologize with active accountability. ☕", 3),
                QuizAnswer("Wait until they notice or ask, then defend your decision.", 2),
                QuizAnswer("Actively hide it, believing it is better to lock worries away.", 1)
            )
        )
    )
    
    var currentQuestionIdx by remember { mutableStateOf(0) }
    var scoreSum by remember { mutableStateOf(0) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (loyaltyScore == null) {
            item {
                Text(
                    text = "Loyalty & Devotion Test",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "Take our interactive loyalty scenarios test and compute your verified Devotion Tier and Trust Score Gauge.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                val q = loyaltyQuestions[currentQuestionIdx]
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "LOYALTY SCENARIO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Question ${currentQuestionIdx + 1} of 5",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = q.text,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            lineHeight = 22.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        q.answers.forEach { ans ->
                            Button(
                                onClick = {
                                    scoreSum += ans.points
                                    if (currentQuestionIdx < 4) {
                                        currentQuestionIdx++
                                    } else {
                                        viewModel.submitLoyaltyTest(scoreSum, 15)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = ans.text,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    textAlign = TextAlign.Start,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LOYALTY TEST COMPLETED 🛡️",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = loyaltyTier ?: "",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(6.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$loyaltyScore%",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Loyalty Index",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Purity & Devotion Evaluation", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = loyaltyReport ?: "", style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                scoreSum = 0
                                currentQuestionIdx = 0
                                viewModel.resetLoyaltyTest()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("retake_loyalty_test_btn")
                        ) {
                            Text("Retake Devotion Test")
                        }
                    }
                }
            }
        }
    }
}


// ==================== NEW SCREEN: SOCIAL CHAT HUB ====================
@Composable
fun SocialChatHubScreen(viewModel: LoveViewModel) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    
    var activeChatUsername by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    if (activeChatUsername == null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Pure Devotees Portal",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "Follow other accounts to enable private direct messaging chat. You can also block profiles to restrict connection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search accounts, bios, or zodiacs...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            val filteredProfiles = profiles.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.bio.contains(searchQuery, ignoreCase = true) ||
                it.zodiac.contains(searchQuery, ignoreCase = true)
            }
            
            items(filteredProfiles) { profile ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (profile.isBlocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (profile.isBlocked) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (profile.isBlocked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.name.take(2).uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = profile.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "(${profile.zodiac})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    if (profile.isBlocked) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text("Blocked", color = Color.White)
                                        }
                                    }
                                }
                                Text(text = "@${profile.username}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = profile.bio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)).padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Verified Loyalty", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                                Text("${profile.loyaltyRate}% Index", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text("Blocking Risk Chance", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                                Text("${profile.blockingRisk}% Chance", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (profile.blockingRisk > 30) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.toggleFollowUser(profile.username) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (profile.isFollowing) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent
                                )
                            ) {
                                Icon(
                                    imageVector = if (profile.isFollowing) Icons.Default.Check else Icons.Default.Add,
                                    contentDescription = "Follow",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (profile.isFollowing) "Following" else "Follow", fontSize = 12.sp)
                            }
                            
                            Button(
                                onClick = { activeChatUsername = profile.username },
                                modifier = Modifier.weight(1f).height(38.dp),
                                enabled = !profile.isBlocked,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = "Message", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Message", fontSize = 12.sp)
                            }
                            
                            IconButton(
                                onClick = { viewModel.toggleBlockUser(profile.username) },
                                modifier = Modifier.size(38.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (profile.isBlocked) Icons.Default.LockOpen else Icons.Default.Block,
                                    contentDescription = "Block Account",
                                    tint = if (profile.isBlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        val targetProfile = profiles.find { it.username == activeChatUsername!! }
        if (targetProfile == null) {
            activeChatUsername = null
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .shadow(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { activeChatUsername = null }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = targetProfile.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Zodiac: ${targetProfile.zodiac}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (targetProfile.isFollowing) Color.Green else Color.Gray)
                            )
                            Text(
                                text = "Blocking Chance Risk: ${targetProfile.blockingRisk}%",
                                fontSize = 10.sp,
                                color = if (targetProfile.blockingRisk > 30) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = {
                            viewModel.toggleBlockUser(targetProfile.username)
                        }
                    ) {
                        Icon(
                            imageVector = if (targetProfile.isBlocked) Icons.Default.LockOpen else Icons.Default.Block,
                            contentDescription = "Block inside Chat",
                            tint = if (targetProfile.isBlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                if (!targetProfile.isFollowing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "To open dialogue channels, follow their account first!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { viewModel.toggleFollowUser(targetProfile.username) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Follow Account Now", fontSize = 11.sp)
                            }
                        }
                    }
                }
                
                if (targetProfile.isBlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Connection Terminated! You blocked ${targetProfile.name}. Unblock to enable messages.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                val targetMessages = messages.filter {
                    (it.sender == "me" && it.receiver == targetProfile.username) ||
                    (it.sender == targetProfile.username && it.receiver == "me")
                }.sortedBy { it.timestamp }
                
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = false
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                        ) {
                            Text(
                                text = "This is a secure connection with ${targetProfile.name}. Messages are locally retained and co-created. Be patient, respect boundaries, and apologize first during arguments! 💖",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    items(targetMessages) { msg ->
                        val isMe = msg.sender == "me"
                        val alignment = if (isMe) Alignment.End else Alignment.Start
                        val bColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val tColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = alignment
                        ) {
                            Card(
                                modifier = Modifier.widthIn(max = 280.dp),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 2.dp,
                                    bottomEnd = if (isMe) 2.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(containerColor = bColor),
                                border = if (!isMe) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                            ) {
                                Text(
                                    text = msg.text,
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 13.sp,
                                    color = tColor,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
                
                var chatInputText by remember { mutableStateOf("") }
                var showShareMomentsMenu by remember { mutableStateOf(false) }
                val capturedMoments by viewModel.capturedMoments.collectAsStateWithLifecycle()
                
                // Share Moments Dialog
                if (showShareMomentsMenu) {
                    Dialog(onDismissRequest = { showShareMomentsMenu = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Share Camera Moments 📸",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                    IconButton(onClick = { showShareMomentsMenu = false }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                                    }
                                }
                                
                                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                
                                if (capturedMoments.isEmpty()) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = "No moments",
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "No captured moments yet!",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Use the Camera button at the top of the app to take beautiful photos and videos of your day.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(capturedMoments) { moment ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                                                        Icon(
                                                            imageVector = if (moment.isVideo) Icons.Default.Videocam else Icons.Default.Photo,
                                                            contentDescription = "Type",
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                        Column {
                                                            Text(text = moment.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                                            Text(text = if (moment.isVideo) "Video • ${moment.dateString}" else "Photo • ${moment.dateString}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                                        }
                                                    }
                                                    Button(
                                                        onClick = {
                                                            viewModel.shareCapturedMoment(moment, targetProfile.username)
                                                            showShareMomentsMenu = false
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                    ) {
                                                        Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { showShareMomentsMenu = true },
                        enabled = targetProfile.isFollowing && !targetProfile.isBlocked,
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Attachment,
                            contentDescription = "Share Moments",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    OutlinedTextField(
                        value = chatInputText,
                        onValueChange = { chatInputText = it },
                        placeholder = { Text("Write your respectful message...") },
                        modifier = Modifier.weight(1f).testTag("chat_input_textfield"),
                        shape = RoundedCornerShape(24.dp),
                        enabled = targetProfile.isFollowing && !targetProfile.isBlocked
                    )
                    
                    IconButton(
                        onClick = {
                            if (chatInputText.isNotBlank()) {
                                viewModel.sendSocialMessage(targetProfile.username, chatInputText)
                                chatInputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (chatInputText.isNotBlank() && targetProfile.isFollowing && !targetProfile.isBlocked)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            ),
                        enabled = chatInputText.isNotBlank() && targetProfile.isFollowing && !targetProfile.isBlocked
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Message",
                            tint = if (chatInputText.isNotBlank() && targetProfile.isFollowing && !targetProfile.isBlocked)
                                Color.White
                            else
                                MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

// ==================== ONBOARDING SYSTEM: LOGIN & REGISTRATION ====================
@Composable
fun LoginScreen(viewModel: LoveViewModel) {
    var selectedMethod by remember { mutableStateOf<String?>(null) } // "Google", "Facebook", "Instagram", "Phone"
    var yourName by remember { mutableStateOf("") }
    var partnerName by remember { mutableStateOf("") }
    
    // Auth specific forms
    var gmailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var instaInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    
    var showOtpScreen by remember { mutableStateOf(false) }
    var otpTimer by remember { mutableStateOf(59) }
    
    LaunchedEffect(showOtpScreen) {
        if (showOtpScreen) {
            while (otpTimer > 0) {
                kotlinx.coroutines.delay(1000)
                otpTimer--
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE91E63), // Warm Devoted Pink
                        Color(0xFFFF5722), // Warm Radiant Amber
                        Color(0xFF3F0C1B)  // Dark Cozy Twilight
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App Branding Icon & Slogan
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(8.dp, CircleShape),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💖",
                        style = MaterialTheme.typography.displayMedium
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PURE SOUL LOVE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                )
                Text(
                    text = "A Secure offline relationship sanctuary for heartmates",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                )
            }

            if (selectedMethod == null) {
                // Landing Method Selector Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "First, let's verify your identity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Choose any verification provider to register or resume your private relationship bond securely.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Direct One-Tap Connection Button (Highlighted Premium Option)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    selectedMethod = "Direct"
                                    if (yourName.isBlank()) yourName = "Liam"
                                    if (partnerName.isBlank()) partnerName = "Emma"
                                },
                            shape = RoundedCornerShape(25.dp),
                            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFFFF5722)))),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⚡", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Direct One-Tap Connect",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                        // Gmail Button
                        Button(
                            onClick = { selectedMethod = "Google" },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335))
                        ) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = "Gmail", tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sign In with Gmail / Google", fontWeight = FontWeight.Bold)
                        }

                        // Phone Button
                        Button(
                            onClick = { selectedMethod = "Phone" },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone", tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sign In with Phone Number", fontWeight = FontWeight.Bold)
                        }

                        // Instagram Button
                        Button(
                            onClick = { selectedMethod = "Instagram" },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C))
                        ) {
                            Icon(imageVector = Icons.Default.Group, contentDescription = "Instagram", tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Fast Link with Instagram", fontWeight = FontWeight.Bold)
                        }

                        // Facebook Button
                        Button(
                            onClick = { selectedMethod = "Facebook" },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                        ) {
                            Icon(imageVector = Icons.Default.Done, contentDescription = "Facebook", tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Authorize via Facebook Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Authentication Form Details Page
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = when(selectedMethod) {
                                        "Google" -> Icons.Default.Email
                                        "Phone" -> Icons.Default.Phone
                                        "Instagram" -> Icons.Default.Group
                                        "Direct" -> Icons.Default.FlashOn
                                        else -> Icons.Default.Done
                                    },
                                    contentDescription = "Verify Method",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$selectedMethod Credentials",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            IconButton(onClick = {
                                selectedMethod = null
                                showOtpScreen = false
                                otpInput = ""
                            }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Common Profile Naming
                        OutlinedTextField(
                            value = yourName,
                            onValueChange = { yourName = it },
                            label = { Text("Your Dynamic Display Name") },
                            placeholder = { Text("e.g. Liam, Vihaan, Chloe") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = partnerName,
                            onValueChange = { partnerName = it },
                            label = { Text("Partner's Devoted Name") },
                            placeholder = { Text("e.g. Aanya, Emma, Sophia") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Dynamic Fields based on method
                        when (selectedMethod) {
                            "Direct" -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "⚡", fontSize = 20.sp)
                                        Text(
                                            text = "Direct Connection Mode: Perfect for fast pairing. Instantly link your profiles and bypass all external verification!",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        val finalYourName = if (yourName.isNotBlank()) yourName.trim() else "Liam"
                                        val finalPartnerName = if (partnerName.isNotBlank()) partnerName.trim() else "Emma"
                                        viewModel.signInUser(finalYourName, "Direct Pair", "Instant Local Bridge", finalPartnerName)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                                ) {
                                    Text("Connect Accounts Instantly ⚡", fontWeight = FontWeight.Bold)
                                }
                            }
                            "Google" -> {
                                OutlinedTextField(
                                    value = gmailInput,
                                    onValueChange = { gmailInput = it },
                                    label = { Text("Google Gmail Account") },
                                    placeholder = { Text("your.name@gmail.com") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it },
                                    label = { Text("Security Password") },
                                    placeholder = { Text("••••••••") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Button(
                                    onClick = {
                                        if (yourName.isNotBlank() && gmailInput.isNotBlank()) {
                                            viewModel.signInUser(yourName, "Google Gmail", gmailInput, partnerName)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    enabled = yourName.isNotBlank() && gmailInput.isNotBlank(),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text("Log In Securely", fontWeight = FontWeight.Bold)
                                }
                            }
                            "Instagram" -> {
                                OutlinedTextField(
                                    value = instaInput,
                                    onValueChange = { instaInput = it },
                                    label = { Text("Instagram Account Handle") },
                                    placeholder = { Text("@soulmate.romance") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Button(
                                    onClick = {
                                        if (yourName.isNotBlank() && instaInput.isNotBlank()) {
                                            viewModel.signInUser(yourName, "Instagram Direct", instaInput, partnerName)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    enabled = yourName.isNotBlank() && instaInput.isNotBlank(),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text("Establish Instagram Bridge", fontWeight = FontWeight.Bold)
                                }
                            }
                            "Facebook" -> {
                                Text(
                                    text = "Clicking the authorize button will trigger a secure token link to register your standard Facebook profile attributes locally.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = {
                                        if (yourName.isNotBlank()) {
                                            viewModel.signInUser(yourName, "Facebook Auth", "Linked Profile", partnerName)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    enabled = yourName.isNotBlank(),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text("Verify & Link Facebook", fontWeight = FontWeight.Bold)
                                }
                            }
                            "Phone" -> {
                                if (!showOtpScreen) {
                                    OutlinedTextField(
                                        value = phoneInput,
                                        onValueChange = { phoneInput = it },
                                        label = { Text("Registered Mobile Number") },
                                        placeholder = { Text("+1 (555) 019-3829") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Button(
                                        onClick = {
                                            if (phoneInput.length > 5 && yourName.isNotBlank()) {
                                                showOtpScreen = true
                                                otpTimer = 59
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        enabled = yourName.isNotBlank() && phoneInput.length > 5,
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text("Request verification SMS", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Verification Code sent to $phoneInput. Check your SMS inbox! (Use mock code: 1234)",
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        OutlinedTextField(
                                            value = otpInput,
                                            onValueChange = { otpInput = it },
                                            label = { Text("4-digit SMS OTP Code") },
                                            placeholder = { Text("Type code...") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (otpTimer > 0) "Resend in ${otpTimer}s" else "Code expired",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                            IconButton(
                                                onClick = {
                                                    otpTimer = 59
                                                    otpInput = ""
                                                },
                                                enabled = otpTimer == 0
                                            ) {
                                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Resend OTP SMS")
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (otpInput == "1234" || otpInput.trim() == "1234") {
                                                    viewModel.signInUser(yourName, "OTP Phone", phoneInput, partnerName)
                                                } else {
                                                    otpInput = ""
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            enabled = otpInput.isNotBlank(),
                                            shape = RoundedCornerShape(24.dp)
                                        ) {
                                            Text("Verify SMS Code", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==================== CAMERAX MOMENTS IN-APP ENGINE ====================
@Composable
fun CameraViewOverlay(viewModel: LoveViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Permission tracking states
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: false
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)) // Studio Theater Dark
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!hasCameraPermission) {
                    // Explain and request permission
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📸 Camera Access Required",
                            style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "To record memorable co-created moments and photos to instantly share in-app with your partner, please allow device Camera and Mic authorization.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                        ) {
                            Text("Grant Access Permissions", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = onDismiss) {
                            Text("Discard", color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                } else {
                    // Main Camera Studio Interface
                    CameraStudioLayout(viewModel = viewModel, onDismiss = onDismiss)
                }
            }
        }
    }
}

@Composable
fun CameraStudioLayout(viewModel: LoveViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var isVideoMode by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var captureTriggered by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    
    // Ticking recording durations
    var recordingSeconds by remember { mutableStateOf(0) }
    
    // Captured Moment Review Overlay
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }
    var capturedVideoPath by remember { mutableStateOf<String?>(null) }
    var lastCapturedTitle by remember { mutableStateOf("") }
    var showReviewPanel by remember { mutableStateOf(false) }
    var saveInJournalToo by remember { mutableStateOf(true) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                kotlinx.coroutines.delay(1000)
                recordingSeconds++
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera viewfinder representation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // CameraX Real process viewport integration
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = CameraPreviewX.Builder().build().apply {
                                setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                }
            )

            // Safe overlay grid and lenses aesthetic
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            ) {
                // Symmetrical grids representation
                Row(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.fillMaxHeight().width(0.5.dp).background(Color.White.copy(alpha = 0.08f)))
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.fillMaxHeight().width(0.5.dp).background(Color.White.copy(alpha = 0.08f)))
                    Spacer(modifier = Modifier.weight(1f))
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color.White.copy(alpha = 0.08f)))
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color.White.copy(alpha = 0.08f)))
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Shutter snapshot flash animation event
                if (captureTriggered) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White))
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(100)
                        captureTriggered = false
                    }
                }
            }
        }

        // --- TOP CONTROLS (Dismiss, Flash, Mode title) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Camera", tint = Color.White)
            }

            // Mode Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isVideoMode) "VIDEO STUDY 🎥" else "PHOTO SNAP 📸",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )
            }

            IconButton(
                onClick = { isFlashOn = !isFlashOn },
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Done, // Representing Flash Done Toggle
                    contentDescription = "Flash toggle",
                    tint = if (isFlashOn) Color.Yellow else Color.White
                )
            }
        }

        // --- BOTTOM CAPTURE INTERFACE ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isRecording) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Red)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                    Text(
                        text = "REC %02d:%02d".format(recordingSeconds / 60, recordingSeconds % 60),
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                // Mode Switch Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (!isVideoMode) Color.White else Color.Transparent)
                            .clickable { isVideoMode = false }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Photo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isVideoMode) Color.Black else Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isVideoMode) Color.White else Color.Transparent)
                            .clickable { isVideoMode = true }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Video",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isVideoMode) Color.Black else Color.White
                        )
                    }
                }
            }

            // Central Pulsating Shutter Trigger Button
            IconButton(
                onClick = {
                    if (isVideoMode) {
                        if (isRecording) {
                            // Stop recording video
                            isRecording = false
                            val randomNames = listOf("Late Night Walks 🌆", "Cooking Devotion 🍲", "Romantic Sunset Spark ✨")
                            lastCapturedTitle = randomNames.random()
                            capturedVideoPath = "video_moment_${System.currentTimeMillis()}.mp4"
                            showReviewPanel = true
                        } else {
                            // Start recording video
                            isRecording = true
                        }
                    } else {
                        // Snap instant Photo
                        captureTriggered = true
                        val randomPhotoNames = listOf("My Heartmate's Smile 😊", "Morning Flower Moment 🌸", "A Cozy Cafe Chat ☕")
                        lastCapturedTitle = randomPhotoNames.random()
                        capturedPhotoPath = "photo_moment_${System.currentTimeMillis()}.jpg"
                        showReviewPanel = true
                    }
                },
                modifier = Modifier
                    .size(76.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(4.dp)
                    .background(if (isRecording) Color.Red else Color(0xFFE91E63), CircleShape)
            ) {
                Icon(
                    imageVector = if (isVideoMode) Icons.Default.Videocam else Icons.Default.PhotoCamera,
                    contentDescription = "Trigger shutter",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = if (isVideoMode) "Tap central red button to start/stop video" else "Tap pink button to snap instant photo moment",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp),
                textAlign = TextAlign.Center
            )
        }

        // --- MOMENT REVIEW MODAL PANEL ---
        if (showReviewPanel) {
            Dialog(onDismissRequest = { showReviewPanel = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Save Soul Moment 💾💖",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )

                        // Mock Thumbnail preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = if (isVideoMode) Icons.Default.Videocam else Icons.Default.Photo,
                                    contentDescription = "Moment Preview",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = if (isVideoMode) "Captured Video Moment" else "Captured Photo Moment",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        // Title naming input field
                        OutlinedTextField(
                            value = lastCapturedTitle,
                            onValueChange = { lastCapturedTitle = it },
                            label = { Text("What happened in this moment?") },
                            placeholder = { Text("Enter a loving description...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Journal linking option
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Post in Gratitude Journal too?", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
                            Switch(
                                checked = saveInJournalToo,
                                onCheckedChange = { saveInJournalToo = it }
                            )
                        }

                        // Actions
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    val finalPath = if (isVideoMode) capturedVideoPath else capturedPhotoPath
                                    viewModel.addCapturedMoment(lastCapturedTitle, isVideoMode, finalPath)
                                    
                                    if (saveInJournalToo) {
                                        viewModel.addJournalEntry(lastCapturedTitle, "Secured lovely photo/video moment. Sharing bliss of memory and cosmic devotion.", "Happy")
                                    }
                                    
                                    // Save to Loverry Gallery database automatically
                                    viewModel.addLoverryPhoto(
                                        title = lastCapturedTitle,
                                        description = "Captured with in-app moments camera.",
                                        imagePath = if (isVideoMode) "preset_devotion" else "preset_sunset"
                                    )
                                    
                                    Toast.makeText(context, "Moment secured in Loverry 💖", Toast.LENGTH_SHORT).show()
                                    showReviewPanel = false
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Save in Local Gallery", fontWeight = FontWeight.Bold)
                            }
                            
                            Button(
                                onClick = {
                                    val finalPath = if (isVideoMode) capturedVideoPath else capturedPhotoPath
                                    val moment = CapturedMoment(title = lastCapturedTitle, isVideo = isVideoMode, filePath = finalPath)
                                    viewModel.addCapturedMoment(lastCapturedTitle, isVideoMode, finalPath)
                                    viewModel.shareCapturedMoment(moment, "partner") // dynamic partner linkage
                                    
                                    if (saveInJournalToo) {
                                        viewModel.addJournalEntry("Shared: $lastCapturedTitle", "Instantly capture shared with partner. Off-grid bond transparency solidified.", "Ecstatic")
                                    }
                                    
                                    // Save to Loverry Gallery database automatically
                                    viewModel.addLoverryPhoto(
                                        title = lastCapturedTitle,
                                        description = "Shared moments camera capture.",
                                        imagePath = if (isVideoMode) "preset_devotion" else "preset_sunset"
                                    )
                                    
                                    Toast.makeText(context, "Shared with partner securely 💖", Toast.LENGTH_SHORT).show()
                                    showReviewPanel = false
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text("Share with Partner Now 💖", fontWeight = FontWeight.Bold)
                            }

                            TextButton(
                                onClick = { showReviewPanel = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Retake Snap", color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}

