package com.team.incube.gsmc.v3.service.project

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.project.dto.Project
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.impl.DeleteProjectServiceImpl
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
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class DeleteProjectServiceTest :
    BehaviorSpec({
        data class TestData(
            val projectRepo: ProjectExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: DeleteProjectServiceImpl,
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

            val service = DeleteProjectServiceImpl(projectRepo, currentMemberProvider)
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
            justRun { c.projectRepo.deleteProjectById(projectId) }

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
