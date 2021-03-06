/**
 * Common rest services use by all fstore modules/apps
 */
(function(){
	
	'use strict';
	
	var fstoreServices;
	
	fstoreServices = angular
		.module('fstore-services-module', ['ngResource'])
		// configuration used by all services
		.config(['$httpProvider', fstoreServicesConfig])
		// @xyz@ values are replaced/filtered by maven during build process
		.constant('FstoreServiceConstants', {
			contextPath: '@application.context@',
			httpUploadHandler: '@http.upload.handler@',
			restServiceStore: '@services.rest.store@',
			restServiceFile: '@services.rest.file@',
			restServiceDirectory: '@services.rest.directory@',
			restServiceCmsSite: '@services.cms.rest.site@',
			restServiceCmsSession: '@services.cms.rest.session@'
		})
		.service('CmsServices', [
			'FstoreServiceConstants', '$log', '$q', '$resource', CmsServices
			]
		)
		.service('FileServices', [
			'FstoreServiceConstants', '$log', '$q', '$resource', '$http', FileServices
			]
		);

	/**
	 * Make sure CSRF token is set in request header for AJAX calls. We store the token
	 * in a meta tag on every page. (see common header jsp)
	 * 
	 * Needed for Spring Security Cross Site Request Forgery (CSRF) protection.
	 */
	function fstoreServicesConfig($httpProvider){
		
		$httpProvider.defaults.xsrfHeaderName = 'X-CSRF-TOKEN';
		$httpProvider.interceptors.push(function() {
			return {
				response: function(response) {
					//$httpProvider.defaults.headers.common['X-CSRF-TOKEN'] = response.headers('X-CSRF-TOKEN');
					//$httpProvider.defaults.headers.common['X-CSRF-Token'] = $('meta[name=csrf-token]').attr('content');
					$httpProvider.defaults.headers.common['X-CSRF-Token'] = getContentByMetaTagName('_csrf');
					return response;
				}
			}    
		});
		
	}

	/**
	 * Fetch 'content' value of meta tag by either 'name' or 'property attribute'
	 */
	function getContentByMetaTagName(c) {
		for (var b = document.getElementsByTagName("meta"), a = 0; a < b.length; a++) {
			if (c == b[a].name || c == b[a].getAttribute("property")) { return b[a].content; }
		}
		return false;
	}	
	
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
		
		// cms session service
		var cmsSessionService = $resource(
				FstoreServiceConstants.restServiceCmsSession, {}, {
				fetchViewMode: {
					url: FstoreServiceConstants.restServiceCmsSession + '/viewmode',
					method: 'GET'				
				},
				toggleViewMode: {
					url: FstoreServiceConstants.restServiceCmsSession + '/viewmode/:mode',
					method: 'POST',
					params: {
						mode: '@mode'
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
		
		// fetch current view mode, ither 'offline' or 'online'
		function _fetchViewMode(){
			
			return cmsSessionService.fetchViewMode().$promise;
			
		}
		
		// set view mode, either 'offline' or 'online'
		function _setCmsViewMode(mode){
			
			return cmsSessionService.toggleViewMode({ 'mode' : mode }).$promise;
			
		}
		
		
		// *********************************
		// External API
		// *********************************
	    return {
			addCmsSite: _addCmsSite,
			getCmsSites: _fetchCmsSiteList,
			fetchViewMode: _fetchViewMode,
			setCmsViewMode: _setCmsViewMode
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
	function FileServices(FstoreServiceConstants, $log, $q, $resource, $http){
		
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
				},
				moveFiles: {
					url: FstoreServiceConstants.restServiceFile + '/move',
					method: 'POST',
					params: {
						fileId: '@fileId', dirId: '@dirId', replaceExisting: true
					}
				},
				copyFiles: {
					url: FstoreServiceConstants.restServiceFile + '/copy',
					method: 'POST',
					params: {
						fileId: '@fileId', dirId: '@dirId', replaceExisting: true
					}
				},
				// not tested (or used)
				fetchTextFile: {
					url: FstoreServiceConstants.restServiceFile + '/text/id/:fileId',
					method: 'GET',
					params: {
						fileId: '@fileId'
					}				
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
				},
				moveDirectories: {
					url: FstoreServiceConstants.restServiceDirectory + '/move',
					method: 'POST',
					params: {
						dirId: '@dirId', targetDirId: '@targetDirId', replaceExisting: true
					}
				},
				copyDirectories: {
					url: FstoreServiceConstants.restServiceDirectory + '/copy',
					method: 'POST',
					params: {
						dirId: '@dirId', targetDirId: '@targetDirId', replaceExisting: true
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
		
		// fetch data for a text/plain or other text mime type file
		function _fetchTextFile(fileId){
			
			$log.debug('fetching text file data for file with id => ' + fileId);		
			
			return $http({
				method: 'GET',
				url: FstoreServiceConstants.restServiceFile + '/text/id/' + fileId				
			}).success(function(data){
				return data;
			});

			//$http({
			//	method: 'GET',
			//	url: FstoreServiceConstants.restServiceFile + '/text/id/:fileId',
			//	params: {
			//		fileId: '@fileId'
			//	}					
			//}).$promise;			
			
			//return fileService.fetchTextFile({ fileId: fileId }).$promise;
			
		}
		
		// send text data to server and save file
		function _saveTextFile(fileId, text){
			
			$log.debug('saving text file data');
			$log.debug('fileId => ' + fileId);
			$log.debug('text => ' + text);
			
			return $http({
				method: 'POST',
				url: FstoreServiceConstants.restServiceFile + '/text',
				params: { fileId: fileId, text: text },
				headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}
			}).success(function(data){
				return data;
			});			
			
		}
		
		// delete a single file resource
		function _deleteFile(fileId){
			
			return fileService.delete({ fileId: fileId }).$promise;
			
		}
		
		// delete a list of files
		function _deleteFiles(fileIdList){
			
			return fileService.deleteFiles({ 'fileId' : fileIdList }).$promise;
			
		}
		
		// move a list of files
		function _moveFiles(fileIdList, targetDirId){
			
			var replaceExisting = true;
			
			return fileService.moveFiles({ 'fileId' : fileIdList, 'dirId' : targetDirId, 'replaceExisting': replaceExisting }).$promise;
			
		}

		// copy a list of files
		function _copyFiles(fileIdList, targetDirId){
			
			var replaceExisting = true;
			
			return fileService.copyFiles({ 'fileId' : fileIdList, 'dirId' : targetDirId, 'replaceExisting': replaceExisting }).$promise;
			
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
		
		// move a list of directories
		function _moveDirectories(dirIdList, targetDirId){
			
			var replaceExisting = true;
			
			return directoryService.moveDirectories({ 'dirId' : dirIdList, 'targetDirId' : targetDirId, 'replaceExisting': replaceExisting }).$promise;
			
		}

		// copy a list of directories
		function _copyDirectories(dirIdList, targetDirId){
			
			var replaceExisting = true;
			
			return directoryService.copyDirectories({ 'dirId' : dirIdList, 'targetDirId' : targetDirId, 'replaceExisting': replaceExisting }).$promise;
			
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
			fetchTextFile: _fetchTextFile,
			saveTextFile: _saveTextFile,
			deleteFile: _deleteFile,
			deleteFiles: _deleteFiles,
			deleteDirectories: _deleteDirectories,
			addDirectory: _addDirectory,
			copyFiles: _copyFiles,
			moveFiles: _moveFiles,
			moveDirectories: _moveDirectories,
			copyDirectories: _copyDirectories
	    };
		
	}	
	
})();