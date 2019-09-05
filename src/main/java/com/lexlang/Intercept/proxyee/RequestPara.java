package com.lexlang.Intercept.proxyee;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;

public class RequestPara {
	
	private boolean isSsl = false;
	private HttpRequest httpRequest;
	private HttpContent httpContent;
	
	public boolean isSsl() {
		return isSsl;
	}
	public void setSsl(boolean isSsl) {
		this.isSsl = isSsl;
	}
	public HttpRequest getHttpRequest() {
		return httpRequest;
	}
	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}
	public HttpContent getHttpContent() {
		return httpContent;
	}
	public void setHttpContent(HttpContent httpContent) {
		this.httpContent = httpContent;
	}
	
	
	
	
}
