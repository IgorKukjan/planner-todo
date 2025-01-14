package ru.javabegin.micro.planner.todo.search

import java.util.*
//в этом файле будут находиться все классы для поиска

// возможные значения, по которым можно искать задачи + значения сортировки
data class TaskSearchValues (
    // сортировка
    var sortColumn: String,
    var sortDirection: String, // такие же названия должны быть у объекта на frontend

    // постраничность
    var pageNumber: Int,
    var pageSize: Int,

    var userId: Long
){
    // поля поиска (все типы - объектные, не примитивные. Чтобы можно было передать null)
    var title: String? = null
    var completed: Int? = null
    var priorityId: Long? = null
    var categoryId: Long? = null


    var dateFrom: Date? = null // для задания периода по датам
    var dateTo: Date? = null
}


// возможные значения, по которым можно искать приоритеты
data class PrioritySearchValues(
    var userId: Long // для фильтрации значений конкретного пользователя - обязательно заполнять
) {
    var title: String? = null // такое же название должно быть у объекта на frontend - необязательно заполнять
}


// возможные значения, по которым можно искать категории
data class CategorySearchValues (
    var userId: Long // для фильтрации значений конкретного пользователя - обязательно нужно заполнять
){
    var title: String? = null // такое же название должно быть у объекта на frontend - необязательно заполнять
}
