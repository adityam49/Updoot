package com.ducktapedapps.navigation

sealed class Event {
    data class ScreenNavigationEvent(val data: NavigationCommand) : Event()

    sealed class AuthEvent : Event() {
        object NewAccountAdded : AuthEvent()
        object LoggedOut : AuthEvent()
        object AccountSwitched : AuthEvent()
    }

    data class ToastEvent(val content: String) : Event()
    data class OpenWebLink(val url:String) :Event()
}