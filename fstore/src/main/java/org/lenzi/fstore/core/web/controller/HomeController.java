/**
 * 
 */
package org.lenzi.fstore.core.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.lenzi.fstore.web.controller.AbstractSpringController;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Home/welcome controller. Directs users to landing page after login.
 * 
 * @author sal
 */
@Controller
@RequestMapping("/core/home")
public class HomeController extends AbstractSpringController {

	@InjectLogger
	private Logger logger;	
	
    @Autowired
    private ManagedProperties appProps; 	
	
	public HomeController() {
		
	}
	
	/**
	 * Forward to welcome/landing page.
	 * 
	 * @ResponseBody - bypass our internal view resolver to we can dispatch the user to the home.jsp
	 * page outside the WEB-INF directory.
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	public String home(HttpServletRequest request, HttpServletResponse response, ModelMap model){
		
		logger.info(LoginController.class.getName() + ".home(....) called.");
		logger.info("Path > " + "/home/home.jsp");
		
		return "/home/home.jsp";
		
	}	

}
