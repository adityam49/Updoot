package com.ducktapedapps.updoot.ui.user

import kotlinx.coroutines.flow.StateFlow

interface IUserViewModel {
    val userName: String

    val isLoading: StateFlow<Boolean>

    val currentSection: StateFlow<UserSection>

    val content: StateFlow<List<UserContent>>

    fun reload()

    fun loadPage()

    fun setSection(section: UserSection)
}