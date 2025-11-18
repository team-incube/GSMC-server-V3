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
    ): ResponseEntity<ByteArrayResource> =
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

            val allCategories = CategoryType.getAllCategories()
            val classScoreDataList = mutableListOf<ClassScoreData>()

            students.forEach { student ->
                val approvedScores = scoreExposedRepository.findByMemberIdAndStatus(student.id, ScoreStatus.APPROVED)
                val categoryScores = mutableMapOf<String, Double>()

                allCategories.forEach { category ->
                    val categoryScoreList = approvedScores.filter { it.categoryType == category }
                    val value =
                        when {
                            categoryScoreList.isEmpty() -> 0.0

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

                classScoreDataList.add(
                    ClassScoreData(
                        studentId = student.id,
                        studentName = student.name,
                        studentNumber = studentNumber,
                        categoryScores = categoryScores,
                        totalScore = totalScore,
                        classRank = 0,
                    ),
                )
            }

            val sortedList =
                classScoreDataList
                    .sortedByDescending { it.totalScore }
                    .mapIndexed { index, data ->
                        data.copy(classRank = index + 1)
                    }

            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("${grade}학년 ${classNumber}반 점수 현황")

            val headerStyle =
                workbook.createCellStyle().apply {
                    fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                    fillPattern = FillPatternType.SOLID_FOREGROUND
                    borderBottom = BorderStyle.THIN
                    borderTop = BorderStyle.THIN
                    borderLeft = BorderStyle.THIN
                    borderRight = BorderStyle.THIN
                    alignment = HorizontalAlignment.CENTER
                    verticalAlignment = VerticalAlignment.CENTER
                    val font = workbook.createFont()
                    font.bold = true
                    setFont(font)
                }

            val cellStyle =
                workbook.createCellStyle().apply {
                    borderBottom = BorderStyle.THIN
                    borderTop = BorderStyle.THIN
                    borderLeft = BorderStyle.THIN
                    borderRight = BorderStyle.THIN
                    alignment = HorizontalAlignment.CENTER
                    verticalAlignment = VerticalAlignment.CENTER
                }

            val headerRow = sheet.createRow(0)
            val headers = mutableListOf("학번", "이름")
            allCategories.forEach { headers.add(it.koreanName) }
            headers.add("총점")
            headers.add("학급 내 순위")

            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            sortedList.forEachIndexed { index, data ->
                val row = sheet.createRow(index + 1)

                var colIndex = 0
                row.createCell(colIndex++).apply {
                    setCellValue(data.studentNumber)
                    this.cellStyle = cellStyle
                }
                row.createCell(colIndex++).apply {
                    setCellValue(data.studentName)
                    this.cellStyle = cellStyle
                }
                allCategories.forEach { category ->
                    row.createCell(colIndex++).apply {
                        setCellValue(data.categoryScores[category.koreanName] ?: 0.0)
                        this.cellStyle = cellStyle
                    }
                }
                row.createCell(colIndex++).apply {
                    setCellValue(data.totalScore)
                    this.cellStyle = cellStyle
                }
                row.createCell(colIndex).apply {
                    setCellValue(data.classRank.toDouble())
                    this.cellStyle = cellStyle
                }
            }

            for (i in 0 until headers.size) {
                sheet.autoSizeColumn(i)
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000)
            }

            val outputStream = ByteArrayOutputStream()
            workbook.write(outputStream)
            workbook.close()

            val resource = ByteArrayResource(outputStream.toByteArray())

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val filename = "${grade}학년_${classNumber}반_점수현황_$timestamp.xlsx"
            val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20")

            ResponseEntity
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
                    calculator.calculate(categoryScores, categoryType, includeApprovedOnly = false)
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
                    includeApprovedOnly = false,
                )
            } else {
                0
            }

        val jlptScore =
            if (jlptScores.isNotEmpty()) {
                jlptCalculator.calculate(jlptScores, CategoryType.JLPT, includeApprovedOnly = false)
            } else {
                0
            }

        return maxOf(toeicScore, jlptScore)
    }
}
