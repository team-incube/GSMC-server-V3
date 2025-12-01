package com.team.incube.gsmc.v3.service.project

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.project.dto.Project
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectResponse
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.impl.CreateProjectServiceImpl
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
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

class CreateProjectServiceTest :
    BehaviorSpec({
        data class TestData(
            val projectRepo: ProjectExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: CreateProjectServiceImpl,
        )

        fun ctx(): TestData {
            val projectRepo = mockk<ProjectExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()

            every { currentMemberProvider.getCurrentMember() } returns
                Member(
                    id = 1L,
                    name = "Test User",
                    email = "test@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val service = CreateProjectServiceImpl(projectRepo, currentMemberProvider)
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

        Given("유효한 프로젝트 정보로 생성할 때") {
            val c = ctx()
            val title = "Test Project"
            val description = "Test Description"
            val fileIds = listOf(10L, 11L)
            val participantIds = listOf(1L, 2L)
            val files =
                listOf(
                    File(
                        id = 10L,
                        member = 1L,
                        originalName = "file1.pdf",
                        storeName = "stored-file1.pdf",
                        uri = "uri-1",
                    ),
                    File(
                        id = 11L,
                        member = 1L,
                        originalName = "file2.pdf",
                        storeName = "stored-file2.pdf",
                        uri = "uri-2",
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
            val savedProject =
                Project(
                    id = 100L,
                    ownerId = 1L,
                    title = title,
                    description = description,
                    files = files,
                    participants = participants,
                )

            every {
                c.projectRepo.saveProject(
                    ownerId = 1L,
                    title = title,
                    description = description,
                    fileIds = fileIds,
                    participantIds = participantIds,
                )
            } returns savedProject

            every { c.projectRepo.findScoreIdsByProjectId(100L) } returns emptyList()

            When("execute를 호출하면") {
                val res: GetProjectResponse = c.service.execute(title, description, fileIds, participantIds)

                Then("정상적으로 생성되어 응답이 반환된다") {
                    res shouldNotBe null
                    res.id shouldBe 100L
                    res.ownerId shouldBe 1L
                    res.title shouldBe title
                    res.description shouldBe description
                    res.files.size shouldBe 2
                    res.participants.size shouldBe 2
                    res.participants[0].id shouldBe 1L
                    res.participants[1].id shouldBe 2L
                }

                Then("프로젝트 저장과 점수 조회가 호출된다") {
                    verify(exactly = 1) {
                        c.projectRepo.saveProject(
                            ownerId = 1L,
                            title = title,
                            description = description,
                            fileIds = fileIds,
                            participantIds = participantIds,
                        )
                    }
                    verify(exactly = 1) { c.projectRepo.findScoreIdsByProjectId(100L) }
                }
            }
        }
    })
