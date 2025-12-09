package com.team.incube.gsmc.v3.service.category

import com.team.incube.gsmc.v3.domain.category.service.impl.FindAllCategoryServiceImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

class FindAllCategoryServiceTest :
    BehaviorSpec({
        Given("전체 카테고리 조회 서비스") {
            val service = FindAllCategoryServiceImpl()

            When("execute를 호출하면") {
                val res = service.execute()

                Then("모든 카테고리가 반환된다") {
                    res.categories.size shouldBeGreaterThan 0
                }

                Then("모든 카테고리가 영어 이름을 가진다") {
                    res.categories.all { it.englishName.isNotBlank() } shouldBe true
                }

                Then("모든 카테고리가 한글 이름을 가진다") {
                    res.categories.all { it.koreanName.isNotBlank() } shouldBe true
                }
            }
        }

        Given("카테고리 데이터 검증") {
            val service = FindAllCategoryServiceImpl()

            When("execute를 호출하면") {
                val res = service.execute()

                Then("자격증 카테고리가 포함된다") {
                    res.categories.any { it.koreanName.contains("자격증") } shouldBe true
                }

                Then("TOEIC 카테고리가 포함된다") {
                    res.categories.any { it.englishName.contains("TOEIC") } shouldBe true
                }

                Then("프로젝트 참여 카테고리가 포함된다") {
                    res.categories.any { it.koreanName.contains("프로젝트") } shouldBe true
                }

                Then("수상경력 카테고리가 포함된다") {
                    res.categories.any { it.koreanName.contains("수상") || it.englishName.contains("AWARD", ignoreCase = true) } shouldBe true
                }
            }
        }
    })
