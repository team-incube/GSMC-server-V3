package com.team.incube.gsmc.v3.global.thirdparty.feign.client.discord

import com.team.incube.gsmc.v3.global.common.discord.data.DiscordWebhookPayload
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "discord-webhook",
    url = $$"${spring.cloud.discord.webhook.url}",
)
interface DiscordWebhookClient {
    @PostMapping(
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    fun sendMessage(
        @RequestBody payload: DiscordWebhookPayload,
    )
}
