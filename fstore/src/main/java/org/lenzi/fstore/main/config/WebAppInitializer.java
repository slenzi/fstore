package org.lenzi.fstore.main.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Bootstraps the application. This is an alternative to the usual /WEB-INF/web.xml file.
 * 
 * @author sal
 */
@Order(2) // run after SpringSecurityInitializer
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	public WebAppInitializer() {
		
		System.out.println(WebAppInitializer.class.getName() + " created");
		
	}

	@Override
	protected Class<?>[] getRootConfigClasses() {

		System.out.println(WebAppInitializer.class.getName() + ".getRootConfigClasses() called");
		
		Class<?>[] rootConfigClasses = new Class<?>[] { 
				AppConfig.class
				,SecurityConfig.class
				//,MethodSecurityConfig.class
				};

		return rootConfigClasses;		
		
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {

		System.out.println(WebAppInitializer.class.getName() + ".getServletConfigClasses() called");
		
		Class<?>[] servletConfigClasses = new Class<?>[] { WebMvcConfig.class };

		return servletConfigClasses;		
		
	}
	
	@Override
	protected String getServletName() {
		
		System.out.println(WebAppInitializer.class.getName() + ".getServletName() called");
		
		return "dispatcher";
		
	}

	@Override
	protected String[] getServletMappings() {
		
		System.out.println(WebAppInitializer.class.getName() + ".getServletMappings() called");
		
		String[] servletMappings = new String[] { "/spring/*" };

		return servletMappings;
		
	}

	@Override
	protected void registerContextLoaderListener(ServletContext servletContext) {
		
		System.out.println(WebAppInitializer.class.getName() + ".registerContextLoaderListener(ServletContext) called");
		
		super.registerContextLoaderListener(servletContext);
		
	}

	/**
	 * Register Apache CXF servlet on startup
	 */
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		
		System.out.println(WebAppInitializer.class.getName() + ".onStartup(ServletContext) called");
		
		super.onStartup(servletContext);
		
		registerApacheCxfServlet(servletContext);
		
	}
	
	/**
	 * Setup CXF Servlet
	 * 
	 * @param servletContext
	 */
	private void registerApacheCxfServlet(ServletContext servletContext){
		
		System.out.println(WebAppInitializer.class.getName() + ".registerApacheCxfServlet(ServletContext) called");
		
		ServletRegistration.Dynamic jaxWsServlet = servletContext.addServlet("cxf", new CXFServlet());
		
		jaxWsServlet.addMapping("/cxf/*");		
		
	}
	
	/**
	 * Make sure to set active spring profiles
	 */
	@Override
	protected WebApplicationContext createRootApplicationContext() {
	
		WebApplicationContext context = super.createRootApplicationContext();
		
		((ConfigurableEnvironment) context.getEnvironment()).setActiveProfiles( loadSpringProfilesFromPropertie() );
		
		return context;
		
	}

	/**
	 * Read spring profile names from application properties file
	 * 
	 * @return
	 */
	private String[] loadSpringProfilesFromPropertie() {
		
		System.out.println(WebAppInitializer.class.getName() + ".loadSpringProfilesFromPropertie() called");
		
		InputStream input = getClass().getClassLoader().getResourceAsStream("my.application.properties");
		
		Properties properties = new Properties();
		try {
			properties.load(input);
			return properties.getProperty("spring.profiles.active").split(",");
		} catch (IOException e) {
			e.printStackTrace();
			String[] defaultProfiles = { "fubar" };
			return defaultProfiles;
		}

	}	


}
