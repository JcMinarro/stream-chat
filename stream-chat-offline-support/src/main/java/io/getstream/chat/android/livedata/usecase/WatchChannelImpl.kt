package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid
import kotlinx.coroutines.launch

public interface WatchChannel {
    /**
     * Watches the given channel and returns a ChannelController
     *
     * @param cid the full channel id. ie messaging:123
     * @param messageLimit how many messages to load on the first request

     * @return A call object with ChannelController as the return type
     * @see io.getstream.chat.android.livedata.controller.ChannelController
     */
    public operator fun invoke(cid: String, messageLimit: Int): Call2<ChannelController>
}

internal class WatchChannelImpl(private val domainImpl: ChatDomainImpl) : WatchChannel {
    override operator fun invoke(cid: String, messageLimit: Int): Call2<ChannelController> {
        validateCid(cid)
        val channelControllerImpl = domainImpl.channel(cid)
        val channelControllerI: ChannelController = channelControllerImpl

        if (messageLimit> 0) {
            channelControllerImpl.scope.launch {
                channelControllerImpl.watch(messageLimit)
            }
        }

        val runnable = suspend {
            Result(channelControllerI, null)
        }
        return CallImpl2(
            runnable,
            channelControllerImpl.scope
        )
    }
}
