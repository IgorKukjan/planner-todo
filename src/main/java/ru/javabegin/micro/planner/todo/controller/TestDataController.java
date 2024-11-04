package ru.javabegin.micro.planner.todo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.javabegin.micro.planner.entity.Category;
import ru.javabegin.micro.planner.entity.Priority;
import ru.javabegin.micro.planner.entity.Task;
import ru.javabegin.micro.planner.todo.service.CategoryService;
import ru.javabegin.micro.planner.todo.service.PriorityService;
import ru.javabegin.micro.planner.todo.service.TaskService;

import java.util.Calendar;
import java.util.Date;

@RestController
@RequestMapping("/data")
public class TestDataController {

    private final TaskService taskService;
    private final PriorityService priorityService;
    private final CategoryService categoryService;


    public TestDataController(TaskService taskService, PriorityService priorityService, CategoryService categoryService) {
        this.taskService = taskService;
        this.priorityService = priorityService;
        this.categoryService = categoryService;
    }


    @PostMapping("/init")
    public ResponseEntity<Boolean> init(@RequestBody Long userId){

        Priority priority1 = new Priority();
        priority1.setColor("#fff");
        priority1.setTitle("Важный");
        priority1.setUserId(userId);

        Priority priority2 = new Priority();
        priority2.setColor("#ffe");
        priority2.setTitle("Неважный");
        priority2.setUserId(userId);

        priorityService.add(priority1);
        priorityService.add(priority2);


        Category category1 = new Category();
        category1.setTitle("Работа");
        category1.setUserId(userId);

        Category category2 = new Category();
        category2.setTitle("Семья");
        category2.setUserId(userId);

        categoryService.add(category1);
        categoryService.add(category2);


        //завтра
        Date tomorrow = new Date();
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(tomorrow);
        calendar1.add(Calendar.DATE, 1);
        tomorrow = calendar1.getTime();

        //неделя
        Date oneWeek = new Date();
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(oneWeek);
        calendar2.add(Calendar.DATE, 7);
        oneWeek = calendar2.getTime();

        Task task1 = new Task();
        task1.setTitle("Покушать");
        task1.setCategory(category1);
        task1.setPriority(priority1);
        task1.setCompleted(true);
        task1.setTaskDate(tomorrow);
        task1.setUserId(userId);

        Task task2 = new Task();
        task2.setTitle("Поспать");
        task2.setCategory(category2);
        task2.setPriority(priority2);
        task2.setCompleted(false);
        task2.setTaskDate(oneWeek);
        task2.setUserId(userId);

        taskService.add(task1);
        taskService.add(task2);

        return ResponseEntity.ok(true);
    }
}
