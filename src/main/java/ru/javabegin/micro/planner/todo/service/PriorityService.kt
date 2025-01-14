package ru.javabegin.micro.planner.todo.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import ru.javabegin.micro.planner.entity.Priority
import ru.javabegin.micro.planner.todo.repo.PriorityRepository

// всегда нужно создавать отдельный класс Service для доступа к данным, даже если кажется,
// что мало методов или это все можно реализовать сразу в контроллере
// Такой подход полезен для будущих доработок и правильной архитектуры (особенно, если работаете с транзакциями)
@Service // все методы класса должны выполниться без ошибки, чтобы транзакция завершилась
// если в методе возникнет исключение - все выполненные операции из данного метода откатятся (Rollback)
@Transactional
class PriorityService(// сервис имеет право обращаться к репозиторию (БД)
    private val repository: PriorityRepository
) {
    fun findAll(userId: Long): List<Priority> {
        return repository.findByUserIdOrderByIdAsc(userId)
    }

    fun add(priority: Priority): Priority {
        return repository.save(priority) // метод save обновляет или создает новый объект, если его не было
    }

    fun update(priority: Priority): Priority {
        return repository.save(priority) // метод save обновляет или создает новый объект, если его не было
    }

    fun deleteById(id: Long) {
        repository.deleteById(id)
    }

    fun findById(id: Long): Priority {
        return repository.findById(id).get() // т.к. возвращается Optional - можно получить объект методом get()
    }

    fun find(title: String?, userId: Long): List<Priority> {
        return repository.findByTitle(title, userId)
    }
}
