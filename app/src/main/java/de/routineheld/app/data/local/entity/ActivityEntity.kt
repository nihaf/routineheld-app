package de.routineheld.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconRef: String,
    val color: Int? = null,
    val isUserCreated: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
