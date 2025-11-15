package com.team.incube.gsmc.v3.domain.score.calculator

import com.team.incube.gsmc.v3.domain.score.dto.Score

abstract class CategoryScoreCalculator {
    abstract fun calculate(scores: List<Score>): Int
}