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
				appConstants.contextPath + '/cxf/resource/store/:storeId',
				{ storeId: '@storeId' }
			);
			
		// file resource service
		var fileService = $resource(
			appConstants.contextPath + '/cxf/resource/file/:fileId', { fileId: '@fileId' }, {
				deleteFiles: {
					url: appConstants.contextPath + '/cxf/resource/file/delete',
					method: 'POST'
				}
			}); 
		
		// directory resource service
		var directoryService = $resource(
			appConstants.contextPath + '/cxf/resource/directory', { dirId: '@dirId' }, {
				depthGet: {
					url: appConstants.contextPath + '/cxf/resource/directory/:dirId/depth/:maxDepth',
					method: 'GET',
					params: {
						dirId: '@dirId', maxDepth: '@maxDepth'
					}
				},
				breadcrumbGet: {
					url: appConstants.contextPath + '/cxf/resource/directory/breadcrumb/:dirId',
					method: 'GET',
					params: {
						dirId: '@dirId'
					}
				},
				deleteDirectories: {
					url: appConstants.contextPath + '/cxf/resource/directory/delete',
					method: 'POST'
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
		
		// fetch directory listing
		function _fetchDirectoryListing(dirId, maxDepth){
			
			//$log.debug('fetching directory lsiting for dir id ' + dirId + ', max depth ' + maxDepth);
			
			//return directoryService.get({ dirId: dirId, maxDepth: maxDepth }).$promise;
			
			return directoryService.depthGet({ dirId: dirId, maxDepth: maxDepth }).$promise;
			
		};
		
		// fetch parent tree / breadcrumb listing for some child directory
		function _fetchBreadcrumb(dirId){
			
			return directoryService.breadcrumbGet({ dirId: dirId }).$promise;
			
		}
		
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
		
		// delete a list of directories
		function _deleteDirectories(dirIdList){
			
			return directoryService.deleteDirectories({ 'dirId' : dirIdList }).$promise;
			
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
			deleteDirectories: _deleteDirectories
	    };
		
	}

})();