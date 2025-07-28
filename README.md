# Auth-microservice – Сервис для аутентификации и регистрации пользователей

## Used requirements:

- Java 21
- Spring Boot 3
- Maven
- PostgreSQL 17
- Docker
- Docker Compose 

## Starting the service:

1) **Клонировать репозиторий:**

```bash
git clone https://github.com/Vlad1slavS/Auth-microservice
```

2) **Перейти в директорию проекта:**

```bash
cd Auth-microservice
```

3) **Запустить БД через Docker Compose:**

```bash
docker-compose up -d
```

4) **Собрать и запустить приложение:**

```bash
mvn clean compile -DskipTests
mvn spring-boot:run
```

5) **Проверка статуса сервиса Postgres**

```bash
docker-compose ps
```

6) **Просмотр логов**

```bash
docker-compose logs -f
```

### Доступ к приложению

- **Приложение**: http://localhost:8081/api/v1
- **База данных PostgreSQL**: localhost:5433

### Подключение к базе данных

Параметры подключения к PostgreSQL:

- **Host**: localhost
- **Port**: 5433
- **Database**: auth_db
- **Username**: auth
- **Password**: 12345


## Endpoints: 

– `/api/v1/auth/signup` – Регистрация нового пользователя

– `/api/v1/auth/signin` – Авторизация пользователя

– GET `/login` - Страница авторизации

– POST `/login` - Авторизация пользователя с передачей логина и пароля

– `/oauth2/redirect` - Страница с токеном при успешной авторизации через Oauth

## Configuration:

`server.port` – Порт, на котором будет запущено приложение (по умолчанию 8081)

`auth.global.salt` – Соль для хеширования паролей

`application.security.jwt.secret-key` – Секретный ключ для JWT

`application.security.jwt.expiration` – Время жизни JWT (в секундах)

`spring.security.oauth2.client.registration.github.client-id` - ID приложения для авторизации через GitHub

`spring.security.oauth2.client.registration.github.client-secret` - Секретный ключ приложения для авторизации через GitHub

`spring.security.oauth2.client.registration.google.client-id` - ID приложения для авторизации через Google

`spring.security.oauth2.client.registration.google.client-secret` - Секретный ключ приложения для авторизации через Google