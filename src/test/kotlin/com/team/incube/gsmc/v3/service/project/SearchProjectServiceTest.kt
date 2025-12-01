package com.team.incube.gsmc.v3.service.project

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.project.dto.Project
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.impl.SearchProjectServiceImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class SearchProjectServiceTest :
    BehaviorSpec({
        data class TestData(
            val projectRepo: ProjectExposedRepository,
            val service: SearchProjectServiceImpl,
        )

        fun ctx(): TestData {
            val projectRepo = mockk<ProjectExposedRepository>()
            val service = SearchProjectServiceImpl(projectRepo)
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

        Given("제목으로 프로젝트를 검색할 때") {
            val c = ctx()
            val pageable = PageRequest.of(0, 10)
            val projects =
                listOf(
                    Project(
                        id = 1L,
                        ownerId = 1L,
                        title = "프로젝트1",
                        description = "설명1",
                        files = emptyList(),
                        participants = listOf(Member(1L, "사용자1", "u1@test.com", 1, 1, 1, MemberRole.STUDENT)),
                    ),
                )
            val page = PageImpl(projects, pageable, 1)

            every { c.projectRepo.searchProjects("프로젝트", pageable) } returns page
            every { c.projectRepo.findScoreIdsByProjectId(1L) } returns listOf(100L)

            When("execute를 호출하면") {
                val res = c.service.execute("프로젝트", pageable)

                Then("검색 결과가 반환된다") {
                    res.projects.size shouldBe 1
                    res.totalPages shouldBe 1
                    res.totalElements shouldBe 1
                }
            }
        }

        Given("검색 결과가 없을 때") {
            val c = ctx()
            val pageable = PageRequest.of(0, 10)
            val page = PageImpl<Project>(emptyList(), pageable, 0)

            every { c.projectRepo.searchProjects(null, pageable) } returns page

            When("execute를 호출하면") {
                val res = c.service.execute(null, pageable)

                Then("빈 결과가 반환된다") {
                    res.projects.size shouldBe 0
                    res.totalElements shouldBe 0
                }
            }
        }
    })
