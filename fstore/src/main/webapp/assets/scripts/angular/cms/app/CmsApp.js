(function(){
	
	'use strict';
	
	var cmsApp;
	
	/**
	 * Initialize app with 'angular material' and our 'fsCmsMain' module
	 *
	 * ui.router - Routing frameworks, essentially more powerful version of built in ngRoute.
	 * ngMaterial - Material design UI components
	 * ngResource - Provides interaction support with RESTful services via the $resource service
	 * fsCmsMain - Our main cms module
	 * fstore-models-module - common fstore models
	 * fstore-services-module - common fstore services
	 * fstore-upload-module - common fstore services
	 * smart-table - lightweight table module
	 */
	cmsApp = angular
		.module('fstoreCms', ['ui.router', 'ngMaterial', 'ngResource', 'fsCmsMain', 'fstore-models-module', 'fstore-services-module', 'fstore-upload-module', 'smart-table'])
		// @xyz@ values are replaced/filtered by maven during build process
		.constant('appConstants', {
			contextPath: '@application.context@',
			httpUploadHandler: '@http.upload.handler@',			
			restServiceCmsSite: '@services.cms.rest.site@'
		})
		// inject our own constants into our config
		.config(['appConstants', '$locationProvider', '$mdThemingProvider', '$stateProvider', '$urlRouterProvider', appConfig]);
		
		/**
		 * Main app config
		 *
		 * appConstants - our own application constants
		 * $locationProvider - default angular location provider
		 * $mdThemingProvider - Angular material theme setup
		 * $stateProvider - angular ui.router state provider
		 * $urlRouterProvider - angualr ui.router url provider
		 */
		function appConfig(appConstants, $locationProvider, $mdThemingProvider, $stateProvider, $urlRouterProvider){
			
			locationConfig($locationProvider);
			
			uiRouteConfig(appConstants, $stateProvider, $urlRouterProvider);
			
			materialConfig($mdThemingProvider);
			
		};
		
		/**
		 * Configure location provider
		 */
		function locationConfig($locationProvider){
			
			//$locationProvider.html5Mode(true);
			
		};
		
		/**
		 * Angular material UI config
		 */
		function materialConfig($mdThemingProvider){
			
			// Extend the gray theme with a few different shades
			var lightGrey = $mdThemingProvider.extendPalette('grey', {
				'50':  'EFEFEF',
				'200': 'C6C6C6',
				'400': '8C8C8C',
				'800': '323232'
			});
			
			// Register the new color palette map with the name <code>light-gray</code>
			$mdThemingProvider.definePalette('light-gray', lightGrey);				
	
			// apply our light-gray theme
			$mdThemingProvider.theme('default')
				.primaryPalette('light-gray', {
					'default': '50', // by default use shade 50 from the light-gray palette for primary intentions
					'hue-1': '200', // use shade 100 for the <code>md-hue-1</code> class
					'hue-2': '400', // use shade 600 for the <code>md-hue-2</code> class
					'hue-3': '800' // use shade A100 for the <code>md-hue-3</code> class
				})
				.accentPalette('red');
				
				// append .dark() to make the theme dark			
			
		};
		
		/**
		 * Angular ui.router config - configure states and partials.
		 */
		function uiRouteConfig(appConstants, $stateProvider, $urlRouterProvider){
			
			// For any unmatched url, redirect to /sites state. This partial shows a list of all cms sites
			$urlRouterProvider.otherwise("/sites");

			// Now set up the states
			$stateProvider
				// list all cms sites
				.state('main_siteList', {
					url: '/sites',
					views: {
						'mainContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/cms/modules/main/partials/siteListPartial.jsp'
						},
						'toolbarContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/cms/modules/main/partials/toolbarSiteListPartial.jsp'							
						}
					}
				})
				// show site settings
				.state('main_siteSettings', {
					url: '/siteSettings',
					views: {
						'mainContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/cms/modules/main/partials/siteSettingsPartial.jsp'
						},
						'toolbarContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/cms/modules/main/partials/toolbarSiteSettingsPartial.jsp'							
						}
					}
				})
				// view specific site, and it's resources
				.state('main_siteResources', {
					url: '/siteResources',
					views: {
						'mainContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/cms/modules/main/partials/siteResourcesViewPartial.jsp'
						},
						'toolbarContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/cms/modules/main/partials/toolbarSiteResourcesViewPartial.jsp'							
						}
					}
				})
				.state('main_upload', {
					url: '/upload',
					views: {
						'mainContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/cms/modules/main/partials/uploadPartial.jsp'
						},
						'toolbarContent': {
							templateUrl: appConstants.contextPath + '/assets/scripts/angular/cms/modules/main/partials/toolbarUploadPartial.jsp'							
						}
					}
				});				
			
		};
			
})();