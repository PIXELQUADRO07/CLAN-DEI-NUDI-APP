package com.example.CDN.db

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- ENTITIES ---

// User Profile & Authentication Account
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String, // Clan handle, e.g. "cyber_nudo_77"
    val email: String,
    val bio: String = "OPERATIVO NEL CLAN DEI NUDI APP.",
    val followersCount: Int = 142,
    val followingCount: Int = 89,
    val biometricEnabled: Boolean = false,
    val firebaseSynced: Boolean = true,
    val passwordHash: String = "clan_pass_123"
)

// Textual feeds & Polls (Instagram style)
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val author: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val sharesCount: Int = 0,
    val likedByUsers: String = "", // Comma-separated list of usernames who liked this
    // Poll attributes (Sondaggio support)
    val isPoll: Boolean = false,
    val pollQuestion: String = "",
    val pollOptions: String = "", // Pipes-separated options: e.g. "REALE|CYBERPUNK"
    val pollVotes: String = "" // Comma-separated votes counters: e.g. "12,24"
)

// Encrypted message model
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idCode: String = "MSG-${(1000..9999).random()}",
    val sender: String,
    val receiver: String,
    val encryptedBody: String, // Encrypted hex/base64 string
    val originalDecryptKey: String, // Simulates E2E dynamic crypto key
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

// Backup logs
@Entity(tableName = "backups")
data class BackupEntity(
    @PrimaryKey val id: String, // Timestamp identifier
    val backupName: String,
    val encryptedData: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAO INTERFACE ---

@Dao
interface ClanDao {
    // Account queries
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getPrimaryUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUser(username: String)

    // Posts & Polls queries (Flow for live streams)
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPostsFlow(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Int)

    // Messages queries
    @Query("SELECT * FROM messages WHERE (sender = :userA AND receiver = :userB) OR (sender = :userB AND receiver = :userA) ORDER BY timestamp ASC")
    fun getChatMessagesFlow(userA: String, userB: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllDirectMessagesFlow(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    // Backups
    @Query("SELECT * FROM backups ORDER BY timestamp DESC")
    fun getAllBackupsFlow(): Flow<List<BackupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupEntity)

    @Query("DELETE FROM backups WHERE id = :backupId")
    suspend fun deleteBackup(backupId: String)
}

// --- DATABASE CLASS ---

@Database(
    entities = [UserEntity::class, PostEntity::class, MessageEntity::class, BackupEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ClanDatabase : RoomDatabase() {
    abstract val dao: ClanDao

    companion object {
        @Volatile
        private var INSTANCE: ClanDatabase? = null

        fun getDatabase(context: Context): ClanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClanDatabase::class.java,
                    "clan_dei_nudi_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
