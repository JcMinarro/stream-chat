package io.getstream.chat.android.livedata

import android.os.Handler
import androidx.arch.core.executor.testing.InstantExecutorExtension
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi

@ExtendWith(InstantExecutorExtension::class)
internal class ChatDomainImplTest {

    private lateinit var sut: ChatDomainImpl

    @BeforeEach
    fun setUp() {
        val client: ChatClient = mock()
        val currentUser = randomUser()
        val db: ChatDatabase = mock {
            on { userDao() } doReturn mock()
            on { channelConfigDao() } doReturn mock()
            on { channelStateDao() } doReturn mock()
            on { queryChannelsQDao() } doReturn mock()
            on { messageDao() } doReturn mock()
            on { reactionDao() } doReturn mock()
            on { syncStateDao() } doReturn mock()
        }
        val handler: Handler = mock()
        val offlineEnabled = true
        val userPresence = true
        val recoveryEnabled = true
        sut = ChatDomainImpl(client, currentUser, db, handler, offlineEnabled, userPresence, recoveryEnabled)
    }

    @Test
    fun `When create a new channel without author should set current user as author and return channel with author`() =
        runBlockingTest {
            val newChannel = randomChannel(cid = "channelType:channelId", createdBy = User())

            val result = sut.createChannel(newChannel)

            result.isSuccess shouldBeEqualTo true
            result.data().createdBy shouldBeEqualTo sut.currentUser
        }
}
