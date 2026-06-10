package com.example.CDN.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.CDN.db.*
import com.example.CDN.security.SecurityUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.random.Random

// Nearby member on Radar Map
data class RadarMember(
    val id: String,
    val username: String,
    val distanceMeters: Int,
    val bearingDegrees: Float,
    val activeStatus: String, // "OPERATIVO", "SILENTE", "INCURSIONE"
    val distressLevel: Int, // Percentage
    val bio: String
)

// GitHub release model representation
data class GitHubRelease(
    val version: String,
    val releaseDate: String,
    val changelog: List<String>,
    val sizeMb: Double,
    val canUpdate: Boolean
)

// Active Custom Notification State
data class SimulatedNotification(
    val id: String,
    val title: String,
    val body: String,
    val type: String, // "MESSAGGI", "SONDAGGIO", "ALLERTA", "FIREBASE"
    val durationMs: Long = 4000L
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ClanDatabase.getDatabase(application)
    private val clanDao = database.dao
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Navigation Screens State
    private val _currentScreen = MutableStateFlow("splash") 
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _activeChannel = MutableStateFlow("generale")
    val activeChannel: StateFlow<String> = _activeChannel.asStateFlow()

    private val _adminUsersList = MutableStateFlow<List<UserEntity>>(emptyList())
    val adminUsersList: StateFlow<List<UserEntity>> = _adminUsersList.asStateFlow()

    // Logged in User State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Active Chat Member Selection
    private val _activeChatReceiver = MutableStateFlow<String>("admin_clan")
    val activeChatReceiver: StateFlow<String> = _activeChatReceiver.asStateFlow()

    // Global App Custom Themes
    private val _isOledDarkMode = MutableStateFlow(true)
    val isOledDarkMode: StateFlow<Boolean> = _isOledDarkMode.asStateFlow()

    // Biometric lock triggers
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _isAuthenticatingBiometrics = MutableStateFlow(false)
    val isAuthenticatingBiometrics: StateFlow<Boolean> = _isAuthenticatingBiometrics.asStateFlow()

    private val _biometricAuthError = MutableStateFlow("")
    val biometricAuthError: StateFlow<String> = _biometricAuthError.asStateFlow()

    // Firebase synchronization monitor
    private val _firebaseSynced = MutableStateFlow(true)
    val firebaseSynced: StateFlow<Boolean> = _firebaseSynced.asStateFlow()

    private val _activeFirebaseStatusLog = MutableStateFlow("CONNESSO AI LIVE NODES CLOUD")
    val activeFirebaseStatusLog: StateFlow<String> = _activeFirebaseStatusLog.asStateFlow()

    // Feed Streams (Posts & Surveys from Firestore)
    private val _firestorePosts = MutableStateFlow<List<PostEntity>>(emptyList())
    val postsFeed: StateFlow<List<PostEntity>> = _firestorePosts.asStateFlow()

    private var postsListener: ListenerRegistration? = null

    // Chat streams for currently selected pair (Firestore)
    private val _firestoreMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val currentChatMessages: StateFlow<List<MessageEntity>> = _firestoreMessages.asStateFlow()

    private var messagesListener: ListenerRegistration? = null

    // Simulated nearby radar members
    val radarMembers = mutableStateListOf<RadarMember>()

    // Notifications
    private val _activeNotification = MutableStateFlow<SimulatedNotification?>(null)
    val activeNotification: StateFlow<SimulatedNotification?> = _activeNotification.asStateFlow()

    // Settings
    private val _pushSettings = MutableStateFlow(mapOf("MESSAGGI" to true, "SONDAGGI" to true, "ALLERTA" to true, "SISTEMA" to false))
    val pushSettings: StateFlow<Map<String, Boolean>> = _pushSettings.asStateFlow()

    // GitHub Updates
    private val _githubRelease = MutableStateFlow<GitHubRelease?>(null)
    val githubRelease: StateFlow<GitHubRelease?> = _githubRelease.asStateFlow()

    private val _checkingForUpdates = MutableStateFlow(false)
    val checkingForUpdates: StateFlow<Boolean> = _checkingForUpdates.asStateFlow()

    val repoOwner: StateFlow<String> = MutableStateFlow("PIXELQUADRO07").asStateFlow()
    val repoName: StateFlow<String> = MutableStateFlow("CLAN-DEI-NUDI-APP").asStateFlow()

    private val _serviceStatus = MutableStateFlow("IDLE")
    val serviceStatus: StateFlow<String> = _serviceStatus.asStateFlow()

    private val _serviceEnabled = MutableStateFlow(true)
    val serviceEnabled: StateFlow<Boolean> = _serviceEnabled.asStateFlow()

    // Language
    private val _appLanguage = MutableStateFlow("IT")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val dictionary = mapOf(
        "IT" to mapOf(
            "app_title" to "CLAN DEI NUDI APP",
            "theme_title" to "TEMATIZZAZIONE INTERFACCIA TATTICA",
            "oled_energy_saving" to "STRUTTURA RISPARMIO ENERGETICO OLED",
            "light_theme" to "CYBER LIGHT",
            "dark_theme" to "OLED BLACK",
            "fcm_title" to "NOTIFICHE DI CLOUD MESSAGING PERSONALIZZATE",
            "pulse_notification" to "NOTIFICHE IMPULSI",
            "github_patches_title" to "CONTROLLO RELEASES GITHUB",
            "github_joined_repo" to "REPOSITORY ABBINATO ATTIVAMENTE:",
            "save_github_config" to "Salva Configurazione GitHub",
            "bg_scanner" to "SERVIZIO SCANSIONE SFONDO",
            "daemon_status" to "STATO DAEMON",
            "active_release" to "RELEASES ATTIVA INTERFACCIA:",
            "check_github_latest" to "Controlla Nuova Release Github",
            "download_github_update" to "INSTALLA AGGIORNAMENTO GITHUB",
            "tab_feed" to "Bacheca",
            "tab_radar" to "Radar GPS",
            "tab_surveys" to "Sondaggi",
            "tab_chat" to "Chat Criptate",
            "tab_settings" to "Config",
            "tab_shell" to "Shell UNIX",
            "shell_title" to "TERMINALE SHELL STRUTTURATO",
            "shell_badge" to "SECURE_ROOT_SHELL",
            "shell_placeholder" to "Digita un comando... (es. help)",
            "shell_hint" to "Terminale di controllo locale ad accesso privilegiato.",
            "profile_shortcut" to "profilo",
            "exit" to "Uscita / Disconnetti",
            "destruct_title" to "AUTODISTRUZIONE TERMINALE"
        ),
        "EN" to mapOf(
            "app_title" to "CLAN DEI NUDI APP",
            "theme_title" to "TACTICAL INTERFACE THEME",
            "oled_energy_saving" to "OLED ENERGY SAVING STRUCTURE",
            "light_theme" to "CYBER LIGHT",
            "dark_theme" to "OLED BLACK",
            "fcm_title" to "CUSTOM CLOUD MESSAGING NOTIFICATIONS",
            "pulse_notification" to "IMPULSE NOTIFICATIONS",
            "github_patches_title" to "GITHUB RELEASES CONTROL",
            "github_joined_repo" to "ACTIVELY ASSOCIATED REPOSITORY:",
            "save_github_config" to "Save GitHub Configuration",
            "bg_scanner" to "BACKGROUND SCAN SERVICE",
            "daemon_status" to "DAEMON STATUS",
            "active_release" to "ACTIVE INTERFACE RELEASE:",
            "check_github_latest" to "Check New GitHub Release",
            "download_github_update" to "INSTALL GITHUB UPDATE",
            "tab_feed" to "Feed",
            "tab_radar" to "GPS Radar",
            "tab_surveys" to "Surveys",
            "tab_chat" to "Encrypted Chat",
            "tab_settings" to "Settings",
            "tab_shell" to "UNIX Shell",
            "shell_title" to "STRUCTURED SHELL TERMINAL",
            "shell_badge" to "SECURE_ROOT_SHELL",
            "shell_placeholder" to "Type a command... (e.g. help)",
            "shell_hint" to "Local control terminal with privileged access.",
            "profile_shortcut" to "profile",
            "exit" to "Exit / Disconnect",
            "destruct_title" to "TERMINAL SELF-DESTRUCT"
        ),
        "ES" to mapOf(
            "app_title" to "CLAN DEI NUDI APP",
            "theme_title" to "TEMA DE INTERFAZ TÁCTICA",
            "oled_energy_saving" to "ESTRUCTURA DE AHORRO DE ENERGÍA OLED",
            "light_theme" to "CYBER LIGHT",
            "dark_theme" to "OLED BLACK",
            "fcm_title" to "NOTIFICACIONES DE MENSAJERÍA EN LA NUBE PERSONALIZADAS",
            "pulse_notification" to "NOTIFICACIONES DE IMPULSO",
            "github_patches_title" to "CONTROL DE VERSIONES DE GITHUB",
            "github_joined_repo" to "REPOSITORIO ASOCIADO ACTIVAMENTE:",
            "save_github_config" to "Guardar Configuración de GitHub",
            "bg_scanner" to "SERVICIO DE ESCANEO EN SEGUNDO PLANO",
            "daemon_status" to "ESTADO DEL DEMONIO",
            "active_release" to "VERSIÓN DE INTERFAZ ACTIVA:",
            "check_github_latest" to "Comprobar Nueva Versión de GitHub",
            "download_github_update" to "INSTALAR ACTUALIZACIÓN DE GITHUB",
            "tab_feed" to "Muro",
            "tab_radar" to "Radar GPS",
            "tab_surveys" to "Encuestas",
            "tab_chat" to "Chats Encriptados",
            "tab_settings" to "Ajustes",
            "tab_shell" to "Terminal Shell",
            "shell_title" to "TERMINAL DE SHELL ESTRUCTURADA",
            "shell_badge" to "SECURE_ROOT_SHELL",
            "shell_placeholder" to "Escribe un comando... (ej. help)",
            "shell_hint" to "Terminal di controllo locale con accesso privilegiato.",
            "profile_shortcut" to "perfil",
            "exit" to "Salir / Disconnect",
            "destruct_title" to "AUTODESTRUCCIÓN DE LA TERMINAL"
        )
    )

    fun translate(key: String, fallback: String): String {
        return dictionary[_appLanguage.value]?.get(key) ?: fallback
    }

    fun setLanguage(lang: String) {
        if (lang == "IT" || lang == "EN" || lang == "ES") {
            _appLanguage.value = lang
            val prefs = getApplication<Application>().getSharedPreferences("clan_github_updater_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("app_language", lang).apply()
            triggerSimulatedNotification("LINGUA CAMBIATA", "App language updated to [$lang]", "SISTEMA")
        }
    }

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == com.example.CDN.service.GitHubUpdateService.ACTION_UPDATE_STATUS) {
                _serviceStatus.value = intent.getStringExtra(com.example.CDN.service.GitHubUpdateService.EXTRA_STATUS) ?: "IDLE"
            }
        }
    }

    init {
        val prefs = application.getSharedPreferences("clan_github_updater_prefs", Context.MODE_PRIVATE)
        _serviceEnabled.value = prefs.getBoolean("service_enabled", true)
        _appLanguage.value = prefs.getString("app_language", "IT") ?: "IT"

        val filter = IntentFilter(com.example.CDN.service.GitHubUpdateService.ACTION_UPDATE_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            application.registerReceiver(updateReceiver, filter)
        }

        if (_serviceEnabled.value) {
            com.example.CDN.service.GitHubUpdateService.start(application)
        }

        setupFirestorePostsListener()
        checkIfUserLoggedIn()
        generateDynamicRadarMembers()
    }

    private fun generateDynamicRadarMembers() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").get().await()
                val activeUsers = snapshot.documents.mapNotNull { it.getString("username") }
                
                val currentList = mutableListOf<RadarMember>()
                val random = Random(System.currentTimeMillis())
                val statuses = listOf("OPERATIVO", "SILENTE", "INCURSIONE")
                
                for (i in activeUsers.indices) {
                    val uname = activeUsers[i]
                    if (uname == _currentUser.value?.username) continue
                    
                    currentList.add(RadarMember(
                        id = (i + 1).toString(),
                        username = uname,
                        distanceMeters = random.nextInt(100, 4000), // Simulated distance for privacy
                        bearingDegrees = random.nextFloat() * 360f,
                        activeStatus = statuses.random(random),
                        distressLevel = random.nextInt(0, 100),
                        bio = "Connessione stabilita."
                    ))
                }
                radarMembers.clear()
                radarMembers.addAll(currentList)
            } catch (e: Exception) {
                _activeFirebaseStatusLog.value = "RADAR SYNC FAILED"
            }
        }
    }

    private fun checkIfUserLoggedIn() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            viewModelScope.launch {
                val localUser = clanDao.getPrimaryUser()
                if (localUser != null) {
                    _currentUser.value = localUser
                    _currentScreen.value = "feed"
                } else {
                    fetchUserFromFirestore(firebaseUser.uid)
                }
            }
        }
    }

    private suspend fun fetchUserFromFirestore(uid: String) {
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val user = UserEntity(
                    username = doc.getString("username") ?: "unknown",
                    email = doc.getString("email") ?: "",
                    bio = doc.getString("bio") ?: "",
                    followersCount = doc.getLong("followersCount")?.toInt() ?: 0,
                    followingCount = doc.getLong("followingCount")?.toInt() ?: 0,
                    role = doc.getString("role") ?: "OPERATIVO",
                    tacticalPin = doc.getString("tacticalPin") ?: ""
                )
                clanDao.insertUser(user)
                _currentUser.value = user
                _currentScreen.value = "feed"
            }
        } catch (e: Exception) {
            _activeFirebaseStatusLog.value = "ERRORE SYNC FIRESTORE: ${e.message}"
        }
    }

    fun changeActiveChatReceiver(username: String) {
        _activeChatReceiver.value = username
        setupFirestoreMessagesListener()
    }

    fun changeActiveChannel(channel: String) {
        _activeChannel.value = channel
        setupFirestorePostsListener()
    }

    private fun setupFirestorePostsListener() {
        postsListener?.remove()
        postsListener = firestore.collection("posts")
            .whereEqualTo("channel", _activeChannel.value)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _activeFirebaseStatusLog.value = "ERRORE FEED: ${e.message}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { doc ->
                        val isGhost = doc.getBoolean("isGhost") ?: false
                        val expiresAt = doc.getLong("expiresAt") ?: 0L
                        val currentTime = System.currentTimeMillis()
                        
                        if (isGhost && currentTime > expiresAt) {
                            doc.reference.delete()
                            null
                        } else {
                            PostEntity(
                                id = doc.id.hashCode(),
                                author = doc.getString("author") ?: "anon",
                                content = doc.getString("content") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                likesCount = doc.getLong("likesCount")?.toInt() ?: 0,
                                sharesCount = doc.getLong("sharesCount")?.toInt() ?: 0,
                                likedByUsers = doc.getString("likedByUsers") ?: "",
                                isPoll = doc.getBoolean("isPoll") ?: false,
                                pollQuestion = doc.getString("pollQuestion") ?: "",
                                pollOptions = doc.getString("pollOptions") ?: "",
                                pollVotes = doc.getString("pollVotes") ?: "",
                                channel = doc.getString("channel") ?: "generale",
                                isGhost = isGhost,
                                expiresAt = expiresAt
                            )
                        }
                    }
                    _firestorePosts.value = posts
                }
            }
    }

    private fun setupFirestoreMessagesListener() {
        val currentUser = _currentUser.value?.username ?: return
        val receiver = _activeChatReceiver.value
        messagesListener?.remove()

        messagesListener = firestore.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { doc ->
                        val mSender = doc.getString("sender") ?: ""
                        val mReceiver = doc.getString("receiver") ?: ""
                        val isGhost = doc.getBoolean("isGhost") ?: false
                        val expiresAt = doc.getLong("expiresAt") ?: 0L
                        val currentTime = System.currentTimeMillis()
                        
                        if (isGhost && currentTime > expiresAt) {
                            doc.reference.delete()
                            return@mapNotNull null
                        }

                        if ((mSender == currentUser && mReceiver == receiver) || 
                            (mSender == receiver && mReceiver == currentUser)) {
                            MessageEntity(
                                idCode = doc.getString("idCode") ?: "MSG-0000",
                                sender = mSender,
                                receiver = mReceiver,
                                encryptedBody = doc.getString("encryptedBody") ?: "",
                                originalDecryptKey = doc.getString("originalDecryptKey") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                isGhost = isGhost,
                                expiresAt = expiresAt
                            )
                        } else null
                    }
                    _firestoreMessages.value = msgs
                }
            }
    }

    fun sendEncryptedMessage(text: String, cryptoKey: String, isGhost: Boolean = false) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val senderUsername = _currentUser.value?.username ?: "anon_clan"
            val receiverUsername = _activeChatReceiver.value
            val msgId = "MSG-${(1000..9999).random()}"
            val messageMap = hashMapOf(
                "idCode" to msgId,
                "sender" to senderUsername,
                "receiver" to receiverUsername,
                "encryptedBody" to SecurityUtils.encryptAES(text, cryptoKey),
                "originalDecryptKey" to cryptoKey,
                "timestamp" to System.currentTimeMillis(),
                "isGhost" to isGhost,
                "expiresAt" to if (isGhost) System.currentTimeMillis() + 3600000L else 0L
            )
            try {
                firestore.collection("messages").add(messageMap).await()
            } catch (e: Exception) { }
        }
    }

    fun createNewPost(text: String, isPoll: Boolean = false, pollQuestion: String = "", pollOptionsList: List<String> = emptyList(), isGhost: Boolean = false) {
        viewModelScope.launch {
            val authorName = _currentUser.value?.username ?: "anon_clan"
            val postMap = hashMapOf(
                "author" to authorName,
                "content" to text,
                "timestamp" to System.currentTimeMillis(),
                "likesCount" to 0,
                "likedByUsers" to "",
                "isPoll" to isPoll,
                "pollQuestion" to pollQuestion,
                "pollOptions" to pollOptionsList.joinToString("|"),
                "pollVotes" to List(pollOptionsList.size) { "0" }.joinToString(","),
                "channel" to _activeChannel.value,
                "isGhost" to isGhost,
                "expiresAt" to if (isGhost) System.currentTimeMillis() + 3600000L else 0L
            )
            try {
                firestore.collection("posts").add(postMap).await()
                triggerSimulatedNotification("POST PUBBLICATO", "Update aggiunto al feed.", if (isPoll) "SONDAGGIO" else "MESSAGGI")
            } catch (e: Exception) { }
        }
    }

    fun toggleLikePost(postId: Int) {
        viewModelScope.launch {
            val user = _currentUser.value?.username ?: return@launch
            val post = postsFeed.value.find { it.id == postId } ?: return@launch
            try {
                val snap = firestore.collection("posts").whereEqualTo("timestamp", post.timestamp).get().await()
                val doc = snap.documents.firstOrNull() ?: return@launch
                val likedList = (doc.getString("likedByUsers") ?: "").split(",").filter { it.isNotEmpty() }.toMutableList()
                val nextLikes: Int
                if (likedList.contains(user)) {
                    likedList.remove(user)
                    nextLikes = ((doc.getLong("likesCount") ?: 0) - 1).toInt().coerceAtLeast(0)
                } else {
                    likedList.add(user)
                    nextLikes = ((doc.getLong("likesCount") ?: 0) + 1).toInt()
                }
                doc.reference.update("likesCount", nextLikes, "likedByUsers", likedList.joinToString(",")).await()
            } catch (e: Exception) { }
        }
    }

    fun submitPollVote(postId: Int, optionIndex: Int) {
        viewModelScope.launch {
            val post = postsFeed.value.find { it.id == postId } ?: return@launch
            try {
                val snap = firestore.collection("posts").whereEqualTo("timestamp", post.timestamp).get().await()
                val doc = snap.documents.firstOrNull() ?: return@launch
                val votes = (doc.getString("pollVotes") ?: "").split(",").map { it.toIntOrNull() ?: 0 }.toMutableList()
                if (optionIndex in votes.indices) {
                    votes[optionIndex]++
                    doc.reference.update("pollVotes", votes.joinToString(",")).await()
                    triggerSimulatedNotification("VOTO REGISTRATO", "Impulso ricevuto.", "SONDAGGIO")
                }
            } catch (e: Exception) { }
        }
    }

    fun deletePostById(id: Int) {
        viewModelScope.launch {
            val post = postsFeed.value.find { it.id == id } ?: return@launch
            try {
                val snap = firestore.collection("posts").whereEqualTo("timestamp", post.timestamp).get().await()
                snap.documents.firstOrNull()?.reference?.delete()?.await()
                clanDao.deletePost(id)
            } catch (e: Exception) { }
        }
    }

    fun navigateTo(screenName: String) {
        viewModelScope.launch {
            if (screenName == "lock") {
                _isAppLocked.value = true
                _biometricAuthError.value = ""
            }
            _currentScreen.value = screenName
        }
    }

    fun performBiometricFingerprintChallenge(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isAuthenticatingBiometrics.value = true
            _biometricAuthError.value = ""
            kotlinx.coroutines.delay(800)
            _isAuthenticatingBiometrics.value = false
            _isAppLocked.value = false
            onSuccess()
        }
    }

    fun toggleOledTheme() { _isOledDarkMode.value = !_isOledDarkMode.value }

    fun toggleFirebaseSync() {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                val updated = user.copy(firebaseSynced = !user.firebaseSynced)
                clanDao.updateUser(updated)
                _currentUser.value = updated
            }
        }
    }

    fun updateUserProfile(newBio: String, newPin: String) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                val updated = user.copy(bio = newBio, tacticalPin = newPin)
                clanDao.updateUser(updated)
                _currentUser.value = updated
                try {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        firestore.collection("users").document(uid).update(
                            "bio", newBio,
                            "tacticalPin", newPin
                        ).await()
                    }
                } catch (e: Exception) { }
            }
        }
    }

    fun fetchAdminUserList() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").get().await()
                val users = snapshot.documents.mapNotNull { doc ->
                    UserEntity(
                        username = doc.getString("username") ?: "unknown",
                        email = doc.getString("email") ?: "",
                        bio = doc.getString("bio") ?: "",
                        role = doc.getString("role") ?: "OPERATIVO",
                        tacticalPin = doc.getString("tacticalPin") ?: ""
                    )
                }
                _adminUsersList.value = users
            } catch (e: Exception) { }
        }
    }

    fun adminChangeUserRole(targetUsername: String, newRole: String) {
        viewModelScope.launch {
            try {
                val snap = firestore.collection("users").whereEqualTo("username", targetUsername).get().await()
                for (doc in snap.documents) {
                    doc.reference.update("role", newRole).await()
                }
                fetchAdminUserList() // refresh
            } catch (e: Exception) { }
        }
    }

    fun togglePushSetting(category: String) {
        val updated = _pushSettings.value.toMutableMap()
        updated[category] = !(updated[category] ?: true)
        _pushSettings.value = updated
    }

    fun triggerSimulatedNotification(title: String, body: String, category: String) {
        if (!(_pushSettings.value[category] ?: true)) return 
        viewModelScope.launch {
            val sim = SimulatedNotification(id = System.currentTimeMillis().toString(), title = title, body = body, type = category)
            _activeNotification.value = sim
            Handler(Looper.getMainLooper()).postDelayed({ if (_activeNotification.value?.id == sim.id) _activeNotification.value = null }, sim.durationMs)
        }
    }

    fun dismissActiveNotification() { _activeNotification.value = null }

    fun checkForClusterUpdates() {
        _checkingForUpdates.value = true
        com.example.CDN.service.GitHubUpdateService.checkNow(getApplication())
        viewModelScope.launch { kotlinx.coroutines.delay(2000); _checkingForUpdates.value = false }
    }

    fun updateGitHubConfig(owner: String, repo: String) { }

    fun toggleGitHubService(enabled: Boolean) {
        _serviceEnabled.value = enabled
        val prefs = getApplication<Application>().getSharedPreferences("clan_github_updater_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("service_enabled", enabled).apply()
        if (enabled) com.example.CDN.service.GitHubUpdateService.start(getApplication())
        else com.example.CDN.service.GitHubUpdateService.stop(getApplication())
    }

    override fun onCleared() {
        super.onCleared()
        try { getApplication<Application>().unregisterReceiver(updateReceiver) } catch (e: Exception) { }
        postsListener?.remove()
        messagesListener?.remove()
    }

    fun performSimulatedPatchUpdate(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _checkingForUpdates.value = true
            try {
                @Suppress("BlockingMethodInNonBlockingContext")
                val response = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val url = java.net.URL("https://api.github.com/repos/${repoOwner.value}/${repoName.value}/releases/latest")
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                    if (connection.responseCode == 200) {
                        connection.inputStream.bufferedReader().readText()
                    } else null
                }
                
                _checkingForUpdates.value = false
                if (response != null) {
                    val versionRegex = "\"tag_name\":\\s*\"([^\"]+)\"".toRegex()
                    val match = versionRegex.find(response as CharSequence)
                    val v = match?.groupValues?.get(1) ?: "SCONOSCIUTA"
                    
                    _githubRelease.value = _githubRelease.value?.copy(canUpdate = false, version = "CLAN $v (ATTUALE)") ?: GitHubRelease("CLAN $v (ATTUALE)", "Oggi", emptyList(), 10.0, false)
                    triggerSimulatedNotification("AGGIORNAMENTO APPLICATO", "Kernel GitHub validato alla versione $v.", "SISTEMA")
                    onSuccess()
                } else {
                    triggerSimulatedNotification("ERRORE AGGIORNAMENTO", "Reticolo GitHub irraggiungibile.", "ALLERTA")
                }
            } catch (e: Exception) {
                _checkingForUpdates.value = false
                triggerSimulatedNotification("ERRORE SCRIPT", e.message ?: "Network error", "ALLERTA")
            }
        }
    }

    val databaseBackups: StateFlow<List<BackupEntity>> = clanDao.getAllBackupsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun executeSecureSystemBackup(nameInput: String) {
        if (nameInput.isBlank()) return
        viewModelScope.launch {
            val b = BackupEntity(id = "BCK-${System.currentTimeMillis()}", backupName = nameInput.uppercase().replace(" ", "_"), encryptedData = SecurityUtils.encrypt("CLAN_DUMP_V2", "KEY_0"))
            clanDao.insertBackup(b)
            triggerSimulatedNotification("BACKUP ESEGUITO", "Dati salvati in locale.", "SISTEMA")
        }
    }

    fun removeBackupById(id: String) { viewModelScope.launch { clanDao.deleteBackup(id) } }

    fun attemptRegisterCredentials(user: String, mail: String, pass: String, pin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (user.isBlank() || mail.isBlank() || pass.isBlank() || pin.isBlank()) { onError("Dati incompleti!"); return }
        viewModelScope.launch {
            try {
                val res = auth.createUserWithEmailAndPassword(mail, pass).await()
                val uid = res.user?.uid ?: throw Exception("UID Error")
                val newUser = UserEntity(username = user, email = mail, followersCount = 0, followingCount = 0, role = "OPERATIVO", tacticalPin = pin)
                val userMap = hashMapOf("username" to user, "email" to mail, "bio" to newUser.bio, "followersCount" to 0, "followingCount" to 0, "role" to "OPERATIVO", "tacticalPin" to pin)
                firestore.collection("users").document(uid).set(userMap).await()
                clanDao.insertUser(newUser)
                _currentUser.value = newUser
                onSuccess()
            } catch (e: Exception) { onError(e.localizedMessage ?: "Errore") }
        }
    }

    fun attemptLoginCredentials(mail: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val res = auth.signInWithEmailAndPassword(mail, pass).await()
                val doc = firestore.collection("users").document(res.user?.uid ?: "").get().await()
                if (doc.exists()) {
                    val u = UserEntity(
                        username = doc.getString("username") ?: "",
                        email = mail,
                        bio = doc.getString("bio") ?: "",
                        followersCount = doc.getLong("followersCount")?.toInt() ?: 0,
                        followingCount = doc.getLong("followingCount")?.toInt() ?: 0,
                        role = doc.getString("role") ?: "OPERATIVO",
                        tacticalPin = doc.getString("tacticalPin") ?: ""
                    )
                    clanDao.insertUser(u)
                    _currentUser.value = u
                    onSuccess()
                } else onError("Profilo mancante")
            } catch (e: Exception) { onError(e.localizedMessage ?: "Accesso negato") }
        }
    }

    fun executeSecureWipeAndSelfDestruct(verify: String, onCompleted: () -> Unit) {
        viewModelScope.launch {
            if (verify != _currentUser.value?.username) return@launch
            try {
                val uid = auth.currentUser?.uid
                if (uid != null) { firestore.collection("users").document(uid).delete().await(); auth.currentUser?.delete()?.await() }
            } catch (e: Exception) { }
            clanDao.deleteUser(verify)
            _currentUser.value = null
            _currentScreen.value = "login"
            onCompleted()
        }
    }

    fun editProfileBio(newBio: String) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                auth.currentUser?.uid?.let { uid -> firestore.collection("users").document(uid).update("bio", newBio) }
                val updated = user.copy(bio = newBio)
                clanDao.updateUser(updated)
                _currentUser.value = updated
            }
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            auth.signOut()
            _currentUser.value?.let { clanDao.deleteUser(it.username) }
            _currentUser.value = null
            _currentScreen.value = "login"
        }
    }
}
