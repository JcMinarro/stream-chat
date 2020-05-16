package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.calendar
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueryChannelsImplTest : BaseConnectedIntegrationTest() {

    @Test
    fun filter() = runBlocking(Dispatchers.IO) {
        // use case style syntax
        var queryChannelResult = chatDomain.useCases.queryChannels(data.filter1, null).execute()
        assertSuccess(queryChannelResult)
        val queryChannelsController = queryChannelResult.data()
        val channels = queryChannelsController.channels.getOrAwaitValue()
        Truth.assertThat(channels).isNotEmpty()
        for (channel in channels) {
            Truth.assertThat(channel.unreadCount).isNotNull()
        }
    }

    @Test
    @Ignore("this is broken somehow")
    fun unreadCountNewMessage() = runBlocking(Dispatchers.IO) {
        val queryChannelResult = chatDomain.useCases.queryChannels(data.filter1, null).execute()
        assertSuccess(queryChannelResult)
        val queryChannelsController = queryChannelResult.data()
        val channels = queryChannelsController.channels.getOrAwaitValue()
        Truth.assertThat(channels).isNotEmpty()
        val channel = channels.first()
        val initialCount = channel.unreadCount!!
        val message2 = Message().apply { text = "it's a beautiful world"; cid = channel.cid; user = data.user2; createdAt = calendar(2020, 5, 14) }
        val messageEvent = NewMessageEvent().apply { message = message2; cid = channel.cid; }
        val channelController = chatDomainImpl.channel(channel)
        chatDomainImpl.eventHandler.handleEvent(messageEvent)
        // new message should increase the count by 1
        Truth.assertThat(channelController.unreadCount.getOrAwaitValue()).isEqualTo(initialCount + 1)
        Truth.assertThat(queryChannelsController.channels.getOrAwaitValue().first().unreadCount).isEqualTo(initialCount + 1)
        // mark read should set it to zero
        val readEvent = MessageReadEvent().apply { message = message2; user = data.user1; cid = data.channel1.cid; createdAt = message2.createdAt }
        chatDomainImpl.eventHandler.handleEvent(readEvent)

        Truth.assertThat(channelController.unreadCount.getOrAwaitValue()).isEqualTo(0)
        Truth.assertThat(queryChannelsController.channels.getOrAwaitValue().first().unreadCount).isEqualTo(0)
    }
}
