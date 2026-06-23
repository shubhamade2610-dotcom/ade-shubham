package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.data.api.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoveViewModel(private val repository: LoveRepository) : ViewModel() {
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab

    fun setTab(index: Int) {
        _currentTab.value = index
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
}
