/**
 * 
 */
package org.lenzi.fstore.core.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.web.controller.AbstractSpringController;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles application login
 * 
 * @author sal
 */
@Controller
@RequestMapping("/core/login")
public class LoginController extends AbstractSpringController {

	@InjectLogger
	private Logger logger;		
	
	public LoginController() {
		
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView defaultMethod(HttpServletRequest request, HttpServletResponse response, ModelMap model){
		
		logger.info(LoginController.class.getName() + ".defaultMethod(....) called.");
		
		ModelAndView modelAndView = new ModelAndView();
		
		modelAndView.setViewName("login/defaultLogin");
		
		return modelAndView;
		
	}

}
