/**
 * Common rest services use by all fstore modules/apps
 */
(function(){
	
	'use strict';
	
	var fstoreServices;
	
	fstoreServices = angular
		.module('fstore-services-module', ['ngResource'])
		// @xyz@ values are replaced/filtered by maven during build process
		.constant('FstoreServiceConstants', {
			contextPath: '@application.context@',
			httpUploadHandler: '@http.upload.handler@',
			restServiceStore: '@services.rest.store@',
			restServiceFile: '@services.rest.file@',
			restServiceDirectory: '@services.rest.directory@',
			restServiceCmsSite: '@services.cms.rest.site@'
		})
		.service('CmsServices', [
			'FstoreServiceConstants', '$log', '$q', '$resource', CmsServices
			]
		)
		.service('FileServices', [
			'FstoreServiceConstants', '$log', '$q', '$resource', FileServices
			]
		);	
	
	/**
	 * Setup CMS services
	 * 
	 * @param FstoreServiceConstants
	 * @param $log
	 * @param $q
	 * @param $resource
	 * @returns
	 */
	function CmsServices(FstoreServiceConstants, $log, $q, $resource){

		// cms site service
		var cmsSiteService = $resource(
				FstoreServiceConstants.restServiceCmsSite, { siteId: '@siteId' }, {
				addSite: {
					url: FstoreServiceConstants.restServiceCmsSite + '/add',
					method: 'POST',
					params: {
						siteName: '@siteName', siteDesc: '@siteDesc', clearIfExists: true
					}					
				}
			});

		
		// *********************************
		// Internal RESTful methods
		// *********************************
		
		
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
			addCmsSite: _addCmsSite,
			getCmsSites: _fetchCmsSiteList
	    };
		
	}
	
	/**
	 * Setup File services
	 * 
	 * @param FstoreServiceConstants
	 * @param $log
	 * @param $q
	 * @param $location
	 * @param $resource
	 * @returns
	 */
	function FileServices(FstoreServiceConstants, $log, $q, $resource){
		
		// resource store service
		var storeService = $resource(
			FstoreServiceConstants.restServiceStore + '/:storeId',
				{ storeId: '@storeId' }
			);
			
		// file resource service
		var fileService = $resource(
			FstoreServiceConstants.restServiceFile + '/:fileId', { fileId: '@fileId' }, {
				deleteFiles: {
					url: FstoreServiceConstants.restServiceFile + '/delete',
					method: 'POST'
				}
			}); 
		
		// directory resource service
		var directoryService = $resource(
			FstoreServiceConstants.restServiceDirectory, { dirId: '@dirId' }, {
				depthGet: {
					url: FstoreServiceConstants.restServiceDirectory + '/:dirId/depth/:maxDepth',
					method: 'GET',
					params: {
						dirId: '@dirId', maxDepth: '@maxDepth'
					}
				},
				breadcrumbGet: {
					url: FstoreServiceConstants.restServiceDirectory + '/breadcrumb/:dirId',
					method: 'GET',
					params: {
						dirId: '@dirId'
					}
				},
				deleteDirectories: {
					url: FstoreServiceConstants.restServiceDirectory + '/delete',
					method: 'POST'
				},
				addDirectory: {
					url: FstoreServiceConstants.restServiceDirectory + '/add',
					method: 'POST',
					params: {
						dirId: '@dirId', dirName: '@newDirName'
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
			
			var downloadUrl = FstoreServiceConstants.contextPath + '/cxf/resource/file/download/id/' + fileId;
			
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
			addDirectory: _addDirectory
	    };
		
	}	
	
})();