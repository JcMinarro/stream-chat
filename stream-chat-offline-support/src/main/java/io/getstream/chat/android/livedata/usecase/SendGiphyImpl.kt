package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid

public interface SendGiphy {
    /**
     * Sends selected giphy message to the channel. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     * @param message the message to send
     * @return A call object with Message as the return type
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public operator fun invoke(message: Message): Call2<Message>
}

internal class SendGiphyImpl(private val domainImpl: ChatDomainImpl) : SendGiphy {
    override operator fun invoke(message: Message): Call2<Message> {
        val cid = message.cid
        validateCid(cid)

        val channelRepo = domainImpl.channel(cid)

        val runnable = suspend {
            channelRepo.sendGiphy(message)
        }

        return CallImpl2(runnable, channelRepo.scope)
    }
}
