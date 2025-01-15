package ru.javabegin.micro.planner.todo.controller

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.javabegin.micro.planner.entity.Category
import ru.javabegin.micro.planner.todo.feign.UserFeignClient
import ru.javabegin.micro.planner.todo.search.CategorySearchValues
import ru.javabegin.micro.planner.todo.service.CategoryService
import ru.javabegin.micro.planner.utils.rest.resttemplate.UserRestBuilder
import ru.javabegin.micro.planner.utils.rest.webclient.UserWebClientBuilder

/*

Используем @RestController вместо обычного @Controller, чтобы все ответы сразу оборачивались в JSON,
иначе пришлось бы добавлять лишние объекты в код, использовать @ResponseBody для ответа, указывать тип отправки JSON

Названия методов могут быть любыми, главное не дублировать их имена внутри класса и URL mapping

*/
@RestController
@RequestMapping("/category") // базовый URI
class CategoryController(// доступ к данным из БД
    private val categoryService: CategoryService,
    //микросервис для работы с пользователем
    private val userRestBuilder: UserRestBuilder,
    private val userWebClientBuilder: UserWebClientBuilder,
    private val userFeignClient: UserFeignClient
) {


    @PostMapping("/all")
    fun findAll(@RequestBody userId: Long): List<Category> {
        return categoryService.findAll(userId)
    }


    @PostMapping("/add")
    fun add(@RequestBody category: Category): ResponseEntity<Any> {
        // проверка на обязательные параметры

        if (category.id != null && category.id != 0L) { // это означает, что id заполнено
            // id создается автоматически в БД (autoincrement), поэтому его передавать не нужно, иначе может быть конфликт уникальности значения
            return ResponseEntity<Any>("redundant param: id MUST be null", HttpStatus.NOT_ACCEPTABLE)
        }

        // если передали пустое значение title
        if (category.title == null || category.title!!.trim { it <= ' ' }.length == 0) {
            return ResponseEntity<Any>("missed param: title MUST be not null", HttpStatus.NOT_ACCEPTABLE)
        }

        //есть ли такой пользователь
//        if(userRestBuilder.userExists(category.getUserId())){
//              return ResponseEntity.ok(categoryService.add(category)); // возвращаем добавленный объект с заполненным ID
//        }

//        if(userWebClientBuilder.userExists(category.getUserId())){
//            return ResponseEntity.ok(categoryService.add(category)); // возвращаем добавленный объект с заполненным ID
//        }

        //подписываемся на результат
//        userWebClientBuilder.userExistsAsync(category.getUserId()).subscribe(user -> System.out.println("user = " + user));

        // вызов мс через feign интерфейс
        val result = userFeignClient.findUserById(category.userId)
            ?: // если мс недоступен - вернется null
            return ResponseEntity<Any>(
                "система пользователей недоступна, попробуйте позже",
                HttpStatus.NOT_FOUND
            )

        if (result.body != null) { // если пользователь не пустой
            return ResponseEntity.ok(categoryService.add(category))
        }

        // если пользователя НЕ существует
        return ResponseEntity<Any>("user id=" + category.userId + " not found", HttpStatus.NOT_ACCEPTABLE)
    }


    @PutMapping("/update")
    fun update(@RequestBody category: Category): ResponseEntity<Any> {
        // проверка на обязательные параметры

        if (category.id == null || category.id == 0L) {
            return ResponseEntity<Any>("missed param: id", HttpStatus.NOT_ACCEPTABLE)
        }

        // если передали пустое значение title
        if (category.title == null || category.title!!.trim { it <= ' ' }.length == 0) {
            return ResponseEntity<Any>("missed param: title", HttpStatus.NOT_ACCEPTABLE)
        }

        // save работает как на добавление, так и на обновление
        categoryService.update(category)

        return ResponseEntity<Any>(HttpStatus.OK) // просто отправляем статус 200 (операция прошла успешно)
    }


    // для удаления используем тип запроса DELETE и передаем ID для удаления
    // можно также использовать метод POST и передавать ID в теле запроса
    @DeleteMapping("/delete/{id}")
    fun delete(@PathVariable("id") id: Long): ResponseEntity<Any> {
        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус

        try {
            categoryService.deleteById(id)
        } catch (e: EmptyResultDataAccessException) {
            e.printStackTrace()
            return ResponseEntity<Any>("id=$id not found", HttpStatus.NOT_ACCEPTABLE)
        }

        return ResponseEntity<Any>(HttpStatus.OK) // просто отправляем статус 200 без объектов (операция прошла успешно)
    }


    // поиск по любым параметрам CategorySearchValues
    @PostMapping("/search")
    fun search(@RequestBody categorySearchValues: CategorySearchValues): ResponseEntity<Any> {
        // проверка на обязательные параметры

        if (categorySearchValues.userId == 0L) {
            return ResponseEntity<Any>("missed param: user id", HttpStatus.NOT_ACCEPTABLE)
        }

        // поиск категорий пользователя по названию
        val list = categoryService.findByTitle(categorySearchValues.title, categorySearchValues.userId)

        return ResponseEntity.ok(list)
    }


    // параметр id передаются не в BODY запроса, а в самом URL
    @PostMapping("/id")
    fun findById(@RequestBody id: Long): ResponseEntity<Any> {
        var category: Category? =

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            categoryService.findById(id)
        } catch (e: NoSuchElementException) { // если объект не будет найден
            e.printStackTrace()
            return ResponseEntity<Any>("id=$id not found", HttpStatus.NOT_ACCEPTABLE)
        }

        return ResponseEntity.ok(category)
    }
}
