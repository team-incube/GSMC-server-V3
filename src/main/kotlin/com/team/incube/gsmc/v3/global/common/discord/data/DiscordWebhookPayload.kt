package com.team.incube.gsmc.v3.global.common.discord.data

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DiscordWebhookPayload(
    @param:JsonProperty("embeds") val embeds: List<DiscordEmbed>? = null,
    @param:JsonProperty("content") val content: String? = null,
) {
    companion object {
        // 필요 시 주석 해제 후 사용
        // fun textMessage(content: String) = DiscordWebhookPayload(content = content)

        fun embedMessage(embed: DiscordEmbed) =
            DiscordWebhookPayload(
                embeds = listOf(embed),
            )
    }
}
