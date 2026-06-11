package com.example.vibehabit.features.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    // Зручний доступ до поточного юзера
    val currentUser: FirebaseUser? get() = auth.currentUser

    // МАГІЯ: Перетворюємо колбеки Firebase на сучасний Kotlin Flow
    fun getAuthStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { authState ->
            trySend(authState.currentUser)
        }
        auth.addAuthStateListener(listener)
        // Коли ViewModel відписується, ми автоматично видаляємо слухача
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<Unit> = runCatching {
        auth.createUserWithEmailAndPassword(email, pass).await()
        auth.currentUser?.sendEmailVerification()
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, pass).await()
    }

    suspend fun reloadUser(): Result<Boolean> = runCatching {
        auth.currentUser?.reload()?.await()
        auth.currentUser?.isEmailVerified == true
    }

    suspend fun resendVerificationEmail(): Result<Unit> = runCatching {
        auth.currentUser?.sendEmailVerification()?.await()
    }

    suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun deleteAccount(): Result<Unit> = runCatching {
        auth.currentUser?.delete()?.await()
    }

    fun signOut() {
        auth.signOut()
    }
}