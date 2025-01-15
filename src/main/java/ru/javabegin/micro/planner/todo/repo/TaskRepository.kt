package ru.javabegin.micro.planner.todo.repo

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.javabegin.micro.planner.entity.Task
import java.util.*

// принцип ООП: абстракция-реализация - здесь описываем все доступные способы доступа к данным
@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    @Query(
        ("SELECT t FROM Task t where " +
                "(:title is null or :title='' or lower(t.title) like lower(concat('%', :title,'%'))) and" +
                "(:completed is null or t.completed=:completed) and " +  // учитываем, что параметр может быть null или пустым
                "(:priorityId is null or t.priority.id=:priorityId) and " +
                "(:categoryId is null or t.category.id=:categoryId) and " +
                "(:categoryId is null or t.category.id=:categoryId) and " +
                "(" +
                "(cast(:dateFrom as timestamp) is null or t.taskDate>=:dateFrom) and " +
                "(cast(:dateTo as timestamp) is null or t.taskDate<=:dateTo)" +
                ") and " +
                "(t.userId=:userId)")
    )
    fun findByParams(
        @Param("title") title: String?,
        @Param("completed") completed: Boolean?,
        @Param("priorityId") priorityId: Long?,
        @Param("categoryId") categoryId: Long?,
        @Param("userId") userId: Long,
        @Param("dateFrom") dateFrom: Date?,
        @Param("dateTo") dateTo: Date?,
        pageable: Pageable
    ): Page<Task>


    // поиск всех задач конкретного пользователя
    fun findByUserIdOrderByTitleAsc(userId: Long): List<Task>
}
