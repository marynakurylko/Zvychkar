package com.example.vibehabit.auth

import com.google.firebase.auth.FirebaseUser

sealed class AuthState {
    object Loading : AuthState()

    data class Authenticated(val user: FirebaseUser) : AuthState()

    object Unauthenticated : AuthState()
}

