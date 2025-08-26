package com.team.incube.gsmc.v3.global.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Any.logger(): Logger = LoggerFactory.getLogger(this.javaClass)