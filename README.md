# AI Scrum Master — Автоматическая декомпозиция задач в Jira

[![Java 17](https://img.shields.io/badge/Java-17-brightgreen)](https://openjdk.org/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.2-blue)](https://spring.io/projects/spring-boot)
[![Grok-4.1](https://img.shields.io/badge/AI-Grok%204.1-ff6600)](https://x.ai)

**AI Scrum Master** — это инструмент, который превращает техническое задание в полностью готовый спринт в Jira:
- Epic → Stories → Sub-tasks
- Оценка в Story Points
- Автоматическое назначение исполнителей

> Ты пишешь: «Сделать P2P-переводы с KYC и отчётами по 115-ФЗ»  
> Получаешь: **Epic + 6 Stories + 30 Sub-tasks** с назначенными людьми — уже в Jira!

## Возможности

| Функция                              | Статус  |
|--------------------------------------|--------|
| Декомпозиция ТЗ через AI             | Done   |
| Автоматическое создание задач в Jira | Done   |
| Назначение по email (accountId)      | Done   |
| Валидация структуры ответа           | Done   |
| Поддержка Company-managed проектов   | Done   |
| Работает с любой командой            | Done   |


### 1. Клонируй репозиторий
```bash
git clone https://github.com/danniyeres/ai-scrum-master.git
cd ai-scrum-master
```

```bash
./mvnw spring-boot:run
```


## API

### Базовый URL: `http://localhost:8080`  
Заголовки по умолчанию:
- `Content-Type: application/json`
- `Accept: application/json`

### POST `/api/ai/decompose`
Создаёт эпик со связанными задачами в Jira на основе текстового ТЗ.

- Назначение исполнителей выполняется по email → `accountId`.
- Ответ модели валидируется, ошибки маппинга и интеграции с Jira возвращаются как ошибки запроса.

Тело запроса:
```json
{
  "technicalSpec": "Сделать мобильное приложение для переводов между пользователями с проверкой KYC, AML, лимитами и генерацией PDF-отчётов по 115-ФЗ."
}
```

Успешный ответ 200:
```json
{
  "Личный кабинет клиента v2": "ASM-421",
  "Как клиент я хочу видеть список своих счетов...": "ASM-422",
  "Как клиент я хочу скачивать выписку в PDF...": "ASM-427",
  "Как клиент я хочу подать заявку на кредитную карту...": "ASM-434"
}
```


Управление командой (для назначения исполнителей)
Создать команду:
### POST /api/teams
```json
{
  "name": "Digital Banking Squad",
  "teamMembers": [
    {
      "fullName": "Tony Stark",
      "email": "stark@gmail.com",
      "role": "Backend Developer",
      "telegramId": null
    },
    {
      "fullName": "Jassy Pinkman",
      "email": "pinkman@gmail.com",
      "role": "Frontend Developer"
    }
  ]
}
```

Добавить участника в существующую команду
### POST /api/teams/members?teamId=3

```json
{
  "fullName": "Daenerys Targaryen",
  "email": "targaryen@@gmail.com",
  "role": "QA Engineer"
}
```

### Удалить участника
### DELETE /api/teams/members?memberId=7


### Получить все команды
### GET /api/teams → список команд со всеми участниками
ИИ видит всех участников из БД и назначает задачи только по email из этого списка.


## Стек

- Java 17 / Spring Boot 3
- Lombok
- Jackson
- RestTemplate
- Jira Cloud REST API v3
- OpenRouter (модель по умолчанию: x-ai/grok-4.1-fast)