package com.ducktapedapps.updoot.ui.user

import com.ducktapedapps.updoot.utils.Page
import kotlinx.coroutines.flow.StateFlow

interface IUserViewModel {
    val userName: String

    val currentSection: StateFlow<UserSection>

    val content: StateFlow<List<Page<UserContent>>>

    fun loadPage()

    fun setSection(section: UserSection)
}