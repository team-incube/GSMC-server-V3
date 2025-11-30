package com.team.incube.gsmc.v3.service.project

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.project.dto.Project
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.impl.FindMyProjectsServiceImpl
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class FindMyProjectsServiceTest :
    BehaviorSpec({
        data class TestData(
            val projectRepo: ProjectExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: FindMyProjectsServiceImpl,
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

            val service = FindMyProjectsServiceImpl(projectRepo, currentMemberProvider)
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

        Given("내가 참여한 프로젝트 목록을 조회할 때") {
            val c = ctx()
            val files =
                listOf(
                    File(
                        id = 10L,
                        member = 1L,
                        originalName = "a.pdf",
                        storeName = "s-a.pdf",
                        uri = "uri-a",
                    ),
                )
            val participants =
                listOf(
                    Member(
                        id = 0L,
                        name = "Test User",
                        email = "test@test.com",
                        grade = 1,
                        classNumber = 1,
                        number = 1,
                        role = MemberRole.STUDENT,
                    ),
                    Member(
                        id = 1L,
                        name = "참가자1",
                        email = "p1@test.com",
                        grade = 1,
                        classNumber = 1,
                        number = 2,
                        role = MemberRole.STUDENT,
                    ),
                )
            val projects =
                listOf(
                    Project(
                        id = 100L,
                        ownerId = 1L,
                        title = "프로젝트1",
                        description = "설명1",
                        files = files,
                        participants = participants,
                    ),
                    Project(
                        id = 101L,
                        ownerId = 0L,
                        title = "프로젝트2",
                        description = "설명2",
                        files = emptyList(),
                        participants = participants,
                    ),
                )

            every { c.projectRepo.findProjectsByParticipantId(0L) } returns projects
            every { c.projectRepo.findScoreIdsByProjectId(100L) } returns listOf(1001L, 1002L)
            every { c.projectRepo.findScoreIdsByProjectId(101L) } returns listOf(1003L)

            When("execute를 호출하면") {
                val res = c.service.execute()

                Then("내가 참여한 프로젝트 목록이 반환된다") {
                    res.size shouldBe 2
                    res[0].id shouldBe 100L
                    res[0].title shouldBe "프로젝트1"
                    res[0].description shouldBe "설명1"
                    res[0].files.size shouldBe 1
                    res[0].participants.size shouldBe 2
                    res[0].scoreIds.size shouldBe 2
                    res[1].id shouldBe 101L
                    res[1].title shouldBe "프로젝트2"
                    res[1].files.size shouldBe 0
                    res[1].scoreIds.size shouldBe 1
                }

                Then("저장소 메서드가 호출된다") {
                    verify(exactly = 1) { c.projectRepo.findProjectsByParticipantId(0L) }
                    verify(exactly = 1) { c.projectRepo.findScoreIdsByProjectId(100L) }
                    verify(exactly = 1) { c.projectRepo.findScoreIdsByProjectId(101L) }
                }
            }
        }

        Given("참여한 프로젝트가 없을 때") {
            val c = ctx()

            every { c.projectRepo.findProjectsByParticipantId(0L) } returns emptyList()

            When("execute를 호출하면") {
                val res = c.service.execute()

                Then("빈 목록이 반환된다") {
                    res.size shouldBe 0
                }

                Then("저장소 메서드가 호출된다") {
                    verify(exactly = 1) { c.projectRepo.findProjectsByParticipantId(0L) }
                }
            }
        }

        Given("내가 소유자인 프로젝트를 조회할 때") {
            val c = ctx()
            val participants =
                listOf(
                    Member(
                        id = 0L,
                        name = "Test User",
                        email = "test@test.com",
                        grade = 1,
                        classNumber = 1,
                        number = 1,
                        role = MemberRole.STUDENT,
                    ),
                )
            val projects =
                listOf(
                    Project(
                        id = 100L,
                        ownerId = 0L,
                        title = "내 프로젝트",
                        description = "내가 만든 프로젝트",
                        files = emptyList(),
                        participants = participants,
                    ),
                )

            every { c.projectRepo.findProjectsByParticipantId(0L) } returns projects
            every { c.projectRepo.findScoreIdsByProjectId(100L) } returns emptyList()

            When("execute를 호출하면") {
                val res = c.service.execute()

                Then("내가 소유자인 프로젝트도 조회된다") {
                    res.size shouldBe 1
                    res[0].ownerId shouldBe 0L
                }
            }
        }
    })
