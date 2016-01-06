package org.lenzi.fstore.external.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpClientTest {

	public static void main(String[] args) {
		(new HttpClientTest()).doTests();
	}
	
	public HttpClientTest() {
		
	}
	
	public void doTests(){
		
		
		//doBasicTest();
		
		//doHeaderCookieTest();
		
		doLoginTest();
		
	}
	
	private void doLoginTest() {
		
		String csrfToken = fetchCsrfToken();
		
		System.out.println("csrf token => " + csrfToken);
		
	}

	public void doBasicTest(){
		
		String content = "";
		
		HttpGet get = new HttpGet("http://localhost:8080/fstore/spring/core/home");
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		
		try {
			response = httpClient.execute(get);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			System.err.print("ClientProtocolException, error executing request. " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.print("IOException, error executing request. " + e.getMessage());
		}
		
		BufferedReader reader = null;
		String line = null;
		
		HttpEntity entity = response.getEntity();

		try {
			
			reader = new BufferedReader(new InputStreamReader(entity.getContent())); 
			while ((line = reader.readLine()) != null) {
				content += line;
			}
			// ensure response is fully consumed
			EntityUtils.consume(entity);
			
		}catch(IOException e){
			e.printStackTrace();
			System.err.print("IOException, error reading HTTP response. " + e.getMessage());
		}
		
		System.out.println(content);	
		
	}
	
	public void doHeaderCookieTest(){
		
		String content = "";
		
		HttpClientContext context = HttpClientContext.create();
		CookieStore cookieStore = new BasicCookieStore();
		context.setCookieStore(cookieStore);		
		
		HttpGet get = new HttpGet("http://localhost:8080/fstore/spring/core/csrf");
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		
		try {
			
			response = httpClient.execute(get, context);
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			System.err.print("ClientProtocolException, error executing request. " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.print("IOException, error executing request. " + e.getMessage());
		}
		
		BufferedReader reader = null;
		String line = null;
		
		HttpEntity entity = response.getEntity();
		Header[] headers = response.getAllHeaders();

		try {
			
			reader = new BufferedReader(new InputStreamReader(entity.getContent())); 
			while ((line = reader.readLine()) != null) {
				content += line;
			}
			// ensure response is fully consumed
			EntityUtils.consume(entity);
			
		}catch(IOException e){
			e.printStackTrace();
			System.err.print("IOException, error reading HTTP response. " + e.getMessage());
		}
		
		System.out.println(content);
		
		for(Header header : headers){
			System.out.println(header.getName() + ": " + header.getValue());
		}		
		
		for (Cookie cookie: cookieStore.getCookies()) {
			System.out.println("Recieved Cookie: " + cookie.getName() + ":" + cookie.getValue());
		}
		
		String csrf = getCookieValue(cookieStore, "_csrf");
		
		System.out.println("_csrf = " + csrf);
		
	}
	
	public String fetchCsrfToken(){
		
		String content = "";
		
		HttpGet get = new HttpGet("http://localhost:8080/fstore/spring/core/csrf");
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		
		try {
			
			response = httpClient.execute(get);
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			System.err.print("ClientProtocolException, error executing request. " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.print("IOException, error executing request. " + e.getMessage());
		}
		
		BufferedReader reader = null;
		String line = null;
		
		HttpEntity entity = response.getEntity();

		try {
			
			reader = new BufferedReader(new InputStreamReader(entity.getContent())); 
			while ((line = reader.readLine()) != null) {
				content += line;
			}
			// ensure response is fully consumed
			EntityUtils.consume(entity);
			
		}catch(IOException e){
			e.printStackTrace();
			System.err.print("IOException, error reading HTTP response. " + e.getMessage());
		}
		
		return content;
		
	}
	
	public String getCookieValue(CookieStore cookieStore, String cookieName) {
		String value = null;
		for (Cookie cookie : cookieStore.getCookies()) {
			if (cookie.getName().equals(cookieName)) {
				value = cookie.getValue();
			}
		}
		return value;
	}	

}
