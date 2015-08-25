(function(){

	angular
		.module('fsCmsMain')
		.controller('mainController',[
			'appConstants', 'mainService', 'CmsSite',
			'$state', '$stateParams', '$mdSidenav', '$mdDialog', '$mdBottomSheet', '$mdUtil', '$log', '$q', '$scope', MainController
			]
		);

	function MainController(
		appConstants, mainService, CmsSite, $state, $stateParams, $mdSidenav, $mdDialog, $mdBottomSheet, $mdUtil, $log, $q, $scope) {
   
   
		/****************************************************************************************
		 * Internal models bound to UI
		 */
		var sectionTitle = "Not set";
		var cmsSiteList = [{ "name": "empty"}];
		
		var currentSite = new CmsSite({
			name: 'Loading...',
			dateCreated: 'Loading...',
			dateUpdated: 'Loading...'
		});		


		/****************************************************************************************
		 * On application load:  load all cms sites when page loads (asynchronously)
		 */		
		_handleOnPageLoad();

		
		/**
		 * Fetch all cms from server and pre-load first one (if one exists)
		 */
		function _handleOnPageLoad(){
			
			_handleEventViewSiteList();
			
		}	
		
		/**
		 * Get current section title
		 */
		function _sectionTitle(){
			return sectionTitle;
		}
		
		/**
		 * Get list of cms sites
		 */
		function _cmsSiteList(){
			return cmsSiteList;
		}		
		
		function _handleEventViewSiteList(){
			
			sectionTitle = "CMS Site List";
			
			$state.go('main_siteList');
			
			mainService
				.getCmsSites()
				.then(_handleCmsSiteDataCallback);			
			
		}
		
		function _handleCmsSiteDataCallback(siteData){
			
			if(siteData.error){
				$log.debug("Error, " + siteData.error);
			}else{
				
				$log.debug("got site data => " + JSON.stringify(siteData));
				
				cmsSiteList = siteData;
				
				if(cmsSiteList != null && cmsSiteList[0]){
					
					currentSite.setData(cmsSiteList[0]);
					
					//_handleLoadDirectory(storeList[0].rootDirectoryId, false);
					
					_leftNavClose();
				}

				$log.debug('current site = ' + JSON.stringify(currentSite));
				$log.debug('current site list = ' + JSON.stringify(cmsSiteList));
			}			
			
		}
		
		/**
		 * View settings for current store
		 */
		function _handleEventViewSiteSettings(){
			$state.go('main_siteSettings');
		}
		
		/**
		 * Handle cancel upload button click
		 */
        function _handleEventClickCancelSiteSettings(){
        	
        	_handleEventViewSiteList()
        	
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
		 * Close left side nav
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
            
		/**
		 * Display dialog where users can enter values for new cms site
		 */
		function _handleEventClickNewCmsSite(event){
			
			$mdDialog.show({
				parent: angular.element(document.body),
				targetEvent: event,
				template:
					'<md-dialog aria-label="List dialog" flex="35">' +
					'	<md-dialog-content>'+
					'		<md-content layout-padding>' +
					'			<h3>Create New CMS Site</h3>' +
					'			<md-input-container flex>' +
					'				<label>Site Context Name</label>' +
					'				<input ng-model="newCmsSiteDialog.siteName" required>' +
					'			</md-input-container>' +
					'			<md-input-container flex>' +
					'				<label>Site Description</label>' +
					'				<textarea ng-model="newCmsSiteDialog.siteDesc" columns="1" md-maxlength="150" required></textarea>' +
					'			</md-input-container>' +
					'		</md-content>' +
					'  </md-dialog-content>' +       
					'  <div class="md-actions">' +
					'    <md-button ng-click="closeDialog()" class="md-primary">' +
					'      Cancel' +
					'    </md-button>' +
					'    <md-button ng-click="createCmsSite()" class="md-primary">' +
					'      Create' +
					'    </md-button>' +					
					'  </div>' +
					'</md-dialog>',
				controller: _createCmsSiteDialogController
			});
		
			function _createCmsSiteDialogController($scope, $mdDialog) {
				$scope.closeDialog = function() {
					$mdDialog.hide();
				}
				$scope.createCmsSite = function() {
				
					var siteName = $scope.newCmsSiteDialog.siteName;
                    var siteDesc = $scope.newCmsSiteDialog.siteDesc;
					
					mainService
						.addCmsSite(siteName, siteDesc)
						.then( function( reply ) {
							
							$log.debug('add cms site reply: ' + JSON.stringify(reply));
							
							// reload sites!
							//_reloadCurrentDirectory();
							
							$mdDialog.hide();
							
						});	
					
				}				
			}
			
		}
		
		function _handleEventClickSiteTable(siteData){
			
			alert('you clicked on a site');
			
		}
	
		var self = this;
		
		/*
		 * External API
		 */
		return {
	
			leftNavClose : _leftNavClose,
			toggleLeftNav : _buildToggler('MyLeftNav'),
			notImplemented : _notImplemented,
			sectionTitle : _sectionTitle,
			cmsSiteList : _cmsSiteList,
		
			handleEventViewSiteSettings : _handleEventViewSiteSettings,
			
			handleEventViewSiteList : _handleEventViewSiteList,
			
			handleEventClickCancelSiteSettings : _handleEventClickCancelSiteSettings,
			
            handleEventClickNewCmsSite : _handleEventClickNewCmsSite,
            
            handleEventClickSiteTable : _handleEventClickSiteTable
		}

	}

})();