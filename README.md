# Ticket System — лабораторная работа №2

## CSV-импорт

Импорт добавляет билеты одной транзакцией. Если хотя бы одна строка некорректна,
все добавленные в рамках файла билеты откатываются.

Кодировка файла — UTF-8, разделитель — запятая. Значения с запятыми заключаются
в двойные кавычки. Заголовок:

```csv
ticket_name,coordinate_x,coordinate_y,price,type,discount,number,person_eye_color,person_hair_color,person_location_x,person_location_y,person_location_name,person_weight,person_passport_id,person_nationality,event_name,event_min_age,event_description,venue_name,venue_capacity,venue_address_street,venue_address_zip_code
```

- ID вложенных объектов в CSV не указываются: для новых записей их генерирует PostgreSQL;
- существующие человек, событие и площадка переиспользуются по их естественным ключам;
- `type`: `VIP`, `USUAL`, `BUDGETARY` или `CHEAP`;
- `discount`, `number`, `person_eye_color`, `person_passport_id`, `person_nationality`
  и `venue_address_street` могут быть пустыми;
- дробные числа записываются с точкой.

Шаблон можно скачать на странице «Импорт».

## Программные ограничения уникальности

- Непустой `Person.passportID` уникален после удаления окружающих пробелов и без
  учёта регистра.
- Непустой `Ticket.number` уникален в рамках одного события.

Ограничения проверяются в сервисах и намеренно не представлены уникальными
ограничениями БД. Изменяющие транзакции, затрагивающие эти правила, используют
уровень изоляции `SERIALIZABLE`.

## Авторизация

Авторизация работает через Spring Security и серверную HTTP-сессию. Пароли хранятся
в виде BCrypt-хешей. Регистрация создаёт пользователя с ролью `USER`. Начальный
администратор создаётся при запуске из переменных `APP_ADMIN_USERNAME` и
`APP_ADMIN_PASSWORD`.

PowerShell:

```powershell
$env:APP_ADMIN_USERNAME = "admin"
$env:APP_ADMIN_PASSWORD = "сложный-пароль"
.\gradlew.bat bootRun
```

Linux/macOS:

```bash
APP_ADMIN_USERNAME=admin APP_ADMIN_PASSWORD='сложный-пароль' ./gradlew bootRun
```

Пароль должен содержать от 8 до 100 символов. Администратор создаётся только при
отсутствии пользователя с указанным именем; последующие запуски не сбрасывают его
пароль.
