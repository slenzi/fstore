package org.lenzi.fstore.main.config;

import java.util.List;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Spring MVC configuration.
 * 
 * Component scanning notes:
 * -- org.lenzi.fstore.example.web.controller : controllers for example package, to test tree creation
 * -- org.lenzi.fstore.file.web.controller : file1 controllers for file store, store files and directories in a tree
 * -- org.lenzi.fstore.file2.web.controller : file2 controllers for file store, store files and directories in a tree
 * -- org.lenzi.fstore.core.logging : custom LoggerBeanPostProccessor which enables us to inject a logger using @InjectLogger annotation.
 * 
 * @author slenzi
 */
@Configuration
@EnableWebMvc
@ComponentScan(
	basePackages = {
		"org.lenzi.fstore.example.web.controller",
		"org.lenzi.fstore.file.web.controller",
		"org.lenzi.fstore.file2.web.controller",
		"org.lenzi.fstore.core.logging"
		}
)
public class WebMvcConfig extends WebMvcConfigurerAdapter {

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
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#configureMessageConverters(java.util.List)
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		
		//
		// for pushing (uploading) files from a spring controller back to the clients browser.
		//
		converters.add(new ResourceHttpMessageConverter());
		
		super.configureMessageConverters(converters);
	}
	
}
