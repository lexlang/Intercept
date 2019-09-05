package com.lexlang.Intercept.connection;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.client.CredentialsProvider;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClientOptions;

public class ConnectionOption {
	
	private boolean useInsecureSSL_=true;
	private CredentialsProvider credentialsProvider =new DefaultCredentialsProvider();
	private CookieManager cookieManager_ = new CookieManager();
	private int timeout_ = 90_000;
	private WebClientOptions options_ = new WebClientOptions();

	public WebClientOptions getOptions(){
		return options_;
	}
	
	public ConnectionOption setUseInsecureSSL(boolean useSSL){
		useInsecureSSL_=useSSL;
		return this;
	}
	
	public ConnectionOption setTimeOut(int timeout){
		timeout_=timeout;
		return this;
	}
	
    public BrowserVersion getBrowserVersion() {
        return BrowserVersion.getDefault();
    }
	
    public boolean isUseInsecureSSL(){
    	return useInsecureSSL_;
    }
    
    public InetAddress getLocalAddress() {
        try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
        return null;
    }
    //
    
    public CredentialsProvider getCredentialsProvider(){
    	return credentialsProvider;
    }
    
    public CookieManager getCookieManager(){
    	return cookieManager_;
    }
    
    public int getTimeout(){
    	return timeout_;
    }
}
