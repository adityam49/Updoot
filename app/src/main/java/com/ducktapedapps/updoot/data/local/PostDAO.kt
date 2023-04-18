package com.ducktapedapps.updoot.data.local

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.ducktapedapps.updoot.data.local.model.Post
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Query("DELETE FROM Post WHERE id IS :id AND subredditName IS :subreddit")
    suspend fun deletePost(id: String, subreddit: String)

    @RawQuery(observedEntities = [Post::class])
    fun observeCachedPosts(query: SupportSQLiteQuery): Flow<List<Post>>

    @Query("SELECT * FROM Post")
    suspend fun getCachedPosts(): List<Post>

    @Query("SELECT * FROM Post WHERE id is :id")
    suspend fun getPost(id: String): Post

    @Query("SELECT * FROM Post WHERE id is :id")
    fun observePost(id: String): Flow<Post>

    @Query("UPDATE Post SET userHasUpVoted = :vote WHERE id is :id")
    suspend fun setVote(id:String,vote:Boolean?)
}