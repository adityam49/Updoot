package com.ducktapedapps.navigation

sealed class Event {
    data class ScreenNavigationEvent(val data: ScreenNavigationCommand) : Event()

    sealed class AuthEvent : Event() {
        object NewAccountAdded : AuthEvent()
        object LoggedOut : AuthEvent()
        object AccountSwitched : AuthEvent()
    }

    data class ToastEvent(val content: String) : Event()
}