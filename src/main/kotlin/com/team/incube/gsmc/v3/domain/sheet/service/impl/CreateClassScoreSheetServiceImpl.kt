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
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class CreateClassScoreSheetServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
) : CreateClassScoreSheetService {
    override fun execute(
        grade: Int,
        classNumber: Int,
    ): ByteArrayResource =
        transaction {
            // 해당 학급의 학생들 조회
            val students =
                memberExposedRepository
                    .searchMembers(
                        email = null,
                        name = null,
                        role = MemberRole.STUDENT,
                        grade = grade,
                        classNumber = classNumber,
                        number = null,
                        pageable = PageRequest.of(0, 1000),
                    ).content

            // 각 학생의 점수 데이터 수집
            val allCategories = CategoryType.getAllCategories()
            val classScoreDataList = mutableListOf<ClassScoreData>()

            students.forEach { student ->
                val allScores = scoreExposedRepository.findAllByMemberId(student.id)
                val approvedScores = allScores.filter { it.status == ScoreStatus.APPROVED }
                val categoryScores = mutableMapOf<String, Double>()

                // 각 카테고리별 원본 값 (승인된 레코드만)
                allCategories.forEach { category ->
                    val categoryScoreList = approvedScores.filter { it.categoryType == category }
                    val value =
                        when {
                            categoryScoreList.isEmpty() -> 0.0

                            category.calculationType == ScoreCalculationType.SCORE_BASED -> {
                                // 점수 기반: scoreValue의 합
                                categoryScoreList.sumOf { it.scoreValue ?: 0.0 }
                            }

                            else -> {
                                // 레코드 기반: 레코드 개수
                                categoryScoreList.size.toDouble()
                            }
                        }
                    categoryScores[category.koreanName] = value
                }

                // 총점 계산 - 환산점 (승인된 레코드만)
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
                        classRank = 0, // 나중에 계산
                    ),
                )
            }

            // 총점 기준 내림차순 정렬 및 등수 계산
            val sortedList =
                classScoreDataList
                    .sortedByDescending { it.totalScore }
                    .mapIndexed { index, data ->
                        data.copy(classRank = index + 1)
                    }

            // 엑셀 생성
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("${grade}학년 ${classNumber}반 점수 현황")

            // 스타일 정의
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

            // 헤더 행 생성
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

            // 데이터 행 생성
            sortedList.forEachIndexed { index, data ->
                val row = sheet.createRow(index + 1)

                var colIndex = 0
                // 학번
                row.createCell(colIndex++).apply {
                    setCellValue(data.studentNumber)
                    this.cellStyle = cellStyle
                }
                // 이름
                row.createCell(colIndex++).apply {
                    setCellValue(data.studentName)
                    this.cellStyle = cellStyle
                }
                // 각 카테고리 점수
                allCategories.forEach { category ->
                    row.createCell(colIndex++).apply {
                        setCellValue(data.categoryScores[category.koreanName] ?: 0.0)
                        this.cellStyle = cellStyle
                    }
                }
                // 총점
                row.createCell(colIndex++).apply {
                    setCellValue(data.totalScore)
                    this.cellStyle = cellStyle
                }
                // 등수
                row.createCell(colIndex).apply {
                    setCellValue(data.classRank.toDouble())
                    this.cellStyle = cellStyle
                }
            }

            // 열 너비 자동 조정
            for (i in 0 until headers.size) {
                sheet.autoSizeColumn(i)
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000)
            }

            // ByteArrayResource로 변환
            val outputStream = ByteArrayOutputStream()
            workbook.write(outputStream)
            workbook.close()

            ByteArrayResource(outputStream.toByteArray())
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
