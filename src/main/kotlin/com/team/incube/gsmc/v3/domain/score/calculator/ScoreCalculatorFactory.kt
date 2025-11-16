package com.team.incube.gsmc.v3.domain.score.calculator

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType.ACADEMIC_GRADE
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType.AWARD
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType.CERTIFICATE
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType.EXTERNAL_ACTIVITY
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType.JLPT
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType.NCS
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType.NEWRROW_SCHOOL
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType.PROJECT
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType.READ_A_THON
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType.TOEIC
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType.TOEIC_ACADEMY
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType.TOPCIT
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType.VOLUNTEER
import com.team.incube.gsmc.v3.domain.score.calculator.impl.AcademicGradeScoreCalculator
import com.team.incube.gsmc.v3.domain.score.calculator.impl.CountBasedScoreCalculator
import com.team.incube.gsmc.v3.domain.score.calculator.impl.JlptScoreCalculator
import com.team.incube.gsmc.v3.domain.score.calculator.impl.NcsScoreCalculator
import com.team.incube.gsmc.v3.domain.score.calculator.impl.NewrrowSchoolScoreCalculator
import com.team.incube.gsmc.v3.domain.score.calculator.impl.ReadAThonScoreCalculator
import com.team.incube.gsmc.v3.domain.score.calculator.impl.ToeicScoreCalculator
import com.team.incube.gsmc.v3.domain.score.calculator.impl.TopcitScoreCalculator
import com.team.incube.gsmc.v3.domain.score.calculator.impl.VolunteerScoreCalculator

object ScoreCalculatorFactory {
    private val topcitScoreCalculator = TopcitScoreCalculator()
    private val toeicScoreCalculator = ToeicScoreCalculator()
    private val jlptScoreCalculator = JlptScoreCalculator()
    private val readAThonScoreCalculator = ReadAThonScoreCalculator()
    private val volunteerScoreCalculator = VolunteerScoreCalculator()
    private val ncsScoreCalculator = NcsScoreCalculator()
    private val newrrowSchoolScoreCalculator = NewrrowSchoolScoreCalculator()
    private val academicGradeScoreCalculator = AcademicGradeScoreCalculator()
    private val countBasedScoreCalculator = CountBasedScoreCalculator()

    fun getCalculator(categoryType: CategoryType): CategoryScoreCalculator =
        when (categoryType) {
            TOPCIT -> topcitScoreCalculator

            TOEIC -> toeicScoreCalculator

            JLPT -> jlptScoreCalculator

            READ_A_THON -> readAThonScoreCalculator

            VOLUNTEER -> volunteerScoreCalculator

            NCS -> ncsScoreCalculator

            NEWRROW_SCHOOL -> newrrowSchoolScoreCalculator

            ACADEMIC_GRADE -> academicGradeScoreCalculator

            CERTIFICATE,
            TOEIC_ACADEMY,
            AWARD,
            EXTERNAL_ACTIVITY,
            -> countBasedScoreCalculator

            PROJECT -> TODO()
        }
}
