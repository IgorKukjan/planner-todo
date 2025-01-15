package ru.javabegin.micro.planner.todo.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import ru.javabegin.micro.planner.entity.Stat
import ru.javabegin.micro.planner.todo.repo.StatRepository

// всегда нужно создавать отдельный класс Service для доступа к данным, даже если кажется,
// что мало методов или это все можно реализовать сразу в контроллере
// Такой подход полезен для будущих доработок и правильной архитектуры (особенно, если работаете с транзакциями)
@Service // все методы класса должны выполниться без ошибки, чтобы транзакция завершилась
// если в методе возникнет исключение - все выполненные операции откатятся (Rollback)
@Transactional
class StatService(// сервис имеет право обращаться к репозиторию (БД)
    private val repository: StatRepository
) {
    fun findStat(userId: Long): Stat? {
        return repository.findByUserId(userId)
    }
}
