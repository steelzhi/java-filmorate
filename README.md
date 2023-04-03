# java-filmorate
Приложение для работы с фильмами и оценками пользователей

![Диаграмма БД.](Диаграмма%20БД.png)

Примеры запросов для основных операций в приложении: <br>
1. Фильмы: <br>
  1.1. POST-запросы: <br>
      1.1.1. `/films` - добавление нового фильма; <br>
  1.2. GET-запросы: <br>
      1.2.1. `/films` - получение всех фильмов; <br>
      1.2.2. `/films/{id}` - получение фильма по id; <br>
      1.2.3. `/films/popular` - получение n наиболее популярных фильмов (по умолчанию, n = 10); <br>
  1.3. PUT-запросы: <br>
      1.3.1. `/films` - обновление существующего фильма; <br>
      1.3.2. `/films/{id}/like/{userId}` - добавление фильму с id лайка от пользователя с userId; <br>
  1.4. DELETE-запросы: <br>
      1.4.1. `/films/{id}/like/{userId}` - удаление у фильма с id лайка от пользователя с userId; <br>
2. Пользователи: <br>
   2.1. POST-запросы: <br>
     2.1.1. `/users` - создание нового пользователя; <br>
   2.2. GET-запросы: <br>
     2.2.1. `/users` - получение списка всех пользователей; <br>
     2.2.2. `/users/{id}` - получение пользователя по id; <br>
     2.2.3. `/users/{id}/friends` - получение списка друзей пользователя с id; <br>
     2.2.4. `/users/{id}/friends/common/{otherId}` - получение списка общих друзей пользователей с id и otherId; <br>
  2.3. PUT-запросы: <br>
     2.3.1. `/users` - обновление существующего пользователя; <br>
  2.4. DELETE-запросы: <br>
     2.4.1. `/users/{id}/friends/{friendId}` - удаление у пользователя с id из списка друзей друга с friendId. <br>