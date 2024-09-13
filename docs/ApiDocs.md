# Transfer

## 1.Submit task

### Method

`POST`

### Path

```
/api/task/submit
```

### Request

```
url: json   Desc: The url of the request
```

### Response

```
key: json   Desc: Task ID, required by the SSE interface
```

## 2.Get task list

### Method

`POST`

### Path

```
/api/task/list
```

### Request

```
status: json   Desc: Task status
```

### Response

```
total: json    Desc: Total number of tasks
data: json     Desc: Task list
```

## 3.Pause tasks

### Method

`POST`

### Path

```
/api/task/pause
```

### Request

```
ids: json   Desc: List of task ids
```

### Response

```
ids: json   Desc: Task ids, required by the SSE interface
```

## 4.Resume tasks

### Method

`POST`

### Path

```
/api/task/resume
```

### Request

```
ids: json   Desc: List of task ids
```

### Response

```
ids: json   Desc: Task ids, required by the SSE interface
```

## 5.Restart tasks

### Method

`POST`

### Path

```
/api/task/restart
```

### Request

```
ids: json   Desc: List of task ids
```

### Response

```
ids: json   Desc: Task ids, required by the SSE interface
```

## 6.Delete tasks

### Method

`POST`

### Path

```
/api/task/delete
```

### Request

```
ids: json   Desc: List of task ids
```

### Response

```
ids: json   Desc: Task ids
```

# Settings

## 1.Save settings

### Method

`POST`

### Path

```
/api/settings
```

### Request

```
null
```

### Response

```
isSuccess: json   Desc: Whether the operation was successful
```

## 2.Get settings

### Method

`GET`

### Path

```
/api/settings
```

### Request

```
null
```

### Response

```
isSuccess: json   Desc: Whether the operation was successful
```
