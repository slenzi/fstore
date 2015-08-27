(function(){

	angular
		.module('fsCmsMain')
		.controller('mainController',[
			'appConstants', 'CmsServices', 'CmsSite', 'FileServices', 'ResourceStore', 'PathResource',
			'$state', '$stateParams', '$mdSidenav', '$mdDialog', '$mdBottomSheet', '$mdUtil', '$log', '$q', '$scope', MainController
			]
		);
		
	// 'mainService'  mainService  - No longer use main services. Moved all services to external module called fstore-services-module

	function MainController(
		appConstants, CmsServices, CmsSite, FileServices, ResourceStore, PathResource, $state, $stateParams, $mdSidenav, $mdDialog, $mdBottomSheet, $mdUtil, $log, $q, $scope) {
   
   
		/****************************************************************************************
		 * Internal models bound to UI
		 */
		var sectionTitle = "Loading...";
		var cmsSiteList = [{ "name": "Loading..."}];
		
		var currentSite = new CmsSite({
			name: 'Loading...',
			dateCreated: 'Loading...',
			dateUpdated: 'Loading...'
		});

		var currentOfflineStore = new ResourceStore({
			name: 'Loading...',
			dateCreated: 'Loading...',
			dateUpdated: 'Loading...'
		});
		var currentOnlineStore = new ResourceStore({
			name: 'Loading...',
			dateCreated: 'Loading...',
			dateUpdated: 'Loading...'
		});

		var currentOfflineDirectory = new PathResource({
			name: 'Loading...',
			dateCreated: 'Loading...',
			dateUpdated: 'Loading...'
		});
		var currentOnlineDirectory = new PathResource({
			name: 'Loading...',
			dateCreated: 'Loading...',
			dateUpdated: 'Loading...'
		});
            
        var offlineBreadcrumbNav = [{"dirId": "empty", "name": "empty"}];
        var onlineBreadcrumbNav = [{"dirId": "empty", "name": "empty"}];

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

		/**
		 * Return current offline directory user is viewing in offline resource store
		 */
		function _offlineDirectory(){
			return currentOfflineDirectory;
		}
		
		/**
		 * Return current online directory user is viewing in online resource store
		 */
		function _onlineDirectory(){
			return currentOnlineDirectory;
		}		
		
		/**
		 * Return current offline resource store
		 */
		function _offlineResourceStore(){
			return currentOfflineStore;
		}
		
		/**
		 * Return current online resource store
		 */
		function _onlineResourceStore(){
			return currentOnlineStore;
		}
            
		function _setOfflineStore(store){
			//$log.debug('set ofline store => ' + JSON.stringify(store));
			currentOfflineStore = store;
		}
		
		function _setOnlineStore(store){
			//$log.debug('set online store => ' + JSON.stringify(store));
			currentOnlineStore = store;
		}
            
        function _setOfflineDirectory(directoryPathResource){
            currentOfflineDirectory = directoryPathResource;
        }
            
        function _setOnlineDirectory(directoryPathResource){
            currentOnlineDirectory = directoryPathResource;
        }
            
        function _offlineBreadcrumb(){
            return offlineBreadcrumbNav;   
        }
            
        function _onlineBreadcrumb(){
            return onlineBreadcrumbNav;   
        }
            
        function _setOfflineBreadcrumb(crumb){
            offlineBreadcrumbNav = crumb;
        }
            
        function _setOnlineBreadcrumb(crumb){
            onlineBreadcrumbNav = crumb;
        }    
		
		function _handleEventViewSiteList(){
			
			sectionTitle = "CMS Site List";
			
			$state.go('main_siteList');
			
			CmsServices
				.getCmsSites()
				.then(_handleCmsSiteDataCallback);			
			
		}
		
		/**
		 * Process cms site data from server
		 */
		function _handleCmsSiteDataCallback(siteData){
			
			if(siteData.error){
				$log.debug("Error, " + siteData.error);
			}else{
				
				$log.debug("got site data => " + JSON.stringify(siteData));
				
				var newSiteList = [];
				cmsSiteList = [{ "name": "loading..."}];
				
				if(siteData != null && siteData[0]){
					
					currentSite.setData(siteData[0]);
					
					for( var siteIndex = 0; siteIndex < siteData.length; siteIndex++ ){
						var newSiteEntry = new CmsSite();
						newSiteEntry.setData(siteData[siteIndex]);
						newSiteList.push(newSiteEntry);						
					}
					
					cmsSiteList = newSiteList;
					
					_handleLoadSiteStores(currentSite);
					
					_leftNavClose();
				}

				//$log.debug('current site = ' + JSON.stringify(currentSite));
				//$log.debug('current site list = ' + JSON.stringify(cmsSiteList));
			}			
			
		}
		
		/**
		 * Load the offline and online resource stores for the cms site
		 */
		function _handleLoadSiteStores(cmsSite){
			
			if(cmsSite && cmsSite.onlineStore && cmsSite.offlineStore){
				
				var onlineStoreId = cmsSite.onlineStore.id;
				var offlineStoreId = cmsSite.offlineStore.id;
				
				//$log.debug('online store id = ' + onlineStoreId + ', offline store id = ' + offlineStoreId);
				
				_fetchStore(offlineStoreId, _processOfflineStoreData);
				_fetchStore(onlineStoreId, _processOnlineStoreData);
				
			}else{
				$log.error('Error, cannot load online and offline resource stores for cms site. Site object is missing requires data');
				$log.error('cms site => ' + JSON.stringify(cmsSite));
			}
			
		}
            
        function _processOfflineStoreData(store){
         
            $log.debug('processing offline store => ' + JSON.stringify(store));
            
            _setOfflineStore(store);
            
            // load offline directory
            _fetchDirectory(store.rootDirectoryId, _processOfflineDirectoryData);
            
        }
            
        function _processOnlineStoreData(store){
         
            $log.debug('processing online store => ' + JSON.stringify(store));
            
            _setOnlineStore(store);
            
            // load online directory
            _fetchDirectory(store.rootDirectoryId, _processOnlineDirectoryData);
            
        }
		
		/**
		 * Fetch resource store data
		 *
		 * storeId - id of store to fetch
		 * storeHandler - callback handler. a new resource store object with all the store data is passed off to this function
		 */
		function _fetchStore(storeId, storeHandler){

			$log.debug('fecthing resource store with id => ' + storeId);
		
			FileServices
				.getResourceStoreById(storeId)
				.then( function( storeData ) {
						if (storeData.error){
							$log.debug("Error, " + storeData.error);
						} else {
							if(storeData && storeData.rootDirectoryId){
								
								var newStore = new ResourceStore({
									name: 'Loading...',
									dateCreated: 'Loading...',
									dateUpdated: 'Loading...'
								});
								newStore.setData(storeData);								
								
								if(typeof storeHandler === "function"){
									storeHandler(newStore);
								}
								
							}else{
								$log.error('Error, no store data, or no root directory id for store...');
							}
						}
					}
				);
			
		}
            
        function _processOnlineDirectoryData(directory){
            
            $log.debug('processing online dir => ' + JSON.stringify(directory));
            
            _setOnlineDirectory(directory);
            
            _fetchBreadcrumb(directory.dirId, _processOnlineBreadcrumb);
            
        }
            
        function _processOfflineDirectoryData(directory){
            
            $log.debug('processing offline dir => ' + JSON.stringify(directory));
            
            _setOfflineDirectory(directory);
            
            _fetchBreadcrumb(directory.dirId, _processOfflineBreadcrumb);
            
        }

        /**
         * Fetch directory data from server
         *
         * dirId - the id of the directory to fetch
         * directoryHandler - callback handler, a new path resource object with the directory data will be passed off to this function
         */
        function _fetchDirectory(dirId, directoryHandler){
			
            // fetch directory listing with max depth 1
			FileServices
				.getDirectoryListing(dirId, 1)
				.then( function( directoryData ) {
					
						if (directoryData.error){
							$log.debug("Error, " + directoryData.error);
						} else {
							
							//$log.debug("got directory data => " + JSON.stringify(directoryData));
							
							// create path resource for directory
							var directory = new PathResource({
								name: 'Loading...'
							});
							directory.setData(directoryData);
							
							// build path resource for each child
							var childResource;
							var childPathResources = [];
							for(childIndex = 0; childIndex < directory.children.length; childIndex++){
								childResource = new PathResource();
								childResource.setData(directory.children[childIndex]);
								childPathResources.push(childResource);
							}
							directory.children = childPathResources;
							
                            //$log.debug('dir data from server => ' + JSON.stringify(directory))
                            
							// update current directory model
                            if(typeof directoryHandler === "function"){
                                directoryHandler(directory);
                            }
							
							//_handleLoadBreadcrumb(directoryData.dirId);
							
							//sectionTitle = currentStore.name;
							
							//isLoadingPathResource = false;
							
						}
					}
				);
            
        };
            
        function _processOfflineBreadcrumb(crumb){
            
            _setOfflineBreadcrumb(crumb);
            
        }
            
        function _processOnlineBreadcrumb(crumb){
            
            _setOnlineBreadcrumb(crumb);
            
        }
        
        /**
         * Fetch breadcrumb tree for specific directory
         *
         * dirId - id of the directory
         * crumbHandler - callback method, the crumb data will be passed to this function
         */
		function _fetchBreadcrumb(dirId, crumbHandler){
			
			FileServices
				.getBreadcrumb(dirId)
				.then( function( directoryData ) {
						if (directoryData.error){
							$log.debug("Error, " + directoryData.error);
						} else {
							
							//$log.debug("got breadcrumb data => " + JSON.stringify(directoryData));
							
							// var breadcrumbNav = [{"dirId": "empty", "name": "empty"}];
							
							var breadcrumbNav = [];
							var crumb = {};
							var currentDir = directoryData;
							if(currentDir.hasOwnProperty('dirId') && currentDir.hasOwnProperty('name')){
								crumb.dirId = currentDir.dirId;
								crumb.name = currentDir.name;
								breadcrumbNav.push(crumb);								
							}
							//$log.debug('added crumb: ' + JSON.stringify(directoryData));
							while(currentDir.hasOwnProperty('children') && currentDir.children.length > 0){
								
								currentDir = currentDir.children[0];
								
								crumb = {};
								crumb.dirId = currentDir.dirId;
								crumb.name = currentDir.name;
								breadcrumbNav.push(crumb);
								
								//$log.debug('added crumb: ' + JSON.stringify(directoryData));
								
							}
                            
                            if(typeof crumbHandler === "function"){
                                crumbHandler(breadcrumbNav);
                            }
						
						}
					}
				);			
			
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
					
					CmsServices
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
			
			//alert('you clicked on a site - load the resources view');
			
			$state.go('main_siteResources');
			
		}
		
		function _handleEventClickTablePathResource(pathResource){
			if(pathResource.type == 'FILE'){
				// pathResource.fileId
				alert('You clicked on a file');
			}else if(pathResource.type == 'DIRECTORY'){
				// pathResource.dirId
				alert('You clicked on a directory');
			}			
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
			offlineDirectory : _offlineDirectory,
			onlineDirectory : _onlineDirectory,
			offlineResourceStore : _offlineResourceStore,
			onlineResourceStore : _onlineResourceStore,
            offlineBreadcrumb : _offlineBreadcrumb,
            onlineBreadcrumb : _onlineBreadcrumb,
		
			handleEventViewSiteSettings : _handleEventViewSiteSettings,
			
			handleEventViewSiteList : _handleEventViewSiteList,
			
			handleEventClickCancelSiteSettings : _handleEventClickCancelSiteSettings,
			
            handleEventClickNewCmsSite : _handleEventClickNewCmsSite,
            
            handleEventClickSiteTable : _handleEventClickSiteTable,
			
			handleEventClickTablePathResource : _handleEventClickTablePathResource
		}

	}

})();