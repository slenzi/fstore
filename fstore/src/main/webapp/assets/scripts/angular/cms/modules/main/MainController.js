(function(){

	angular
		.module('fsCmsMain')
		.controller('mainController',[
			'appConstants', 'CmsServices', 'CmsSite', 'FileServices', 'ResourceStore', 'PathResource', 'FsClipboard', 'FsFileUploader',
			'$state', '$stateParams', '$mdSidenav', '$mdDialog', '$mdBottomSheet', '$mdUtil', '$log', '$q', '$scope', MainController
			]
		);
		
	// 'mainService'  mainService  - No longer use main services. Moved all services to external module called fstore-services-module

	function MainController(
		appConstants, CmsServices, CmsSite, FileServices, ResourceStore, PathResource, FsClipboard, FsFileUploader, $state, $stateParams, $mdSidenav, $mdDialog, $mdBottomSheet, $mdUtil, $log, $q, $scope) {
   
   
		/****************************************************************************************
		 * Internal models bound to UI
		 */
		var sectionTitle = "Loading...";
		var cmsSiteList = [{ "name": "Loading..."}];
		
		var myFsUploader = new FsFileUploader({
			url: appConstants.httpUploadHandler
        });		
		
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
		
		$scope.selectedResourceTabIndex = 0;
		var isViewingOnline = false; // true if viewing online, false if viewing offline (switch this value when user clicks 'Offline Resources' and 'Online Resources' tabs) 

		// for toggle on left-hand nav bar
		$scope.session = {
			isViewingOnline: true
		};		
		
		var clipboard = new FsClipboard({});
		
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
		 * Fetch 'content' value of meta tag by either 'name' or 'property attribute'
		 */
		function getContentByMetaTagName(c) {
			for (var b = document.getElementsByTagName("meta"), a = 0; a < b.length; a++) {
				if (c == b[a].name || c == b[a].getAttribute("property")) { return b[a].content; }
			}
			return false;
		}		

		/**
		 * Get reference to fsUploader
		 */
		function _uploader(){
			return myFsUploader;
		}

		function _handleEventViewUploadForm(){
			$state.go('main_upload');
		}

		/**
		 * Handle cancel upload button click
		 */
        function _handleEventClickCancelUpload(){
			$state.go('main_siteResources');
        }

		/**
		 * Clear upload queue for FsFileUploader
		 */
		function _handleEventClearUploadQueue(){
			myFsUploader.clearQueue();
		}

		/**
		 * Trigger FsFileUploader to start uploading
		 */
		function _handleEventDoUpload(event){
			if(_isViewingOnline()){
				_handleEventStartUpload(event, true);
			}else{
				_handleEventStartUpload(event, false);
			}
		}
		function _handleEventStartUpload(event, isOnline){
			
				var confirm = $mdDialog.confirm()
					.parent(angular.element(document.body))
					.title('Upload Confirmation')
					.content("Please confirm upload.")
					.ariaLabel('Continue Upload')
					.ok('Continue')
					.cancel('Cancel')
					.targetEvent(event);
				
				$mdDialog.show(confirm).then(function() {
					
					var uploadDirId = 0;
					if(isOnline){
						uploadDirId = currentOnlineDirectory.dirId;
					}else{
						uploadDirId = currentOfflineDirectory.dirId;
					}
					
					// set parent dir id so we know where to create the new directory on the server
					myFsUploader.addFormValue('dirId', uploadDirId);
					
					// add Spring Security Cross Site Request Forgery (CSRF) token
					// We store the token in a meta tag on every page. (see common header jsp)
					// Also stored as a hidden input field on every page
					myFsUploader.addFormValue('_csrf', getContentByMetaTagName('_csrf'));					
					
					// upload all files in queue as one single upload
					//myFsUploader.doUpload(_uploadProgressHandler, _uploadAllCompleteHandler);
					
					// upload all files in queue as separate, individual uploads.
					myFsUploader.doUploadSingular(_uploadProgressHandler, _uploadSingleCompleteHandler, _uploadAllCompleteHandler);
						
				}, function() {
					
					$log.debug('Uploade operation canceled.');
					
				});
			
		}
		function _uploadProgressHandler(event){
			var progressValue = Math.round(event.lengthComputable ? event.loaded * 100 / event.total : 0);
			//$log.debug('main progress = ' + progressValue);
			$scope.$apply();
		}
        function _uploadSingleCompleteHandler(event){
           
            $log.debug('Upload of single file complete.');
            
        }
		function _uploadAllCompleteHandler(event){
			
			$log.debug('Upload completed. Have event obj? => ' + event);
            
            myFsUploader.clearQueue();
			
			$scope.$apply();
			
			$state.go('main_siteResources');
			
			alert('All files have been received on the server. Please note large file take time to procee. Refresh view to see latest files.');
			
			if(_isViewingOnline()){
				_fetchDirectory(currentOnlineDirectory.dirId, _processOnlineDirectoryData);
			}else{
				_fetchDirectory(currentOfflineDirectory.dirId, _processOfflineDirectoryData);
			}
			
		}		
		
		/**
		 * Get current section title
		 */
		function _sectionTitle(){
			return sectionTitle;
		}
		
		function _isViewingOnline(){
			return isViewingOnline;
		}		
		function _setIsViewingOnline(isOnline){
			isViewingOnline = isOnline;
			if(isOnline){
				$scope.selectedResourceTabIndex = 1;
			}else{
				$scope.selectedResourceTabIndex = 0;
			}
		}
		function _selectedResourceTabIndex(){
			return selectedResourceTabIndex;
		}
		
		/**
		 * For the CMS view mode toggle on the left nav bar
		 */
		function _setCmsViewMode(onlineViewMode){
			
			$log.debug('_setCmsViewMode => ' + onlineViewMode);
			
			if(onlineViewMode){
				
				_setIsViewingOnline(true);
				
				
				CmsServices
					.setCmsViewMode("ONLINE")
					.then(_cmsViewModeDataCallback);				
				
			}else{
				
				_setIsViewingOnline(false);
				
				CmsServices
					.setCmsViewMode("OFFLINE")
					.then(_cmsViewModeDataCallback);					
				
			}
			
		}

		function _cmsViewModeDataCallback(){
			
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
         * Handle event click for offline breadcrumb navigation
         */
        function _handleEventClickOfflineBreadcrumb(crumb){
            
			_fetchDirectory(crumb.dirId ,_processOfflineDirectoryData);
			
        }
         
        /**
         * Handle event click for online breadcrumb navigation
         */
        function _handleEventClickOnlineBreadcrumb(crumb){
            
			_fetchDirectory(crumb.dirId ,_processOnlineDirectoryData);
			
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
							_handleEventViewSiteList();
							
							$mdDialog.hide();
							
						});	
					
				}				
			}
			
		}
		
		function _handleEventClickSiteTable(siteData){
	
			_handleLoadSiteStores(siteData);
			
			sectionTitle = siteData.name;
			
			$state.go('main_siteResources');
			
		}
		
		/**
		 * Handle click of path resource in our smart table
		 */
		function _handleEventClickTableOfflinePathResource(pathResource){
			_handleEventClickTablePathResource(pathResource, false);
		}
		function _handleEventClickTableOnlinePathResource(pathResource){
			_handleEventClickTablePathResource(pathResource, true);
		}
		function _handleEventClickTablePathResource(pathResource, isOnline){
			if(isOnline){
				if(pathResource.type == 'FILE'){
					// pathResource.fileId
					FileServices.downloadFile(pathResource.fileId);
				}else if(pathResource.type == 'DIRECTORY'){
					// pathResource.dirId
					_fetchDirectory(pathResource.dirId, _processOnlineDirectoryData);
				}				
			}else{
				if(pathResource.type == 'FILE'){
					// pathResource.fileId
					FileServices.downloadFile(pathResource.fileId);
				}else if(pathResource.type == 'DIRECTORY'){
					// pathResource.dirId
					_fetchDirectory(pathResource.dirId, _processOfflineDirectoryData);
				}				
			}
		}

		/**
		 * smart table will add a property to the values it's displaying called isSelected, and set it to true
		 */
		function _haveSelectedOfflinePathResources(){
			return _haveSelectedPathResources(false);
		}
		function _haveSelectedOnlinePathResources(){
			return _haveSelectedPathResources(true);
		}
		function _haveSelectedPathResources(isOnline){
			if(isOnline){
				if(currentOnlineDirectory && currentOnlineDirectory.children){
					for(i=0; i<currentOnlineDirectory.children.length; i++){
						if(currentOnlineDirectory.children[i].isSelected){
							return true;
						}
					}				
				}
			}else{
				if(currentOfflineDirectory && currentOfflineDirectory.children){
					for(i=0; i<currentOfflineDirectory.children.length; i++){
						if(currentOfflineDirectory.children[i].isSelected){
							return true;
						}
					}				
				}
			}
		}
		
		/**
		 * unselect all resources in current working directory (offline or online)
		 */
		function _handleEventClickClearSelectedOfflinePathResources(){
			_handleEventClickClearSelectedPathResources(false);			
		}
		function _handleEventClickClearSelectedOnlinePathResources(){
			_handleEventClickClearSelectedPathResources(true);
		}
		function _handleEventClickClearSelectedPathResources(isOnline){
			if(isOnline){
				if(currentOnlineDirectory && currentOnlineDirectory.children){
					for(i=0; i<currentOnlineDirectory.children.length; i++){
						if(currentOnlineDirectory.children[i].isSelected){
							currentOnlineDirectory.children[i].isSelected = false;
						}
					}				
				}
			}else{
				if(currentOfflineDirectory && currentOfflineDirectory.children){
					for(i=0; i<currentOfflineDirectory.children.length; i++){
						if(currentOfflineDirectory.children[i].isSelected){
							currentOfflineDirectory.children[i].isSelected = false;
						}
					}				
				}				
			}
		}

		/**
		 * select all resources in current working directory (offline or online)
		 */
		function _handleEventClickSelectAllOfflinePathResources(){
			_handleEventClickSelectAllPathResources(false);
		}
		function _handleEventClickSelectAllOnlinePathResources(){
			_handleEventClickSelectAllPathResources(true);
		}
		function _handleEventClickSelectAllPathResources(isOnline){
			if(isOnline){
				if(currentOnlineDirectory && currentOnlineDirectory.children){
					for(i=0; i<currentOnlineDirectory.children.length; i++){
						currentOnlineDirectory.children[i].isSelected = true;
					}				
				}				
			}else{
				if(currentOfflineDirectory && currentOfflineDirectory.children){
					for(i=0; i<currentOfflineDirectory.children.length; i++){
						currentOfflineDirectory.children[i].isSelected = true;
					}				
				}				
			}
		}

		/**
		 * Check if current working directory (offline or online) has any child resources
		 */
		function _haveOfflineChildPathResources(){
			return _haveChildPathResources(false);
		}
		function _haveOnlineChildPathResources(){
			return _haveChildPathResources(true);
		}
		function _haveChildPathResources(isOnline){
			if(isOnline){
				return currentOnlineDirectory && currentOnlineDirectory.children && (currentOnlineDirectory.children.length > 0);
			}else{
				return currentOfflineDirectory && currentOfflineDirectory.children && (currentOfflineDirectory.children.length > 0);
			}
		}

		/**
		 * Create new folder
		 */
		function _handleEventClickNewOfflineFolder(event){			
			_handleEventClickNewFolder(event, false);
		}
		function _handleEventClickNewOnlineFolder(event){
			_handleEventClickNewFolder(event, true);
		}
		function _handleEventClickNewFolder(event, isOnline){
			
			var dialogTitle = isOnline ? 'Create New Online Folder' : 'Create New Offline Folder';
			
			$mdDialog.show({
				parent: angular.element(document.body),
				targetEvent: event,
				template:
					'<md-dialog aria-label="List dialog" flex="35">' +
					'	<md-dialog-content>'+
					'		<md-content layout-padding>' +
					'			<h3>' + dialogTitle + '</h3>' +
					'			<h4>For site \'' + currentSite.name + '\'</h4>' +					
					'			<md-input-container flex>' +
					'				<label>Folder Name</label>' +
					'				<input ng-model="newFolderDialog.newFolderName" required>' +
					'			</md-input-container>' +
					'		</md-content>' +
					'  </md-dialog-content>' + 					
					'  <div class="md-actions">' +
					'    <md-button ng-click="closeDialog()" class="md-primary">' +
					'      Cancel' +
					'    </md-button>' +
					'    <md-button ng-click="createFolder()" class="md-primary">' +
					'      Create' +
					'    </md-button>' +					
					'  </div>' +
					'</md-dialog>',
				controller: _createFolderDialogController
			});
		
			function _createFolderDialogController($scope, $mdDialog) {
				$scope.closeDialog = function() {
					$mdDialog.hide();
				}
				$scope.createFolder = function() {
					
					var parentDirId = 0;
					if(isOnline){
						parentDirId = currentOnlineDirectory.dirId;
					}else{
						parentDirId = currentOfflineDirectory.dirId;
					}
					
					var newFolderName = $scope.newFolderDialog.newFolderName;
					
					FileServices
						.addDirectory(parentDirId, newFolderName)
						.then( function( reply ) {
							
							$log.debug('add directory reply: ' + JSON.stringify(reply));
							
							if(isOnline){
								_fetchDirectory(parentDirId ,_processOnlineDirectoryData);
							}else{
								_fetchDirectory(parentDirId ,_processOfflineDirectoryData);
							}
							
							$mdDialog.hide();
							
						});
				}				
			}
		}
		
		/**
		 * Clipboard methods
		 */
		function _haveClipboardResources(){
			return clipboard && !clipboard.isEmpty();
		}
		function _handleEventClickClearClipboard(){
			if(clipboard){
				clipboard.clear();
			}
		}  
		function _handleEventClickCopyOnlinePathResources(event){
			_doClipboardOperationOnSelectedResources('copy', true, event);
		}
		function _handleEventClickCopyOfflinePathResources(event){
			_doClipboardOperationOnSelectedResources('copy', false, event);
		}		
		function _handleEventClickCutOnlinePathResources(event){
			_doClipboardOperationOnSelectedResources('cut', true, event);
		}
		function _handleEventClickCutOfflinePathResources(event){
			_doClipboardOperationOnSelectedResources('cut', false, event);
		}		
		function _doClipboardOperationOnSelectedResources(operationType, isOnline, event){
			
			var fileIdList = [];
			var dirIdList = [];
			
			var currentDirectory;
			if(isOnline){
				currentDirectory = currentOnlineDirectory;
			}else{
				currentDirectory = currentOfflineDirectory;
			}
		
			if(currentDirectory && currentDirectory.children){
				for(i=0; i<currentDirectory.children.length; i++){
					if(currentDirectory.children[i].isSelected){
						
						pathResource = currentDirectory.children[i];
						if(pathResource.type == 'FILE'){
							fileIdList.push(pathResource.fileId);
						}else if(pathResource.type == 'DIRECTORY'){
							dirIdList.push(pathResource.dirId);
						}else{
							$log.error('Unknown path resource type \'' + pathResource.type + '\'. Don\'t know how to \'' + operationType + '\'.');
						}						
						
					}
				}
			}
			
			clipboard.setOperation(operationType, fileIdList, dirIdList, currentDirectory.dirId, -1, true);	
			
			$log.debug('clipboard => ' + JSON.stringify(clipboard));
			$log.debug('is empty => ' + clipboard.isEmpty());			
			
		}

		/**
		 * Handle clipboard paste event (copy-paste or cut-paste)
		 */
		function _handleEventClickPasteOnlinePathResources(event){
			_handleEventClickPastePathResources(true, event);
		}
		function _handleEventClickPasteOfflinePathResources(event){
			_handleEventClickPastePathResources(false, event);
		}
		function _handleEventClickPastePathResources(isOnline, event){
			
			var operation = clipboard.operation;
			var operationType = clipboard.operation.type;
			
			var currentDirectory;
			if(isOnline){
				currentDirectory = currentOnlineDirectory;
			}else{
				currentDirectory = currentOfflineDirectory;
			}			
			
			if(operation.data.sourceDirId && operation.data.sourceDirId == currentDirectory.dirId){
				alert('Cannot paste. Source and target directories are the same. Please navigate to a different directory');
			}else{
				
				// perform copy operation
				if(operationType.toLowerCase() == 'copy'){
					
					_handleCopyPaste(isOnline, event);
	
				// perform cut (move) operation
				}else if(operationType.toLowerCase() == 'cut'){
					
					_handleCutPaste(isOnline, event);
					
				}else{
					
					alert('Cannot paste. Unknown operation type. Type = \'' + operationType + '\'');
					
				}
				
			}
			
		}
		
		/**
		 * Handle copy-paste event
		 */
		function _handleCopyPaste(isOnline, event){
			
			var operation = clipboard.operation;
			
			var currentDirectory;
			if(isOnline){
				currentDirectory = currentOnlineDirectory;
			}else{
				currentDirectory = currentOfflineDirectory;
			}			
			
			var fileIdList = operation.data.fileIdList;
			var dirIdList = operation.data.dirIdList;
			var targetDirId = operation.data.targetDirId;
			var replaceExisting = operation.data.replaceExisting;
			
			var haveFilesToCopy = fileIdList.length > 0;
			var haveDirectoriesToCopy = dirIdList.length > 0;
			
			if(haveFilesToCopy && haveDirectoriesToCopy){
				
				FileServices
					.copyFiles(fileIdList, currentDirectory.dirId)
					.then( function( reply ) {
						$log.debug('copy files reply: ' + JSON.stringify(reply));
						return FileServices
							.copyDirectories(dirIdList, currentDirectory.dirId)
							.then( function( reply ) {
								$log.debug('copy directories reply: ' + JSON.stringify(reply));
							});
					}).then( function( result ) {
						_handleEventClickClearClipboard();
						if(isOnline){
							_fetchDirectory(currentDirectory.dirId ,_processOnlineDirectoryData);
						}else{
							_fetchDirectory(currentDirectory.dirId ,_processOfflineDirectoryData);
						}
					});						
				
			}else if(haveFilesToCopy){
				
				FileServices
					.copyFiles(fileIdList, currentDirectory.dirId)
					.then( function( reply ) {
						$log.debug('copy files reply: ' + JSON.stringify(reply));
						_handleEventClickClearClipboard();
						if(isOnline){
							_fetchDirectory(currentDirectory.dirId ,_processOnlineDirectoryData);
						}else{
							_fetchDirectory(currentDirectory.dirId ,_processOfflineDirectoryData);
						}
					});					
				
			}else if(haveDirectoriesToCopy){
				
				FileServices
					.copyDirectories(dirIdList, currentDirectory.dirId)
					.then( function( reply ) {
						$log.debug('copy directories reply: ' + JSON.stringify(reply));
						_handleEventClickClearClipboard();
						if(isOnline){
							_fetchDirectory(currentDirectory.dirId ,_processOnlineDirectoryData);
						}else{
							_fetchDirectory(currentDirectory.dirId ,_processOfflineDirectoryData);
						}
					});						
				
			}else{
				
			}			
			
		}
		
		/**
		 * Handle cut-paste event
		 */
		function _handleCutPaste(isOnline, event){
			
			var operation = clipboard.operation;
			
			var currentDirectory;
			if(isOnline){
				currentDirectory = currentOnlineDirectory;
			}else{
				currentDirectory = currentOfflineDirectory;
			}			
			
			var fileIdList = operation.data.fileIdList;
			var dirIdList = operation.data.dirIdList;
			var targetDirId = operation.data.targetDirId;
			var replaceExisting = operation.data.replaceExisting;
			
			var haveFilesToCopy = fileIdList.length > 0;
			var haveDirectoriesToCopy = dirIdList.length > 0;
			
			if(haveFilesToCopy && haveDirectoriesToCopy){
				
				FileServices
					.moveFiles(fileIdList, currentDirectory.dirId)
					.then( function( reply ) {
						$log.debug('move files reply: ' + JSON.stringify(reply));
						return FileServices
							.moveDirectories(dirIdList, currentDirectory.dirId)
							.then( function( reply ) {
								$log.debug('move directories reply: ' + JSON.stringify(reply));
							});
					}).then( function( result ) {
						_handleEventClickClearClipboard();
						if(isOnline){
							_fetchDirectory(currentDirectory.dirId ,_processOnlineDirectoryData);
						}else{
							_fetchDirectory(currentDirectory.dirId ,_processOfflineDirectoryData);
						}
					});						
				
			}else if(haveFilesToCopy){
				
				FileServices
					.moveFiles(fileIdList, currentDirectory.dirId)
					.then( function( reply ) {
						$log.debug('move files reply: ' + JSON.stringify(reply));
						_handleEventClickClearClipboard();
						if(isOnline){
							_fetchDirectory(currentDirectory.dirId ,_processOnlineDirectoryData);
						}else{
							_fetchDirectory(currentDirectory.dirId ,_processOfflineDirectoryData);
						}
					});					
				
			}else if(haveDirectoriesToCopy){
				
				FileServices
					.moveDirectories(dirIdList, currentDirectory.dirId)
					.then( function( reply ) {
						$log.debug('move directories reply: ' + JSON.stringify(reply));
						_handleEventClickClearClipboard();
						if(isOnline){
							_fetchDirectory(currentDirectory.dirId ,_processOnlineDirectoryData);
						}else{
							_fetchDirectory(currentDirectory.dirId ,_processOfflineDirectoryData);
						}
					});						
				
			}else{
				
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
			
			uploader : _uploader,
			handleEventViewUploadForm : _handleEventViewUploadForm,
			handleEventClickCancelUpload : _handleEventClickCancelUpload,
			handleEventClearUploadQueue : _handleEventClearUploadQueue,
			handleEventDoUpload : _handleEventDoUpload,
			
			selectedResourceTabIndex : _selectedResourceTabIndex,
			isViewingOnline : _isViewingOnline,
			setIsViewingOnline : _setIsViewingOnline,
			setCmsViewMode : _setCmsViewMode,
			
			cmsSiteList : _cmsSiteList,
			
			offlineDirectory : _offlineDirectory,
			onlineDirectory : _onlineDirectory,
			
			offlineResourceStore : _offlineResourceStore,
			onlineResourceStore : _onlineResourceStore,
			
            offlineBreadcrumb : _offlineBreadcrumb,
            onlineBreadcrumb : _onlineBreadcrumb,
			
			handleEventViewUploadForm : _handleEventViewUploadForm,
			
			haveOfflineChildPathResources : _haveOfflineChildPathResources,
			haveOnlineChildPathResources : _haveOnlineChildPathResources,
			haveSelectedOfflinePathResources : _haveSelectedOfflinePathResources,
			haveSelectedOnlinePathResources : _haveSelectedOnlinePathResources,
			handleEventClickClearSelectedOfflinePathResources : _handleEventClickClearSelectedOfflinePathResources,
			handleEventClickClearSelectedOnlinePathResources : _handleEventClickClearSelectedOnlinePathResources,
			handleEventClickSelectAllOfflinePathResources : _handleEventClickSelectAllOfflinePathResources,
			handleEventClickSelectAllOnlinePathResources : _handleEventClickSelectAllOnlinePathResources,
			handleEventClickNewOfflineFolder : _handleEventClickNewOfflineFolder,
			handleEventClickNewOnlineFolder : _handleEventClickNewOnlineFolder,
		
			handleEventViewSiteSettings : _handleEventViewSiteSettings,
			
			handleEventViewSiteList : _handleEventViewSiteList,
			
			handleEventClickCancelSiteSettings : _handleEventClickCancelSiteSettings,
			
            handleEventClickNewCmsSite : _handleEventClickNewCmsSite,
            
            handleEventClickSiteTable : _handleEventClickSiteTable,
			
			handleEventClickTableOfflinePathResource : _handleEventClickTableOfflinePathResource,
			handleEventClickTableOnlinePathResource : _handleEventClickTableOnlinePathResource,
            
            handleEventClickOfflineBreadcrumb : _handleEventClickOfflineBreadcrumb,
            handleEventClickOnlineBreadcrumb : _handleEventClickOnlineBreadcrumb,
			
			haveClipboardResources : _haveClipboardResources,
			handleEventClickClearClipboard : _handleEventClickClearClipboard,
			handleEventClickCopyOnlinePathResources : _handleEventClickCopyOnlinePathResources,
			handleEventClickCopyOfflinePathResources : _handleEventClickCopyOfflinePathResources,
			handleEventClickCutOnlinePathResources : _handleEventClickCutOnlinePathResources,
			handleEventClickCutOfflinePathResources : _handleEventClickCutOfflinePathResources,
			handleEventClickPasteOnlinePathResources : _handleEventClickPasteOnlinePathResources,
			handleEventClickPasteOfflinePathResources : _handleEventClickPasteOfflinePathResources
		}

	}

})();