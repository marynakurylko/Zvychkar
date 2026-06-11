package com.example.vibehabit.shared_viewmodels

import com.example.vibehabit.core.models.Habit
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // МАГІЯ 2: Перетворюємо Firestore SnapshotListener на Flow
    fun getHabitsFlow(userId: String): Flow<List<Habit>> = callbackFlow {
        // 1. Екстра-захист від порожнього userId
        if (userId.isBlank()) {
            close(Exception("Користувач не авторизований"))
            return@callbackFlow
        }

        val subscription = firestore.collection("users")
            .document(userId)
            .collection("habits")
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val habits = snapshot.toObjects(Habit::class.java)
                    trySend(habits)
                }
            }

        awaitClose { subscription.remove() }
    }

    suspend fun updateHabitDates(userId: String, habitId: Int, newDates: List<String>): Result<Unit> = runCatching {
        firestore.collection("users").document(userId).collection("habits").document(habitId.toString())
            .update("completedDates", newDates).await()
    }

    suspend fun updateHabitFavorite(userId: String, habitId: Int, isFavorite: Boolean): Result<Unit> = runCatching {
        firestore.collection("users").document(userId).collection("habits").document(habitId.toString())
            .update("isFavorite", isFavorite).await()
    }

    suspend fun saveHabit(userId: String, habit: Habit): Result<Unit> = runCatching {
        firestore.collection("users").document(userId).collection("habits").document(habit.id.toString())
            .set(habit).await()
    }

    suspend fun deleteHabit(userId: String, habitId: Int): Result<Unit> = runCatching {
        firestore.collection("users").document(userId).collection("habits").document(habitId.toString())
            .delete().await()
    }

    // Для видалення акаунту
    suspend fun deleteAllUserData(userId: String): Result<Unit> = runCatching {
        val habitsRef = firestore.collection("users").document(userId).collection("habits")
        val snapshot = habitsRef.get().await()
        val batch = firestore.batch()

        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
        }
        batch.delete(firestore.collection("users").document(userId))
        batch.commit().await()
    }

    suspend fun sendFeedback(userId: String, email: String, message: String): Result<Unit> = runCatching {
        val feedbackData = hashMapOf(
            "userId" to userId,
            "email" to email,
            "message" to message,
            "timestamp" to FieldValue.serverTimestamp()
        )
        firestore.collection("feedback").add(feedbackData).await()
    }
}