# java-filmorate
Приложение для работы с фильмами и оценками пользователей

![Диаграмм БД.](Диаграмма%20БД.png)

Примеры запросов:
1. Поиск 10 фильмов с наивысшим рейтингом:
   SELECT name,
          rating
   FROM films
   ORDER BY rating DESC
   LIMIT 10;

2. Отображение имен и логинов всех пользователей, которые поставили лайк фильму (id фильма = 1):
   SELECT name,
          login
   FROM users AS u
   RIGHT JOIN user_likes AS ul ON ul.user_id=u.user_id
   WHERE ul.film_id = 1

3. Поиск общих друзей у 2-х пользователей (с id = 1 и id = 2):
   SELECT friend_two_id AS common_friends
   FROM friendship
   WHERE friend_one_id = 2
     AND friend_two_id IN (SELECT friend_two_id AS all_friends_of_first
                           FROM friendship
                           WHERE friend_one_id = 1) AS first_user_friends;

Пояснение по содержимому булева поля friendship_status таблице friendship:
- frienship_status = 1 -> оба есть друг у друга в друзьях;
- frienship_status = 0 -> user произвел добавление в друзья, но потенциальный друг (пока) не принял приглашение;
Всех, кто отсутствует в поле friend_two_id для user-а, user в друзья не добавлял.