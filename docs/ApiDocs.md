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

## 7. SSE interface

### Method

`GET`

### Path

```
/api/event/{taskId}
```

### Request

```
taskId: json
```

### Response

```
taskId: json
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
Settings : json   Desc: Settings is an object with a request body in JSON format, where id = 1.
Sample:
{
    "id": 1,
    "downloadPath":"/test1",
    "maxTasks":-1,
    "maxDownloadSpeed":1.3
}
```

### Response

```
Result: json   Desc: Result is an object with a request body in JSON format
Sample:
{
    "code": "200",
    "data": {
        "id": 1,
        "downloadPath": "/test1",
        "maxTasks": 4,
        "maxDownloadSpeed": 1.3
    }
}
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
Result: json   Desc: Result is an object with a request body in JSON format
Sample:
{
    "code": "200",
    "data": {
        "id": 1,
        "downloadPath": "/test1",
        "maxTasks": 4,
        "maxDownloadSpeed": 1.3
    }
}
```

# File


## 1.Get Files

### Method

`POST`

### Path

```
/getFileList
```

### Request

```
FileParams : json   Desc: Parameter set of file query
Sample:
{
    "type": "All"/"Video"/"Photo"/"Archive"/"Document",
    "sort": "name"/"size"/"gmtCreated",
    "order": "up"/"down",
}
```

### Response

```
Result: json   Desc: Result is an object with a request body in JSON format
Sample:
{
    "code": "200",
    "data": {
        "fileList": [
            {
                "name": "1-10.mp4",
                "path": "\\storage\\1-10.mp4",
                "size": 32003339,
                "gmtModified": "2024-09-18T14:52:31.026+00:00"
            },
            {
                "name": "PixPin_2024-08-22_19-11-22.png",
                "path": "\\storage\\PixPin_2024-08-22_19-11-22.png",
                "size": 15997,
                "gmtModified": "2024-08-22T11:11:23.778+00:00"
            },
            {
                "name": "b_54f53e1c231f2713ed264effe7a1b68b.jpg",
                "path": "\\storage\\b_54f53e1c231f2713ed264effe7a1b68b.jpg",
                "size": 41590,
                "gmtModified": "2024-08-22T07:53:28.126+00:00"
            }
        ],
        "total": 3
    }
}
```