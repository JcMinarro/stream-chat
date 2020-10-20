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
internal class ChannelRepositoryTest : BaseDomainTest() {
    val repo by lazy { chatDomainImpl.repos.channels }

    @Test
    fun testInsertAndRead() = runBlocking(Dispatchers.IO) {
        repo.insertChannel(data.channel1)
        val entity = repo.select(data.channel1.cid)
        val channel = entity!!.toChannel(data.userMap)
        channel.config = data.channel1.config
        channel.watchers = data.channel1.watchers
        channel.watcherCount = data.channel1.watcherCount

        Truth.assertThat(channel).isEqualTo(data.channel1)
    }

    @Test
    fun testInsertAndDelete() = runBlocking(Dispatchers.IO) {
        repo.insertChannel(data.channel1)
        repo.delete(data.channel1.cid)
        val entity = repo.select(data.channel1.cid)

        Truth.assertThat(entity).isNull()
    }

    @Test
    fun testUpdate() = runBlocking(Dispatchers.IO) {
        repo.insertChannel(data.channel1)
        repo.insertChannel(data.channel1Updated)
        val entity = repo.select(data.channel1.cid)
        val channel = entity!!.toChannel(data.userMap)

        // ignore these 4 fields
        channel.config = data.channel1.config
        channel.createdBy = data.channel1.createdBy
        channel.watchers = data.channel1Updated.watchers
        channel.watcherCount = data.channel1Updated.watcherCount
        Truth.assertThat(channel).isEqualTo(data.channel1Updated)
    }

    @Test
    fun testSyncNeeded() = runBlocking(Dispatchers.IO) {
        data.channel1.syncStatus = SyncStatus.SYNC_NEEDED
        data.channel2.syncStatus = SyncStatus.COMPLETED

        repo.insertChannel(listOf(data.channel1, data.channel2))

        var channels = repo.selectSyncNeeded()
        Truth.assertThat(channels.size).isEqualTo(1)
        Truth.assertThat(channels.first().syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)

        channels = repo.retryChannels()
        Truth.assertThat(channels.size).isEqualTo(1)
        Truth.assertThat(channels.first().syncStatus).isEqualTo(SyncStatus.COMPLETED)

        channels = repo.selectSyncNeeded()
        Truth.assertThat(channels.size).isEqualTo(0)
    }
}
