package com.lexlang.Intercept.proxyee;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.github.monkeywie.proxyee.crt.CertPool;
import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import com.github.monkeywie.proxyee.handler.HttpProxyInitializer;
import com.github.monkeywie.proxyee.handler.TunnelProxyInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.proxy.ProxyConfig;
import com.github.monkeywie.proxyee.proxy.ProxyHandleFactory;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.util.ProtoUtil;
import com.github.monkeywie.proxyee.util.ProtoUtil.RequestProto;
import com.lexlang.Intercept.connection.ConnectionOption;
import com.lexlang.Intercept.connection.HttpWebConnection;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.resolver.NoopAddressResolverGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.channel.ChannelHandler.Sharable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class HttpProxyServerHandle extends ChannelInboundHandlerAdapter {

  private ChannelFuture cf;
  private String host;
  private int port;
  private boolean isSsl = false;
  private int status = 0;
  private RequestPara para=new RequestPara();
  private HttpProxyExceptionHandle exceptionHandle;
  private HttpProxyServerConfig serverConfig;

  public HttpProxyServerHandle(HttpProxyServerConfig serverConfig, HttpProxyExceptionHandle exceptionHandle) {
		this.exceptionHandle=exceptionHandle;
		this.serverConfig=serverConfig;
  }

@Override
  public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
    if (msg instanceof HttpRequest) {
      HttpRequest request = (HttpRequest) msg;
      //第一次建立连接取host和端口号和处理代理握手
      if (status == 0) {
        RequestProto requestProto = ProtoUtil.getRequestProto(request);
        if (requestProto == null) { //bad request
          ctx.channel().close();
          return;
        }
        status = 1;
        this.host = requestProto.getHost();
        this.port = requestProto.getPort();
        if ("CONNECT".equalsIgnoreCase(request.method().name())) {//建立代理握手
          status = 2;
          HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
              HttpProxyServer.SUCCESS);
          ctx.writeAndFlush(response);
          ctx.channel().pipeline().remove("httpCodec");
          //fix issue #42
          ReferenceCountUtil.release(msg);
          return;
        }
      }
      //interceptPipeline = buildPipeline();
      //interceptPipeline.setRequestProto(new RequestProto(host, port, isSsl));
      //fix issue #27
      if (request.uri().indexOf("/") != 0) {
        URL url = new URL(request.uri());
        request.setUri(url.getFile());
      }
      para.setHttpRequest(request);
    } else if (msg instanceof HttpContent) {
      if (status != 2) {
    	  beforeRequest(ctx.channel(),para.getHttpRequest(), (HttpContent) msg,para.isSsl());
        //interceptPipeline.beforeRequest(ctx.channel(), (HttpContent) msg);
      } else {
        ReferenceCountUtil.release(msg);
        status = 1;
      }
    } else { //ssl和websocket的握手处理
      if (serverConfig.isHandleSsl()) {
        ByteBuf byteBuf = (ByteBuf) msg;
        if (byteBuf.getByte(0) == 22) {//ssl握手
          isSsl = true;
          para.setSsl(isSsl);
          int port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
          SslContext sslCtx = SslContextBuilder
              .forServer(serverConfig.getServerPriKey(), CertPool.getCert(port,this.host, serverConfig))
              .build();
          ctx.pipeline().addFirst("httpCodec", new HttpServerCodec());
          ctx.pipeline().addFirst("sslHandle", sslCtx.newHandler(ctx.alloc()));
          //重新过一遍pipeline，拿到解密后的的http报文
          ctx.pipeline().fireChannelRead(msg);
          return;
        }
      }
    }
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    if (cf != null) {
      cf.channel().close();
    }
    ctx.channel().close();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (cf != null) {
      cf.channel().close();
    }
    ctx.channel().close();
    exceptionHandle.beforeCatch(ctx.channel(), cause);
  }
  
  public void beforeRequest(Channel clientChannel,HttpRequest httpRequest, HttpContent httpContent,boolean isSSL) throws Exception {
	  WebRequest webRequest=turnHttpRequestToWebRequest(httpRequest, httpContent,isSSL);
	  HttpWebConnection connection=new HttpWebConnection(new ConnectionOption());
  	  WebResponse response = connection.getResponse(webRequest);
  	  //输出
  	  clientChannel.writeAndFlush(getHttpResponse(response)).addListener(ChannelFutureListener.CLOSE);
  }
  
  private WebRequest turnHttpRequestToWebRequest(HttpRequest request, HttpContent httpContent,boolean isSSL) throws MalformedURLException{
		String method=request.getMethod().name().toUpperCase();
		String url = (isSSL?"https://":"http://")+request.headers().get(HttpHeaderNames.HOST)+request.getUri();
		System.out.println(url);
		HttpHeaders rhds = request.headers();
		List<Entry<String, String>> hds = rhds.entries();
		//构造url
		WebRequest req=new WebRequest(new URL(url),getMethod(method));
		//添加消息头
		for(int index=0;index<hds.size();index++){
			Entry<String, String> item = hds.get(index);
			if(! item.getKey().equals("Content-Length")){
				req.setAdditionalHeader(item.getKey(), item.getValue());
			}
		}
		//添加消息体
		if(method.equals("POST")){
			req.setRequestBody(getContent(httpContent));
		}
		return req;
  }
  

  
  /**
   * 获得消息头
   * @param response
   * @return
 * @throws IOException 
   */
  public FullHttpResponse getHttpResponse(WebResponse response) throws IOException{
		//消息头
	   FullHttpResponse httpHeader = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.valueOf(response.getStatusCode()));
	   List<NameValuePair> hds = response.getResponseHeaders();
	   List<String> cks=new ArrayList<String>();
	   for(int i=0;i<hds.size();i++){
			NameValuePair item = hds.get(i);
			if(item.getName().equals("Content-Encoding") || item.getName().equals("Transfer-Encoding")){
				continue;
			}
			if(item.getName().equals("Set-Cookie")){
				cks.add(item.getValue());
			}
			else{
				httpHeader.headers().add(item.getName(), item.getValue());
			}
	   }
	   if(cks.size()>0){
			httpHeader.headers().add("Set-Cookie", cks);
	   }
	   
	   httpHeader.content().writeBytes(getByte(response.getContentAsStream()));
	   return httpHeader;
  }
  
  private byte[] getByte(InputStream input) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();		 
		byte[] buffer = new byte[1024];
		int len;
		while ((len = input.read(buffer)) > -1 ) {
		    baos.write(buffer, 0, len);
		}
		baos.flush();
		return baos.toByteArray();
  }
  
  public String getContent(HttpContent req){
	  ByteBuf bf =req.content(); 
	  byte[] byteArray = new byte[bf.capacity()];
	  bf.readBytes(byteArray);
	  String result = new String(byteArray);
	  return result;
  }
  
  private HttpMethod getMethod(String method){
	  if(method.equals("POST")){
		  return HttpMethod.POST;
	  }else if(method.equals("DELETE")){
		  return HttpMethod.DELETE;
	  }else if(method.equals("HEAD")){
		  return HttpMethod.HEAD;
	  }else if(method.equals("OPTIONS")){
		  return HttpMethod.OPTIONS;
	  }else if(method.equals("PATCH")){
		  return HttpMethod.PATCH;
	  }else if(method.equals("PUT")){
		  return HttpMethod.PUT;
	  }else if(method.equals("TRACE")){
		  return HttpMethod.TRACE;
	  }else {
		  return HttpMethod.GET;
	  }
  }
  
  
}

