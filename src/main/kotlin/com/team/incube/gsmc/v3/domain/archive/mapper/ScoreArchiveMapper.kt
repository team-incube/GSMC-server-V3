package com.team.incube.gsmc.v3.domain.archive.mapper

import com.team.incube.gsmc.v3.domain.archive.dto.ScoreArchive
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface ScoreArchiveMapper {
    fun deleteByAcademicYear(
        @Param("academicYear") academicYear: Int,
    ): Int

    fun insertBatch(
        @Param("archives") archives: List<ScoreArchive>,
    ): Int

    fun findByAcademicYear(
        @Param("academicYear") academicYear: Int,
    ): List<ScoreArchive>

    fun existsByAcademicYear(
        @Param("academicYear") academicYear: Int,
    ): Boolean

    fun countByAcademicYear(
        @Param("academicYear") academicYear: Int,
    ): Long
}
