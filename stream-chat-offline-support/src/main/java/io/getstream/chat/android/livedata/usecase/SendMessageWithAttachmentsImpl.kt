package io.getstream.chat.android.livedata.usecase

import android.webkit.MimeTypeMap
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ChannelControllerImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid
import java.io.File

public interface SendMessageWithAttachments {

    @Deprecated(
        message = "Use sendMessage() and attachment.upload instead of this useCase",
        level = DeprecationLevel.WARNING
    )
    public operator fun invoke(cid: String, message: Message, files: List<File>, attachmentTransformer: Attachment.(file: File) -> Unit = { }): Call2<Message>
}

internal class SendMessageWithAttachmentsImpl(private val domainImpl: ChatDomainImpl) : SendMessageWithAttachments {
    override fun invoke(cid: String, message: Message, files: List<File>, attachmentTransformer: Attachment.(file: File) -> Unit): Call2<Message> {
        validateCid(cid)
        val channel = domainImpl.channel(cid)
        message.cid = cid
        val runnable = suspend {
            val attachments = uploadFiles(channel, files, attachmentTransformer)
            if (attachments.isError) {
                Result(attachments.error())
            } else {
                message.attachments.addAll(attachments.data())
                channel.sendMessage(message)
            }
        }
        return CallImpl2(
            runnable,
            channel.scope
        )
    }

    private suspend fun uploadFiles(channelControllerImpl: ChannelControllerImpl, files: List<File>, attachmentTransformer: Attachment.(file: File) -> Unit): Result<List<Attachment>> =
        files.fold(Result(emptyList())) { acc, file ->
            if (acc.isError) {
                acc
            } else {
                val attachment = uploadFile(channelControllerImpl, file)
                if (attachment.isError) {
                    Result(attachment.error())
                } else {
                    Result(acc.data() + attachment.data().apply { attachmentTransformer(file) })
                }
            }
        }

    private suspend fun uploadFile(channelControllerImpl: ChannelControllerImpl, file: File): Result<Attachment> =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension).let { mimetype ->
            val pathResult = when (mimetype.isImageMimetype()) {
                true -> channelControllerImpl.sendImage(file)
                false -> channelControllerImpl.sendFile(file)
            }
            if (pathResult.isError) {
                Result(pathResult.error())
            } else {
                val path = pathResult.data()
                Result(
                    Attachment(
                        name = file.name,
                        fileSize = file.length().toInt(),
                        mimeType = mimetype,
                        url = path
                    ).apply {
                        when (mimetype.isImageMimetype()) {
                            true -> {
                                imageUrl = path
                                type = "image"
                            }
                            false -> {
                                assetUrl = path
                                type = "file"
                            }
                        }
                    }
                )
            }
        }
}

internal fun String?.isImageMimetype() = this?.contains("image") ?: false
