package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid

public interface ShowChannel {
    /**
     * Shows a channel that was previously hidden
     *
     * @param cid: the full channel id IE messaging:123
     *
     * @return A call object with Unit as the return type
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin">Hiding a channel</a>
     */
    public operator fun invoke(cid: String): Call2<Unit>
}

internal class ShowChannelImpl(private val domainImpl: ChatDomainImpl) : ShowChannel {
    override operator fun invoke(cid: String): Call2<Unit> {
        validateCid(cid)
        val channelController = domainImpl.channel(cid)

        val runnable = suspend {
            channelController.show()
        }
        return CallImpl2(
            runnable,
            channelController.scope
        )
    }
}
