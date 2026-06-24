package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "love_journal")
data class LoveJournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partnerName: String,
    val content: String,
    val category: String, // "Gratitude", "Breakthrough", "Sweet Moment", "Growth Note"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "relationship_goals")
data class RelationshipGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val pillar: String, // "Trust", "Respect", "Loyalty", "Responsibilities", "Active Listening"
    val isCompleted: Boolean = false,
    val completedDate: Long = 0L,
    val isCustom: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "loverry_gallery")
data class LoverryPhoto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val imagePath: String, // Could be preset resource identifier name, URL, or local CameraX path
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface LoveDao {
    // Journal
    @Query("SELECT * FROM love_journal ORDER BY timestamp DESC")
    fun getAllJournalEntries(): Flow<List<LoveJournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: LoveJournalEntry)

    @Delete
    suspend fun deleteJournalEntry(entry: LoveJournalEntry)

    // Goals
    @Query("SELECT * FROM relationship_goals ORDER BY timestamp DESC")
    fun getAllGoals(): Flow<List<RelationshipGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: RelationshipGoal)

    @Update
    suspend fun updateGoal(goal: RelationshipGoal)

    @Delete
    suspend fun deleteGoal(goal: RelationshipGoal)

    @Query("SELECT COUNT(*) FROM relationship_goals WHERE isCompleted = 1")
    fun getCompletedGoalsCount(): Flow<Int>

    // Loverry Gallery
    @Query("SELECT * FROM loverry_gallery ORDER BY timestamp DESC")
    fun getAllLoverryPhotos(): Flow<List<LoverryPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoverryPhoto(photo: LoverryPhoto)

    @Delete
    suspend fun deleteLoverryPhoto(photo: LoverryPhoto)
}

@Database(entities = [LoveJournalEntry::class, RelationshipGoal::class, LoverryPhoto::class], version = 2, exportSchema = false)
abstract class LoveDatabase : RoomDatabase() {
    abstract fun loveDao(): LoveDao
}

class LoveRepository(private val loveDao: LoveDao) {
    val allJournalEntries: Flow<List<LoveJournalEntry>> = loveDao.getAllJournalEntries()
    val allGoals: Flow<List<RelationshipGoal>> = loveDao.getAllGoals()
    val completedGoalsCount: Flow<Int> = loveDao.getCompletedGoalsCount()
    val allLoverryPhotos: Flow<List<LoverryPhoto>> = loveDao.getAllLoverryPhotos()

    suspend fun insertJournal(entry: LoveJournalEntry) = loveDao.insertJournalEntry(entry)
    suspend fun deleteJournal(entry: LoveJournalEntry) = loveDao.deleteJournalEntry(entry)

    suspend fun insertGoal(goal: RelationshipGoal) = loveDao.insertGoal(goal)
    suspend fun updateGoal(goal: RelationshipGoal) = loveDao.updateGoal(goal)
    suspend fun deleteGoal(goal: RelationshipGoal) = loveDao.deleteGoal(goal)

    suspend fun insertLoverryPhoto(photo: LoverryPhoto) = loveDao.insertLoverryPhoto(photo)
    suspend fun deleteLoverryPhoto(photo: LoverryPhoto) = loveDao.deleteLoverryPhoto(photo)
}
