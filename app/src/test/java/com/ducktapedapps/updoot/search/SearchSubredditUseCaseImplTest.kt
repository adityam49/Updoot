package com.ducktapedapps.updoot.search

import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.data.mappers.toLocalSubreddit
import com.ducktapedapps.updoot.data.remote.RedditAPI
import com.ducktapedapps.updoot.data.remote.model.Listing
import com.ducktapedapps.updoot.data.remote.model.RemoteSubreddit
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.util.*

class SearchSubredditUseCaseImplTest {

    lateinit var useCase: SearchSubredditUseCase
    var searchKeyWord: String = ""
    private val subredditDAO: SubredditDAO = mockk()
    private val redditClient: RedditClient = mockk()
    private val redditApi: RedditAPI = mockk()

    @Before
    fun setUp() = runBlockingTest {
        useCase = SearchSubredditUseCaseImpl(subredditDAO, redditClient)
        coEvery { redditClient.api() } returns redditApi
    }

    @Test
    fun `when query field is empty show cached trending subreddits`() = runBlockingTest {
        searchKeyWord = ""

        coEvery { subredditDAO.observeTrendingSubreddits() } returns flow {
            emit(buildDummyLocalSubreddits(5))
        }

        val expectedSubs = buildDummyLocalSubreddits(5)

        val results = useCase.getSubreddits(searchKeyWord).first()

        assert(subredditsAreSame(results, expectedSubs))
    }

    @Test
    fun `when query field is not empty show show 2 or less cached subreddits and rest fetched subs`() =
        runBlockingTest {
            searchKeyWord = "some_keyword"

            coEvery { redditApi.search(searchKeyWord) } returns buildDummyRemoteSubredditsListing(10)

            coEvery { subredditDAO.observeSubredditWithKeyword(searchKeyWord) } returns flow {
                emit(buildDummyLocalSubreddits(10))
            }

            val expectedLocalSubs = buildDummyLocalSubreddits(2)
            val expectedRemoteSubs =
                buildDummyRemoteSubredditsListing(10)
                    .children
                    .map { it.toLocalSubreddit() }
                    .filterNot { it.subredditName in expectedLocalSubs.map { localSub -> localSub.subredditName } }

            val outputFlow = useCase.getSubreddits(searchKeyWord)

            val results = outputFlow
                // First remote emission is always empty so local results can be immediately shown with combine
                //  TODO : fix implementation to avoid skipping first emission in test
                .drop(1)
                .first()
            assert(
                subredditsAreSame(results, expectedLocalSubs + expectedRemoteSubs)
            ) { "expected subs : ${(expectedLocalSubs + expectedRemoteSubs)} actual subs :$results" }
        }

    @Test
    fun `when query is not empty && local cache is empty show only remote results`() =
        runBlockingTest {
            searchKeyWord = "some_keyword"

            coEvery { subredditDAO.observeSubredditWithKeyword(searchKeyWord) } returns
                    flow { emit(emptyList<LocalSubreddit>()) }
            coEvery { redditApi.search(searchKeyWord) } returns buildDummyRemoteSubredditsListing(10)

            val results = useCase.getSubreddits(searchKeyWord).drop(1).first()
            val expectedResults =
                buildDummyRemoteSubredditsListing(10).children.map { it.toLocalSubreddit() }

            assert(subredditsAreSame(results, expectedResults)) {
                "actual results : $results expected : $expectedResults"
            }
        }

    @Test
    fun `when query is not empty && remote results are empty show only local results`() =
        runBlockingTest {
            searchKeyWord = "some_keyword"

            coEvery { subredditDAO.observeSubredditWithKeyword(searchKeyWord) } returns
                    flow { emit(buildDummyLocalSubreddits(10)) }
            coEvery { redditApi.search(searchKeyWord) } returns buildDummyRemoteSubredditsListing(0)

            val results = useCase.getSubreddits(searchKeyWord).drop(1).first()
            val expectedResults = buildDummyLocalSubreddits(2)

            assert(subredditsAreSame(results, expectedResults)) {
                "actual results : $results expected : $expectedResults"
            }

        }

    @Test
    fun `when query is not empty & no local and remote results are found show empty results`() =
        runBlockingTest {
            searchKeyWord = "some_keyword"

            coEvery { subredditDAO.observeSubredditWithKeyword(searchKeyWord) } returns
                    flow { emit(emptyList<LocalSubreddit>()) }
            coEvery { redditApi.search(searchKeyWord) } returns buildDummyRemoteSubredditsListing(0)

            val results = useCase.getSubreddits(searchKeyWord).drop(1).first()
            val expectedResults = emptyList<LocalSubreddit>()

            assert(subredditsAreSame(results, expectedResults)) {
                "actual results : $results expected : $expectedResults"
            }
        }
}

private fun subredditsAreSame(
    list1: List<LocalSubreddit>,
    list2: List<LocalSubreddit>
) = list1.map { it.subredditName }.containsAll(list2.map { it.subredditName }) &&
        list2.map { it.subredditName }.containsAll(list1.map { it.subredditName })

private fun buildDummyRemoteSubredditsListing(count: Int) = Listing(
    children = mutableListOf<RemoteSubreddit>().apply {
        repeat(count) {
            add(
                RemoteSubreddit(
                    display_name = "remoteSubreddit$it",
                    created = 0L,
                    community_icon = "",
                    description = "",
                    public_description = "",
                    subscribers = 0,
                    accounts_active = 0,
                )
            )
        }
    }
)

private fun buildDummyLocalSubreddits(count: Int) =
    mutableListOf<LocalSubreddit>().apply {
        repeat(count) {
            add(
                LocalSubreddit(
                    subredditName = "localSubreddit$it",
                    created = Date(0L),
                    icon = "",
                    longDescription = "",
                    shortDescription = "",
                    lastUpdated = Date(0L),
                    subscribers = 0,
                    accountsActive = 0,
                )
            )
        }
    }