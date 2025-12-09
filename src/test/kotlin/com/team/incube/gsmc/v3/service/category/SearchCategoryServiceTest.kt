package com.team.incube.gsmc.v3.service.category

import com.team.incube.gsmc.v3.domain.category.service.impl.SearchCategoryServiceImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

class SearchCategoryServiceTest :
    BehaviorSpec({
        Given("카테고리 검색 서비스") {
            val service = SearchCategoryServiceImpl()

            When("키워드 없이 검색하면") {
                val res = service.execute(null)

                Then("모든 카테고리가 반환된다") {
                    res.categories.size shouldBeGreaterThan 0
                }
            }

            When("'자격증'으로 검색하면") {
                val res = service.execute("자격증")

                Then("자격증 카테고리가 포함된다") {
                    res.categories.any { it.koreanName.contains("자격증") } shouldBe true
                }
            }

            When("'TOEIC'으로 검색하면") {
                val res = service.execute("TOEIC")

                Then("TOEIC 카테고리가 포함된다") {
                    res.categories.any { it.englishName.contains("TOEIC") } shouldBe true
                }
            }

            When("'certificate'로 검색하면") {
                val res = service.execute("certificate")

                Then("영어 이름에 certificate가 포함된 카테고리가 반환된다") {
                    res.categories.any { it.englishName.contains("CERTIFICATE", ignoreCase = true) } shouldBe true
                }
            }

            When("존재하지 않는 키워드로 검색하면") {
                val res = service.execute("존재하지않는키워드12345")

                Then("빈 목록이 반환된다") {
                    res.categories.size shouldBe 0
                }
            }

            When("부분 일치하는 키워드로 검색하면") {
                val res = service.execute("프로젝트")

                Then("프로젝트 관련 카테고리가 반환된다") {
                    res.categories.any { it.koreanName.contains("프로젝트") } shouldBe true
                }
            }
        }
    })
