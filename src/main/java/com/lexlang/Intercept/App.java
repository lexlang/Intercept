package com.lexlang.Intercept;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.lexlang.Intercept.connection.ConnectionOption;
import com.lexlang.Intercept.connection.HttpWebConnection;
import com.lexlang.Intercept.proxyee.ProxyServer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException
    {

    	HttpProxyServerConfig serverConfig=new HttpProxyServerConfig();
        serverConfig.setHandleSsl(true);
    	new ProxyServer().serverConfig(serverConfig).start(7777);
    	
    }
}
