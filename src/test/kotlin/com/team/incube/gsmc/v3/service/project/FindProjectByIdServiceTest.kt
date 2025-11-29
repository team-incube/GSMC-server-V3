package com.team.incube.gsmc.v3.service.project

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.project.dto.Project
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectResponse
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.impl.FindProjectByIdServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class FindProjectByIdServiceTest :
    BehaviorSpec({
        data class TestData(
            val projectRepo: ProjectExposedRepository,
            val service: FindProjectByIdServiceImpl,
        )

        fun ctx(): TestData {
            val projectRepo = mockk<ProjectExposedRepository>()
            val service = FindProjectByIdServiceImpl(projectRepo)
            return TestData(projectRepo, service)
        }

        beforeTest {
            mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
            every {
                transaction(db = any(), statement = any<Transaction.() -> Any>())
            } answers {
                secondArg<Transaction.() -> Any>().invoke(mockk(relaxed = true))
            }
        }

        afterTest {
            unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        }

        Given("존재하는 프로젝트 ID로 조회할 때") {
            val c = ctx()
            val projectId = 100L
            val files =
                listOf(
                    File(
                        id = 10L,
                        member = 1L,
                        originalName = "file1.pdf",
                        storeName = "stored-file1.pdf",
                        uri = "uri-1",
                    ),
                )
            val participants =
                listOf(
                    Member(
                        id = 1L,
                        name = "Participant 1",
                        email = "participant1@test.com",
                        grade = 1,
                        classNumber = 1,
                        number = 1,
                        role = MemberRole.STUDENT,
                    ),
                    Member(
                        id = 2L,
                        name = "Participant 2",
                        email = "participant2@test.com",
                        grade = 1,
                        classNumber = 1,
                        number = 2,
                        role = MemberRole.STUDENT,
                    ),
                )
            val project =
                Project(
                    id = projectId,
                    ownerId = 1L,
                    title = "Test Project",
                    description = "Test Description",
                    files = files,
                    participants = participants,
                )

            every { c.projectRepo.findProjectById(projectId) } returns project
            every { c.projectRepo.findScoreIdsByProjectId(projectId) } returns listOf(1L, 2L)

            When("execute를 호출하면") {
                val res: GetProjectResponse = c.service.execute(projectId)

                Then("프로젝트 정보가 정상적으로 반환된다") {
                    res shouldNotBe null
                    res.id shouldBe projectId
                    res.title shouldBe "Test Project"
                    res.description shouldBe "Test Description"
                    res.files.size shouldBe 1
                    res.scoreIds shouldBe listOf(1L, 2L)
                }

                Then("프로젝트 조회와 점수 조회가 호출된다") {
                    verify(exactly = 1) { c.projectRepo.findProjectById(projectId) }
                    verify(exactly = 1) { c.projectRepo.findScoreIdsByProjectId(projectId) }
                }
            }
        }

        Given("존재하지 않는 프로젝트 ID로 조회할 때") {
            val c = ctx()
            val projectId = 999L

            every { c.projectRepo.findProjectById(projectId) } returns null

            When("execute를 호출하면") {
                Then("PROJECT_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(projectId) }
                    ex.errorCode shouldBe ErrorCode.PROJECT_NOT_FOUND
                }
            }
        }
    })
