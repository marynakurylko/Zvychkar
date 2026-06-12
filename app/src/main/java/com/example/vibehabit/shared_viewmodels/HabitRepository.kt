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
    fun getHabitsFlow(userId: String): Flow<List<Habit>> = callbackFlow {
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
                    val habits = snapshot.documents.mapNotNull { doc ->
                        // Надійне ручне мапування, щоб уникнути Crash при невідповідності типів у Firestore
                        val data = doc.data ?: return@mapNotNull null
                        try {
                            Habit(
                                id = doc.id, // Завжди використовуємо ID документа як String
                                name = data["name"]?.toString() ?: "",
                                isFavorite = data["isFavorite"] as? Boolean ?: false,
                                colorHex = data["colorHex"]?.toString() ?: "",
                                completedDates = (data["completedDates"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                                targetDays = (data["targetDays"] as? Number)?.toInt() ?: 7,
                                iconName = data["iconName"]?.toString() ?: "Bolt",
                                frequency = data["frequency"]?.toString() ?: "Daily",
                                reminderTime = data["reminderTime"]?.toString()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(habits)
                }
            }

        awaitClose { subscription.remove() }
    }

    suspend fun updateHabitDates(userId: String, habitId: String, newDates: List<String>): Result<Unit> = runCatching {
        firestore.collection("users").document(userId).collection("habits").document(habitId)
            .update("completedDates", newDates).await()
    }

    suspend fun updateHabitFavorite(userId: String, habitId: String, isFavorite: Boolean): Result<Unit> = runCatching {
        firestore.collection("users").document(userId).collection("habits").document(habitId)
            .update("isFavorite", isFavorite).await()
    }

    suspend fun saveHabit(userId: String, habit: Habit): Result<Unit> = runCatching {
        firestore.collection("users").document(userId).collection("habits").document(habit.id)
            .set(habit).await()
    }

    suspend fun deleteHabit(userId: String, habitId: String): Result<Unit> = runCatching {
        firestore.collection("users").document(userId).collection("habits").document(habitId)
            .delete().await()
    }

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
