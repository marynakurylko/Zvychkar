package com.example.vibehabit.core.ui

sealed class UiEvent {
    object Success : UiEvent()
    data class Error(val messageResId: Int) : UiEvent()

    data class ErrorText(val message: String) : UiEvent()
}