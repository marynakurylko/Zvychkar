package com.example.vibehabit.data

/*
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // 1. Отримати всі звички.
    @Query("SELECT * FROM habits ORDER BY isFavorite DESC, id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    // 2. Додати нову звичку.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    // 3. Оновити існуючу звичку
    @Update
    suspend fun updateHabit(habit: HabitEntity)

    // 4. Видалити звичку
    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabit(habitId: Int)
}
*/