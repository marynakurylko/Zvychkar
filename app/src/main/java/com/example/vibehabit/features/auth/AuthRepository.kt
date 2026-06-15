package com.example.vibehabit.features.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    private val TIMEOUT_MS = 10000L // Auth usually needs more time than Firestore writes

    val currentUser: FirebaseUser? get() = auth.currentUser

    fun getAuthStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { authState ->
            trySend(authState.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            withTimeoutOrNull(TIMEOUT_MS) {
                auth.signInWithCredential(credential).await()
            } ?: throw Exception("Sign in timeout")
            Unit
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeoutOrNull(TIMEOUT_MS) {
                auth.createUserWithEmailAndPassword(email, pass).await()
                auth.currentUser?.sendEmailVerification()?.await()
            } ?: throw Exception("Sign up timeout")
            Unit
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeoutOrNull(TIMEOUT_MS) {
                auth.signInWithEmailAndPassword(email, pass).await()
            } ?: throw Exception("Sign in timeout")
            Unit
        }
    }

    suspend fun reloadUser(): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeoutOrNull(TIMEOUT_MS) {
                auth.currentUser?.reload()?.await()
            }
            auth.currentUser?.isEmailVerified == true
        }
    }

    suspend fun resendVerificationEmail(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeoutOrNull(TIMEOUT_MS) {
                auth.currentUser?.sendEmailVerification()?.await()
            } ?: throw Exception("Resend email timeout")
            Unit
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeoutOrNull(TIMEOUT_MS) {
                auth.sendPasswordResetEmail(email).await()
            } ?: throw Exception("Reset password timeout")
            Unit
        }
    }

    suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            withTimeoutOrNull(TIMEOUT_MS) {
                auth.currentUser?.delete()?.await()
            } ?: throw Exception("Delete account timeout")
            Unit
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
