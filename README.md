# HTTP-download-server (Java version)

## Technology Used

Web: `Vue3` + `Vite`

Server: `Springboot` + `Mybatis-plus` + `MySQL` + `Redis`

JDK: `21`

## Run locally

## Web

### 1. Enter the web

```bash
cd web
```

### 2. Install Dependencies

```bash
npm install
```

### 3. Run

```shell
npm run dev
```

## Server

### 1. Download project

```bash
git clone https://github.com/Lemon001017/HTTP-download-server-Java.git
```

### 2.Configure the database and redis

- MySQL: Run `settings.sql` and `task.sql` to create the table, then configure the **userName** and **password**.
- Redis: Configure the **host**, **port** and **password**.

### 3. Run unit tests

```bash
mvn test
```

### 4. Run

```bash
mvn spring-boot:run
```
