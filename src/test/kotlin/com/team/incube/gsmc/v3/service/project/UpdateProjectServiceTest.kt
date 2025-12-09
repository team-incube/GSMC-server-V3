package com.team.incube.gsmc.v3.service.project

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.project.dto.Project
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.impl.UpdateProjectServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
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

class UpdateProjectServiceTest :
    BehaviorSpec({
        data class TestData(
            val projectRepo: ProjectExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: UpdateProjectServiceImpl,
        )

        fun ctx(): TestData {
            val projectRepo = mockk<ProjectExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()

            every { currentMemberProvider.getCurrentMember() } returns
                Member(
                    id = 0L,
                    name = "Test User",
                    email = "test@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val service = UpdateProjectServiceImpl(projectRepo, currentMemberProvider)
            return TestData(projectRepo, currentMemberProvider, service)
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

        Given("프로젝트의 제목과 설명을 업데이트할 때") {
            val c = ctx()
            val projectId = 100L
            val files =
                listOf(
                    File(
                        id = 10L,
                        member = 0L,
                        originalName = "a.pdf",
                        storeName = "s-a.pdf",
                        uri = "uri-a",
                    ),
                )
            val participants =
                listOf(
                    Member(
                        id = 1L,
                        name = "참가자1",
                        email = "p1@test.com",
                        grade = 1,
                        classNumber = 1,
                        number = 1,
                        role = MemberRole.STUDENT,
                    ),
                )
            val existingProject =
                Project(
                    id = projectId,
                    ownerId = 0L,
                    title = "기존 제목",
                    description = "기존 설명",
                    files = files,
                    participants = participants,
                )
            val updatedProject =
                Project(
                    id = projectId,
                    ownerId = 0L,
                    title = "새 제목",
                    description = "새 설명",
                    files = files,
                    participants = participants,
                )

            every { c.projectRepo.findProjectById(projectId) } returns existingProject
            every {
                c.projectRepo.updateProject(
                    id = projectId,
                    ownerId = 0L,
                    title = "새 제목",
                    description = "새 설명",
                    fileIds = listOf(10L),
                    participantIds = listOf(1L),
                )
            } returns updatedProject
            every { c.projectRepo.findScoreIdsByProjectId(projectId) } returns listOf(1001L)

            When("execute를 호출하면") {
                val res = c.service.execute(projectId, "새 제목", "새 설명", null, null)

                Then("프로젝트가 업데이트된다") {
                    res shouldNotBe null
                    res.id shouldBe projectId
                    res.title shouldBe "새 제목"
                    res.description shouldBe "새 설명"
                    res.files.size shouldBe 1
                    res.participants.size shouldBe 1
                }

                Then("저장소 메서드가 호출된다") {
                    verify(exactly = 1) { c.projectRepo.findProjectById(projectId) }
                    verify(exactly = 1) {
                        c.projectRepo.updateProject(
                            id = projectId,
                            ownerId = 0L,
                            title = "새 제목",
                            description = "새 설명",
                            fileIds = listOf(10L),
                            participantIds = listOf(1L),
                        )
                    }
                }
            }
        }

        Given("프로젝트의 파일 목록을 업데이트할 때") {
            val c = ctx()
            val projectId = 100L
            val existingProject =
                Project(
                    id = projectId,
                    ownerId = 0L,
                    title = "프로젝트",
                    description = "설명",
                    files =
                        listOf(
                            File(
                                id = 10L,
                                member = 0L,
                                originalName = "old.pdf",
                                storeName = "s-old.pdf",
                                uri = "uri-old",
                            ),
                        ),
                    participants = emptyList(),
                )
            val updatedProject =
                Project(
                    id = projectId,
                    ownerId = 0L,
                    title = "프로젝트",
                    description = "설명",
                    files =
                        listOf(
                            File(
                                id = 20L,
                                member = 0L,
                                originalName = "new.pdf",
                                storeName = "s-new.pdf",
                                uri = "uri-new",
                            ),
                        ),
                    participants = emptyList(),
                )

            every { c.projectRepo.findProjectById(projectId) } returns existingProject
            every {
                c.projectRepo.updateProject(
                    id = projectId,
                    ownerId = 0L,
                    title = "프로젝트",
                    description = "설명",
                    fileIds = listOf(20L),
                    participantIds = emptyList(),
                )
            } returns updatedProject
            every { c.projectRepo.findScoreIdsByProjectId(projectId) } returns emptyList()

            When("execute를 호출하면") {
                val res = c.service.execute(projectId, null, null, listOf(20L), null)

                Then("파일 목록이 업데이트된다") {
                    res.files.size shouldBe 1
                    res.files[0].id shouldBe 20L
                }
            }
        }

        Given("존재하지 않는 프로젝트를 업데이트하려고 할 때") {
            val c = ctx()
            val projectId = 999L

            every { c.projectRepo.findProjectById(projectId) } returns null

            When("execute를 호출하면") {
                Then("PROJECT_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(projectId, "제목", null, null, null) }
                    ex.errorCode shouldBe ErrorCode.PROJECT_NOT_FOUND
                }
            }
        }

        Given("다른 사용자가 소유한 프로젝트를 업데이트하려고 할 때") {
            val c = ctx()
            val projectId = 100L
            val existingProject =
                Project(
                    id = projectId,
                    ownerId = 99L,
                    title = "다른 사람의 프로젝트",
                    description = "설명",
                    files = emptyList(),
                    participants = emptyList(),
                )

            every { c.projectRepo.findProjectById(projectId) } returns existingProject

            When("execute를 호출하면") {
                Then("PROJECT_FORBIDDEN 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(projectId, "새 제목", null, null, null) }
                    ex.errorCode shouldBe ErrorCode.PROJECT_FORBIDDEN
                }

                Then("프로젝트 업데이트는 호출되지 않는다") {
                    verify(exactly = 0) { c.projectRepo.updateProject(any(), any(), any(), any(), any(), any()) }
                }
            }
        }

        Given("일부 필드만 업데이트할 때") {
            val c = ctx()
            val projectId = 100L
            val existingProject =
                Project(
                    id = projectId,
                    ownerId = 0L,
                    title = "기존 제목",
                    description = "기존 설명",
                    files = emptyList(),
                    participants = emptyList(),
                )
            val updatedProject =
                Project(
                    id = projectId,
                    ownerId = 0L,
                    title = "새 제목",
                    description = "기존 설명",
                    files = emptyList(),
                    participants = emptyList(),
                )

            every { c.projectRepo.findProjectById(projectId) } returns existingProject
            every {
                c.projectRepo.updateProject(
                    id = projectId,
                    ownerId = 0L,
                    title = "새 제목",
                    description = "기존 설명",
                    fileIds = emptyList(),
                    participantIds = emptyList(),
                )
            } returns updatedProject
            every { c.projectRepo.findScoreIdsByProjectId(projectId) } returns emptyList()

            When("execute를 호출하면") {
                val res = c.service.execute(projectId, "새 제목", null, null, null)

                Then("제목만 업데이트되고 나머지는 유지된다") {
                    res.title shouldBe "새 제목"
                    res.description shouldBe "기존 설명"
                }
            }
        }
    })
