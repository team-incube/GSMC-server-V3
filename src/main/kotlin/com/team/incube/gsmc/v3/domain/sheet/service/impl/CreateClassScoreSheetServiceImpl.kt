package com.team.incube.gsmc.v3.domain.sheet.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.category.constant.ScoreCalculationType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.calculator.ScoreCalculatorFactory
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.sheet.dto.ClassScoreData
import com.team.incube.gsmc.v3.domain.sheet.service.CreateClassScoreSheetService
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.core.io.ByteArrayResource
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class CreateClassScoreSheetServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
) : CreateClassScoreSheetService {
    companion object {
        private const val MAX_STUDENTS_PER_CLASS = 1000
    }

    override fun execute(
        grade: Int,
        classNumber: Int,
    ): ResponseEntity<ByteArrayResource> {
        val allCategories = CategoryType.getAllCategories()
        val classScoreDataList =
            transaction {
                val students =
                    memberExposedRepository
                        .searchMembers(
                            email = null,
                            name = null,
                            role = MemberRole.STUDENT,
                            grade = grade,
                            classNumber = classNumber,
                            number = null,
                            pageable = PageRequest.of(0, MAX_STUDENTS_PER_CLASS),
                        ).content

                val studentIds = students.map { it.id }
                val allApprovedScores = scoreExposedRepository.findByMemberIdsAndStatus(studentIds, ScoreStatus.APPROVED)
                val scoresByStudentId = allApprovedScores.groupBy { it.member.id }

                students.map { student ->
                    val approvedScores = scoresByStudentId[student.id] ?: emptyList()
                    val categoryScores = mutableMapOf<String, Double>()

                    allCategories.forEach { category ->
                        val categoryScoreList = approvedScores.filter { it.categoryType == category }
                        val value =
                            when {
                                categoryScoreList.isEmpty() -> {
                                    0.0
                                }

                                category.calculationType == ScoreCalculationType.SCORE_BASED -> {
                                    categoryScoreList.sumOf { it.scoreValue ?: 0.0 }
                                }

                                else -> {
                                    categoryScoreList.size.toDouble()
                                }
                            }
                        categoryScores[category.koreanName] = value
                    }

                    val totalScore = calculateTotalScore(approvedScores).toDouble()

                    val studentNumber =
                        String.format(
                            "%d%d%02d",
                            student.grade ?: 0,
                            student.classNumber ?: 0,
                            student.number ?: 0,
                        )

                    ClassScoreData(
                        studentId = student.id,
                        studentName = student.name,
                        studentNumber = studentNumber,
                        categoryScores = categoryScores,
                        totalScore = totalScore,
                        classRank = 0,
                    )
                }
            }

        val sortedList =
            classScoreDataList
                .sortedByDescending { it.totalScore }
                .mapIndexed { index, data ->
                    data.copy(classRank = index + 1)
                }

        val resource = createExcelFile(grade, classNumber, sortedList, allCategories)

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val filename = "${grade}학년_${classNumber}반_점수현황_$timestamp.xlsx"
        val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20")

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''$encodedFilename")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(resource)
    }

    private fun calculateTotalScore(scores: List<Score>): Int {
        val scoresByCategory = scores.groupBy { it.categoryType }

        val foreignLanguageCategories = CategoryType.getForeignLanguageCategories()

        val foreignLanguageScores =
            foreignLanguageCategories.flatMap { categoryType ->
                scoresByCategory[categoryType] ?: emptyList()
            }

        val foreignLanguageScore =
            if (foreignLanguageScores.isNotEmpty()) {
                calculateForeignLanguageScore(foreignLanguageScores)
            } else {
                0
            }

        val otherCategories = CategoryType.getAllCategories() - foreignLanguageCategories.toSet()

        val otherScoresSum =
            otherCategories.sumOf { categoryType ->
                val categoryScores = scoresByCategory[categoryType] ?: emptyList()
                if (categoryScores.isEmpty()) {
                    0
                } else {
                    val calculator = ScoreCalculatorFactory.getCalculator(categoryType)
                    calculator.calculate(categoryScores, categoryType, includeApprovedOnly = true)
                }
            }

        return foreignLanguageScore + otherScoresSum
    }

    private fun calculateForeignLanguageScore(scores: List<Score>): Int {
        val toeicScores = scores.filter { it.categoryType == CategoryType.TOEIC || it.categoryType == CategoryType.TOEIC_ACADEMY }
        val jlptScores = scores.filter { it.categoryType == CategoryType.JLPT || it.categoryType == CategoryType.TOEIC_ACADEMY }

        val toeicCalculator = ScoreCalculatorFactory.getCalculator(CategoryType.TOEIC)
        val jlptCalculator = ScoreCalculatorFactory.getCalculator(CategoryType.JLPT)

        val toeicScore =
            if (toeicScores.isNotEmpty()) {
                toeicCalculator.calculate(
                    toeicScores,
                    CategoryType.TOEIC,
                    includeApprovedOnly = true,
                )
            } else {
                0
            }

        val jlptScore =
            if (jlptScores.isNotEmpty()) {
                jlptCalculator.calculate(jlptScores, CategoryType.JLPT, includeApprovedOnly = true)
            } else {
                0
            }

        return maxOf(toeicScore, jlptScore)
    }
}
