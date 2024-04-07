package com.project.accommodations.model
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "accommodations")
data class Accommodation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val downloadUrl: String,
    val email: String,
    val name: String,
    val phone: String,
    val comment: String,
    val longitude: Double,
    val latitude: Double,
    val date: Long
)