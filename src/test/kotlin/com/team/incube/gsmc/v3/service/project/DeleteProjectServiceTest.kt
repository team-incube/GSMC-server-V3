package com.team.incube.gsmc.v3.service.project

import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.project.dto.Project
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.impl.DeleteProjectServiceImpl
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.springframework.context.ApplicationEventPublisher

class DeleteProjectServiceTest :
    BehaviorSpec({
        data class TestData(
            val projectRepo: ProjectExposedRepository,
            val scoreRepo: ScoreExposedRepository,
            val evidenceRepo: EvidenceExposedRepository,
            val fileRepo: FileExposedRepository,
            val alertRepo: AlertExposedRepository,
            val eventPublisher: ApplicationEventPublisher,
            val currentMemberProvider: CurrentMemberProvider,
            val service: DeleteProjectServiceImpl,
        )

        fun ctx(): TestData {
            val projectRepo = mockk<ProjectExposedRepository>()
            val scoreRepo = mockk<ScoreExposedRepository>()
            val evidenceRepo = mockk<EvidenceExposedRepository>()
            val fileRepo = mockk<FileExposedRepository>()
            val alertRepo = mockk<AlertExposedRepository>()
            val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
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

            val service =
                DeleteProjectServiceImpl(
                    projectExposedRepository = projectRepo,
                    currentMemberProvider = currentMemberProvider,
                    scoreExposedRepository = scoreRepo,
                    evidenceExposedRepository = evidenceRepo,
                    fileExposedRepository = fileRepo,
                    alertExposedRepository = alertRepo,
                    eventPublisher = eventPublisher,
                )
            return TestData(projectRepo, scoreRepo, evidenceRepo, fileRepo, alertRepo, eventPublisher, currentMemberProvider, service)
        }

        val mockTransaction = mockk<JdbcTransaction>(relaxed = true)

        mockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        every {
            org.jetbrains.exposed.v1.jdbc.transactions.transaction(
                db = null,
                statement = any<JdbcTransaction.() -> Any?>(),
            )
        } answers { call ->
            @Suppress("UNCHECKED_CAST")
            val block = call.invocation.args.last() as JdbcTransaction.() -> Any?
            block.invoke(mockTransaction)
        }

        afterSpec {
            unmockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        }

        Given("내가 소유한 프로젝트를 삭제할 때") {
            val c = ctx()
            val projectId = 100L
            val project =
                Project(
                    id = projectId,
                    ownerId = 0L,
                    title = "프로젝트",
                    description = "설명",
                    files = emptyList(),
                    participants = emptyList(),
                )

            every { c.projectRepo.findProjectById(projectId) } returns project
            every { c.projectRepo.findScoreIdsByProjectId(projectId) } returns emptyList()
            every { c.scoreRepo.findAllByIdIn(emptyList()) } returns emptyList()
            every { c.evidenceRepo.findAllByIdIn(emptyList()) } returns emptyList()
            every { c.alertRepo.deleteAllByScoreIdIn(emptyList()) } returns 0
            every { c.alertRepo.deleteByProjectId(projectId) } returns 0
            justRun { c.evidenceRepo.deleteAllByIdIn(emptyList()) }
            justRun { c.scoreRepo.deleteAllByIdIn(emptyList()) }
            justRun { c.projectRepo.deleteProjectById(projectId) }
            justRun { c.fileRepo.deleteAllByIdIn(emptyList()) }

            When("execute를 호출하면") {
                c.service.execute(projectId)

                Then("프로젝트가 삭제된다") {
                    verify(exactly = 1) { c.projectRepo.findProjectById(projectId) }
                    verify(exactly = 1) { c.projectRepo.deleteProjectById(projectId) }
                }
            }
        }

        Given("존재하지 않는 프로젝트를 삭제하려고 할 때") {
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

        Given("다른 사용자가 소유한 프로젝트를 삭제하려고 할 때") {
            val c = ctx()
            val projectId = 100L
            val project =
                Project(
                    id = projectId,
                    ownerId = 99L,
                    title = "다른 사람의 프로젝트",
                    description = "설명",
                    files = emptyList(),
                    participants = emptyList(),
                )

            every { c.projectRepo.findProjectById(projectId) } returns project

            When("execute를 호출하면") {
                Then("PROJECT_FORBIDDEN 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(projectId) }
                    ex.errorCode shouldBe ErrorCode.PROJECT_FORBIDDEN
                }

                Then("프로젝트 삭제는 호출되지 않는다") {
                    verify(exactly = 0) { c.projectRepo.deleteProjectById(any()) }
                }
            }
        }
    })
