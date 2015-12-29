(function(){

	angular
		.module('fsLoginMain')
		.controller('loginController',[
			'appConstants', '$state', '$stateParams', '$mdSidenav', '$mdDialog', '$mdMenu',
				'$mdUtil', '$log', '$q', '$scope', '$rootScope', '$element', LoginController
			]
		);
	
	function LoginController(
		appConstants, $state, $stateParams, $mdSidenav, $mdDialog, $mdMenu, $mdUtil, $log, $q, $scope, $rootScope, $element) {
   
   
		/****************************************************************************************
		 * Internal models bound to UI
		 */
		var sectionTitle = "Not set";
		
		$scope.user = {
			username: "",
			password: ""
		};


		/****************************************************************************************
		 * On application load:  load all resource stores when page loads (asynchronously)
		 */		
		_handleOnPageLoad();

		
		/**
		 * Fetch all resource stores from server and pre-load first one (if one exists)
		 */
		function _handleOnPageLoad(){
			
			sectionTitle = 'Fstore Login';

		}
		
		/**
		 * Get current section title
		 */
		function _sectionTitle(){
			return sectionTitle;
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
		 * Say hello
		 */
		function _doHello(){
			alert("hello from login controller");
		}
		
		/**
		 * Build handler to open/close a SideNav; when animation finishes
		 * report completion in console
		 */
		function _buildToggler(navID) {
			var debounceFn = $mdUtil.debounce(function(){
				$mdSidenav(navID)
				.toggle()
				.then(function () {
					$log.debug("toggle " + navID + " is done");
				});
			},300);
			return debounceFn;
		}
		
		/**
		 * Close left-side nav panel
		 */
		function _leftNavClose() {
			$mdSidenav('MyLeftNav').close()
			.then(function () {
				$log.debug("close MyLeftNav is done");
			});
		};

		/**
		 *
		 */
		function _notImplemented(){
			alert("Feature implementation is forthcoming.");
		}
	
		var self = this;
		
		/*
		 * External API
		 */
		return {
			
			doHello : _doHello,

			leftNavClose : _leftNavClose,

			toggleLeftNav : _buildToggler('MyLeftNav'),

			notImplemented : _notImplemented,
			
			sectionTitle : _sectionTitle
            
		}

	}

})();