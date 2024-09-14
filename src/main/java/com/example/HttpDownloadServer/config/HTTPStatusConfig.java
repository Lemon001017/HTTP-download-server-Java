package com.example.HttpDownloadServer.config;

import com.example.HttpDownloadServer.param.HTTPStatusParam;
import org.json.HTTP;
import org.springframework.http.HttpStatus;

/**
 * HTTP状态码配置类
 **/
public class HTTPStatusConfig {

    public static final HTTPStatusParam DOWNLOAD_TIMEOUT = new HTTPStatusParam(504,HttpStatus.GATEWAY_TIMEOUT,"下载超时");
    public static final HTTPStatusParam RESOURCE_NOTFOUND =  new HTTPStatusParam(503,HttpStatus.SERVICE_UNAVAILABLE,"URL资源无法访问");
    public static final HTTPStatusParam DATABASE_FULL =  new HTTPStatusParam(507,HttpStatus.INSUFFICIENT_STORAGE,"数据库");
    public static final HTTPStatusParam DOWNLOAD_SERVER_TERMINATION = new HTTPStatusParam(501,HttpStatus.NOT_IMPLEMENTED,"下载服务终止");
}
