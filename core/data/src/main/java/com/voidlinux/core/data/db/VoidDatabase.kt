package com.voidlinux.core.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.voidlinux.core.common.Logger

/**
 * Base SQLite locale pour l'historique des événements de sécurité.
 */
class VoidDatabase(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_EVENTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                severity TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                package_name TEXT,
                file_path TEXT,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            "CREATE INDEX idx_timestamp ON $TABLE_EVENTS(timestamp DESC)"
        )

        Logger.d("Base VoidDatabase créée")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
        onCreate(db)
    }

    fun insertEvent(
        type: String,
        severity: String,
        title: String,
        description: String?,
        packageName: String?,
        filePath: String?,
        timestamp: Long
    ): Long {
        val values = android.content.ContentValues().apply {
            put("type", type)
            put("severity", severity)
            put("title", title)
            put("description", description)
            put("package_name", packageName)
            put("file_path", filePath)
            put("timestamp", timestamp)
        }
        return writableDatabase.insert(TABLE_EVENTS, null, values)
    }

    fun getAllEvents(limit: Int = 200): List<SecurityEventRow> {
        val list = mutableListOf<SecurityEventRow>()
        readableDatabase.query(
            TABLE_EVENTS,
            null,
            null,
            null,
            null,
            null,
            "timestamp DESC",
            limit.toString()
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    SecurityEventRow(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        severity = cursor.getString(cursor.getColumnIndexOrThrow("severity")),
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        packageName = cursor.getString(cursor.getColumnIndexOrThrow("package_name")),
                        filePath = cursor.getString(cursor.getColumnIndexOrThrow("file_path")),
                        timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
                    )
                )
            }
        }
        return list
    }

    fun clearAll() {
        writableDatabase.delete(TABLE_EVENTS, null, null)
    }

    data class SecurityEventRow(
        val id: Long,
        val type: String,
        val severity: String,
        val title: String,
        val description: String?,
        val packageName: String?,
        val filePath: String?,
        val timestamp: Long
    )

    companion object {
        private const val DB_NAME = "void_linux.db"
        private const val DB_VERSION = 1
        private const val TABLE_EVENTS = "security_events"
    }
}