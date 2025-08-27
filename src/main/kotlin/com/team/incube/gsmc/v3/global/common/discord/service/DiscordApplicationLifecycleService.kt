package com.team.incube.gsmc.v3.global.common.discord.service

import org.springframework.context.SmartLifecycle
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("prod")
@Service
class DiscordApplicationLifecycleService(
    private val discordNotificationService: DiscordNotificationService,
) : SmartLifecycle {
    @Volatile
    private var isRunning = false

    override fun start() {
        discordNotificationService.sendServerStartNotification()
        isRunning = true
    }

    override fun stop() {
        discordNotificationService.sendServerStopNotification()
        isRunning = false
    }

    override fun isRunning(): Boolean = isRunning
}
