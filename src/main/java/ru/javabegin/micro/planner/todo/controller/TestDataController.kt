package ru.javabegin.micro.planner.todo.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.javabegin.micro.planner.todo.service.TestDataService

@RestController
@RequestMapping("/data")
class TestDataController(private val testDataService: TestDataService) {
    @PostMapping("/init")
    fun init(@RequestBody userId: Long): ResponseEntity<Boolean> {
        testDataService.initTestData(userId)

        return ResponseEntity.ok(true)
    }
}
