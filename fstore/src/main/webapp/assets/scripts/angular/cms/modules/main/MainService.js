(function(){
	
	'use strict';
	
	angular
		.module('fsCmsMain')
		.service('mainService', [
			'appConstants', '$log', '$q', '$location', '$resource', MainService
			]
		);
	
	function MainService(appConstants, $log, $q, $location, $resource){

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

})();