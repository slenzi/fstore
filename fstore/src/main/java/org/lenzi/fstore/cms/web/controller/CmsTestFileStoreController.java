package org.lenzi.fstore.cms.web.controller;

import java.nio.file.Paths;

import org.lenzi.fstore.cms.repository.model.impl.CmsDirectory;
import org.lenzi.fstore.cms.repository.model.impl.CmsFileStore;
import org.lenzi.fstore.cms.service.CmsFileStoreHelper;
import org.lenzi.fstore.cms.service.CmsFileStoreService;
import org.lenzi.fstore.cms.service.exception.CmsServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.main.properties.ManagedProperties;
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
    private ManagedProperties appProps;
    
    @Autowired
    private CmsFileStoreService cmsFileStoreService;
    
    @Autowired
    private CmsFileStoreHelper cmsFileStoreHelper;
	
    
	public CmsTestFileStoreController() {
		
	}
	
    /**
     * Create sample file store, and display to user
     * 
     * @param model
     * @return
     */
	@RequestMapping(value="/makestore", method = RequestMethod.GET)
	public String makeStore(ModelMap model) {
		
		String sampleStoreRootPath = appProps.getProperty("sample.cms.file.store");
		
		CmsFileStore sampleStore = null;
		try {
			sampleStore = cmsFileStoreService.createSampleFileStore(Paths.get(sampleStoreRootPath));
		} catch (CmsServiceException e) {
			handleError(logger, "Error creating sample file store", model, e);
			return "/test/cmstest/test.jsp";
		}
		
		Tree<CmsDirectory> cmsTree = null;
		try {
			//cmsTree = cmsFileStoreService.getTree(sampleStore.getRootDir().getDirId());
			cmsTree = cmsFileStoreService.getTreeWithFileMeta(sampleStore.getRootDir().getDirId());
		} catch (CmsServiceException e) {
			handleError(logger, "Error building tree for cms file store => " + sampleStore.getName(), model, e);
			return "/test/cmstest/test.jsp";
		}
		
		final CmsFileStore finalStore = sampleStore;
		
		// print cms directory tree. show directory name and absolute path for each directory in the tree
		String treeData = cmsTree.printHtmlTree(
				n -> { 
					return n.getName() + ": " + 
							cmsFileStoreHelper.getAbsoluteDirectoryString(finalStore, n) + 
							((n.hasFileEntries()) ? " (File Count: " + n.getFileEntries().size() + ")" : " (No file entries)");
				});
		
		model.addAttribute("test-data", treeData);
		
		return "/test/cmstest/test.jsp";
		
	}

}
