package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid

public interface Keystroke {
    /**
     * Keystroke should be called whenever a user enters text into the message input
     * It automatically calls stopTyping when the user stops typing after 5 seconds
     *
     * @param cid: the full channel id IE messaging:123
     *
     * @return A call object with Boolean as the return type. True when a typing event was sent, false if it wasn't sent
     */
    public operator fun invoke(cid: String): Call2<Boolean>
}

internal class KeystrokeImpl(private val domainImpl: ChatDomainImpl) : Keystroke {
    override operator fun invoke(cid: String): Call2<Boolean> {
        validateCid(cid)
        val channelController = domainImpl.channel(cid)

        val runnable = suspend {
            channelController.keystroke()
        }
        return CallImpl2(
            runnable,
            channelController.scope
        )
    }
}
