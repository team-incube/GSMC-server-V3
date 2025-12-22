package com.team.incube.gsmc.v3.service.developer

import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.developer.service.impl.DeleteMemberByEmailServiceImpl
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.project.dto.Project
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

class DeleteMemberByEmailServiceTest :
    BehaviorSpec({
        data class TestData(
            val memberRepo: MemberExposedRepository,
            val scoreRepo: ScoreExposedRepository,
            val evidenceRepo: EvidenceExposedRepository,
            val alertRepo: AlertExposedRepository,
            val fileRepo: FileExposedRepository,
            val s3DeleteService: S3DeleteService,
            val projectRepo: ProjectExposedRepository,
            val service: DeleteMemberByEmailServiceImpl,
        )

        fun ctx(): TestData {
            val memberRepo = mockk<MemberExposedRepository>()
            val scoreRepo = mockk<ScoreExposedRepository>()
            val evidenceRepo = mockk<EvidenceExposedRepository>()
            val alertRepo = mockk<AlertExposedRepository>(relaxed = true)
            val fileRepo = mockk<FileExposedRepository>()
            val s3DeleteService = mockk<S3DeleteService>(relaxed = true)
            val projectRepo = mockk<ProjectExposedRepository>()
            val service =
                DeleteMemberByEmailServiceImpl(
                    memberRepo,
                    scoreRepo,
                    alertRepo,
                    evidenceRepo,
                    fileRepo,
                    projectRepo,
                    s3DeleteService,
                )
            return TestData(
                memberRepo,
                scoreRepo,
                evidenceRepo,
                alertRepo,
                fileRepo,
                s3DeleteService,
                projectRepo,
                service,
            )
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

        Given("존재하는 이메일로 회원을 삭제할 때") {
            val c = ctx()
            val email = "test@test.com"
            val member =
                Member(
                    id = 1L,
                    name = "Test User",
                    email = email,
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            every { c.memberRepo.findByEmail(email) } returns member
            every { c.scoreRepo.findAllByMemberId(member.id) } returns emptyList()
            every { c.fileRepo.findAllByUserId(member.id) } returns emptyList()
            every { c.memberRepo.deleteMemberByEmail(email) } returns 1
            every { c.projectRepo.findProjectsByOwnerId(member.id) } returns emptyList()

            When("execute를 호출하면") {
                c.service.execute(email)

                Then("회원 관련 데이터가 삭제된다") {
                    verify(exactly = 1) { c.alertRepo.deleteAllByMemberId(member.id) }
                    verify(exactly = 1) { c.memberRepo.deleteMemberByEmail(email) }
                }
            }
        }

        Given("존재하지 않는 이메일로 회원을 삭제하려고 할 때") {
            val c = ctx()
            val email = "none@test.com"

            every { c.memberRepo.findByEmail(email) } returns null

            When("execute를 호출하면") {
                Then("MEMBER_NOT_FOUND 예외가 발생한다") {
                    val ex =
                        shouldThrow<GsmcException> {
                            c.service.execute(email)
                        }
                    ex.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
                }
            }
        }
        Given("점수가 존재하는 회원을 삭제할 때") {
            val c = ctx()
            val email = "test@test.com"

            val member =
                Member(
                    id = 1L,
                    name = "Test User",
                    email = email,
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val score1 =
                Score(
                    id = 10L,
                    member = member,
                    categoryType = CategoryType.TOEIC_ACADEMY,
                    status = ScoreStatus.APPROVED,
                    sourceId = null,
                    activityName = null,
                    scoreValue = null,
                    rejectionReason = null,
                    updatedAt = null,
                )

            val score2 =
                Score(
                    id = 20L,
                    member = member,
                    categoryType = CategoryType.VOLUNTEER,
                    status = ScoreStatus.APPROVED,
                    sourceId = null,
                    activityName = null,
                    scoreValue = null,
                    rejectionReason = null,
                    updatedAt = null,
                )

            every { c.memberRepo.findByEmail(email) } returns member
            every { c.scoreRepo.findAllByMemberId(member.id) } returns listOf(score1, score2)
            every { c.fileRepo.findAllByUserId(member.id) } returns emptyList()
            every { c.scoreRepo.deleteAllByIdIn(listOf(10L, 20L)) } just runs
            every { c.memberRepo.deleteMemberByEmail(email) } returns 1
            every { c.projectRepo.findProjectsByOwnerId(member.id) } returns emptyList()

            When("execute를 호출하면") {
                c.service.execute(email)

                Then("점수와 연관된 알림이 삭제된다") {
                    verify(exactly = 1) {
                        c.alertRepo.deleteAllByScoreIdIn(listOf(10L, 20L))
                    }
                }

                Then("점수가 삭제된다") {
                    verify(exactly = 1) {
                        c.scoreRepo.deleteAllByIdIn(listOf(10L, 20L))
                    }
                }

                Then("회원이 삭제된다") {
                    verify(exactly = 1) {
                        c.memberRepo.deleteMemberByEmail(email)
                    }
                }
            }
        }

        Given("회원이 소유한 프로젝트가 존재할 때") {
            val c = ctx()
            val email = "test@test.com"

            val member =
                Member(
                    id = 1L,
                    name = "Test User",
                    email = email,
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val project1 =
                mockk<Project> {
                    every { id } returns 100L
                }
            val project2 =
                mockk<Project> {
                    every { id } returns 200L
                }

            every { c.memberRepo.findByEmail(email) } returns member
            every { c.scoreRepo.findAllByMemberId(member.id) } returns emptyList()
            every { c.fileRepo.findAllByUserId(member.id) } returns emptyList()
            every { c.projectRepo.findProjectsByOwnerId(member.id) } returns listOf(project1, project2)
            every { c.projectRepo.deleteProjectById(any()) } just runs
            every { c.memberRepo.deleteMemberByEmail(email) } returns 1

            When("execute를 호출하면") {
                c.service.execute(email)

                Then("소유한 프로젝트가 모두 삭제된다") {
                    verify(exactly = 1) { c.projectRepo.deleteProjectById(100L) }
                    verify(exactly = 1) { c.projectRepo.deleteProjectById(200L) }
                }
            }
        }
    })
