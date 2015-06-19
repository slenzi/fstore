package org.lenzi.fstore.file.web.controller;

import java.nio.file.Paths;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.lenzi.fstore.file.repository.model.impl.FsFileStore;
import org.lenzi.fstore.file.service.FsHelper;
import org.lenzi.fstore.file.service.FsService;
import org.lenzi.fstore.file.service.exception.FsServiceException;
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
@RequestMapping("/file/test")
public class FsFile1TestController extends AbstractSpringController {

    @InjectLogger
    private Logger logger;
    
    @Autowired
    private ManagedProperties appProps;
    
    @Autowired
    private FsService fsFileStoreService;
    
    @Autowired
    private FsHelper fsHelper;
	
    
	public FsFile1TestController() {
		
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
		
		FsFileStore sampleStore = null;
		try {
			sampleStore = fsFileStoreService.createSampleFileStore(Paths.get(sampleStoreRootPath));
		} catch (FsServiceException e) {
			handleError(logger, "Error creating sample file store", model, e);
			return "/test/filetest/test.jsp";
		}
		
		Tree<FsDirectory> dirTree = null;
		try {
			//dirTree = fsFileStoreService.getTree(sampleStore.getRootDir().getDirId());
			dirTree = fsFileStoreService.getTreeWithFileMeta(sampleStore.getRootDir().getDirId());
		} catch (FsServiceException e) {
			handleError(logger, "Error building tree for file store => " + sampleStore.getName(), model, e);
			return "/test/filetest/test.jsp";
		}
		
		final FsFileStore finalStore = sampleStore;
		
		// print directory tree. show directory name and absolute path for each directory in the tree
		String treeData = dirTree.printHtmlTree(
				n -> { 
					return n.getName() + ": " + 
							fsHelper.getAbsoluteDirectoryString(finalStore, n) + 
							((n.hasFileEntries()) ? " (File Count: " + n.getFileEntries().size() + ")" : " (No file entries)");
				});
		
		model.addAttribute("test-data", treeData);
		
		return "/test/filetest/test.jsp";
		
	}

}
