package org.lenzi.fstore.cms.web.controller;

import org.lenzi.fstore.cms.service.CmsFileStoreService;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.web.controller.AbstractSpringController;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for testing CMS File Store code.
 * 
 * 
 * @author slenzi
 */
@Controller
@RequestMapping("/cms/test")
public class CmsTestFileStoreController extends AbstractSpringController {

    @InjectLogger
    private Logger logger;
    
    @Autowired
    CmsFileStoreService cmsFileStoreService;
	
    
	public CmsTestFileStoreController() {
		
	}
	
    /**
     * Fetch sample tree (creating it if necessary) and print it in HTML format.
     * 
     * @param model
     * @return
     */
	@RequestMapping(value="/tree", method = RequestMethod.GET)
	public String showSampleTree(ModelMap model) {
		
		
		return "/test/cmstest/test.jsp";
		
	}

}
