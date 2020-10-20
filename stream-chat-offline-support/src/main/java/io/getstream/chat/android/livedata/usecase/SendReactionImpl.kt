package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid

public interface SendReaction {
    /**
     * Sends the reaction. Immediately adds the reaction to local storage and updates the reaction fields on the related message.
     * API call to send the reaction is retried according to the retry policy specified on the chatDomain
     * @param cid: the full channel id IE messaging:123
     * @param reaction the reaction to add
     * @return A call object with Reaction as the return type
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public operator fun invoke(cid: String, reaction: Reaction): Call2<Reaction>
}

internal class SendReactionImpl(private val domainImpl: ChatDomainImpl) : SendReaction {
    override operator fun invoke(cid: String, reaction: Reaction): Call2<Reaction> {
        validateCid(cid)

        val channelRepo = domainImpl.channel(cid)

        val runnable = suspend {

            channelRepo.sendReaction(reaction)
        }
        return CallImpl2(
            runnable,
            channelRepo.scope
        )
    }
}
