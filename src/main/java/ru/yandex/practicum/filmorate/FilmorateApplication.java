/*
Никита, здравствуйте.
Подскажите, пожалуйста:
1. В класс User добавилось поле Set<Long> friendsIds, в класс Film - List<Long> userLikes. Нужны ли эти поля в конструкторах (сейчас они в них передаются)?
Если нет, создаю отдельные конструкторы для каждого из этих классов без этих новых полей?
2.
 */


package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilmorateApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilmorateApplication.class, args);
    }

}
