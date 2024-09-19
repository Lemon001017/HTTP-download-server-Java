# HTTP-download-server (Java version)

## Technology Used

Web: `Vue3` + `Vite`

Server: `Springboot` + `Mybatis-plus` + `MySQL` + `Redis` + `RabbitMQ`

JDK: `21`

## Run locally

### 1. Download project

```bash
git clone https://github.com/Lemon001017/HTTP-download-server-Java.git
```

### 2.Configure the database and redis

- MySQL: Run `settings.sql` and `task.sql` to create the table, then configure the **userName** and **password**.
- Redis: Configure the **host**, **port** and **password**.
- RabbitMQ: Configure the **host**, **port** , **username** and **password**.

### 3. Run unit tests

```bash
mvn test
```

### 4. Run

```bash
mvn spring-boot:run
```
