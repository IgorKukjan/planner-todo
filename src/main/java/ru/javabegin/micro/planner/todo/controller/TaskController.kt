package ru.javabegin.micro.planner.todo.controller

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.javabegin.micro.planner.entity.Task
import ru.javabegin.micro.planner.todo.search.TaskSearchValues
import ru.javabegin.micro.planner.todo.service.TaskService
import ru.javabegin.micro.planner.utils.rest.resttemplate.UserRestBuilder
import ru.javabegin.micro.planner.utils.rest.webclient.UserWebClientBuilder
import java.text.ParseException
import java.util.*

/*

Чтобы дать меньше шансов для взлома (например, CSRF атак): POST/PUT запросы могут изменять/фильтровать закрытые данные, а GET запросы - для получения незащищенных данных
Т.е. GET-запросы не должны использоваться для изменения/получения секретных данных

Если возникнет exception - вернется код  500 Internal Server Error, поэтому не нужно все действия оборачивать в try-catch

Используем @RestController вместо обычного @Controller, чтобы все ответы сразу оборачивались в JSON,
иначе пришлось бы добавлять лишние объекты в код, использовать @ResponseBody для ответа, указывать тип отправки JSON

Названия методов могут быть любыми, главное не дублировать их имена и URL mapping

*/
@RestController
@RequestMapping("/task") // базовый URI
class TaskController(// сервис для доступа к данным (напрямую к репозиториям не обращаемся)
    private val taskService: TaskService, //микросервис для работы с пользователем
    private val userRestBuilder: UserRestBuilder,
    private val userWebClientBuilder: UserWebClientBuilder
) {

    companion object {
        const val ID_COLUMN: String = "id" // имя столбца id
    }
    // получение всех данных
    @PostMapping("/all")
    fun findAll(@RequestBody userId: Long): ResponseEntity<List<Task>> {
        return ResponseEntity.ok(taskService.findAll(userId)) // поиск всех задач конкретного пользователя
    }

    // добавление
    @PostMapping("/add")
    fun add(@RequestBody task: Task): ResponseEntity<Any> {
        // проверка на обязательные параметры

        if (task.id != null && task.id != 0L) {
            // id создается автоматически в БД (autoincrement), поэтому его передавать не нужно, иначе может быть конфликт уникальности значения
            return ResponseEntity<Any>("redundant param: id MUST be null", HttpStatus.NOT_ACCEPTABLE)
        }

        // если передали пустое значение title
        if (task.title == null || task.title!!.trim { it <= ' ' }.length == 0) {
            return ResponseEntity<Any>("missed param: title", HttpStatus.NOT_ACCEPTABLE)
        }

        //есть ли такой пользователь
//        if(userRestBuilder.userExists(task.getUserId())){
//            return ResponseEntity.ok(taskService.add(task)); // возвращаем добавленный объект с заполненным ID
//        }
        if (userWebClientBuilder.userExists(task.userId!!)) {
            return ResponseEntity.ok(taskService.add(task)) // возвращаем добавленный объект с заполненным ID
        }

        //если пользователя не существует
        return ResponseEntity<Any>("user id =" + task.userId + " not found", HttpStatus.NOT_FOUND)
    }


    // обновление
    @PutMapping("/update")
    fun update(@RequestBody task: Task): ResponseEntity<Any> {
        // проверка на обязательные параметры

        if (task.id == null || task.id == 0L) {
            return ResponseEntity<Any>("missed param: id", HttpStatus.NOT_ACCEPTABLE)
        }

        // если передали пустое значение title
        if (task.title == null || task.title!!.trim { it <= ' ' }.length == 0) {
            return ResponseEntity<Any>("missed param: title", HttpStatus.NOT_ACCEPTABLE)
        }


        // save работает как на добавление, так и на обновление
        taskService.update(task)

        return ResponseEntity<Any>(HttpStatus.OK) // просто отправляем статус 200 (операция прошла успешно)
    }


    // для удаления используем типа запроса put, а не delete, т.к. он позволяет передавать значение в body, а не в адресной строке
    @DeleteMapping("/delete/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Any> {
        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус

        try {
            taskService.deleteById(id)
        } catch (e: EmptyResultDataAccessException) {
            e.printStackTrace()
            return ResponseEntity<Any>("id=$id not found", HttpStatus.NOT_ACCEPTABLE)
        }
        return ResponseEntity<Any>(HttpStatus.OK) // просто отправляем статус 200 (операция прошла успешно)
    }


    // получение объекта по id
    @PostMapping("/id")
    fun findById(@RequestBody id: Long): ResponseEntity<Any> {
        var task: Task? =

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
             taskService.findById(id)
        } catch (e: NoSuchElementException) { // если объект не будет найден
            e.printStackTrace()
            return ResponseEntity<Any>("id=$id not found", HttpStatus.NOT_ACCEPTABLE)
        }

        return ResponseEntity.ok(task)
    }


    // поиск по любым параметрам TaskSearchValues
    @PostMapping("/search")
    @Throws(ParseException::class)
    fun search(@RequestBody taskSearchValues: TaskSearchValues): ResponseEntity<Any> {
        // исключить NullPointerException

        val title = if (taskSearchValues.title != null) taskSearchValues.title else null

        // конвертируем Boolean в Integer
        val completed = if (taskSearchValues.completed != null && taskSearchValues.completed == 1) true else false

        val priorityId = if (taskSearchValues.priorityId != null) taskSearchValues.priorityId else null
        val categoryId = if (taskSearchValues.categoryId != null) taskSearchValues.categoryId else null

        val sortColumn: String = taskSearchValues.sortColumn
        val sortDirection: String = taskSearchValues.sortDirection

        val pageNumber = taskSearchValues.pageNumber
        val pageSize = taskSearchValues.pageSize

        val userId = taskSearchValues.userId // для показа задач только этого пользователя

        // проверка на обязательные параметры
        if (userId == null || userId == 0L) {
            return ResponseEntity<Any>("missed param: user id", HttpStatus.NOT_ACCEPTABLE)
        }


        // чтобы захватить в выборке все задачи по датам, независимо от времени - можно выставить время с 00:00 до 23:59
        var dateFrom: Date? = null
        var dateTo: Date? = null


        // выставить 00:01 для начальной даты (если она указана)
        if (taskSearchValues.dateFrom != null) {
            val calendarFrom = Calendar.getInstance()
            calendarFrom.time = taskSearchValues.dateFrom
            calendarFrom[Calendar.HOUR_OF_DAY] = 0
            calendarFrom[Calendar.MINUTE] = 1
            calendarFrom[Calendar.SECOND] = 1
            calendarFrom[Calendar.MILLISECOND] = 1

            dateFrom = calendarFrom.time // записываем начальную дату с 00:01
        }


        // выставить 23:59 для конечной даты (если она указана)
        if (taskSearchValues.dateTo != null) {
            val calendarTo = Calendar.getInstance()
            calendarTo.time = taskSearchValues.dateTo
            calendarTo[Calendar.HOUR_OF_DAY] = 23
            calendarTo[Calendar.MINUTE] = 59
            calendarTo[Calendar.SECOND] = 59
            calendarTo[Calendar.MILLISECOND] = 999

            dateTo = calendarTo.time // записываем конечную дату с 23:59
        }


        // направление сортировки
        val direction =
            if (sortDirection == null || sortDirection.trim { it <= ' ' }.length == 0 || sortDirection.trim { it <= ' ' } == "asc") Sort.Direction.ASC else Sort.Direction.DESC

        /* Вторым полем для сортировки добавляем id, чтобы всегда сохранялся строгий порядок.
            Например, если у 2-х задач одинаковое значение приоритета и мы сортируем по этому полю.
            Порядок следования этих 2-х записей после выполнения запроса может каждый раз меняться, т.к. не указано второе поле сортировки.
            Поэтому и используем ID - тогда все записи с одинаковым значением приоритета будут следовать в одном порядке по ID.
         */

        // объект сортировки, который содержит стобец и направление
        val sort = Sort.by(direction, sortColumn, ID_COLUMN)

        // объект постраничности
        val pageRequest = PageRequest.of(pageNumber, pageSize, sort)

        // результат запроса с постраничным выводом
        val result =
            taskService.findByParams(title, completed, priorityId, categoryId, userId, dateFrom, dateTo, pageRequest)

        // результат запроса
        return ResponseEntity.ok(result)
    }


}
