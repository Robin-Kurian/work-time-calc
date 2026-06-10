package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE dayString = :day ORDER BY inTime DESC")
    fun getSessionsForDay(day: String): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE dayString = :day ORDER BY inTime DESC")
    suspend fun getSessionsForDaySync(day: String): List<Session>

    @Query("SELECT * FROM sessions ORDER BY inTime DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session)

    @Update
    suspend fun updateSession(session: Session)

    @Delete
    suspend fun deleteSession(session: Session)

    @Query("DELETE FROM sessions WHERE dayString = :day")
    suspend fun clearSessionsForDay(day: String)

    @Query("DELETE FROM sessions")
    suspend fun clearAll()
}
