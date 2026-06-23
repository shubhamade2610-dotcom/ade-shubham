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
import com.example.ui.LoveViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

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

        // Initialize ViewModel using manual Factory for simple constructor injection
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LoveViewModel(repository) as T
            }
        })[LoveViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainAppScreen(viewModel: LoveViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

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
                        Text(
                            text = "Pure Soul Love",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                    // Elegant Avatar badge matching JD in the design HTML
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .border(2.dp, Color.White, CircleShape)
                            .shadow(1.dp, CircleShape)
                            .clickable { quoteIndex.value = (0..4).random() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JD",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
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
                    Triple(2, "Quizzes", Icons.Default.Psychology),
                    Triple(3, "Gratitude", Icons.Default.Favorite),
                    Triple(4, "AI Advisor", Icons.Default.Chat)
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
                2 -> QuizzesScreen()
                3 -> GratitudeJarScreen(viewModel)
                4 -> AIAdvisorScreen(viewModel)
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
                    painter = painterResource(id = R.drawable.img_pure_soul_hero_1782200050790),
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
