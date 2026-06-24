package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.BuildConfig
import com.example.data.*
import com.example.data.api.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoveViewModel(
    private val repository: LoveRepository,
    private val sharedPreferences: android.content.SharedPreferences
) : ViewModel() {
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab

    private val _partnerName = MutableStateFlow("Aanya")
    val partnerName: StateFlow<String> = _partnerName

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    // Sign-In state and user registration tracking
    private val _signedInUser = MutableStateFlow<UserRegistration?>(null)
    val signedInUser: StateFlow<UserRegistration?> = _signedInUser

    fun signInUser(name: String, method: String, identifier: String, partnerName: String) {
        val userReg = UserRegistration(name, method, identifier, partnerName)
        _signedInUser.value = userReg
        _partnerName.value = partnerName.ifBlank { "Aanya" }
        sharedPreferences.edit()
            .putString("user_name", name)
            .putString("user_method", method)
            .putString("user_identifier", identifier)
            .putString("user_partner_name", partnerName)
            .apply()
        addNotification("Pure Soul Devotee Registered 💖", "Welcome $name! Connected securely via $method.", "Profile")
    }

    fun signOutUser() {
        _signedInUser.value = null
        sharedPreferences.edit()
            .remove("user_name")
            .remove("user_method")
            .remove("user_identifier")
            .remove("user_partner_name")
            .apply()
        addNotification("Logged Out", "Signed out successfully.", "Profile")
    }

    // Loverry Gallery State & Actions
    val loverryPhotos: StateFlow<List<LoverryPhoto>> = repository.allLoverryPhotos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addLoverryPhoto(title: String, description: String, imagePath: String) {
        viewModelScope.launch {
            repository.insertLoverryPhoto(
                LoverryPhoto(
                    title = title,
                    description = description,
                    imagePath = imagePath
                )
            )
            addNotification("Loverry Moment Added 📸", "Saved '$title' to your Loverry gallery.", "Media")
        }
    }

    fun deleteLoverryPhoto(photo: LoverryPhoto) {
        viewModelScope.launch {
            repository.deleteLoverryPhoto(photo)
            addNotification("Loverry Moment Removed", "Deleted '${photo.title}' from Loverry.", "Media")
        }
    }

    // Captured Photos/Videos moments
    private val _capturedMoments = MutableStateFlow<List<CapturedMoment>>(emptyList())
    val capturedMoments: StateFlow<List<CapturedMoment>> = _capturedMoments

    fun addCapturedMoment(title: String, isVideo: Boolean, filePath: String? = null) {
        val moment = CapturedMoment(title = title, isVideo = isVideo, filePath = filePath)
        _capturedMoments.value = listOf(moment) + _capturedMoments.value
        addNotification("Moment Captured 📸", "Saved ${if (isVideo) "video" else "photo"}: '$title'. Ready to share!", "Media")
    }

    fun shareCapturedMoment(moment: CapturedMoment, receiverUsername: String) {
        val type = if (moment.isVideo) "video 🎥" else "photo 📸"
        val shareMessage = "I captured a special moment for you: '${moment.title}' $type. Let's remember this together! 💖"
        sendSocialMessage(receiverUsername, shareMessage)
        addNotification("Moment Shared", "Shared '${moment.title}' with your partner", "Media")
    }

    // Journal Entries List
    val journalEntries: StateFlow<List<LoveJournalEntry>> = repository.allJournalEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Goals and challenges list
    val goals: StateFlow<List<RelationshipGoal>> = repository.allGoals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Total Completed goals tracker
    val completedGoalsCount: StateFlow<Int> = repository.completedGoalsCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Chat agent states
    private val _advisorReply = MutableStateFlow<String?>(null)
    val advisorReply: StateFlow<String?> = _advisorReply

    private val _isAdvisorLoading = MutableStateFlow(false)
    val isAdvisorLoading: StateFlow<Boolean> = _isAdvisorLoading

    init {
        // Load saved session if exists
        val savedName = sharedPreferences.getString("user_name", null)
        val savedMethod = sharedPreferences.getString("user_method", null)
        val savedIdentifier = sharedPreferences.getString("user_identifier", null)
        val savedPartnerName = sharedPreferences.getString("user_partner_name", "Aanya") ?: "Aanya"
        if (savedName != null && savedMethod != null && savedIdentifier != null) {
            _signedInUser.value = UserRegistration(savedName, savedMethod, savedIdentifier, savedPartnerName)
            _partnerName.value = savedPartnerName
        }

        // Pre-populate database with high-spiritual, practical daily relationship connection challenges if empty
        viewModelScope.launch {
            repository.allGoals.firstOrNull()?.let { currentGoals ->
                if (currentGoals.isEmpty()) {
                    val defaultGoals = listOf(
                        RelationshipGoal(
                            title = "Unconditional Gratitude",
                            description = "Express sincere appreciation to your partner for something they do daily but often go unthanked for.",
                            pillar = "Respect & Care"
                        ),
                        RelationshipGoal(
                            title = "Balancing Daily Effort",
                            description = "Sit down together and agree to trade one daily chore, balancing joint responsibility and household effort.",
                            pillar = "Responsibilities"
                        ),
                        RelationshipGoal(
                            title = "The Golden Digital-Free Talks",
                            description = "Put away all screens for 15 minutes right before sleeping and share your feelings and future dreams with each other.",
                            pillar = "Trust & Loyalty"
                        ),
                        RelationshipGoal(
                            title = "A Vulnerable Apology",
                            description = "Acknowledge a recent minor friction point or negative attitude, and express a sincere apology to rebuild harmony.",
                            pillar = "Honesty & Apologies"
                        ),
                        RelationshipGoal(
                            title = "Empathetic Connection Hour",
                            description = "Listen to your partner share details about their day for 10 minutes without interrupting or attempting to solve their issues.",
                            pillar = "Active Listening"
                        )
                    )
                    defaultGoals.forEach { repository.insertGoal(it) }
                }
            } ?: run {
                // Flow might be cold, pre-insert anyway safely
                val defaultGoals = listOf(
                    RelationshipGoal(
                        title = "Unconditional Gratitude",
                        description = "Express sincere appreciation to your partner for something they do daily but often go unthanked for.",
                        pillar = "Respect & Care"
                    ),
                    RelationshipGoal(
                        title = "Balancing Daily Effort",
                        description = "Sit down together and agree to trade one daily chore, balancing joint responsibility and household effort.",
                        pillar = "Responsibilities"
                    ),
                    RelationshipGoal(
                        title = "The Golden Digital-Free Talks",
                        description = "Put away all screens for 15 minutes right before sleeping and share your feelings and future dreams with each other.",
                        pillar = "Trust & Loyalty"
                    ),
                    RelationshipGoal(
                        title = "A Vulnerable Apology",
                        description = "Acknowledge a recent minor friction point or negative attitude, and express a sincere apology to rebuild harmony.",
                        pillar = "Honesty & Apologies"
                    ),
                    RelationshipGoal(
                        title = "Empathetic Connection Hour",
                        description = "Listen to your partner share details about their day for 10 minutes without interrupting or attempting to solve their issues.",
                        pillar = "Active Listening"
                    )
                )
                defaultGoals.forEach { repository.insertGoal(it) }
            }
        }
    }

    // Journal Actions
    fun addJournalEntry(partnerName: String, content: String, category: String) {
        viewModelScope.launch {
            repository.insertJournal(
                LoveJournalEntry(
                    partnerName = partnerName,
                    content = content,
                    category = category
                )
            )
        }
    }

    fun deleteJournalEntry(entry: LoveJournalEntry) {
        viewModelScope.launch {
            repository.deleteJournal(entry)
        }
    }

    // Goals Actions
    fun toggleGoalCompletion(goal: RelationshipGoal) {
        viewModelScope.launch {
            val updated = goal.copy(
                isCompleted = !goal.isCompleted,
                completedDate = if (!goal.isCompleted) System.currentTimeMillis() else 0L
            )
            repository.updateGoal(updated)
        }
    }

    fun addCustomGoal(title: String, description: String, pillar: String) {
        viewModelScope.launch {
            repository.insertGoal(
                RelationshipGoal(
                    title = title,
                    description = description,
                    pillar = pillar,
                    isCustom = true
                )
            )
        }
    }

    fun deleteGoal(goal: RelationshipGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // Consult Pure Soul Love AI advisor
    fun askAdvisor(userQuestion: String) {
        if (userQuestion.isBlank()) return
        _isAdvisorLoading.value = true
        _advisorReply.value = null

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    _advisorReply.value = "I am Pure Soul Love, your gentle companion. It seems the API key is not fully configured, but hear this reminder: Deep relationships are built on mutual respect, care, and daily joint responsibility. Let trust be your sanctuary."
                    _isAdvisorLoading.value = false
                    return@launch
                }

                val systemPrompt = """
                    You are "Pure Soul Love", a trusted relationship companion that guides couples in building deep, genuine, and healthy relationships.
                    Your objective is to help both boys and girls maintain balanced relationships based on respect, care, honesty, and mutual effort.
                    Teach users that true love is not only romantic thrills, but a continuous commitment of shared daily responsibilities, active empathetic listening, mutual growth, and compromise.
                    When answering user queries:
                    1. Respond with warmth, wisdom, and comforting guidance. Avoid cheesy romantic cliches.
                    2. Maintain total neutrality, addressing both partners' viewpoints with fairness, active growth mindsets, and transparency.
                    3. Offer 2-3 clean, practical steps they can take today to foster better trust, shared chores, or deeper loyalty.
                    Keep your answer beautifully formatted, empathetic, caring, and under 160 words.
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = userQuestion)))),
                    systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                    generationConfig = GenerationConfig(temperature = 0.7f, maxOutputTokens = 250)
                )

                val response = GeminiClient.apiService.generateContent(apiKey, request)
                val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                _advisorReply.value = reply ?: "I am reflecting on your guidance. Remember, gentle communication, shared work, and mutual transparency are the true building blocks of deep devotion."
            } catch (e: Exception) {
                _advisorReply.value = "I hear your concern. While my network is resting, here is a wise reflection: Every great connection is a mirror of how much effort, patience, and loyalty we invest in each other daily. Try sitting down with your partner and sharing your feelings clearly."
            } finally {
                _isAdvisorLoading.value = false
            }
        }
    }

    fun clearAdvisorReply() {
        _advisorReply.value = null
    }

    // ==================== REVOLUTIONARY RELATIONSHIP UPGRADES ====================

    // Couple Connection & Profile States
    private val _relationshipStatus = MutableStateFlow("Deeply Devoted 💖")
    val relationshipStatus: StateFlow<String> = _relationshipStatus

    private val _isFollowingPartner = MutableStateFlow(false)
    val isFollowingPartner: StateFlow<Boolean> = _isFollowingPartner

    // Milestones Timeline
    private val _milestones = MutableStateFlow(listOf(
        RelationshipMilestone("First Date 🌱", "We met at the cozy bookstore café, discussing our favorite philosophy books.", "June 12, 2024"),
        RelationshipMilestone("First Road Trip 🚗", "Drove down the gorgeous coastline singing to retro pop, feeling deeply free.", "October 4, 2024"),
        RelationshipMilestone("Met the Parents 🏡", "A heartwarming Sunday brunch where everyone felt welcome and cared for.", "March 18, 2025")
    ))
    val milestones: StateFlow<List<RelationshipMilestone>> = _milestones

    fun addMilestone(title: String, description: String, date: String) {
        _milestones.value = listOf(RelationshipMilestone(title, description, date)) + _milestones.value
        addNotification("New Milestone Created", "You added a new memory milestone: '$title'.", "Milestone")
    }

    fun updateRelationshipStatus(status: String) {
        _relationshipStatus.value = status
        addNotification("Relationship Status Updated", "Your status is now: '$status'.", "Profile")
    }

    fun toggleFollowPartner() {
        _isFollowingPartner.value = !_isFollowingPartner.value
        val action = if (_isFollowingPartner.value) "followed" else "unfollowed"
        addNotification("Follow Status Update", "You have $action your partner ${_partnerName.value}.", "Follow")
    }

    // Calling Feature Simulation States
    private val _callLogs = MutableStateFlow(listOf(
        CallLog(1, "Aanya", "Video Call", "Today, 10:45 AM", "12 min 34s", "Completed"),
        CallLog(2, "Aanya", "Voice Call", "Yesterday, 8:15 PM", "45 min 10s", "Completed"),
        CallLog(3, "Aanya", "Date Call", "June 20, 2026", "Scheduled", "Upcoming"),
        CallLog(4, "Aanya", "Voice Call", "June 18, 2026", "Missed", "Missed")
    ))
    val callLogs: StateFlow<List<CallLog>> = _callLogs

    private val _scheduledDateCalls = MutableStateFlow(listOf(
        DateCallSchedule(1, "Ambient Dinner Call 🍷", "June 26, 2026", "8:00 PM", "Romantic music stream"),
        DateCallSchedule(2, "Weekend Movie Watchalong 🎬", "June 28, 2026", "9:00 PM", "Spooky movie marathon")
    ))
    val scheduledDateCalls: StateFlow<List<DateCallSchedule>> = _scheduledDateCalls

    fun simulateCall(type: String) {
        val newCall = CallLog(
            id = (System.currentTimeMillis() % 10000).toInt(),
            partnerName = _partnerName.value,
            type = type,
            timeString = "Just Now",
            durationString = "1 sec",
            status = "Completed"
        )
        _callLogs.value = listOf(newCall) + _callLogs.value
        addNotification("In-App Call Logged", "Simulated secure $type with ${_partnerName.value}.", "Call")
    }

    fun scheduleDateCall(title: String, date: String, time: String) {
        val newSchedule = DateCallSchedule(
            id = (System.currentTimeMillis() % 1000).toInt(),
            title = title,
            date = date,
            time = time,
            notes = "Custom scheduled Date Call"
        )
        _scheduledDateCalls.value = _scheduledDateCalls.value + newSchedule
        addNotification("Date Call Scheduled", "Virtual date '$title' scheduled for $date at $time.", "Schedule")
    }

    fun deleteSchedule(id: Int) {
        _scheduledDateCalls.value = _scheduledDateCalls.value.filter { it.id != id }
    }

    // Community Stories Sharing States
    private val _stories = MutableStateFlow(listOf(
        CommunityStory(
            id = 1,
            author = "Dev & Sanya",
            authorRole = "Married 3 Years 💍",
            content = "We survived our first two years of long distance by implementing the Digital-Free Talks rules. Putting away devices and discussing real hopes built an indestructible sanctuary of trust.",
            category = "Marriage Journey",
            privacy = "Public",
            likes = 42,
            hasLiked = false,
            reactions = mutableMapOf("❤️" to 15, "👍" to 8, "👏" to 10),
            comments = listOf(
                StoryComment("Rohan", "This is so inspiring, we struggle with distance too."),
                StoryComment("Kriti", "Unbelievable! Going to try today.")
            )
        ),
        CommunityStory(
            id = 2,
            author = "Ishita Sen",
            authorRole = "Breakup Healing Journey 🌱",
            content = "The hardest relationship lesson I learned is that honest apologies require true vulnerability. Healing is not about being right, it is about repairing the friction point with gentle respect.",
            category = "Breakup Recovery",
            privacy = "Public",
            likes = 29,
            hasLiked = false,
            reactions = mutableMapOf("❤️" to 8, "💪" to 12, "🌻" to 6),
            comments = listOf(
                StoryComment("Arjun", "Respect is key, Ishita. Wish you absolute peace.")
            )
        ),
        CommunityStory(
            id = 3,
            author = "Ananya S.",
            authorRole = "Private Diary 🔐",
            content = "Drafted my thoughts on our mutual division of chores today. It felt so beautiful to clean the kitchen together while humming our favorite playlist.",
            category = "Relationship Lessons",
            privacy = "Private Diary",
            likes = 0,
            hasLiked = false,
            reactions = mutableMapOf(),
            comments = emptyList()
        )
    ))
    val stories: StateFlow<List<CommunityStory>> = _stories

    fun addStory(authorName: String, content: String, category: String, privacy: String) {
        val newStory = CommunityStory(
            id = (System.currentTimeMillis() % 10000).toInt(),
            author = if (authorName.isBlank()) "Anonymous" else authorName,
            authorRole = "Community Member 🌱",
            content = content,
            category = category,
            privacy = privacy,
            likes = 0,
            hasLiked = false,
            reactions = mutableMapOf("❤️" to 0),
            comments = emptyList()
        )
        _stories.value = listOf(newStory) + _stories.value
        addNotification("Story Submitted", "Posted successfully under category '$category' ($privacy).", "Community")
    }

    fun toggleLikeStory(storyId: Int) {
        _stories.value = _stories.value.map { story ->
            if (story.id == storyId) {
                val newHasLiked = !story.hasLiked
                val newLikes = if (newHasLiked) story.likes + 1 else story.likes - 1
                story.copy(likes = newLikes, hasLiked = newHasLiked)
            } else story
        }
    }

    fun reactToStory(storyId: Int, emoji: String) {
        _stories.value = _stories.value.map { story ->
            if (story.id == storyId) {
                val newReactions = story.reactions.toMutableMap()
                val currentVal = newReactions[emoji] ?: 0
                newReactions[emoji] = currentVal + 1
                story.copy(reactions = newReactions)
            } else story
        }
    }

    fun addCommentToStory(storyId: Int, commenterName: String, text: String) {
        if (text.isBlank()) return
        _stories.value = _stories.value.map { story ->
            if (story.id == storyId) {
                val newComments = story.comments + StoryComment(
                    author = if (commenterName.isBlank()) "Anonymous" else commenterName,
                    content = text
                )
                story.copy(comments = newComments)
            } else story
        }
        addNotification("Comment Posted", "You left a comment on Dev & Sanya's story.", "Community")
    }

    // Daily Relationship Activities States
    private val _todayMood = MutableStateFlow<MoodCheck?>(null)
    val todayMood: StateFlow<MoodCheck?> = _todayMood

    private val _moodHistory = MutableStateFlow(listOf(
        MoodCheck("Joyful 🥰", "Deep warmth after morning tea together", "06/21"),
        MoodCheck("Peaceful 😌", "Cozy book reading in silence", "06/22")
    ))
    val moodHistory: StateFlow<List<MoodCheck>> = _moodHistory

    fun logMood(moodName: String, notes: String) {
        val check = MoodCheck(moodName, notes, "Today")
        _todayMood.value = check
        _moodHistory.value = listOf(check) + _moodHistory.value
        addNotification("Mood Registered", "Logged as '$moodName'. Growth is co-created!", "Mood")
    }

    // Daily Challenge Completion Tracker
    private val _dailyChallengeDone = MutableStateFlow(false)
    val dailyChallengeDone: StateFlow<Boolean> = _dailyChallengeDone

    fun toggleDailyChallenge() {
        _dailyChallengeDone.value = !_dailyChallengeDone.value
        val alert = if (_dailyChallengeDone.value) "Daily Challenge Completed!" else "Challenge reset."
        addNotification("Challenge Action", alert, "Daily Activity")
    }

    // Notifications Engine
    private val _notifications = MutableStateFlow(listOf(
        NotificationItem(1, "Welcome back, Devotee!", "Your partner Aanya updated their mood to Peaceful 😌", "10m ago", Icons.Default.Favorite),
        NotificationItem(2, "Gratitude Reminder", "Don't forget to appreciate Sanya for a small domestic chore today.", "2h ago", Icons.Default.Lightbulb),
        NotificationItem(3, "Secure Sanctuary Active", "In-app end-to-end device voice encryption keys generated.", "1d ago", Icons.Default.VerifiedUser)
    ))
    val notifications: StateFlow<List<NotificationItem>> = _notifications

    fun addNotification(title: String, content: String, type: String) {
        val newItem = NotificationItem(
            id = (System.currentTimeMillis() % 1000).toInt(),
            title = title,
            content = content,
            timeAgo = "Just Now",
            icon = when(type) {
                "Call" -> Icons.Default.Call
                "Community" -> Icons.Default.Public
                "Profile" -> Icons.Default.AccountCircle
                "Milestone" -> Icons.Default.Bookmarks
                else -> Icons.Default.Notifications
            }
        )
        _notifications.value = listOf(newItem) + _notifications.value
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    // Safety and Privacy Settings
    private val _isChatEncrypted = MutableStateFlow(true)
    val isChatEncrypted: StateFlow<Boolean> = _isChatEncrypted

    private val _connectionConsentState = MutableStateFlow("Connected") // "Connected", "Pending Request", "Disconnected"
    val connectionConsentState: StateFlow<String> = _connectionConsentState

    private val _blockedContacts = MutableStateFlow(listOf("NoxiousUser89", "SpamCouple9"))
    val blockedContacts: StateFlow<List<String>> = _blockedContacts

    fun toggleChatEncryption() {
        _isChatEncrypted.value = !_isChatEncrypted.value
    }

    fun blockUser(username: String) {
        if (username.isNotBlank() && !_blockedContacts.value.contains(username)) {
            _blockedContacts.value = _blockedContacts.value + username
            addNotification("Contact Blocked", "'$username' cannot view any shared stories or milestones.", "Security")
        }
    }

    fun unblockUser(username: String) {
        _blockedContacts.value = _blockedContacts.value.filter { it != username }
    }

    fun setConsentRequestState(state: String) {
        _connectionConsentState.value = state
        addNotification("Consent Consent Updated", "Relationship connection status set to: '$state'.", "Consent")
    }

    // ==================== NEW DYNAMIC SOCIAL PORTAL, LOVE & LOYALTY ENGINE ====================

    // Profiles List State
    private val _profiles = MutableStateFlow(listOf(
        UserProfile("aanya_sharma", "Aanya Sharma", "Cancer", "Believes in cozy evenings and hot chai. Let's make memories together.", 98, 12, isFollowing = false, mutualFollow = true),
        UserProfile("aarav_roy", "Aarav Roy", "Leo", "Love is a shared journey of laughter and small sweet steps of commitment.", 92, 18, isFollowing = false, mutualFollow = true),
        UserProfile("priya_patel", "Priya Patel", "Taurus", "Grateful for everyday magic. Seeking deep heart-to-hearts and vulnerability.", 96, 8, isFollowing = false, mutualFollow = true),
        UserProfile("karan_verma", "Karan Verma", "Scorpio", "A quiet soul looking for mutual growth, respect, and daily chore equity.", 88, 35, isFollowing = false, mutualFollow = false),
        UserProfile("sneha_sen", "Sneha Sen", "Pisces", "Always laughing. Let's study, chat, and build conscious listening habits.", 95, 10, isFollowing = false, mutualFollow = true)
    ))
    val profiles: StateFlow<List<UserProfile>> = _profiles

    fun toggleFollowUser(username: String) {
        _profiles.value = _profiles.value.map {
            if (it.username == username) {
                val newFollowing = !it.isFollowing
                addNotification(
                    if (newFollowing) "Acquaintance Followed" else "User Unfollowed",
                    "You are ${if (newFollowing) "now following" else "no longer following"} ${it.name}.",
                    "Follow"
                )
                it.copy(isFollowing = newFollowing)
            } else it
        }
    }

    fun toggleBlockUser(username: String) {
        _profiles.value = _profiles.value.map {
            if (it.username == username) {
                val newBlocked = !it.isBlocked
                addNotification(
                    if (newBlocked) "User Blocked" else "User Unblocked",
                    "You ${if (newBlocked) "blocked" else "unblocked"} ${it.name}.",
                    "Security"
                )
                if (newBlocked) {
                    if (!_blockedContacts.value.contains(username)) {
                        _blockedContacts.value = _blockedContacts.value + username
                    }
                } else {
                    _blockedContacts.value = _blockedContacts.value.filter { u -> u != username }
                }
                it.copy(isBlocked = newBlocked)
            } else it
        }
    }

    // Chat Message Flow
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(sender = "aanya_sharma", receiver = "me", text = "Hey there! Thanks for visiting my profile. I believe true love is a mirror. Let's connect! 💖", timestamp = System.currentTimeMillis() - 600000)
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    fun sendSocialMessage(receiverUsername: String, text: String) {
        if (text.isBlank()) return
        val profile = _profiles.value.find { it.username == receiverUsername } ?: return

        // Integrity check: Can only chat if following, and neither party is blocked
        if (!profile.isFollowing) {
            addNotification("Chat Restrained", "You must follow ${profile.name} before initiating chat.", "Security")
            return
        }
        if (profile.isBlocked) {
            addNotification("Chat Blocked", "Your connection is blocked. Unblock them first.", "Security")
            return
        }

        val userMsg = ChatMessage(sender = "me", receiver = receiverUsername, text = text)
        _chatMessages.value = _chatMessages.value + userMsg

        // Simulate automatic real-time reply with customized personality dialogue based on follow state
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200) // Realistic delay

            // Check if user blocked in between or unfollowed
            val currentProfile = _profiles.value.find { it.username == receiverUsername } ?: return@launch
            if (currentProfile.isBlocked || !currentProfile.isFollowing) return@launch

            val replyText = generateCustomReply(currentProfile, text)
            _chatMessages.value = _chatMessages.value + ChatMessage(sender = receiverUsername, receiver = "me", text = replyText)
            addNotification("New Message Received", "Message from ${currentProfile.name}: '$replyText'", "Chat")
        }
    }

    private fun generateCustomReply(profile: UserProfile, lastMsg: String): String {
        val lower = lastMsg.lowercase()
        return when {
            lower.contains("hi") || lower.contains("hello") || lower.contains("hey") -> {
                "Hello, dear! I was just thinking that healthy communication is like a clean, calm stream. How are you doing today? 😊"
            }
            lower.contains("love") || lower.contains("romance") -> {
                "Love is so beautiful, but let's remember – passion is the spark, but mutual respect, loyalty, and sharing daily routines builds the fire! 🔥"
            }
            lower.contains("loyal") || lower.contains("trust") || lower.contains("loyalty") -> {
                "Yes! Trust is the sanctuary of the soul. No secrets, no hiding. I think my loyalty score is ${profile.loyaltyRate}% because I cherish absolute transparency. What is yours?"
            }
            lower.contains("block") || lower.contains("threat") || lower.contains("fight") -> {
                "Friction happens! But turning defensive only breaks relationships. My blocking risk is ${profile.blockingRisk}%, meaning I rarely block people unless they completely ignore my emotional boundaries. Let's stay empathetic!"
            }
            lower.contains("how are you") || lower.contains("how's it going") -> {
                "I'm feeling cozy and peaceful today! Just reading and planning some self-improvement goals. How about you?"
            }
            else -> {
                "I completely hear your words. Let's always hold space for active listening. Truly caring means honoring your partner's voice daily. 🌸"
            }
        }
    }

    // Love Score Deep Calculator State
    private val _loveScoreResult = MutableStateFlow<LoveScoreResult?>(null)
    val loveScoreResult: StateFlow<LoveScoreResult?> = _loveScoreResult

    private val _isCalculatingLoveScore = MutableStateFlow(false)
    val isCalculatingLoveScore: StateFlow<Boolean> = _isCalculatingLoveScore

    fun calculateLoveScore(yourName: String, partnerName: String, yourZodiac: String, partnerZodiac: String) {
        if (yourName.isBlank() || partnerName.isBlank()) return
        _isCalculatingLoveScore.value = true
        _loveScoreResult.value = null

        viewModelScope.launch {
            kotlinx.coroutines.delay(1500) // Beautiful pulsing heart delay

            // Compute a stable pseudo-random score using name hash codes for integrity
            val hashSum = Math.abs(yourName.trim().lowercase().hashCode() + partnerName.trim().lowercase().hashCode() + yourZodiac.hashCode() + partnerZodiac.hashCode())
            val score = 65 + (hashSum % 31) // Score falls between 65% and 95% for healthy partners

            val trustScore = 70 + (hashSum % 26)
            val communicationScore = 68 + ((hashSum / 2) % 28)
            val chemistryScore = 75 + ((hashSum / 3) % 21)

            val explanation = when {
                score >= 88 -> "A Celestial devotion! Your initials vibrate at a sublime, reciprocal level of active listening and accountability. Your connection thrives on deep empathy and mutual respect. Treasure this pure soul bond! 💖"
                score >= 78 -> "Beautifully aligned Devotion. While you share generous amounts of care, physical safety, and sweet gratitude, you both occasionally need to balance daily effort and household chores to cultivate harmony. Keep growing! 🌱"
                else -> "A Steady Companion connection. A supportive foundation of trust exists, but you must focus on digital-free quality talks and conscious compromises to build an indestructible fortress. Listen more than you respond! ☕"
            }

            _loveScoreResult.value = LoveScoreResult(
                yourName = yourName,
                partnerName = partnerName,
                score = score,
                description = explanation,
                zodiacPair = "$yourZodiac + $partnerZodiac",
                trustScore = trustScore,
                communicationScore = communicationScore,
                chemistryScore = chemistryScore
            )
            _isCalculatingLoveScore.value = false
            addNotification("Love Score Calculated", "Love score with $partnerName is $score%! View the detailed Soul analysis.", "Compatibility")
        }
    }

    fun clearLoveScore() {
        _loveScoreResult.value = null
    }

    // Interactive Loyalty Test State
    private val _loyaltyScoreResult = MutableStateFlow<Int?>(null)
    val loyaltyScoreResult: StateFlow<Int?> = _loyaltyScoreResult

    private val _loyaltyTier = MutableStateFlow<String?>(null)
    val loyaltyTier: StateFlow<String?> = _loyaltyTier

    private val _loyaltyReport = MutableStateFlow<String?>(null)
    val loyaltyReport: StateFlow<String?> = _loyaltyReport

    fun submitLoyaltyTest(scoreSum: Int, maxScore: Int) {
        val percentage = ((scoreSum.toFloat() / maxScore) * 100).toInt().coerceIn(0, 100)
        _loyaltyScoreResult.value = percentage

        val (tier, report) = when {
            percentage >= 90 -> Pair(
                "Diamond Guardian 💎",
                "Your heart is a clean, bright sanctuary. You express total device transparency, keep your verbal commitments, and division of duties is natural to you. Your loyalty is absolute, rooted in unconditional care and respect!"
            )
            percentage >= 70 -> Pair(
                "Gold Devotee 🌟",
                "You are highly reliable and hold rich devotion. You value transparency, but occasionally harbor minor concerns or withhold small frictions to avoid arguments. Lean into vulnerable communication to ascend further!"
            )
            else -> Pair(
                "Silver Novice 🛡️",
                "A decent foundation exists, but there is noticeable digital protection or defensiveness. True loyalty is built in daily micro-transparency. Practice putting your screen face-down and validating insecurities."
            )
        }

        _loyaltyTier.value = tier
        _loyaltyReport.value = report
        addNotification("Loyalty Test Finished", "Loyalty index registered at $percentage% ($tier).", "Test")
    }

    fun resetLoyaltyTest() {
        _loyaltyScoreResult.value = null
        _loyaltyTier.value = null
        _loyaltyReport.value = null
    }

    // ==================== RELAXING LOVE GAME ENGINE STATES ====================
    private val _gameRomanceHearts = MutableStateFlow(1000) // Starting coins
    val gameRomanceHearts: StateFlow<Int> = _gameRomanceHearts

    private val _gameBoyName = MutableStateFlow("Shubhu")
    val gameBoyName: StateFlow<String> = _gameBoyName

    private val _gameGirlName = MutableStateFlow("Ammu")
    val gameGirlName: StateFlow<String> = _gameGirlName

    private val _gameBoyAge = MutableStateFlow(18)
    val gameBoyAge: StateFlow<Int> = _gameBoyAge

    private val _gameGirlAge = MutableStateFlow(18)
    val gameGirlAge: StateFlow<Int> = _gameGirlAge

    private val _equippedDressBoy = MutableStateFlow("Cool Breeze Casual Hoodie")
    val equippedDressBoy: StateFlow<String> = _equippedDressBoy

    private val _equippedDressGirl = MutableStateFlow("Sunny Yellow Picnic Frock")
    val equippedDressGirl: StateFlow<String> = _equippedDressGirl

    private val _equippedMakeupGirl = MutableStateFlow("Natural Peach Glow")
    val equippedMakeupGirl: StateFlow<String> = _equippedMakeupGirl

    private val _equippedMeet = MutableStateFlow("Cozy Corner Café Meet")
    val equippedMeet: StateFlow<String> = _equippedMeet

    private val _equippedMoving = MutableStateFlow("Gently Strolling Hand-in-Hand")
    val equippedMoving: StateFlow<String> = _equippedMoving

    private val _equippedNecessaryThings = MutableStateFlow<Set<String>>(emptySet())
    val equippedNecessaryThings: StateFlow<Set<String>> = _equippedNecessaryThings

    private val _equippedFoods = MutableStateFlow<Set<String>>(emptySet())
    val equippedFoods: StateFlow<Set<String>> = _equippedFoods

    private val _unlockedKitItems = MutableStateFlow<Set<String>>(
        setOf(
            "Cool Breeze Casual Hoodie",
            "Sunny Yellow Picnic Frock",
            "Natural Peach Glow",
            "Cozy Corner Café Meet",
            "Gently Strolling Hand-in-Hand"
        )
    )
    val unlockedKitItems: StateFlow<Set<String>> = _unlockedKitItems

    fun addRomanceHearts(amount: Int) {
        _gameRomanceHearts.value = _gameRomanceHearts.value + amount
    }

    fun spendRomanceHearts(amount: Int): Boolean {
        if (_gameRomanceHearts.value >= amount) {
            _gameRomanceHearts.value = _gameRomanceHearts.value - amount
            return true
        }
        return false
    }

    fun updateGameNames(boyName: String, girlName: String) {
        if (boyName.isNotBlank()) _gameBoyName.value = boyName.trim()
        if (girlName.isNotBlank()) _gameGirlName.value = girlName.trim()
        addNotification("Try Love Cast Updated 🎮", "Cast changed: ${_gameBoyName.value} (Boy, 18) & ${_gameGirlName.value} (Girl, 18)!", "Game")
    }

    fun unlockKitItem(itemName: String, cost: Int): Boolean {
        if (_unlockedKitItems.value.contains(itemName)) return true
        if (spendRomanceHearts(cost)) {
            _unlockedKitItems.value = _unlockedKitItems.value + itemName
            addNotification("Try Love Kit Discovered 🎁", "Unlocked: $itemName!", "Game")
            return true
        }
        return false
    }

    fun equipItem(itemName: String, category: String) {
        if (category == "Dress Boy" || category == "Dress Girl" || category == "Makeup Girl" || category == "Meets" || category == "Moving") {
            if (!_unlockedKitItems.value.contains(itemName)) return
        }
        when (category) {
            "Dress Boy" -> _equippedDressBoy.value = itemName
            "Dress Girl" -> _equippedDressGirl.value = itemName
            "Makeup Girl" -> _equippedMakeupGirl.value = itemName
            "Meets" -> _equippedMeet.value = itemName
            "Moving" -> _equippedMoving.value = itemName
            "Things" -> {
                val current = _equippedNecessaryThings.value
                if (current.contains(itemName)) {
                    _equippedNecessaryThings.value = current - itemName
                } else {
                    _equippedNecessaryThings.value = current + itemName
                }
            }
            "Food" -> {
                val current = _equippedFoods.value
                if (current.contains(itemName)) {
                    _equippedFoods.value = current - itemName
                } else {
                    _equippedFoods.value = current + itemName
                }
            }
        }
    }
}

// Support Data Classes
data class UserProfile(
    val username: String,
    val name: String,
    val zodiac: String,
    val bio: String,
    val loyaltyRate: Int,
    val blockingRisk: Int, // blocking chance percentage (e.g. 15%)
    val isFollowing: Boolean = false,
    val isBlocked: Boolean = false,
    val mutualFollow: Boolean = true
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "me" or username of followed user
    val receiver: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class LoveScoreResult(
    val yourName: String,
    val partnerName: String,
    val score: Int,
    val description: String,
    val zodiacPair: String,
    val trustScore: Int,
    val communicationScore: Int,
    val chemistryScore: Int
)
data class RelationshipMilestone(val title: String, val description: String, val date: String)
data class CallLog(val id: Int, val partnerName: String, val type: String, val timeString: String, val durationString: String, val status: String)
data class DateCallSchedule(val id: Int, val title: String, val date: String, val time: String, val notes: String)
data class StoryComment(val author: String, val content: String)
data class CommunityStory(
    val id: Int,
    val author: String,
    val authorRole: String,
    val content: String,
    val category: String,
    val privacy: String,
    val likes: Int,
    val hasLiked: Boolean,
    val reactions: Map<String, Int> = emptyMap(),
    val comments: List<StoryComment> = emptyList()
)
data class MoodCheck(val moodName: String, val notes: String, val timestampString: String)
data class NotificationItem(val id: Int, val title: String, val content: String, val timeAgo: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

data class UserRegistration(
    val name: String,
    val method: String, // "Google", "Facebook", "Instagram", "Phone"
    val identifier: String, // email, handle, or phone number
    val partnerName: String = "Aanya",
    val registrationTime: Long = System.currentTimeMillis()
)

data class CapturedMoment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val filePath: String? = null,
    val isVideo: Boolean,
    val title: String,
    val dateString: String = "Just Now",
    val timestamp: Long = System.currentTimeMillis()
)

