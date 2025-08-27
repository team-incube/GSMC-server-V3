package com.team.incube.gsmc.v3.global.common.discord.data

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DiscordWebhookPayload(
    @param:JsonProperty("embeds") val embeds: List<DiscordEmbed>? = null,
    @param:JsonProperty("content") val content: String? = null,
) {
    companion object {
        fun textMessage(content: String) = DiscordWebhookPayload(content = content)

        fun embedMessage(embed: DiscordEmbed) =
            DiscordWebhookPayload(
                embeds = listOf(embed),
            )

        fun multipleEmbeds(embeds: List<DiscordEmbed>) =
            DiscordWebhookPayload(
                embeds = embeds,
            )

        // 테스트용 단순 메시지
        fun simpleMessage(title: String) =
            DiscordWebhookPayload(
                content = "🚀 $title",
            )
    }
}
