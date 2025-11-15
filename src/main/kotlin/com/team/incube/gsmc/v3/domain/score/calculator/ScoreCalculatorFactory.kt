package com.team.incube.gsmc.v3.domain.score.calculator

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.calculator.impl.CountBasedScoreCalculator
import com.team.incube.gsmc.v3.domain.score.calculator.impl.JlptScoreCalculator
import com.team.incube.gsmc.v3.domain.score.calculator.impl.ReadAThonScoreCalculator
import com.team.incube.gsmc.v3.domain.score.calculator.impl.ToeicScoreCalculator
import com.team.incube.gsmc.v3.domain.score.calculator.impl.TopcitScoreCalculator

object ScoreCalculatorFactory {
    private val topcitScoreCalculator = TopcitScoreCalculator()
    private val toeicScoreCalculator = ToeicScoreCalculator()
    private val jlptScoreCalculator = JlptScoreCalculator()
    private val readAThonScoreCalculator = ReadAThonScoreCalculator()
    private val countBasedScoreCalculator = CountBasedScoreCalculator()

    fun getCalculator(categoryType: CategoryType): CategoryScoreCalculator =
        when (categoryType) {
            CategoryType.TOPCIT -> topcitScoreCalculator

            CategoryType.TOEIC -> toeicScoreCalculator

            CategoryType.JLPT -> jlptScoreCalculator

            CategoryType.READ_A_THON -> readAThonScoreCalculator

            CategoryType.CERTIFICATE,
            CategoryType.TOEIC_ACADEMY,
            -> countBasedScoreCalculator
        }
}
