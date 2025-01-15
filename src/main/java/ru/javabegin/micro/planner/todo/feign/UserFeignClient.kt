package ru.javabegin.micro.planner.todo.feign

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import ru.javabegin.micro.planner.entity.User

@FeignClient(name = "planner-users", fallback = UserFeignClientFallback::class)
interface UserFeignClient {
    @PostMapping("/user/id")
    fun findUserById(@RequestBody id: Long): ResponseEntity<User?>?
}

@Component
internal class UserFeignClientFallback : UserFeignClient {
    //этот метод будет вызываться, если сервис /user/id не будет доступен
    override fun findUserById(id: Long): ResponseEntity<User?>? {
        return null
    }
}
