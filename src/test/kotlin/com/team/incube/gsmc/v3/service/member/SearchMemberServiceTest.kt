package com.team.incube.gsmc.v3.service.member

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.dto.constant.SortDirection
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.member.service.impl.SearchMemberServiceImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class SearchMemberServiceTest :
    BehaviorSpec({
        data class TestData(
            val memberRepo: MemberExposedRepository,
            val service: SearchMemberServiceImpl,
        )

        fun ctx(): TestData {
            val memberRepo = mockk<MemberExposedRepository>()
            val service = SearchMemberServiceImpl(memberRepo)
            return TestData(memberRepo, service)
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

        Given("멤버를 검색할 때") {
            val c = ctx()
            val pageable = PageRequest.of(0, 10)
            val members =
                listOf(
                    Member(
                        id = 1L,
                        name = "홍길동",
                        email = "hong@gsm.hs.kr",
                        grade = 1,
                        classNumber = 1,
                        number = 1,
                        role = MemberRole.STUDENT,
                    ),
                    Member(
                        id = 2L,
                        name = "김철수",
                        email = "kim@gsm.hs.kr",
                        grade = 1,
                        classNumber = 1,
                        number = 2,
                        role = MemberRole.STUDENT,
                    ),
                )
            val page = PageImpl(members, pageable, 2)

            every {
                c.memberRepo.searchMembers(
                    email = null,
                    name = null,
                    role = null,
                    grade = null,
                    classNumber = null,
                    number = null,
                    sortBy = SortDirection.ASC,
                    pageable = pageable,
                )
            } returns page

            When("execute를 호출하면") {
                val res =
                    c.service.execute(
                        email = null,
                        name = null,
                        role = null,
                        grade = null,
                        classNumber = null,
                        number = null,
                        sortBy = SortDirection.ASC,
                        pageable = pageable,
                    )

                Then("검색 결과가 반환된다") {
                    res.totalPages shouldBe 1
                    res.totalElements shouldBe 2
                    res.members.size shouldBe 2
                    res.members[0].id shouldBe 1L
                    res.members[0].name shouldBe "홍길동"
                    res.members[1].id shouldBe 2L
                    res.members[1].name shouldBe "김철수"
                }

                Then("저장소 메서드가 호출된다") {
                    verify(exactly = 1) {
                        c.memberRepo.searchMembers(
                            email = null,
                            name = null,
                            role = null,
                            grade = null,
                            classNumber = null,
                            number = null,
                            sortBy = SortDirection.ASC,
                            pageable = pageable,
                        )
                    }
                }
            }
        }

        Given("이름으로 필터링하여 검색할 때") {
            val c = ctx()
            val pageable = PageRequest.of(0, 10)
            val members =
                listOf(
                    Member(
                        id = 1L,
                        name = "홍길동",
                        email = "hong@gsm.hs.kr",
                        grade = 1,
                        classNumber = 1,
                        number = 1,
                        role = MemberRole.STUDENT,
                    ),
                )
            val page = PageImpl(members, pageable, 1)

            every {
                c.memberRepo.searchMembers(
                    email = null,
                    name = "홍길동",
                    role = null,
                    grade = null,
                    classNumber = null,
                    number = null,
                    sortBy = SortDirection.ASC,
                    pageable = pageable,
                )
            } returns page

            When("execute를 호출하면") {
                val res =
                    c.service.execute(
                        email = null,
                        name = "홍길동",
                        role = null,
                        grade = null,
                        classNumber = null,
                        number = null,
                        sortBy = SortDirection.ASC,
                        pageable = pageable,
                    )

                Then("필터링된 결과가 반환된다") {
                    res.members.size shouldBe 1
                    res.members[0].name shouldBe "홍길동"
                }
            }
        }

        Given("역할로 필터링하여 검색할 때") {
            val c = ctx()
            val pageable = PageRequest.of(0, 10)
            val members =
                listOf(
                    Member(
                        id = 100L,
                        name = "김선생",
                        email = "teacher@gsm.hs.kr",
                        grade = 0,
                        classNumber = 0,
                        number = 0,
                        role = MemberRole.TEACHER,
                    ),
                )
            val page = PageImpl(members, pageable, 1)

            every {
                c.memberRepo.searchMembers(
                    email = null,
                    name = null,
                    role = MemberRole.TEACHER,
                    grade = null,
                    classNumber = null,
                    number = null,
                    sortBy = SortDirection.ASC,
                    pageable = pageable,
                )
            } returns page

            When("execute를 호출하면") {
                val res =
                    c.service.execute(
                        email = null,
                        name = null,
                        role = MemberRole.TEACHER,
                        grade = null,
                        classNumber = null,
                        number = null,
                        sortBy = SortDirection.ASC,
                        pageable = pageable,
                    )

                Then("교사만 반환된다") {
                    res.members.size shouldBe 1
                    res.members[0].role shouldBe MemberRole.TEACHER
                }
            }
        }

        Given("학년, 반, 번호로 필터링하여 검색할 때") {
            val c = ctx()
            val pageable = PageRequest.of(0, 10)
            val members =
                listOf(
                    Member(
                        id = 1L,
                        name = "홍길동",
                        email = "hong@gsm.hs.kr",
                        grade = 2,
                        classNumber = 1,
                        number = 5,
                        role = MemberRole.STUDENT,
                    ),
                )
            val page = PageImpl(members, pageable, 1)

            every {
                c.memberRepo.searchMembers(
                    email = null,
                    name = null,
                    role = null,
                    grade = 2,
                    classNumber = 1,
                    number = 5,
                    sortBy = SortDirection.ASC,
                    pageable = pageable,
                )
            } returns page

            When("execute를 호출하면") {
                val res =
                    c.service.execute(
                        email = null,
                        name = null,
                        role = null,
                        grade = 2,
                        classNumber = 1,
                        number = 5,
                        sortBy = SortDirection.ASC,
                        pageable = pageable,
                    )

                Then("해당 학생만 반환된다") {
                    res.members.size shouldBe 1
                    res.members[0].grade shouldBe 2
                    res.members[0].classNumber shouldBe 1
                    res.members[0].number shouldBe 5
                }
            }
        }

        Given("검색 결과가 없을 때") {
            val c = ctx()
            val pageable = PageRequest.of(0, 10)
            val page = PageImpl<Member>(emptyList(), pageable, 0)

            every {
                c.memberRepo.searchMembers(
                    email = null,
                    name = "존재하지않음",
                    role = null,
                    grade = null,
                    classNumber = null,
                    number = null,
                    sortBy = SortDirection.ASC,
                    pageable = pageable,
                )
            } returns page

            When("execute를 호출하면") {
                val res =
                    c.service.execute(
                        email = null,
                        name = "존재하지않음",
                        role = null,
                        grade = null,
                        classNumber = null,
                        number = null,
                        sortBy = SortDirection.ASC,
                        pageable = pageable,
                    )

                Then("빈 결과가 반환된다") {
                    res.totalElements shouldBe 0
                    res.members.size shouldBe 0
                }
            }
        }

        Given("내림차순으로 정렬할 때") {
            val c = ctx()
            val pageable = PageRequest.of(0, 10)
            val members =
                listOf(
                    Member(
                        id = 2L,
                        name = "홍길동",
                        email = "hong@gsm.hs.kr",
                        grade = 1,
                        classNumber = 1,
                        number = 2,
                        role = MemberRole.STUDENT,
                    ),
                    Member(
                        id = 1L,
                        name = "김철수",
                        email = "kim@gsm.hs.kr",
                        grade = 1,
                        classNumber = 1,
                        number = 1,
                        role = MemberRole.STUDENT,
                    ),
                )
            val page = PageImpl(members, pageable, 2)

            every {
                c.memberRepo.searchMembers(
                    email = null,
                    name = null,
                    role = null,
                    grade = null,
                    classNumber = null,
                    number = null,
                    sortBy = SortDirection.DESC,
                    pageable = pageable,
                )
            } returns page

            When("execute를 호출하면") {
                val res =
                    c.service.execute(
                        email = null,
                        name = null,
                        role = null,
                        grade = null,
                        classNumber = null,
                        number = null,
                        sortBy = SortDirection.DESC,
                        pageable = pageable,
                    )

                Then("내림차순 정렬된 결과가 반환된다") {
                    res.members.size shouldBe 2
                    res.members[0].id shouldBe 2L
                    res.members[1].id shouldBe 1L
                }
            }
        }
    })
