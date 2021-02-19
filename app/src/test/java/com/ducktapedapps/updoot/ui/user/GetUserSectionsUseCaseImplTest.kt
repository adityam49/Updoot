package com.ducktapedapps.updoot.ui.user

import com.ducktapedapps.updoot.data.remote.RedditAPI
import com.ducktapedapps.updoot.data.remote.model.Token
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.AnonymousAccount
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.UserModel
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class GetUserSectionsUseCaseImplTest {

    private lateinit var useCase: GetUserSectionsUseCase
    private val userAccount = UserModel(_name = "userName", isCurrent = false, userIcon = "")
    private val redditClient = StubRedditClient(userAccount)
    private val nonLoggedInUserName = "someOtherName"
    private val anonymousAccount = AnonymousAccount(isCurrent = false)

    @Before
    fun setUp() {
        useCase = GetUserSectionsUseCaseImpl(redditClient)
    }

    @Test
    fun `when user is logged-out then emit non user specific sections`() = runBlockingTest {
        redditClient.setCurrentAccount(anonymousAccount.name)
        val actualSections = useCase.getUserSections(nonLoggedInUserName).first()
        assert(sectionsAreSame(actualSections, getNonUserSpecificSections()))
    }

    @Test
    fun `when user is logged-in and target username is logged-out username then emit non user specific sections`() =
        runBlockingTest {
            redditClient.setCurrentAccount(userAccount.name)
            val actualSections = useCase.getUserSections(nonLoggedInUserName).first()
            assert(sectionsAreSame(actualSections, getNonUserSpecificSections()))
        }

    @Test
    fun `when user is logged-in and target username is logged-in username then emit user specific sections`() =
        runBlockingTest {
            redditClient.setCurrentAccount(userAccount.name)
            val actualSections = useCase.getUserSections(userAccount.name).first()
            assert(sectionsAreSame(actualSections, getAllUserSections()))
        }

    private fun sectionsAreSame(list1: List<UserSection>, list2: List<UserSection>) =
        list1.containsAll(list2) && list2.containsAll(list1)

}

class StubRedditClient(private val userAccount: UserModel) : IRedditClient {

    private val accounts = listOf(userAccount, AnonymousAccount(true))
    override val allAccounts: MutableStateFlow<List<AccountModel>> = MutableStateFlow(accounts)

    override suspend fun api(): RedditAPI = throw UnsupportedOperationException()

    override suspend fun setCurrentAccount(name: String) {
        allAccounts.value = when (name) {
            Constants.ANON_USER -> listOf(AnonymousAccount(true), userAccount)
            userAccount.name -> listOf(
                userAccount.copy(isCurrent = true),
                AnonymousAccount(false)
            )
            else -> throw UnsupportedOperationException()
        }
    }

    override suspend fun createAccount(username: String, icon: String, token: Token) =
        throw UnsupportedOperationException()

    override suspend fun removeUser(accountName: String) = throw UnsupportedOperationException()

}