package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.ChannelDao
import com.example.data.local.ChannelEntity
import com.example.data.local.PlaylistDao
import com.example.data.local.PlaylistEntity
import com.example.data.parser.ContentCategoryClassifier
import com.example.data.parser.LanguageClassifier
import com.example.data.parser.M3uParser
import com.example.data.parser.ParsedChannel
import com.example.data.parser.PlaylistValidator
import com.example.data.parser.ValidationResult
import com.example.data.remote.DownloadResult
import com.example.data.remote.PlaylistDownloader
import com.example.domain.model.Playlist
import com.example.domain.model.ImportStatus
import com.example.domain.repository.IPlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.InputStream

typealias ImportStatus = com.example.domain.model.ImportStatus

class PlaylistRepository(
    private val context: Context,
    private val playlistDao: PlaylistDao,
    private val channelDao: ChannelDao,
    private val downloader: PlaylistDownloader = PlaylistDownloader(),
    private val parser: M3uParser = M3uParser()
) : IPlaylistRepository {

    companion object {
        const val DEFAULT_PLAYLIST_NAME = "Tamil TV"
        const val DEFAULT_PLAYLIST_URL = "https://iptv-org.github.io/iptv/languages/tam.m3u"
    }

    override val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun getPlaylistById(id: Long): Playlist? = withContext(Dispatchers.IO) {
        playlistDao.getPlaylistById(id)?.toDomain()
    }

    override suspend fun getDefaultPlaylist(): Playlist? = withContext(Dispatchers.IO) {
        playlistDao.getDefaultPlaylist()?.toDomain()
    }

    override suspend fun getPlaylistCount(): Int = withContext(Dispatchers.IO) {
        playlistDao.getPlaylistCount()
    }

    override suspend fun updateLastUsed(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.updateLastUsed(playlistId, System.currentTimeMillis())
    }

    override suspend fun renamePlaylist(playlistId: Long, newName: String) = withContext(Dispatchers.IO) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank()) {
            playlistDao.renamePlaylist(playlistId, trimmed)
        }
    }

    override suspend fun deletePlaylist(playlistId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val entity = playlistDao.getPlaylistById(playlistId)
            ?: return@withContext Result.failure(IllegalArgumentException("Playlist not found"))

        if (entity.isDefault) {
            return@withContext Result.failure(IllegalStateException("The built-in Tamil TV playlist cannot be deleted."))
        }

        channelDao.deleteChannelsByPlaylist(playlistId)
        playlistDao.deletePlaylistById(playlistId)
        Result.success(Unit)
    }

    /**
     * Clears cached channels from local Room database without removing user playlists.
     */
    override suspend fun clearChannelCache(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            channelDao.deleteAllCachedChannels()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Ensures the default Tamil TV playlist is loaded and cached.
     * If already cached and forceRefresh is false, it uses the local cache without network request.
     */
    override suspend fun ensureDefaultPlaylistLoaded(forceRefresh: Boolean): ImportStatus = withContext(Dispatchers.IO) {
        val existing = playlistDao.getDefaultPlaylist()
        if (existing != null) {
            val count = channelDao.getChannelCountByPlaylist(existing.id)
            if (count > 0 && !forceRefresh) {
                return@withContext ImportStatus.Success(existing.toDomain(), count)
            } else if (forceRefresh) {
                return@withContext refreshPlaylist(existing.id)
            }
        }

        // Fetch over network
        when (val result = downloader.download(DEFAULT_PLAYLIST_URL)) {
            is DownloadResult.Error -> {
                if (existing != null) {
                    val count = channelDao.getChannelCountByPlaylist(existing.id)
                    if (count > 0) {
                        return@withContext ImportStatus.Success(existing.toDomain(), count)
                    }
                }
                // Fallback to verified demo channels so app always has playable channels
                loadDemoChannels()
            }
            is DownloadResult.Success -> {
                try {
                    val parsedChannels = result.inputStream.use { parser.parse(it) }
                    if (parsedChannels.isEmpty()) {
                        return@withContext ImportStatus.Error("Tamil TV playlist returned no channels.")
                    }

                    val playlistId = if (existing != null) {
                        saveChannelsForExistingPlaylist(existing.id, parsedChannels, LanguageClassifier.LANG_TAMIL)
                        existing.id
                    } else {
                        savePlaylistWithChannels(
                            name = DEFAULT_PLAYLIST_NAME,
                            sourceUri = DEFAULT_PLAYLIST_URL,
                            isLocal = false,
                            isDefault = true,
                            declaredLanguage = LanguageClassifier.LANG_TAMIL,
                            channels = parsedChannels
                        )
                    }

                    val saved = playlistDao.getPlaylistById(playlistId)!!.toDomain()
                    ImportStatus.Success(saved, parsedChannels.size)
                } catch (e: Exception) {
                    ImportStatus.Error("Failed to parse Tamil TV playlist: ${e.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    override suspend fun importFromUrl(url: String, customName: String?): ImportStatus = withContext(Dispatchers.IO) {
        val validation = PlaylistValidator.validateUrl(url)
        if (validation is ValidationResult.Invalid) {
            return@withContext ImportStatus.Error(validation.reason)
        }

        val existing = playlistDao.getPlaylistByUri(url.trim())
        if (existing != null) {
            // Already imported: refresh and switch to it
            return@withContext refreshPlaylist(existing.id)
        }

        when (val result = downloader.download(url.trim())) {
            is DownloadResult.Error -> {
                ImportStatus.Error("Download failed: ${result.message}")
            }
            is DownloadResult.Success -> {
                try {
                    val parsedChannels = result.inputStream.use { parser.parse(it) }
                    if (parsedChannels.isEmpty()) {
                        return@withContext ImportStatus.Error("The playlist downloaded successfully, but contained 0 valid stream channels.")
                    }

                    val derivedName = customName?.takeIf { it.isNotBlank() }
                        ?: PlaylistValidator.derivePlaylistNameFromUrl(url.trim())

                    val declaredLang = inferPlaylistLanguage(url, derivedName)

                    val playlistId = savePlaylistWithChannels(
                        name = derivedName,
                        sourceUri = url.trim(),
                        isLocal = false,
                        isDefault = false,
                        declaredLanguage = declaredLang,
                        channels = parsedChannels
                    )

                    val savedPlaylist = playlistDao.getPlaylistById(playlistId)!!.toDomain()
                    ImportStatus.Success(savedPlaylist, parsedChannels.size)
                } catch (e: Exception) {
                    ImportStatus.Error("Failed to parse playlist: ${e.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    override suspend fun refreshPlaylist(playlistId: Long): ImportStatus = withContext(Dispatchers.IO) {
        val playlist = playlistDao.getPlaylistById(playlistId)
            ?: return@withContext ImportStatus.Error("Playlist not found.")

        if (playlist.isLocal) {
            return@withContext ImportStatus.Error("Local file playlists cannot be refreshed from the network.")
        }

        when (val result = downloader.download(playlist.sourceUri)) {
            is DownloadResult.Error -> {
                ImportStatus.Error("Failed to refresh: ${result.message}")
            }
            is DownloadResult.Success -> {
                try {
                    val parsedChannels = result.inputStream.use { parser.parse(it) }
                    if (parsedChannels.isEmpty()) {
                        return@withContext ImportStatus.Error("Refreshed playlist contains 0 channels.")
                    }

                    val declaredLang = inferPlaylistLanguage(playlist.sourceUri, playlist.name)
                    saveChannelsForExistingPlaylist(playlistId, parsedChannels, declaredLang)

                    val updated = playlistDao.getPlaylistById(playlistId)!!.toDomain()
                    ImportStatus.Success(updated, parsedChannels.size, isExisting = true)
                } catch (e: Exception) {
                    ImportStatus.Error("Failed while parsing updated channels: ${e.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    override suspend fun importFromUri(uri: Uri, customName: String?): ImportStatus = withContext(Dispatchers.IO) {
        try {
            val stream = context.contentResolver.openInputStream(uri)
                ?: return@withContext ImportStatus.Error("Could not access file. Permission might be revoked.")

            val parsedChannels = stream.use { nonNullStream: InputStream -> parser.parse(nonNullStream) }
            if (parsedChannels.isEmpty()) {
                return@withContext ImportStatus.Error("The selected file does not contain any playable channels.")
            }

            val derivedName = customName?.takeIf { it.isNotBlank() }
                ?: uri.lastPathSegment?.substringAfterLast('/')?.substringBefore(".m3u")
                ?: "Local Playlist"

            val playlistId = savePlaylistWithChannels(
                name = derivedName,
                sourceUri = uri.toString(),
                isLocal = true,
                isDefault = false,
                declaredLanguage = null,
                channels = parsedChannels
            )

            val savedPlaylist = playlistDao.getPlaylistById(playlistId)!!.toDomain()
            ImportStatus.Success(savedPlaylist, parsedChannels.size)
        } catch (e: Exception) {
            ImportStatus.Error("Error importing local M3U file: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    override suspend fun loadDemoChannels(): ImportStatus = withContext(Dispatchers.IO) {
        try {
            val existing = playlistDao.getPlaylistByUri("demo://open-streams")
            if (existing != null) {
                return@withContext ImportStatus.Success(existing.toDomain(), existing.channelCount, isExisting = true)
            }

            val playlistId = savePlaylistWithChannels(
                name = DemoChannels.SAMPLE_PLAYLIST_NAME,
                sourceUri = "demo://open-streams",
                isLocal = false,
                isDefault = false,
                declaredLanguage = LanguageClassifier.LANG_ENGLISH,
                channels = DemoChannels.SAMPLE_CHANNELS
            )

            val savedPlaylist = playlistDao.getPlaylistById(playlistId)!!.toDomain()
            ImportStatus.Success(savedPlaylist, DemoChannels.SAMPLE_CHANNELS.size)
        } catch (e: Exception) {
            ImportStatus.Error("Failed to load demo channels: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private suspend fun saveChannelsForExistingPlaylist(
        playlistId: Long,
        channels: List<ParsedChannel>,
        declaredLanguage: String?
    ) {
        // Retrieve existing channels to preserve user favorites and manual language overrides
        val existingChannels = channelDao.getChannelsListByPlaylist(playlistId)
        val favoriteUrls = existingChannels.filter { it.isFavorite }.map { it.streamUrl }.toSet()
        val favoriteNames = existingChannels.filter { it.isFavorite }.map { it.name.trim().lowercase() }.toSet()

        val userLanguageOverridesByUrl = existingChannels
            .filter { !it.userLanguageOverride.isNullOrBlank() }
            .associate { it.streamUrl to it.userLanguageOverride }

        val userLanguageOverridesByName = existingChannels
            .filter { !it.userLanguageOverride.isNullOrBlank() }
            .associate { it.name.trim().lowercase() to it.userLanguageOverride }

        val channelEntities = channels.mapIndexed { index, parsed ->
            val isFav = parsed.streamUrl in favoriteUrls || parsed.name.trim().lowercase() in favoriteNames
            val userOverride = userLanguageOverridesByUrl[parsed.streamUrl]
                ?: userLanguageOverridesByName[parsed.name.trim().lowercase()]

            val langResult = LanguageClassifier.classify(
                channelName = parsed.name,
                tvgLanguage = parsed.tvgLanguage,
                tvgId = parsed.tvgId,
                groupTitle = parsed.groupTitle,
                playlistDeclaredLanguage = declaredLanguage,
                userOverride = userOverride
            )

            val contentCat = ContentCategoryClassifier.classify(
                groupTitle = parsed.groupTitle,
                channelName = parsed.name,
                tvgId = parsed.tvgId
            )

            ChannelEntity(
                playlistId = playlistId,
                name = parsed.name,
                streamUrl = parsed.streamUrl,
                groupTitle = parsed.groupTitle,
                tvgId = parsed.tvgId,
                tvgLogo = parsed.tvgLogo,
                isFavorite = isFav,
                orderIndex = index,
                isLocalMedia = false,
                language = langResult.primaryLanguage,
                userLanguageOverride = userOverride,
                contentCategory = contentCat
            )
        }

        channelDao.replacePlaylistChannels(playlistId, channelEntities)
        playlistDao.updatePlaylistRefresh(playlistId, channels.size, System.currentTimeMillis())
    }

    private suspend fun savePlaylistWithChannels(
        name: String,
        sourceUri: String,
        isLocal: Boolean,
        isDefault: Boolean,
        declaredLanguage: String?,
        channels: List<ParsedChannel>
    ): Long {
        val playlistEntity = PlaylistEntity(
            name = name,
            sourceUri = sourceUri,
            isLocal = isLocal,
            isDefault = isDefault,
            channelCount = channels.size,
            createdAt = System.currentTimeMillis(),
            lastUsed = System.currentTimeMillis(),
            lastRefreshed = System.currentTimeMillis()
        )
        val playlistId = playlistDao.insertPlaylist(playlistEntity)

        val channelEntities = channels.mapIndexed { index, parsed ->
            val langResult = LanguageClassifier.classify(
                channelName = parsed.name,
                tvgLanguage = parsed.tvgLanguage,
                tvgId = parsed.tvgId,
                groupTitle = parsed.groupTitle,
                playlistDeclaredLanguage = declaredLanguage,
                userOverride = null
            )

            val contentCat = ContentCategoryClassifier.classify(
                groupTitle = parsed.groupTitle,
                channelName = parsed.name,
                tvgId = parsed.tvgId
            )

            ChannelEntity(
                playlistId = playlistId,
                name = parsed.name,
                streamUrl = parsed.streamUrl,
                groupTitle = parsed.groupTitle,
                tvgId = parsed.tvgId,
                tvgLogo = parsed.tvgLogo,
                isFavorite = false,
                orderIndex = index,
                isLocalMedia = false,
                language = langResult.primaryLanguage,
                userLanguageOverride = null,
                contentCategory = contentCat
            )
        }
        channelDao.insertChannels(channelEntities)
        return playlistId
    }

    private fun inferPlaylistLanguage(url: String, name: String): String? {
        val combined = "$url $name".lowercase()
        return when {
            combined.contains("tam.m3u") || combined.contains("tamil") -> LanguageClassifier.LANG_TAMIL
            combined.contains("hin.m3u") || combined.contains("hindi") -> LanguageClassifier.LANG_HINDI
            combined.contains("eng.m3u") || combined.contains("english") -> LanguageClassifier.LANG_ENGLISH
            combined.contains("tel.m3u") || combined.contains("telugu") -> LanguageClassifier.LANG_TELUGU
            combined.contains("mal.m3u") || combined.contains("malayalam") -> LanguageClassifier.LANG_MALAYALAM
            combined.contains("kan.m3u") || combined.contains("kannada") -> LanguageClassifier.LANG_KANNADA
            combined.contains("ben.m3u") || combined.contains("bengali") -> LanguageClassifier.LANG_BENGALI
            combined.contains("mar.m3u") || combined.contains("marathi") -> LanguageClassifier.LANG_MARATHI
            combined.contains("pan.m3u") || combined.contains("punjabi") -> LanguageClassifier.LANG_PUNJABI
            else -> null
        }
    }
}
