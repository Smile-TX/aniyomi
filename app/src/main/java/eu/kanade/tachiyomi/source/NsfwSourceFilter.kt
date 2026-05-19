package eu.kanade.tachiyomi.source

import eu.kanade.tachiyomi.extension.anime.AnimeExtensionManager
import eu.kanade.tachiyomi.extension.manga.MangaExtensionManager

/**
 * Utility for detecting whether a source is NSFW/18+.
 *
 * Detection uses two layers:
 *  1. The official [isNsfw] flag from the extension's metadata (set by the extension developer).
 *  2. A manual fallback list of source IDs for extensions that lack the official flag.
 *
 * This filter is intentionally read-only and never touches the database.
 * It is used exclusively for in-memory display filtering in the History screen.
 */
object NsfwSourceFilter {

    /**
     * Manual fallback list of manga source IDs considered NSFW.
     *
     * Add entries here when an extension does not carry the official [isNsfw] flag.
     * Source IDs are stable Long values derived from the source name + language hash.
     *
     * How to find a source ID at runtime:
     *   val source = Injekt.get<MangaSourceManager>().get(sourceId)
     *   Log.d("NsfwSourceFilter", "name=${source?.name}  id=${source?.id}")
     */
    private val manualNsfwMangaSourceIds: Set<Long> = setOf(
        // Example (replace with real IDs if needed):
        // 1998944621602463836L, // SomeMangaSource
    )

    /**
     * Manual fallback list of anime source IDs considered NSFW.
     */
    private val manualNsfwAnimeSourceIds: Set<Long> = setOf(
        // Example:
        // 2187383858672236614L, // SomeAnimeSource
    )

    /**
     * Returns the complete set of NSFW manga source IDs:
     * union of all officially-flagged installed extension sources and the manual list.
     *
     * This is O(n) in installed extensions and is intended to be called once per
     * Flow emission, not on every list item.
     */
    fun getAllNsfwMangaSourceIds(mangaExtensionManager: MangaExtensionManager): Set<Long> {
        val fromExtensions = mangaExtensionManager.installedExtensionsFlow.value
            .filter { it.isNsfw }
            .flatMap { ext -> ext.sources.map { it.id } }
            .toSet()
        return fromExtensions + manualNsfwMangaSourceIds
    }

    /**
     * Returns the complete set of NSFW anime source IDs.
     */
    fun getAllNsfwAnimeSourceIds(animeExtensionManager: AnimeExtensionManager): Set<Long> {
        val fromExtensions = animeExtensionManager.installedExtensionsFlow.value
            .filter { it.isNsfw }
            .flatMap { ext -> ext.sources.map { it.id } }
            .toSet()
        return fromExtensions + manualNsfwAnimeSourceIds
    }
}
