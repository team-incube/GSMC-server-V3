package com.team.incube.gsmc.v3.domain.sheet.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.category.constant.ScoreCalculationType
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.dto.constant.SortDirection
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.calculator.ScoreCalculatorFactory
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.sheet.dto.ClassScoreData
import com.team.incube.gsmc.v3.domain.sheet.service.CreateGradeScoreSheetService
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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
class CreateGradeScoreSheetServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
) : CreateGradeScoreSheetService {
    companion object {
        private const val MAX_STUDENTS_PER_GRADE = 1000
    }

    override fun execute(grade: Int): ResponseEntity<ByteArrayResource> {
        val allCategories = CategoryType.getAllCategories()
        val gradeScoreDataList =
            transaction {
                val students =
                    memberExposedRepository
                        .searchMembers(
                            email = null,
                            name = null,
                            role = MemberRole.STUDENT,
                            grade = grade,
                            classNumber = null,
                            number = null,
                            sortBy = SortDirection.ASC,
                            pageable = PageRequest.of(0, MAX_STUDENTS_PER_GRADE),
                        ).content

                val studentIds = students.map { it.id }
                val allApprovedScores = scoreExposedRepository.findByMemberIdsAndStatus(studentIds, ScoreStatus.APPROVED)
                val scoresByStudentId = allApprovedScores.groupBy { it.member.id }

                students.map { student ->
                    val approvedScores = scoresByStudentId[student.id] ?: emptyList()
                    val scoresByCategory = approvedScores.groupBy { it.categoryType }
                    val categoryScores = mutableMapOf<String, Double>()

                    allCategories.forEach { category ->
                        val categoryScoreList = scoresByCategory[category] ?: emptyList()
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
            gradeScoreDataList
                .sortedWith(
                    compareByDescending<ClassScoreData> { it.totalScore }
                        .thenByDescending { it.categoryScores["자격증"] ?: 0.0 }
                        .thenByDescending { it.categoryScores["TOPCIT"] ?: 0.0 }
                        .thenByDescending { it.categoryScores["교과성적"] ?: 0.0 },
                ).mapIndexed { index, data ->
                    data.copy(classRank = index + 1)
                }.sortedBy { it.studentNumber }

        val resource = createExcelFile(grade, sortedList, allCategories)

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val filename = "${grade}학년_전체_점수현황_$timestamp.xlsx"
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

    private fun createExcelFile(
        grade: Int,
        scoreDataList: List<ClassScoreData>,
        categories: List<CategoryType>,
    ): ByteArrayResource {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("${grade}학년 전체 점수 현황")

        val headerStyle = createHeaderStyle(workbook)
        val cellStyle = createCellStyle(workbook)

        val headers = buildHeaders(categories)
        createHeaderRow(sheet, headers, headerStyle)
        populateDataRows(sheet, scoreDataList, categories, cellStyle)
        adjustColumnWidths(sheet, headers.size)

        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

        return ByteArrayResource(outputStream.toByteArray())
    }

    private fun createHeaderStyle(workbook: XSSFWorkbook) =
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

    private fun createCellStyle(workbook: XSSFWorkbook) =
        workbook.createCellStyle().apply {
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }

    private fun buildHeaders(categories: List<CategoryType>): List<String> {
        val headers = mutableListOf("학번", "이름")
        categories.forEach { headers.add(it.koreanName) }
        headers.add("총점")
        headers.add("학년 내 순위")
        return headers
    }

    private fun createHeaderRow(
        sheet: Sheet,
        headers: List<String>,
        headerStyle: CellStyle,
    ) {
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }
    }

    private fun populateDataRows(
        sheet: Sheet,
        scoreDataList: List<ClassScoreData>,
        categories: List<CategoryType>,
        cellStyle: CellStyle,
    ) {
        scoreDataList.forEachIndexed { index, data ->
            val row = sheet.createRow(index + 1)

            var colIndex = 0
            createCell(row, colIndex++, data.studentNumber, cellStyle)
            createCell(row, colIndex++, data.studentName, cellStyle)
            categories.forEach { category ->
                createCell(row, colIndex++, data.categoryScores[category.koreanName] ?: 0.0, cellStyle)
            }
            createCell(row, colIndex++, data.totalScore, cellStyle)
            createCell(row, colIndex, data.classRank.toDouble(), cellStyle)
        }
    }

    private fun createCell(
        row: Row,
        columnIndex: Int,
        value: String,
        style: CellStyle,
    ) {
        row.createCell(columnIndex).apply {
            setCellValue(value)
            cellStyle = style
        }
    }

    private fun createCell(
        row: Row,
        columnIndex: Int,
        value: Double,
        style: CellStyle,
    ) {
        row.createCell(columnIndex).apply {
            setCellValue(value)
            cellStyle = style
        }
    }

    private fun adjustColumnWidths(
        sheet: Sheet,
        columnCount: Int,
    ) {
        for (i in 0 until columnCount) {
            sheet.autoSizeColumn(i)
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000)
        }
    }
}
