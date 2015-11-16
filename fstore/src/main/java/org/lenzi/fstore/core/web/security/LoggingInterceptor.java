/**
 * 
 */
package org.lenzi.fstore.core.web.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author sal
 *
 * Sample interceptor, configured in WebMvcConfig.
 *
 * @see org.lenzi.fstore.main.config.WebMvcConfig
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

	@InjectLogger
	private Logger logger;	
	
	/**
	 * 
	 */
	public LoggingInterceptor() {
	
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.HandlerInterceptor#afterCompletion(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
	 */
	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		
		logger.debug(LoggingInterceptor.class.getName() + ".afterCompletion(...) called");

	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.HandlerInterceptor#postHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler, ModelAndView modelAndView) throws Exception {
		
		logger.debug(LoggingInterceptor.class.getName() + ".postHandle(...) called");

	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.HandlerInterceptor#preHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object)
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object arg2) throws Exception {

		logger.debug(LoggingInterceptor.class.getName() + ".postHandle(...) called");		
		
		return true;
		
	}

}
