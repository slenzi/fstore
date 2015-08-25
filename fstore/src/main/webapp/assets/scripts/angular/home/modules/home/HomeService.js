(function(){
	
	'use strict';
	
	angular
		.module('home')
		.service('homeService', [
			'appConstants', '$log', '$q', '$location', '$resource', HomeService
			]
		);
	
	function HomeService(appConstants, $log, $q, $location, $resource){
		
		// resource store service
		var storeService = $resource(
			appConstants.restServiceStore + '/:storeId',
				{ storeId: '@storeId' }
			);
			
		// file resource service
		var fileService = $resource(
			appConstants.restServiceFile + '/:fileId', { fileId: '@fileId' }, {
				deleteFiles: {
					url: appConstants.restServiceFile + '/delete',
					method: 'POST'
				}
			}); 
		
		// directory resource service
		var directoryService = $resource(
			appConstants.restServiceDirectory, { dirId: '@dirId' }, {
				depthGet: {
					url: appConstants.restServiceDirectory + '/:dirId/depth/:maxDepth',
					method: 'GET',
					params: {
						dirId: '@dirId', maxDepth: '@maxDepth'
					}
				},
				breadcrumbGet: {
					url: appConstants.restServiceDirectory + '/breadcrumb/:dirId',
					method: 'GET',
					params: {
						dirId: '@dirId'
					}
				},
				deleteDirectories: {
					url: appConstants.restServiceDirectory + '/delete',
					method: 'POST'
				},
				addDirectory: {
					url: appConstants.restServiceDirectory + '/add',
					method: 'POST',
					params: {
						dirId: '@dirId', dirName: '@newDirName'
					}					
				}
			});

		// cms site service
		var cmsSiteService = $resource(
			appConstants.restServiceCmsSite, { siteId: '@siteId' }, {
				addSite: {
					url: appConstants.restServiceCmsSite + '/add',
					method: 'POST',
					params: {
						siteName: '@siteName', siteDesc: '@siteDesc', clearIfExists: true
					}					
				}
			});		
		
		var sampleData = [
			{
				name: 'Fubar',
				avatar: 'weee',
				content: 'this is a test'
			}
		];
		
		// *********************************
		// Internal test methods
		// *********************************
		
		function _doLoadTestRegular(){
			return sampleData;
		};		
		
		// promise based API (asynchronous)
		function _doLoadTestWithPromise(){
			
			$log.debug('loading test, path = ' + $location.path());
			
			return $q.when(sampleData);
		};
		
		// *********************************
		// Internal RESTful methods
		// *********************************

		
		// *********************************
		// Store resource operations
		// *********************************			
		
		// fetch all resource stores
		function _fetchResourceStoreList(){
			
			//$log.debug('fetching resource store list...');
			
			return storeService.query().$promise;
			
		};
		
		// fetch store by id
		function _fetchStoreById(storeId){
			
			//$log.debug('fetching resource store for store id ' + storeId + '...');
			
			return storeService.get({ storeId: storeId }).$promise;
			
		};
		
		
		// *********************************
		// File resource operations
		// *********************************		
		
		// download a file resource
		function _downloadFile(fileId){
			
			//alert('download file coming soon. file id = ' + fileId);
			
			var downloadUrl = appConstants.contextPath + '/cxf/resource/file/download/id/' + fileId;
			
			window.location.href = downloadUrl;
			
		}
		
		// delete a single file resource
		function _deleteFile(fileId){
			
			return fileService.delete({ fileId: fileId }).$promise;
			
		}
		
		// delete a list of files
		function _deleteFiles(fileIdList){
			
			return fileService.deleteFiles({ 'fileId' : fileIdList }).$promise;
			
		}
		
		
		// *********************************
		// Directory resource operations
		// *********************************
		
		// fetch directory listing
		function _fetchDirectoryListing(dirId, maxDepth){
			
			//$log.debug('fetching directory listing for dir id ' + dirId + ', max depth ' + maxDepth);
			
			//return directoryService.get({ dirId: dirId, maxDepth: maxDepth }).$promise;
			
			return directoryService.depthGet({ dirId: dirId, maxDepth: maxDepth }).$promise;
			
		};
		
		// fetch parent tree / breadcrumb listing for some child directory
		function _fetchBreadcrumb(dirId){
			
			return directoryService.breadcrumbGet({ dirId: dirId }).$promise;
			
		}		
		
		// delete a list of directories
		function _deleteDirectories(dirIdList){
			
			return directoryService.deleteDirectories({ 'dirId' : dirIdList }).$promise;
			
		}

		// add new directory
		function _addDirectory(parentDirId, newDirName){
			
			return directoryService.addDirectory({ 'dirId' : parentDirId, 'newDirName': newDirName }).$promise;
			
		}
		
		
		// *********************************
		// CMS Site operations
		// *********************************		
		
		// fetch all cms sites
		function _fetchCmsSiteList(){
			
			return cmsSiteService.query().$promise;
			
		};		
		
		// add new cms site
		function _addCmsSite(siteName, siteDesc){
			
			var clearIfExists = true;
			
			return cmsSiteService.addSite({ 'siteName' : siteName, 'siteDesc': siteDesc, 'clearIfExists': clearIfExists }).$promise;
			
		}
		
		
		// *********************************
		// External API
		// *********************************
	    return {
			load: _doLoadTestRegular,
			loadPromise: _doLoadTestWithPromise,
			getResourceStores: _fetchResourceStoreList,
			getResourceStoreById: _fetchStoreById,
			getDirectoryListing: _fetchDirectoryListing,
			getBreadcrumb: _fetchBreadcrumb,
			downloadFile: _downloadFile,
			deleteFile: _deleteFile,
			deleteFiles: _deleteFiles,
			deleteDirectories: _deleteDirectories,
			addDirectory: _addDirectory,
			addCmsSite: _addCmsSite,
			getCmsSites: _fetchCmsSiteList
	    };
		
	}

})();