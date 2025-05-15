package com.trios2025dj.superpodcast.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Database helper for managing podcast subscriptions
 */
class PodcastDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "podcasts.db"
        private const val DATABASE_VERSION = 1

        // Table name
        const val TABLE_SUBSCRIPTIONS = "subscriptions"

        // Column names for subscriptions
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_ARTIST = "artist"
        const val COLUMN_ARTWORK_URL = "artwork_url"
        const val COLUMN_FEED_URL = "feed_url"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create subscriptions table
        val createSubscriptionsTable = """
            CREATE TABLE $TABLE_SUBSCRIPTIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_ARTIST TEXT NOT NULL,
                $COLUMN_ARTWORK_URL TEXT NOT NULL,
                $COLUMN_FEED_URL TEXT NOT NULL UNIQUE,
                $COLUMN_TIMESTAMP INTEGER NOT NULL
            )
        """.trimIndent()
        
        db.execSQL(createSubscriptionsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // For future database schema upgrades
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SUBSCRIPTIONS")
        onCreate(db)
    }

    /**
     * Subscribe to a podcast
     */
    fun subscribeToPodcast(podcast: Podcast): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, podcast.collectionName)
            put(COLUMN_ARTIST, podcast.artistName)
            put(COLUMN_ARTWORK_URL, podcast.artworkUrl100)
            put(COLUMN_FEED_URL, podcast.feedUrl)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
        }

        // Insert or replace if podcast with same feed URL already exists
        return db.insertWithOnConflict(
            TABLE_SUBSCRIPTIONS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /**
     * Unsubscribe from a podcast
     */
    fun unsubscribeFromPodcast(feedUrl: String): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_SUBSCRIPTIONS,
            "$COLUMN_FEED_URL = ?",
            arrayOf(feedUrl)
        )
    }

    /**
     * Check if user is subscribed to a podcast
     */
    fun isSubscribed(feedUrl: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SUBSCRIPTIONS,
            arrayOf(COLUMN_ID),
            "$COLUMN_FEED_URL = ?",
            arrayOf(feedUrl),
            null,
            null,
            null
        )
        
        val isSubscribed = cursor.count > 0
        cursor.close()
        return isSubscribed
    }

    /**
     * Get all subscribed podcasts
     */
    fun getAllSubscriptions(): List<Podcast> {
        val subscriptions = mutableListOf<Podcast>()
        val db = readableDatabase
        
        val cursor = db.query(
            TABLE_SUBSCRIPTIONS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_TIMESTAMP DESC" // Sort by most recently subscribed
        )

        with(cursor) {
            while (moveToNext()) {
                val title = getString(getColumnIndexOrThrow(COLUMN_TITLE))
                val artist = getString(getColumnIndexOrThrow(COLUMN_ARTIST))
                val artworkUrl = getString(getColumnIndexOrThrow(COLUMN_ARTWORK_URL))
                val feedUrl = getString(getColumnIndexOrThrow(COLUMN_FEED_URL))
                
                subscriptions.add(Podcast(title, artist, artworkUrl, feedUrl))
            }
        }
        
        cursor.close()
        return subscriptions
    }
    
    // Saved video functionality has been moved to VideoPreferences class
}
