package org.lenzi.fstore.file2.web.controller;

import java.nio.file.Paths;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.lenzi.fstore.file.repository.model.impl.FsFileStore;
import org.lenzi.fstore.file.service.FsHelper;
import org.lenzi.fstore.file.service.FsService;
import org.lenzi.fstore.file.service.exception.FsServiceException;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceHelper;
import org.lenzi.fstore.file2.service.FsResourceService;
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
@RequestMapping("/file2/test")
public class FsTestController extends AbstractSpringController {

    @InjectLogger
    private Logger logger;
    
    @Autowired
    private ManagedProperties appProps;
    
    @Autowired
    private FsQueuedResourceService fsQueuedResourceService;
    
    @Autowired
    private FsResourceService fsResourceService;
    
    @Autowired
    private FsResourceHelper fsResourceHelper;
   
	public FsTestController() {
		
	}
	
    /**
     * Create sample file store, and display to user
     * 
     * @param model
     * @return
     */
	@RequestMapping(value="/makestore", method = RequestMethod.GET)
	public String makeStore(ModelMap model) {
		
		String sampleStoreRootPath = appProps.getProperty("sample.file.store");
		
		FsResourceStore sampleStore = null;
		try {
			sampleStore = fsResourceService.createSampleResourceStore(Paths.get(sampleStoreRootPath));
		} catch (ServiceException e) {
			handleError(logger, "Error creating sample file store", model, e);
			return "/test/filetest/test.jsp";
		}
		
		Tree<FsPathResource> resourceTree = null;
		try {
			resourceTree = fsResourceService.getTree(sampleStore.getRootDirectoryResource().getDirId());
		} catch (ServiceException e) {
			handleError(logger, "Error building resource tree for file store => " + sampleStore.getName(), model, e);
			return "/test/filetest/test.jsp";
		}
		
		final FsResourceStore finalStore = sampleStore;
		
		// print directory tree. show directory name and absolute path for each directory in the tree
		String treeData = resourceTree.printHtmlTree(
				n -> {
					return n.getName() +
						", [id=" + n.getNodeId() + 
						", type=" +n.getPathType().getType() + 
						", date-modified=" + DateUtil.defaultFormat(n.getDateUpdated()) + "]";
				});
		
		String displayData = sampleStore.getName() + "<br><br>" + sampleStore.getDescription() + "<br><br>" + treeData;
		
		model.addAttribute("test-data", displayData);
		
		return "/test/filetest/test.jsp";
		
	}

}
