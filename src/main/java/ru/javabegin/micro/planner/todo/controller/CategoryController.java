package ru.javabegin.micro.planner.todo.controller;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.javabegin.micro.planner.entity.Category;
import ru.javabegin.micro.planner.entity.User;
import ru.javabegin.micro.planner.todo.feign.UserFeignClient;
import ru.javabegin.micro.planner.todo.search.CategorySearchValues;
import ru.javabegin.micro.planner.todo.service.CategoryService;
import ru.javabegin.micro.planner.utils.rest.resttemplate.UserRestBuilder;
import ru.javabegin.micro.planner.utils.rest.webclient.UserWebClientBuilder;

import java.util.List;
import java.util.NoSuchElementException;


/*

Используем @RestController вместо обычного @Controller, чтобы все ответы сразу оборачивались в JSON,
иначе пришлось бы добавлять лишние объекты в код, использовать @ResponseBody для ответа, указывать тип отправки JSON

Названия методов могут быть любыми, главное не дублировать их имена внутри класса и URL mapping

*/

@RestController
@RequestMapping("/category") // базовый URI
public class CategoryController {

    // доступ к данным из БД
    private CategoryService categoryService;


    //микросервис для работы с пользователем
    private UserRestBuilder userRestBuilder;
    private UserWebClientBuilder userWebClientBuilder;


    //клиент для вызова мс
    private UserFeignClient userFeignClient;


    // используем автоматическое внедрение экземпляра класса через конструктор
    // не используем @Autowired ля переменной класса, т.к. "Field injection is not recommended "
    public CategoryController(CategoryService categoryService
                        , UserRestBuilder userRestBuilder
                        , UserWebClientBuilder userWebClientBuilder
                        , UserFeignClient userFeignClient) {
        this.categoryService = categoryService;
        this.userRestBuilder = userRestBuilder;
        this.userWebClientBuilder = userWebClientBuilder;
        this.userFeignClient = userFeignClient;
    }

    @PostMapping("/all")
    public List<Category> findAll(@AuthenticationPrincipal Jwt jwt) {
        return categoryService.findAll(jwt.getSubject());
    }


    @PostMapping("/add")
    public ResponseEntity<Category> add(@RequestBody Category category, @AuthenticationPrincipal Jwt jwt) {

        // проверка на обязательные параметры
        if (category.getId() != null && category.getId() != 0) { // это означает, что id заполнено
            // id создается автоматически в БД (autoincrement), поэтому его передавать не нужно, иначе может быть конфликт уникальности значения
            return new ResponseEntity("redundant param: id MUST be null", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение title
        if (category.getTitle() == null || category.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title MUST be not null", HttpStatus.NOT_ACCEPTABLE);
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

//        ResponseEntity<User> result =  userFeignClient.findUserById(category.getUserId());
//
//        if (result == null){ // если мс недоступен - вернется null
//            return new ResponseEntity("система пользователей недоступна, попробуйте позже", HttpStatus.NOT_FOUND);
//        }
//
//        if (result.getBody() != null){ // если пользователь не пустой
//            return ResponseEntity.ok(categoryService.add(category));
//        }

        category.setUserId(jwt.getSubject());//UUID пользователя из keycloak

        if(!category.getUserId().isBlank()){
            return ResponseEntity.ok(categoryService.add(category));
        }

        // если пользователя НЕ существует
        return new ResponseEntity("user id=" + category.getUserId() + " not found", HttpStatus.NOT_ACCEPTABLE);
    }



    @PutMapping("/update")
    public ResponseEntity update(@RequestBody Category category) {

        // проверка на обязательные параметры
        if (category.getId() == null || category.getId() == 0) {
            return new ResponseEntity("missed param: id", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение title
        if (category.getTitle() == null || category.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title", HttpStatus.NOT_ACCEPTABLE);
        }

        // save работает как на добавление, так и на обновление
        categoryService.update(category);

        return new ResponseEntity(HttpStatus.OK); // просто отправляем статус 200 (операция прошла успешно)
    }



    // для удаления используем тип запроса DELETE и передаем ID для удаления
    // можно также использовать метод POST и передавать ID в теле запроса
    @DeleteMapping("/delete/{id}")
    public ResponseEntity delete(@PathVariable("id") Long id) {

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            categoryService.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity("id=" + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity(HttpStatus.OK); // просто отправляем статус 200 без объектов (операция прошла успешно)
    }


    // поиск по любым параметрам CategorySearchValues
    @PostMapping("/search")
    public ResponseEntity<List<Category>> search(@RequestBody CategorySearchValues categorySearchValues, @AuthenticationPrincipal Jwt jwt) {

        categorySearchValues.setUserId(jwt.getSubject());

        // проверка на обязательные параметры
        if (categorySearchValues.getUserId().isBlank()) {
            return new ResponseEntity("missed param: user id", HttpStatus.NOT_ACCEPTABLE);
        }

        // поиск категорий пользователя по названию
        List<Category> list = categoryService.findByTitle(categorySearchValues.getTitle(), categorySearchValues.getUserId());

        return ResponseEntity.ok(list);
    }


    // параметр id передаются не в BODY запроса, а в самом URL
    @PostMapping("/id")
    public ResponseEntity<Category> findById(@RequestBody Long id) {

        Category category = null;

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            category = categoryService.findById(id);
        } catch (NoSuchElementException e) { // если объект не будет найден
            e.printStackTrace();
            return new ResponseEntity("id=" + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return ResponseEntity.ok(category);
    }

}
