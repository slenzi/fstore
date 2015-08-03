(function(){

	angular
		.module('home')
		.controller('homeController',[
			'appConstants', 'homeService', 'ResourceStore', 'PathResource', 'FsFileUploader', 'FsStomp',
			'$state', '$stateParams', '$mdSidenav', '$mdDialog', '$mdBottomSheet', '$mdUtil', '$log', '$q', '$scope', HomeController
			]
		);

	function HomeController(
		appConstants, homeService, ResourceStore, PathResource, FsFileUploader, FsStomp, $state, $stateParams, $mdSidenav, $mdDialog, $mdBottomSheet, $mdUtil, $log, $q, $scope) {
   
		// internal models bound to UI
		var sectionTitle = "Not set";
		var storeList = [{ "name": "empty"}];
		var breadcrumbNav = [{"dirId": "empty", "name": "empty"}];
		var currentDirectory = new PathResource({
			name: 'Loading...',
			dateCreated: 'Loading...',
			dateUpdated: 'Loading...'
		});
		var currentStore = new ResourceStore({
			name: 'Loading...',
			dateCreated: 'Loading...',
			dateUpdated: 'Loading...'
		});
		var myFsUploader = new FsFileUploader({
			url: appConstants.httpUploadHandler
        });
		var myStomp;

		/*
		var socket = {
			client: null, // SockJS object
			stomp: null // Stomp object
		};
		*/

		//
		// load all resource stores when page loads (asynchronously)
		//
		_handleOnPageLoad();

		/**
		 * Fetch all resource stores from server and pre-load first one (if one exists)
		 */
		function _handleOnPageLoad(){
			
			_handleEventViewStoreList();
			
			_doWebSocketTest();
			
		}	
		
		function _doWebSocketTest(){
			$log.debug('peforming websocket test');
			_initSocket();	
		}
		function _initSocket(){
			
			$log.debug('initializing websocket');
			
			myStomp = new FsStomp({
				sockJsUrl: '/fstore/spring/hello'
			});
			myStomp.setDebug(_myStompDebug);
			myStomp.connect(_myStompConnect, _myStompConnectError);
			
		}
		function _handleEventSendSampleStomp(){
			myStomp.send('/app/hello', {}, JSON.stringify({ 'message': 'this is a test' }));
		}
		function _myStompDebug(str){
			$log.debug('my stomp debug = ' + str);	
		}		
		function _myStompConnect(frame){
			var testSubscription = myStomp.subscribe('/topic/tests', _myStompReceiveTestMessages);
			var echoSubscription = myStomp.subscribe('/topic/echos', _myStompReceiveEchoMessages);
		}
		function _myStompConnectError(error){
			$log.debug('_onStompConnectError...');
			//$log.debug(error.headers.message);
			$log.debug('error = ' + JSON.stringify(error));
		}
		function _myStompReceiveTestMessages(socketMessage){
			$log.info('message = ' + JSON.stringify(socketMessage));
		}
		function _myStompReceiveEchoMessages(socketMessage){
			$log.info('message = ' + JSON.stringify(socketMessage));
		}		
		
		/**
		 * Say hello
		 */
		function _doHello(){
			alert("hello from home controller");
			// $mdSidenav('MyLeftNav').toggle();
			// $mdBottomSheet.hide()
		}
		
		/**
		 * Fetch sample ui-grid
		 */
		 /*
		 self.sampleGrid = sampleGrid;
		var sampleGridData = [
			{ "Store Name": "Example Store 1" },
			{ "Store Name": "Example Store 2" },
			{ "Store Name": "Example Store 3" },
			{ "Store Name": "Example Store 4" },
			{ "Store Name": "Example Store 5" },
			{ "Store Name": "Example Store 6" },
			{ "Store Name": "Example Store 7" },
			{ "Store Name": "Example Store 8" },
			{ "Store Name": "Example Store 9" },
		];
		var sampleGrid = {
			paginationPageSizes: [25, 50, 75],
			paginationPageSize: 25,
			columnDefs: [
			  { name: 'Store Name' },
			],
			data: sampleGridData
		  };		 
		function sampleGrid(){
			return sampleGrid;
		}
		*/
		
		/**
		 * Get current section title
		 */
		function _sectionTitle(){
			return sectionTitle;
		}
		
		/**
		 * Get list of stores
		 */
		function _storeList(){
			return storeList;
		}
		
		/**
		 * Get current store being viewed
		 */		
		function _currentStore(){
			return currentStore;
		}		
		
		/**
		 * Get current directory being viewed
		 */
		function _currentDirectory(){
			return currentDirectory;
		}
		
		/**
		 * Get reference to fsUploader
		 */
		function _uploader(){
			return myFsUploader;
		}
		
		/**
		 * Get current breadcrumb navigation
		 */
		function _breadcrumb(){
			return breadcrumbNav;
		}
		
        /**
         * Show the upload form.
         */
        function _handleEventViewUploadForm(){
        	$state.go('home_upload');
        }
		
		/**
		 * Handle cancle upload button click
		 */
        function _handleEventClickCancelUpload(){
        	$state.go('home_directory');
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
		function _handleEventDoUpload(){
			// pass in optional callback for progress
			myFsUploader.doUpload(_uploadProgressHandler, _uploadCompleteHandler);
		}
		function _uploadProgressHandler(event){
			var progressValue = Math.round(event.lengthComputable ? event.loaded * 100 / event.total : 0);
			//$log.debug('Home progress = ' + progressValue);
			$scope.$apply();
		}
		function _uploadCompleteHandler(event){
			
			$log.debug('Upload completed.');
			
			$scope.$apply();
			
			alert('Upload complete. Thank you.');
			
			_handleLoadDirectory(currentDirectory.dirId, true);
			
			//$state.go('home_directory');
		}

		/**
		 * Check if any path resources are selected
		 */
		function _haveSelectedPathResources(){
			
			if(currentDirectory && currentDirectory.children){
				for(i=0; i<currentDirectory.children.length; i++){
					if(currentDirectory.children[i].isSelected){
						return true;
					}
				}
			}
			return false;
		}
		
		/**
		 * Handle delete of selected path resources
		 */
		function _handleEventClickDeletePathResources(event){
			
			// get types and ids of all selected path resources
			
			var filesToDelete = [];
			var directoriesToDelete = [];
			var pathResource;
			
			if(currentDirectory && currentDirectory.children){
				for(i=0; i<currentDirectory.children.length; i++){
					if(currentDirectory.children[i].isSelected){
						pathResource = currentDirectory.children[i];
						if(pathResource.type == 'FILE'){
							filesToDelete.push(pathResource);
						}else if(pathResource.type == 'DIRECTORY'){
							directoriesToDelete.push(pathResource);
						}else{
							$log.error('Unknown path resource type \'' + pathResource.type + '\'. Don\'t know how to delete.');
						}
					}
				}
			}
			var haveFilesToDelete = (filesToDelete.length > 0) || (directoriesToDelete.length > 0);
			if(haveFilesToDelete){
				var confirmMessage = 'Are you sure you want to delete the resources? The following items will be permanently removed: ';	
				if(filesToDelete.length > 1){
					confirmMessage += '\n' + filesToDelete.length + ' files.';
				}else if(filesToDelete.length == 1){
					confirmMessage += '\n' + filesToDelete.length + ' file.';
				}
				if(directoriesToDelete.length > 1){
					confirmMessage += '\n' + directoriesToDelete.length + ' directories.';
				}else if(directoriesToDelete.length == 1){
					confirmMessage += '\n' + directoriesToDelete.length + ' directory.';
				}
				var confirm = $mdDialog.confirm()
					.parent(angular.element(document.body))
					.title('Delete Confirmation')
					.content(confirmMessage)
					.ariaLabel('Lucky day')
					.ok('Continue with Delete')
					.cancel('Cancel')
					.targetEvent(event);
				$mdDialog.show(confirm).then(function() {
						$log.debug('Proceed with delete!');
					}, function() {
						$log.debug('Delete operation canceled.');
					});					
			}			
		}
		
		/**
		 * Clear selected resource. Loops through all path resources and sets isSelected to false
		 * if the resource is currently selected.
		 */
		function _handleEventClickClearSelectedPathResources(){
			
			if(currentDirectory && currentDirectory.children){
				for(i=0; i<currentDirectory.children.length; i++){
					if(currentDirectory.children[i].isSelected){
						currentDirectory.children[i].isSelected = false;
					}
				}
			}			
			
		}
		
		/**
		 * View list of all stores
		 */
		function _handleEventViewStoreList(){

			sectionTitle = "Resource Store List";
			$state.go('home_storeList');
	
			homeService
				.getResourceStores()
				.then(_handleStoreDataCallback);
				
		}
		
		/**
		 * Helper function for _handleEventViewStoreList. Called when store data is
		 * returned from REST service.
		 *
		 * storeData - data returned from home service getResourceStores() function.
		 */
		function _handleStoreDataCallback(storeData){
			
			if(storeData.error){
				$log.debug("Error, " + storeData.error);
			}else{
				//$log.debug("got store data => " + JSON.stringify(storeData));
				storeList = storeData;
				if(storeList != null && storeList[0]){
					currentStore.setData(storeList[0]);
					_handleLoadDirectory(storeList[0].rootDirectoryId, false);
					
					_leftNavClose();
				}				
			}			
			
		}
		
		/**
		 * View settings for current store
		 */
		function _handleEventViewStoreSettings(){
			$state.go('home_storeSettings');
		}
		
		/**
		 * Handle cancle upload button click
		 */
        function _handleEventClickCancelStoreSettings(){
        	$state.go('home_directory');
        }
		
		/**
		 * pathResource - the path resource the user clicked on (file, directory, etc)
		 */
		function _handleEventClickPathResource(pathResource){
			
			// toggle isSelected attribute
			pathResource.isSelected = !pathResource.isSelected;
			
			/*
			if(pathResource.type == 'FILE'){
				_handleEventClickFile(pathResource);
			}else if(pathResource.type == 'DIRECTORY'){
				_handleEventClickDirectory(pathResource);
			}
			*/
		}

		/**
		 * Event handler for click of file resource
		 */
		function _handleEventClickFile(fileResource){
			alert('You clicked on a file, id = ' + fileResource.fileId);
		}
		
		/**
		 * Event handler for click of directory resource
		 */
		function _handleEventClickDirectory(directoryResource){
			alert('You clicked on a directory, id = ' + directoryResource.dirId);
		}		
		
		/**
		 * pathResource - the path resource the user double clicked on (file, directory, etc)
		 */
		function _handleEventDblClickPathResource(pathResource){
			if(pathResource.type == 'FILE'){
				_handleEventDblClickFile(pathResource);
			}else if(pathResource.type == 'DIRECTORY'){
				_handleEventDblClickDirectory(pathResource);
			}
		}
		
		/**
		 * Event handler for double-click of file resource
		 */
		function _handleEventDblClickFile(fileResource){
			//alert('You double clicked on a file, id = ' + fileResource.fileId);
			homeService.downloadFile(fileResource.fileId)
		}
		
		/**
		 * Event handler for double-click of directory resource
		 */
		function _handleEventDblClickDirectory(directoryResource){
			_handleLoadDirectory(directoryResource.dirId, true);
		}
		
		/**
		 * Event handler for click of directory breadcrumb
		 */
		function _handleEventClickBreadcrumb(crumb){
			_handleLoadDirectory(crumb.dirId, true);
		}
        
		/**
		 * When user clicks on resource store, fetch store data from service.
		 */
		function _handleEventViewStore(storeId){

			homeService
				.getResourceStoreById(storeId)
				.then( function( storeData ) {
						if (storeData.error){
							$log.debug("Error, " + storeData.error);
						} else {
							if(storeData && storeData.rootDirectoryId){
								//$log.debug("got store data => " + JSON.stringify(storeData));
								
								//currentStore.setName(storeData.name);
								
								currentStore.setData(storeData);
								
								_handleLoadDirectory(storeData.rootDirectoryId, true);
								_leftNavClose();
								
								sectionTitle = currentStore.name;
								
							}else{
								$log.error('Error, no store data, or no root directory id for store...');
								alert('Error, no store data, or no root directory id for store...');
							}
						}
					}
				);
			
		}
        
        /**
         * Fetch directory data from server, populate UI
         */
        function _handleLoadDirectory(dirId, showDirectoryPartial){
            
			if(showDirectoryPartial){
				$log.info('show directory partial');
				$state.go('home_directory');
			}
			
            // fetch directory listing with max depth 1
			homeService
				.getDirectoryListing(dirId, 1)
				.then( function( directoryData ) {
					
						if (directoryData.error){
							$log.debug("Error, " + directoryData.error);
						} else {
							
							$log.debug("got directory data => " + JSON.stringify(directoryData));
							
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
							
							// update current directory model
							currentDirectory = directory;
							
							_handleLoadBreadcrumb(directoryData.dirId);
							
							//sectionTitle = currentStore.name;
							
						}
					}
				);
            
        };
		
        /**
         * Fetch breadcrumb / parent tree data for some child directory
         */		
		function _handleLoadBreadcrumb(dirId){
			
			homeService
				.getBreadcrumb(dirId)
				.then( function( directoryData ) {
						if (directoryData.error){
							$log.debug("Error, " + directoryData.error);
						} else {
							
							//$log.debug("got breadcrumb data => " + JSON.stringify(directoryData));
							
							// var breadcrumbNav = [{"dirId": "empty", "name": "empty"}];
							
							breadcrumbNav = [];
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
						
						}
					}
				);			
			
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
		 * Show the bottom sheet
		 */
		function _showContactOptions($event) {

			/*
			var user = self.selected;

			return $mdBottomSheet.show({
			parent: angular.element(document.getElementById('content')),
			templateUrl: './src/users/view/contactSheet.html',
			controller: [ '$mdBottomSheet', ContactPanelController],
			controllerAs: "cp",
			bindToController : true,
			targetEvent: $event
			}).then(function(clickedItem) {
			clickedItem && $log.debug( clickedItem.name + ' clicked!');
			});
			*/

		}
	
		var self = this;
		
		/*
		 * External API
		 */
		return {
			doHello : _doHello,
			showContactOptions : _showContactOptions,
			leftNavClose : _leftNavClose,
			toggleLeftNav : _buildToggler('MyLeftNav'),
			notImplemented : _notImplemented,
			sectionTitle : _sectionTitle,
			store : _currentStore,
			storeList : _storeList,
			directory : _currentDirectory,
			uploader : _uploader,
			breadcrumb : _breadcrumb,
			handleEventViewStore : _handleEventViewStore,
			handleEventViewStoreSettings : _handleEventViewStoreSettings,
			handleEventViewStoreList : _handleEventViewStoreList,
			handleEventClickPathResource : _handleEventClickPathResource,
			handleEventDblClickPathResource : _handleEventDblClickPathResource,
			handleEventClickBreadcrumb : _handleEventClickBreadcrumb,
			handleEventViewUploadForm : _handleEventViewUploadForm,
			handleEventClearUploadQueue : _handleEventClearUploadQueue,
			handleEventDoUpload : _handleEventDoUpload,
			handleEventClickCancelUpload : _handleEventClickCancelUpload,
			handleEventClickCancelStoreSettings : _handleEventClickCancelStoreSettings,
			handleEventSendSampleStomp : _handleEventSendSampleStomp,
			haveSelectedPathResources : _haveSelectedPathResources,
			handleEventClickDeletePathResources : _handleEventClickDeletePathResources,
			handleEventClickClearSelectedPathResources : _handleEventClickClearSelectedPathResources
		}

	}

})();