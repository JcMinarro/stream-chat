package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.BaseDomainTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ReactionRepositoryTest : BaseDomainTest() {
    val repo by lazy { chatDomainImpl.repos.reactions }

    @Test
    fun testInsertAndRead() = runBlocking(Dispatchers.IO) {
        repo.insertReaction(data.reaction1)
        val entity =
            repo.select(data.reaction1.messageId, data.reaction1.user!!.id, data.reaction1.type)
        val reaction = entity!!.toReaction(data.userMap)
        Truth.assertThat(reaction).isEqualTo(data.reaction1)
    }

    @Test
    fun testSyncNeeded() = runBlocking(Dispatchers.IO) {
        data.reaction1.syncStatus = SyncStatus.FAILED_PERMANENTLY
        val reaction2 =
            data.reaction1.copy().apply { type = "love"; syncStatus = SyncStatus.SYNC_NEEDED }
        repo.insertManyReactions(listOf(data.reaction1, reaction2))
        var reactions = repo.selectSyncNeeded()
        Truth.assertThat(reactions.size).isEqualTo(1)
        Truth.assertThat(reactions.first().syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)

        reactions = repo.retryReactions()
        Truth.assertThat(reactions.size).isEqualTo(1)
        Truth.assertThat(reactions.first().syncStatus).isEqualTo(SyncStatus.COMPLETED)

        reactions = repo.selectSyncNeeded()
        Truth.assertThat(reactions.size).isEqualTo(0)
    }

    @Test
    fun testUpdate() = runBlocking(Dispatchers.IO) {
        val reaction1Updated =
            data.reaction1.copy().apply { extraData = mutableMapOf("theanswer" to 42.0) }
        repo.insertReaction(data.reaction1)
        repo.insertReaction(reaction1Updated)

        val entity =
            repo.select(data.reaction1.messageId, data.reaction1.user!!.id, data.reaction1.type)
        val reaction = entity!!.toReaction(data.userMap)
        Truth.assertThat(reaction).isEqualTo(reaction1Updated)
    }
}
