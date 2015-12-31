package org.lenzi.fstore.main.config;

import java.util.List;

import org.lenzi.fstore.core.security.LoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * Spring MVC configuration.
 * 
 * Component scanning notes:
 * -- org.lenzi.fstore.example.web.controller : controllers for example package, to test tree creation
 * -- org.lenzi.fstore.file2.web.controller : file2 controllers for file store, store files and directories in a tree
 * -- org.lenzi.fstore.file2.web.messaging.controller : file2 websocket / stomp messaging
 * -- org.lenzi.fstore.cms.web.controller : cms controllers
 * -- org.lenzi.fstore.core.web.controller : core controllers (e.g. login controller)
 * -- org.lenzi.fstore.core.logging : custom LoggerBeanPostProccessor which enables us to inject a logger using @InjectLogger annotation.
 * 
 * @author slenzi
 */
@Configuration
@EnableWebMvc
@ComponentScan(
	basePackages = {
		"org.lenzi.fstore.example.web.controller",
		"org.lenzi.fstore.file2.web.controller",
		"org.lenzi.fstore.file2.web.messaging.controller",
		"org.lenzi.fstore.cms.web.controller",
		"org.lenzi.fstore.core.web.controller",
		"org.lenzi.fstore.core.logging"
		}
)
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	@Autowired
	private LoggingInterceptor loggingInterceptor;
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#configureDefaultServletHandling(org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer)
	 */
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry)
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/public/**").addResourceLocations("classpath:/public/");
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#configureViewResolvers(org.springframework.web.servlet.config.annotation.ViewResolverRegistry)
	 */
	/*
	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        
        registry.viewResolver(viewResolver);
	}
	*/

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#configureMessageConverters(java.util.List)
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		
		//
		// for pushing (uploading) files from a spring controller back to the clients browser.
		//
		converters.add(new ResourceHttpMessageConverter());
		converters.add(new ByteArrayHttpMessageConverter());
		
		// TODO - test...
		//converters.add(new StringHttpMessageConverter());
		
		super.configureMessageConverters(converters);
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry)
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		
		//super.addInterceptors(registry);
		
		// sample interceptor which simply logs before and after interception of request
		registry.addInterceptor(loggingInterceptor);
		
		// example path mapping...
		//registry.addInterceptor(new LoggingInterceptor()).addPathPatterns("/fstore/administration/*");
	}
	

	
}
