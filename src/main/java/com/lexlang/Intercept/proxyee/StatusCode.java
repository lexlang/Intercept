package com.lexlang.Intercept.proxyee;

import java.util.HashMap;

/**
* @author lexlang
* @version 2018年12月1日 下午9:59:50
* 
*/
public class StatusCode {
	private static HashMap<Integer,String> storeCode=new HashMap<Integer,String>();
	static{
		storeCode.put(100, "Continue");
		storeCode.put(101, "Switching Protocols");
		storeCode.put(200, "OK");
		storeCode.put(201, "Created");
		storeCode.put(202, "Accepted");
		storeCode.put(203, "Non-Authoritative Information");
		storeCode.put(204, "No Content");
		storeCode.put(205, "Reset Content");
		storeCode.put(206, "Partial Content");
		storeCode.put(300, "Multiple Choices");
		storeCode.put(301, "Moved Permanently");
		storeCode.put(302, "Found");
		storeCode.put(303, "See Other");
		storeCode.put(304, "Not Modified");
		storeCode.put(305, "Use Proxy");
		storeCode.put(306, "Unused");
		storeCode.put(307, "Temporary Redirect");
		storeCode.put(400, "Bad Request");
		storeCode.put(401, "Unauthorized");
		storeCode.put(402, "Payment Required");
		storeCode.put(403, "Forbidden");
		storeCode.put(404, "Not Found");
		storeCode.put(405, "Method Not Allowed");
		storeCode.put(406, "Not Acceptable");
		storeCode.put(407, "Proxy Authentication Required");
		storeCode.put(408, "Request Time-out");
		storeCode.put(409, "Conflict");
		storeCode.put(410, "Gone");
		storeCode.put(411, "Length Required");
		storeCode.put(412, "Precondition Failed");
		storeCode.put(413, "Request Entity Too Large");
		storeCode.put(414, "Request-URI Too Large");
		storeCode.put(415, "Unsupported Media Type");
		storeCode.put(416, "Requested range not satisfiable");
		storeCode.put(417, "Expectation Failed");
		storeCode.put(500, "Internal Server Error");
		storeCode.put(501, "Not Implemented");
		storeCode.put(502, "Bad Gateway");
		storeCode.put(503, "Service Unavailable");
		storeCode.put(504, "Gateway Time-out");
		storeCode.put(505, "HTTP Version not supported");
	}
	
	public static String getStatusDescribe(int code){
		return storeCode.get(code);
	}
}
