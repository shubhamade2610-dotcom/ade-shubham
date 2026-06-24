package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoveGameScreen(viewModel: LoveViewModel) {
    val context = LocalContext.current
    
    // ViewModel state integrations
    val romanceHearts by viewModel.gameRomanceHearts.collectAsStateWithLifecycle()
    val boyName by viewModel.gameBoyName.collectAsStateWithLifecycle()
    val girlName by viewModel.gameGirlName.collectAsStateWithLifecycle()
    val boyAge by viewModel.gameBoyAge.collectAsStateWithLifecycle()
    val girlAge by viewModel.gameGirlAge.collectAsStateWithLifecycle()
    
    val equippedDressBoy by viewModel.equippedDressBoy.collectAsStateWithLifecycle()
    val equippedDressGirl by viewModel.equippedDressGirl.collectAsStateWithLifecycle()
    val equippedMakeupGirl by viewModel.equippedMakeupGirl.collectAsStateWithLifecycle()
    
    val equippedThings by viewModel.equippedNecessaryThings.collectAsStateWithLifecycle()
    val equippedFoods by viewModel.equippedFoods.collectAsStateWithLifecycle()
    val unlockedItems by viewModel.unlockedKitItems.collectAsStateWithLifecycle()
    val equippedMeet by viewModel.equippedMeet.collectAsStateWithLifecycle()
    val equippedMoving by viewModel.equippedMoving.collectAsStateWithLifecycle()

    var showNameEditDialog by remember { mutableStateOf(false) }
    var editBoyName by remember { mutableStateOf(boyName) }
    var editGirlName by remember { mutableStateOf(girlName) }
    
    var selectedGameTab by remember { mutableStateOf("Kit Closet") } // "Kit Closet", "Play Scenarios"
    
    // Scenario game progression
    var activeScenarioIndex by remember { mutableStateOf(0) } // 0..6
    var activeStep by remember { mutableStateOf(0) } // 0: Init, 1: Choice Selected (Result), 2: Finished
    var selectedOptionText by remember { mutableStateOf("") }
    var outcomeText by remember { mutableStateOf("") }
    var romanceBonusAwarded by remember { mutableStateOf(0) }

    val scenarios = listOf(
        ScenarioData(
            id = 0,
            category = "Dating",
            title = "Starry Roof-top Coffee Date ☕",
            icon = Icons.Default.Restaurant,
            gradient = listOf(Color(0xFFFF8A80), Color(0xFFFF5252)),
            intro = { boy, girl -> "Under a soft canopy of fairy lights, $boy holds $girl's hands. The warm night breeze carries quiet acoustic guitar hums..." },
            choices = listOf(
                ScenarioChoice("Sip your coffee, look in her eyes and tell her she is more beautiful than the stars.", "Love is in the air! She blushes deeply and leans head on your shoulder.", 25),
                ScenarioChoice("Present the warm, comforting hand-woven gloves to wrap her hands in cold breeze.", "Care & warmth overflows! She feels highly cared for by your pure soul connection.", 20),
                ScenarioChoice("Sing a light playful melodious love ballad while tapping the coffee table.", "Rhythm of romance! She laughs and sings along, feeling incredibly relaxed.", 30)
            ),
            specialKitItem = "Acoustic Serenading Guitar",
            specialKitUnlockChoice = ScenarioChoice(
                "Perform a deep emotional customized fingerstyle song with your equipped Acoustic Guitar!",
                "Incredible Love Resonance! She gets overwhelmed with joyful tears, hugging you closely.",
                50
            )
        ),
        ScenarioData(
            id = 1,
            category = "Proposing",
            title = "A Glowing Proposal Under the Stars 💍✨",
            icon = Icons.Default.Favorite,
            gradient = listOf(Color(0xFFEA80FC), Color(0xFFE040FB)),
            intro = { boy, girl -> "$boy leads $girl to a quiet, scenic meadow. The moonlight frames her elegant presence. He feels his heartbeat ticking loudly..." },
            choices = listOf(
                ScenarioChoice("Express dedication with words: 'From this lifetime to eternity, my heart belongs to you.'", "Emotional peak! Tears fill her eyes as she whispers sweet confessions.", 30),
                ScenarioChoice("Offer a customized hand-carved message box with key to your secret journal.", "Deep mental security! She adores your absolute trust in her pure soul.", 25)
            ),
            specialKitItem = "Engraved Ring of Eternity",
            specialKitUnlockChoice = ScenarioChoice(
                "Kneel on one knee, present the equipped Engraved Ring of Eternity and ask: 'Will you grow old with me?'",
                "ULTIMATE PROPOSAL SUCCESS! She screams yes and wraps her arms around your neck. Hearts overflowing! 💖",
                60
            )
        ),
        ScenarioData(
            id = 2,
            category = "Romance",
            title = "Fireplace Slow Dance 🎵",
            icon = Icons.Default.MusicNote,
            gradient = listOf(Color(0xFFFFD180), Color(0xFFFFAB40)),
            intro = { boy, girl -> "Inside a warm log cabin with a crackling digital fire, romantic slow-tempo jazz is playing. $boy invites $girl to dance." },
            choices = listOf(
                ScenarioChoice("Place hands gently on her waist and match her slow, elegant steps.", "Harmonized bliss! Your hearts beat as one in absolute peace.", 20),
                ScenarioChoice("Whisper soft praises about her glowing look and delicate natural makeup in your ear.", "Pure intimacy! She hides her glowing face in your chest in sweet solace.", 25)
            ),
            specialKitItem = "Midnight Silk Rose Dress",
            specialKitUnlockChoice = ScenarioChoice(
                "Compliment her specifically wearing the Midnight Silk Rose Dress under golden flames.",
                "Fashion & Devotion unified! She spins around gracefully, feeling like a romantic queen.",
                45
            )
        ),
        ScenarioData(
            id = 3,
            category = "Tours",
            title = "Virtual Backpacking Tour are Paris ✈️🏰",
            icon = Icons.Default.FlightTakeoff,
            gradient = listOf(Color(0xFF80D8FF), Color(0xFF40C4FF)),
            intro = { boy, girl -> "Standing right beneath the digital lights of Eiffel Tower. $boy holds the backpack maps, planning their next sweet stop." },
            choices = listOf(
                ScenarioChoice("Buy a couple sketch from a street artist by the Seine river.", "Memories forever! You both store this cartoon frame in your soul records.", 20),
                ScenarioChoice("Buy freshly baked sweet warm croissants and share them on a park bench.", "Sweet sensations! Tasting gourmet sweets under European morning lights.", 25)
            ),
            specialKitItem = "Rainbow Macaron Box",
            specialKitUnlockChoice = ScenarioChoice(
                "Open the equipped Sunset Rainbow Macaron Box for a romantic snack together.",
                "Gastronomic Delight! She feeds you a strawberry macaron with a wide cheerful grin.",
                45
            )
        ),
        ScenarioData(
            id = 4,
            category = "Fightings",
            title = "The Funny Chocolate Dispute 🍫🥊",
            icon = Icons.Default.SentimentVeryDissatisfied,
            gradient = listOf(Color(0xFFFF9E80), Color(0xFFFF6E40)),
            intro = { boy, girl -> "An adorable, playful friction occurs: $girl realizes $boy accidentally finished the last bite of their favorite hazelnut chocolate." },
            choices = listOf(
                ScenarioChoice("Immediately offer a cute kitten-pout face, apologetically hugging her.", "Crisis averted! She can't resist your innocent face, laughing out loud.", 25),
                ScenarioChoice("Tickle her lightheartedly, demanding a trial to earn her happy chocolate forgiveness back.", "Giggle attacks! She surrenders in giggles, claiming you win her heart.", 30)
            ),
            specialKitItem = "Chilled Gourmet Chocolate Strawberries",
            specialKitUnlockChoice = ScenarioChoice(
                "Present the secret stash of equipped Chilled Gourmet Chocolate Strawberries as backup peace-offering!",
                "PERFECT KISS & MAKE UP! She gasps in excitement. Safe, relaxing sweet reconciliation achieved!",
                50
            )
        ),
        ScenarioData(
            id = 5,
            category = "Picnic",
            title = "Windblown Lakeside Clover Picnic 🧺🍀",
            icon = Icons.Default.FilterHdr,
            gradient = listOf(Color(0xFFB9F6CA), Color(0xFF69F0AE)),
            intro = { boy, girl -> "Under a tall, swaying willow tree by the crystal blue lake, $boy spreads out the picnic blanket while $girl sets up cushions." },
            choices = listOf(
                ScenarioChoice("Weave a crown of fresh wild yellow daisies and gently place it on her hair.", "Cottagecore Royalty! She blushes, thanking her devoted nature king.", 20),
                ScenarioChoice("Lie down together looking at moving cloud patterns, finding shapes of hearts.", "Vibe of tranquility! Peaceful minds fully aligned in natural harmony.", 25)
            ),
            specialKitItem = "Heart-Shaped Woodfired Pizza",
            specialKitUnlockChoice = ScenarioChoice(
                "Open the hot container of equipped Heart-Shaped Pizza to feast together on the clover field.",
                "Picnic Feast Masterclass! It's delicious, cozy, warm, and highly visual.",
                45
            )
        ),
        ScenarioData(
            id = 6,
            category = "Feelings",
            title = "Vulnerable Soul Card Connection 🎴💬",
            icon = Icons.Default.Psychology,
            gradient = listOf(Color(0xFFCCFF90), Color(0xFFB2FF59)),
            intro = { boy, girl -> "Cuddled inside a warm fort of sheets and pillows, $boy draws an intimacy feeling card to understand each other deeply." },
            choices = listOf(
                ScenarioChoice("Card Question: 'What small thing did I do that made you feel most loved?'", "Deep soul bonding! She shares a silent, beautiful moment that melts your heart.", 30),
                ScenarioChoice("Card Question: 'In what ways do you think we protect and heal each other?'", "Solidified trust! You talk with mature empathy, raising your spiritual unity.", 35)
            ),
            specialKitItem = "Handwritten Wax-Sealed Scented Scroll",
            specialKitUnlockChoice = ScenarioChoice(
                "Read out a beautiful dedicated handwritten wax-sealed scroll filled with emotional poems.",
                "Ethereal devotion! Deepest mental comfort achieved. Perfect pure intimacy secured.",
                55
            )
        )
    )

    // Love Closet Items definition (Dresses, Makeup, Things, Foods, Meets, Moving)
    val boutiqueItems = listOf(
        BoutiqueItem("Midnight Silk Rose Dress", "Dress Girl", 40, "An elegant deep crimson gown decorated with delicate lace. Special romance bonus!"),
        BoutiqueItem("Sunny Yellow Picnic Frock", "Dress Girl", 0, "A cheerful breezy dress suitable for clover meadows. Unlocked by default."),
        BoutiqueItem("Fitted Midnight Velvet Suit", "Dress Boy", 45, "A tailored suit making the boy look like a dapper prince."),
        BoutiqueItem("Cool Breeze Casual Hoodie", "Dress Boy", 0, "A super soft cozy sweatshirt for relaxed cuddle scenarios."),
        BoutiqueItem("Natural Peach Glow", "Makeup Girl", 0, "Soft rosy glow highlighting her natural beauty. Starter item."),
        BoutiqueItem("Ethereal Sparkle Eyeshaow", "Makeup Girl", 25, "Breathtaking stellar glitter on eyes reflecting digital flame lights."),
        BoutiqueItem("Cherry Blush Gloss", "Makeup Girl", 20, "Vibrant juicy lip gloss adding premium shine and deep sweet allure."),
        BoutiqueItem("Bouquet of 101 Red Roses", "Things", 30, "Classic massive arrangement of handpicked red roses indicating devotion."),
        BoutiqueItem("Acoustic Serenading Guitar", "Things", 40, "A lightweight warm-toned guitar. Unlocks customized acoustic scene choices!"),
        BoutiqueItem("Engraved Ring of Eternity", "Things", 50, "A spectacular silver ring representing non-stop soul commitment."),
        BoutiqueItem("Handwritten Wax-Sealed Scented Scroll", "Things", 30, "A scroll containing cozy poetry details, sealed with high-devoted wax."),
        BoutiqueItem("Chilled Gourmet Chocolate Strawberries", "Food", 25, "Rich Belgian chocolate melted over fresh sour strawberries. Resolves fights!"),
        BoutiqueItem("Heart-Shaped Woodfired Pizza", "Food", 30, "Delectable fresh pizza showing love shape. Unlocks lakeside pizza feasting."),
        BoutiqueItem("Rainbow Macaron Box", "Food", 20, "Parisian crisp pastries reflecting beautiful sunset pastel rainbows."),
        BoutiqueItem("Cozy Cinnamon Hot Cocoa", "Food", 15, "Two mugs of creamy hot chocolate topped with marshmallow fluff."),
        
        BoutiqueItem("Cozy Corner Café Meet", "Meets", 0, "A quiet street cafe with warm yellow lights. Perfect for talking. Unlocked by default."),
        BoutiqueItem("Seaside Sunset Beach Meet", "Meets", 35, "Holding hands on the sandy beach while the golden sun sinks below the waves."),
        BoutiqueItem("Midnight Drive Meet", "Meets", 40, "A midnight drive through city lights with soft romance songs on the radio."),
        BoutiqueItem("Dreamy Cinema Hall Meet", "Meets", 25, "Sharing popcorn in a cozy dark theater watching a classic romantic film."),
        
        BoutiqueItem("Gently Strolling Hand-in-Hand", "Moving", 0, "Walking slowly, fingers intertwined, feeling the rhythm of heartbeats. Unlocked by default."),
        BoutiqueItem("Vespa Scooter Ride", "Moving", 30, "Zipping through winding roads together on a vintage scooter in the wind."),
        BoutiqueItem("Open-Top Sunset Cruiser", "Moving", 45, "Cruising the highway with top down under soft pastel colored skies."),
        BoutiqueItem("Bullet Train Escapade", "Moving", 50, "Speeding across scenic mountains on a bullet train to a new getaway destination.")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 56.dp)
        ) {
            // --- MAIN HEADER GRADIENT BANNER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                Color(0xFFE91E63)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TRY LOVE 🎮💖",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "A beautiful place to play real-life meets, moving & custom kits!",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f))
                        )
                    }

                    // Love Coins badge counter + Claim free coins button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.addRomanceHearts(500)
                                Toast.makeText(context, "Claimed 500 Free Love Coins! 🪙✨", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White.copy(alpha = 0.15f),
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(text = "🪙 +500 Free", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "🪙", fontSize = 16.sp)
                                Text(
                                    text = "$romanceHearts",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOY & GIRL AGE 18 CUSTOM CAST CARD ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "🎭", fontSize = 20.sp)
                            Text(
                                text = "Current Relationship Cast (Sovereign age 18)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(
                            onClick = {
                                editBoyName = boyName
                                editGirlName = girlName
                                showNameEditDialog = true
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit names",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Cast names layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Boy Column
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFBBDEFB), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "👦", fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = boyName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "Age: $boyAge (Gentleman)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Outfit: $equippedDressBoy",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Heart Spark connection
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(top = 16.dp)) {
                            Text(text = "💝", fontSize = 24.sp, modifier = Modifier.animateContentSize())
                        }

                        // Girl Column
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF8BBD0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "👧", fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = girlName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "Age: $girlAge (Lady)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Outfit: $equippedDressGirl",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Equipped tools & foods recap
                    if (equippedThings.isNotEmpty() || equippedFoods.isNotEmpty()) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Active Love Kit Equipped:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                equippedThings.forEach { thing ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "🎒 $thing", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                equippedFoods.forEach { food ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFFE0B2))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "🍕 $food", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "📍 Active Real-Life Meet Location:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "📍", fontSize = 14.sp)
                                Text(text = equippedMeet, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "🚗 Active Real-Life Moving Kit (Travel style):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "🚗", fontSize = 14.sp)
                                Text(text = equippedMoving, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- STUDIO GAMEPLAY & CLOSET SWITCHER TABS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { selectedGameTab = "Kit Closet" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedGameTab == "Kit Closet") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedGameTab == "Kit Closet") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.AllInbox, contentDescription = "Kit Closet")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Love Kit Closet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = { selectedGameTab = "Play Scenarios" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedGameTab == "Play Scenarios") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedGameTab == "Play Scenarios") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.SportsEsports, contentDescription = "Play Scenarios")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Interactive Scenarios", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CURRENT TAB VIEWER ---
            if (selectedGameTab == "Kit Closet") {
                // CLOSET MODULE: DRESS, MAKE-UP, FOOD, NECESSARY THINGS
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Interactive Closet Boutique",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Unlock beautiful dresses, makeups, and foods!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Group Items into categories for easier management
                    val categories = listOf("Dress Girl", "Dress Boy", "Makeup Girl", "Things", "Food", "Meets", "Moving")
                    categories.forEach { cat ->
                        val catLabel = when (cat) {
                            "Dress Girl" -> "👗 Girls' Dresses & Frocks"
                            "Dress Boy" -> "👔 Boys' Warm Outfits"
                            "Makeup Girl" -> "💄 Rosy Make-up Boutique"
                            "Things" -> "🎁 Romantic Necessary Kits"
                            "Food" -> "🍕 Delicacies & Sweets Foods"
                            "Meets" -> "📍 Real-Life Romantic Meets (Locations)"
                            "Moving" -> "🚗 Real-Life Moving & Travel Kits"
                            else -> "🎁 Kits"
                        }

                        Text(
                            text = catLabel,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )

                        val catItems = boutiqueItems.filter { it.category == cat }
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(catItems) { item ->
                                val isUnlocked = unlockedItems.contains(item.name) || item.cost == 0
                                val isEquipped = when (item.category) {
                                    "Dress Boy" -> equippedDressBoy == item.name
                                    "Dress Girl" -> equippedDressGirl == item.name
                                    "Makeup Girl" -> equippedMakeupGirl == item.name
                                    "Things" -> equippedThings.contains(item.name)
                                    "Food" -> equippedFoods.contains(item.name)
                                    "Meets" -> equippedMeet == item.name
                                    "Moving" -> equippedMoving == item.name
                                    else -> false
                                }

                                Card(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .border(
                                            width = if (isEquipped) 2.dp else 1.dp,
                                            color = if (isEquipped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isEquipped) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(70.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                                            MaterialTheme.colorScheme.surfaceVariant
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val itemArt = when (item.category) {
                                                "Dress Girl" -> "👗"
                                                "Dress Boy" -> "👔"
                                                "Makeup Girl" -> "💄"
                                                "Things" -> "🎁"
                                                "Food" -> "🍕"
                                                "Meets" -> "📍"
                                                "Moving" -> "🚗"
                                                else -> "🎁"
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = itemArt, fontSize = 28.sp)
                                                if (isEquipped) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(MaterialTheme.colorScheme.primary)
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("Equipped", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }

                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1
                                        )
                                        
                                        Text(
                                            text = item.description,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            lineHeight = 11.sp,
                                            maxLines = 2,
                                            modifier = Modifier.height(24.dp)
                                        )

                                        if (isUnlocked) {
                                            Button(
                                                onClick = {
                                                    if (!isUnlocked) {
                                                        viewModel.unlockKitItem(item.name, item.cost)
                                                    } else {
                                                        viewModel.equipItem(item.name, item.category)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isEquipped) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                                                ),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(
                                                    text = if (isEquipped) "Unequip" else "Equip Item",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    val success = viewModel.unlockKitItem(item.name, item.cost)
                                                    if (success) {
                                                        Toast.makeText(context, "${item.name} unlocked! 🎁", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Not enough Love Coins!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Text("Unlock  ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    Text("🪙 ${item.cost}", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
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
                // SCENARIOS MODULE: 7 TYPES
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Interactive Soulmate Scenarios",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Earn Love Coins by exploring heartwarming dialogue outcomes!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Horizontal selection chips for scenarios
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(scenarios) { sc ->
                            val isActive = activeScenarioIndex == sc.id
                            FilterChip(
                                selected = isActive,
                                onClick = {
                                    activeScenarioIndex = sc.id
                                    activeStep = 0 // Reset steps when switching scenarios
                                },
                                label = { Text(text = sc.category) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = sc.icon,
                                        contentDescription = sc.category,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }

                    // Scenario active card
                    val sc = scenarios[activeScenarioIndex]
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column {
                            // Atmospheric Header Gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Brush.verticalGradient(colors = sc.gradient)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = sc.icon,
                                        contentDescription = sc.title,
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = sc.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (activeStep == 0) {
                                    // Introduce story details
                                    Text(
                                        text = "📍 Location: $equippedMeet\n🚗 Movement: $equippedMoving\n\n" + sc.intro(boyName, girlName),
                                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                                    Text(
                                        text = "How should $boyName respond?",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    // Display general options
                                    sc.choices.forEach { choice ->
                                        OutlinedButton(
                                            onClick = {
                                                selectedOptionText = choice.text
                                                outcomeText = choice.outcome
                                                romanceBonusAwarded = choice.romanceDelta
                                                viewModel.addRomanceHearts(choice.romanceDelta)
                                                activeStep = 1
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = choice.text,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Start
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "+💖 ${choice.romanceDelta}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = Color(0xFFE91E63)
                                                )
                                            }
                                        }
                                    }

                                    // Check special Love Kit integration option
                                    val hasSpecialItemEquipped = when (sc.category) {
                                        "Dress Girl" -> equippedDressGirl == sc.specialKitItem
                                        "Dress Boy" -> equippedDressBoy == sc.specialKitItem
                                        "Makeup Girl" -> equippedMakeupGirl == sc.specialKitItem
                                        "Things" -> equippedThings.contains(sc.specialKitItem)
                                        "Food" -> equippedFoods.contains(sc.specialKitItem)
                                        else -> equippedThings.contains(sc.specialKitItem) || equippedFoods.contains(sc.specialKitItem) || equippedDressBoy == sc.specialKitItem || equippedDressGirl == sc.specialKitItem
                                    }

                                    if (hasSpecialItemEquipped && sc.specialKitUnlockChoice != null) {
                                        // Highlight premium path button
                                        Button(
                                            onClick = {
                                                val premiumChoice = sc.specialKitUnlockChoice
                                                selectedOptionText = premiumChoice.text
                                                outcomeText = premiumChoice.outcome
                                                romanceBonusAwarded = premiumChoice.romanceDelta
                                                viewModel.addRomanceHearts(premiumChoice.romanceDelta)
                                                activeStep = 1
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Stars,
                                                    contentDescription = "Premium item unlock",
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = sc.specialKitUnlockChoice.text,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Start
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "+💖 ${sc.specialKitUnlockChoice.romanceDelta}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = Color.Yellow
                                                )
                                            }
                                        }
                                    } else if (sc.specialKitUnlockChoice != null) {
                                        // Hint item is missing
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Locked choice",
                                                    tint = MaterialTheme.colorScheme.outline
                                                )
                                                Column {
                                                    Text(
                                                        text = "Secret Path Locked 🔒",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Unlock & equip '${sc.specialKitItem}' in Closet to access a high-yield romantic dialogue path!",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    // Dialogue selection results page
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Decision Selected:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.outline
                                        )

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                                        ) {
                                            Text(
                                                text = "\"$selectedOptionText\"",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                                modifier = Modifier.padding(14.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Outcome heart",
                                            tint = Color(0xFFE91E63),
                                            modifier = Modifier.size(36.dp)
                                        )

                                        Text(
                                            text = outcomeText,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            textAlign = TextAlign.Center
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFE8F5E9))
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(text = "🎉 Reward:  ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                                Text(text = "+🪙 $romanceBonusAwarded Love Coins", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE65100))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Button(
                                            onClick = { activeStep = 0 },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            shape = RoundedCornerShape(24.dp)
                                        ) {
                                            Text("Play Again / Try Another Path 🔁", fontWeight = FontWeight.Bold)
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

    // Custom Name Editor Dialog
    if (showNameEditDialog) {
        Dialog(onDismissRequest = { showNameEditDialog = false }) {
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Customize Relationship Cast 🎭",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    OutlinedTextField(
                        value = editBoyName,
                        onValueChange = { editBoyName = it },
                        label = { Text("Boy name (Age 18)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = editGirlName,
                        onValueChange = { editGirlName = it },
                        label = { Text("Girl name (Age 18)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showNameEditDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Discard", color = MaterialTheme.colorScheme.outline)
                        }

                        Button(
                            onClick = {
                                viewModel.updateGameNames(editBoyName, editGirlName)
                                showNameEditDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Cast")
                        }
                    }
                }
            }
        }
    }
}

// Helper Data Classes
data class ScenarioChoice(val text: String, val outcome: String, val romanceDelta: Int)

data class ScenarioData(
    val id: Int,
    val category: String,
    val title: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val intro: (String, String) -> String,
    val choices: List<ScenarioChoice>,
    val specialKitItem: String,
    val specialKitUnlockChoice: ScenarioChoice? = null
)

data class BoutiqueItem(
    val name: String,
    val category: String, // "Dress Girl", "Dress Boy", "Makeup Girl", "Things", "Food"
    val cost: Int,
    val description: String
)
