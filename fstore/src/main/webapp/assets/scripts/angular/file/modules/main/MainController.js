(function(){

	angular
		.module('fsFileManagerMain')
		.controller('mainController',[
			'appConstants', 'FileServices', 'ResourceStore', 'PathResource', 'FsClipboard', 'FsFileUploader', 'FsStomp',
			'$state', '$stateParams', '$mdSidenav', '$mdDialog', '$mdMenu', '$mdToast', '$mdBottomSheet', '$mdUtil', '$log', '$q', '$scope', '$element', MainController
			]
		);

	// 'mainService'  mainService  - No longer use main services. Moved all services to external module called fstore-services-module
	
	function MainController(
		appConstants, FileServices, ResourceStore, PathResource, FsClipboard, FsFileUploader, FsStomp, $state, $stateParams, $mdSidenav, $mdDialog, $mdMenu, $mdToast, $mdBottomSheet, $mdUtil, $log, $q, $scope, $element) {
   
   
		/****************************************************************************************
		 * Internal models bound to UI
		 */
		var sectionTitle = "Not set";
		var storeList = [{ "name": "empty"}];
		//var cmsSiteList = [{ "name": "empty"}];
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
		
		// true to use icon view to display resource, false to use smart table view
		var useIconView = true;
		
		// will be true when directory data, or some other path resource data is being fetched from the server. 
		var isLoadingPathResource = false;
		
		// tracks path resources that have been selected for "copy".
		// they will be copied to a new location when the user performs a "paste"
		//var pathResourceToCopy = [];
		// tracks path resources that have been selected for "cut".
		// they will be moved to a new location when the user performs a "paste"
		//var pathResourceToMove = [];
		
		var clipboard = new FsClipboard({});
		
		// true if right nav is open, false if closed. By default it's closed.
		var isRightNavOpen = false;


		/****************************************************************************************
		 * On application load:  load all resource stores when page loads (asynchronously)
		 */		
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
			var uploadSubscription = myStomp.subscribe('/topic/uploads', _myStompReceiveUploadMessages);
		}
		function _myStompConnectError(error){
			$log.debug('_onStompConnectError...');
			//$log.debug(error.headers.message);
			$log.debug('error = ' + JSON.stringify(error));
		}
		function _myStompReceiveTestMessages(socketMessage){
			$log.info('STOMP Received = ' + JSON.stringify(socketMessage));
		}
		function _myStompReceiveEchoMessages(socketMessage){
			$log.info('STOMP Received = ' + JSON.stringify(socketMessage));
		}
		function _myStompReceiveUploadMessages(socketMessage){
			
			$log.info('STOMP Received = ' + JSON.stringify(socketMessage));
			
			var uploadMessage = JSON.parse(socketMessage.body);
			
			$log.info('Upload Message Type = ' + uploadMessage.type);
			
			if(uploadMessage.type == 'UPLOAD_PROCESSED'){
				_handleUploadProcessedMessage(uploadMessage);
			}
			
		}
		function _handleUploadProcessedMessage(uploadMessage){
			
			var messageDirId = uploadMessage.dirId;
			if(_currentDirectory().dirId == messageDirId){
				$log.debug("Current directory user is viewing has updated files on the server.");
			}
			
		}
		
		/**
		 * Say hello
		 */
		function _doHello(){
			alert("hello from main controller");
			// $mdSidenav('MyLeftNav').toggle();
			// $mdBottomSheet.hide()
		}
		
		/**
		 * Get current section title
		 */
		function _sectionTitle(){
			return sectionTitle;
		}
		
		/**
		 * Get list of resource stores
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
		 * Return true if there is a currently running async process that's fetching path resource data from server.
		 */
		function _isLoadingPathResource(){
			return isLoadingPathResource;
		}
		
		/**
		 * Return true if currently using icon view, otherwise false if using smart table view.
		 */
		function _isUsingIconView(){
			return useIconView;
		}
		
        /**
         * Show the upload form.
         */
        function _handleEventViewUploadForm(){
        	$state.go('main_upload');
        }
		
		/**
		 * Handle cancel upload button click
		 */
        function _handleEventClickCancelUpload(){
			_showDirectoryView();
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
			
				var confirm = $mdDialog.confirm()
					.parent(angular.element(document.body))
					.title('Upload Confirmation')
					.content("Please confirm upload.")
					.ariaLabel('Continue Upload')
					.ok('Continue')
					.cancel('Cancel')
					.targetEvent(event);
				
				$mdDialog.show(confirm).then(function() {
					
					// set parent dir id so we know where to create the new directory on the server
					myFsUploader.addFormValue('dirId', _currentDirectory().dirId);
					
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
			
			_showDirectoryView();
			
			alert('All files have been received on the server. Please note large file take time to procee. Refresh view to see latest files.');
			/*
			$mdDialog.show(
				$mdDialog.alert()
					.parent(angular.element(document.body))
					.clickOutsideToClose(true)
					.title('Upload Complete')
					.content('All files have been received on the server.')
					.ariaLabel('Upload Complete')
					.ok('Got it!')
					.targetEvent(event)
			);
			*/
			
			_handleLoadDirectory(_currentDirectory().dirId, true);
			
			//_showDirectoryView();
		}
		
		/**
		 * View list of all stores
		 */
		function _handleEventViewStoreList(){

			sectionTitle = "Resource Store List";
			$state.go('main_storeList');
	
			FileServices
				.getResourceStores()
				.then(_handleStoreDataCallback);
				
		}
		
		/**
		 * Helper function for _handleEventViewStoreList. Called when store data is
		 * returned from REST service.
		 *
		 * storeData - data returned from main service getResourceStores() function.
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
			$state.go('main_storeSettings');
		}
		
		/**
		 * Handle cancel upload button click
		 */
        function _handleEventClickCancelStoreSettings(){
        	_showDirectoryView();
        }
		
		/**
		 * Switches resource view between icon style and table style
		 */
		function _handleEventSwitchResourceView(){
			if(useIconView){
				useIconView = false;
				$state.go('main_directory_table');
				
			}else{
				useIconView = true;
				$state.go('main_directory_icon');
			}
		}
		
		/**
		 * Shows the directory view route
		 */
		function _showDirectoryView(){
			if(useIconView){
				$state.go('main_directory_icon');
			}else{
				$state.go('main_directory_table');
			}
		}

		/**
		 * handle click of path resource in our smart table
		 */
		function _handleEventClickTablePathResource(pathResource){
			if(pathResource.type == 'FILE'){
				FileServices.downloadFile(pathResource.fileId);
			}else if(pathResource.type == 'DIRECTORY'){
				_handleLoadDirectory(pathResource.dirId, true);
			}
		}
		
		/**
		 * pathResource - the path resource the user moused-over
		 */		
		function _handlePathResourceMouseOver(pathResource){
			//$log.debug('user moused-over path resource ' + JSON.stringify(pathResource));
		}		
		
		/**
		 * pathResource - the path resource the user clicked on (file, directory, etc)
		 */
		function _handleEventClickIconGridPathResource(pathResource){
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
		function _handleEventClickFile(fileResource){
			//alert('You clicked on a file, id = ' + fileResource.fileId);
		}
		function _handleEventClickDirectory(directoryResource){
			//alert('You clicked on a directory, id = ' + directoryResource.dirId);
		}		
		
		/**
		 * pathResource - the path resource the user double clicked on (file, directory, etc)
		 */
		function _handleEventDblClickIconGridPathResource(pathResource){
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
			FileServices.downloadFile(fileResource.fileId)
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
		 * Reload the current directory.
		 */
		function _reloadCurrentDirectory(){
			_handleLoadDirectory(_currentDirectory().dirId, true);
		}
        
		/**
		 * When user clicks on resource store, fetch store data from service.
		 */
		function _handleEventViewStore(storeId){

			FileServices
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
            
			isLoadingPathResource = true;
			
			if(showDirectoryPartial){
				$log.info('show directory partial');
				_showDirectoryView();
			}
			
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
							
							// update current directory model
							currentDirectory = directory;
							
							_handleLoadBreadcrumb(directoryData.dirId);
							
							//sectionTitle = currentStore.name;
							
							isLoadingPathResource = false;
							
						}
					}
				);
            
        };
		
        /**
         * Fetch breadcrumb / parent tree data for some child directory
         */		
		function _handleLoadBreadcrumb(dirId){
			
			FileServices
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
		 * Close left-side nav panel
		 */
		function _leftNavClose() {
			$mdSidenav('MyLeftNav').close()
			.then(function () {
				$log.debug("close MyLeftNav is done");
			});
		};
		
		/**
		 * Close right-side nav panel
		 */
		function _rightNavClose() {
			$mdSidenav('MyRightNav').close()
			.then(function () {
				$log.debug("close MyRightNav is done");
			});
		};

		/**
		 * Return true if right nav is open, false otherwise
		 */
		function _isRightNavOpen(){
			return isRightNavOpen;
		};
		
		/**
		 * Toggle right-nav md-is-locked-open attribute
		 */
		function _toggleRightNavLock(){
			isRightNavOpen = !isRightNavOpen;
			$log.debug("toggle right nav lock");
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
		
		/**
		 * Checks if the current directory has any child path resources
		 */
		function _haveChildPathResources(){
			
			if(currentDirectory && currentDirectory.children && currentDirectory.children.length > 0){
				return true;
			}
			return false;
			
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
			var haveResourcesToDelete = (filesToDelete.length > 0) || (directoriesToDelete.length > 0);
			if(haveResourcesToDelete){
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
					
					_doDeleteResourcesHelper(event, filesToDelete, directoriesToDelete);
						
				}, function() {
					
					$log.debug('Delete operation canceled.');
					
				});					
			}			
		}
		function _doDeleteResourcesHelper(event, filesToDelete, directoriesToDelete){
			
			var fileIdList = [];
			var dirIdList = [];
			var pleaseWaitDialog;
			
			if(filesToDelete && filesToDelete.length > 0){
				for(i=0; i<filesToDelete.length; i++){
					fileIdList.push(filesToDelete[i].fileId);
				}
			}
			if(directoriesToDelete && directoriesToDelete.length > 0){
				for(i=0; i<directoriesToDelete.length; i++){
					dirIdList.push(directoriesToDelete[i].dirId);
				}
			}
			
			if(fileIdList.length > 0 || dirIdList.length > 0){
			
				// show please wait
				pleaseWaitDialog = $mdDialog.show(
					$mdDialog.alert()
						.parent(angular.element(document.body))
						.clickOutsideToClose(false)
						.title('Delete in progress')
						.content('Please wait while resources are deleted...')
						.ariaLabel('Please wait while resources are deleted.')
						.targetEvent(event)
				);
				
			}
			
			if(fileIdList.length > 0 && dirIdList.length > 0){
				
				FileServices
					.deleteFiles(fileIdList)
					.then( function( reply ) {
						
						$log.debug('delete files reply: ' + JSON.stringify(reply));
						
						return FileServices
							.deleteDirectories(dirIdList)
							.then( function( reply ) {
								
								$log.debug('delete directories reply: ' + JSON.stringify(reply));
								
							});
						
					}).then( function( result ) {
						
						_reloadCurrentDirectory();
						
						$mdDialog.hide(pleaseWaitDialog, "finished");
						
					});
							
				
				
			} else if (fileIdList.length > 0){
				
				// delete via rest service (pass array of file ids)
				FileServices
					.deleteFiles(fileIdList)
					.then( function( reply ) {
						
						$log.debug('delete files reply: ' + JSON.stringify(reply));
						
						_reloadCurrentDirectory();
						
						$mdDialog.hide(pleaseWaitDialog, "finished");
						
					});					
				
			} else if (dirIdList.length > 0){
				
				// delete via rest service (pass array of dir ids)
				FileServices
					.deleteDirectories(dirIdList)
					.then( function( reply ) {
						
						$log.debug('delete directories reply: ' + JSON.stringify(reply));
						
						_reloadCurrentDirectory();
						
						$mdDialog.hide(pleaseWaitDialog, "finished");
						
					});					
				
			}			
			
		}
		
		/**
		 * Select all child path resources in the current directory.
		 */
		function _handleEventClickSelectAllPathResources(){
			
			if(currentDirectory && currentDirectory.children){
				for(i=0; i<currentDirectory.children.length; i++){
					if(!currentDirectory.children[i].isSelected){
						currentDirectory.children[i].isSelected = true;
					}
				}
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
		
		/*
		function _handleEventClickCopyPathResources(event){
			
			if(_haveSelectedPathResources()){
				
			}else{
				$log.error('No selected resources to copy.');
			}
			
		}
		*
		
		/*
		function _handleEventClickCutPathResources(event){
			
			if(_haveSelectedPathResources()){
				
			}else{
				$log.error('No selected resources to cut.');
			}			
			
		}
		*/
	
		/*
		function _handleEventClickPastePathResources(event){
			
			
			
		}
		*
		
		/**
		 * Display dialog where users can enter name of new folder. Submit to REST service and reload current directory.
		 */
		function _handleEventClickNewFolder(event){
			
			$mdDialog.show({
				parent: angular.element(document.body),
				targetEvent: event,
				template:
					'<md-dialog aria-label="List dialog" flex="35">' +
					'	<md-dialog-content>'+
					'		<md-content layout-padding>' +
					'			<h3>Create New Folder</h3>' +
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
					
					var parentDirId = currentDirectory.dirId;
					var newFolderName = $scope.newFolderDialog.newFolderName;
					
					FileServices
						.addDirectory(parentDirId, newFolderName)
						.then( function( reply ) {
							
							$log.debug('add directory reply: ' + JSON.stringify(reply));
							
							_reloadCurrentDirectory();
							
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
		function _handleEventClickCopyPathResources(event){
			_doClipboardOperationOnSelectedResources('copy', event);
		}
		function _handleEventClickCutPathResources(event){
			_doClipboardOperationOnSelectedResources('cut', event);
		}
		function _doClipboardOperationOnSelectedResources(operationType, event){
			
			var fileIdList = [];
			var dirIdList = [];
		
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
		function _handleEventClickPastePathResources(event){
			
			var operation = clipboard.operation;
			var operationType = clipboard.operation.type;
			
			if(operation.data.sourceDirId && operation.data.sourceDirId == currentDirectory.dirId){
				alert('Cannot paste. Source and target directories are the same. Please navigate to a different directory');
			}else{
				
				// perform copy operation
				if(operationType.toLowerCase() == 'copy'){
					
					_handleCopyPaste(event);
	
				// perform cut (move) operation
				}else if(operationType.toLowerCase() == 'cut'){
					
					_handleCutPaste(event);
					
				}else{
					
					alert('Cannot paste. Unknown operation type. Type = \'' + operationType + '\'');
					
				}
				
			}
			
		}
		
		/**
		 * Handle copy-paste event
		 */
		function _handleCopyPaste(event){
			
			var operation = clipboard.operation;
			
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
						_reloadCurrentDirectory();
					});						
				
			}else if(haveFilesToCopy){
				
				FileServices
					.copyFiles(fileIdList, currentDirectory.dirId)
					.then( function( reply ) {
						$log.debug('copy files reply: ' + JSON.stringify(reply));
						_handleEventClickClearClipboard();
						_reloadCurrentDirectory();
					});					
				
			}else if(haveDirectoriesToCopy){
				
				FileServices
					.copyDirectories(dirIdList, currentDirectory.dirId)
					.then( function( reply ) {
						$log.debug('copy directories reply: ' + JSON.stringify(reply));
						_handleEventClickClearClipboard();
						_reloadCurrentDirectory();
					});						
				
			}else{
				
			}			
			
		}
		
		/**
		 * Handle cut-paste event
		 */
		function _handleCutPaste(event){
			
			var operation = clipboard.operation;
			
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
						_reloadCurrentDirectory();
					});						
				
			}else if(haveFilesToCopy){
				
				FileServices
					.moveFiles(fileIdList, currentDirectory.dirId)
					.then( function( reply ) {
						$log.debug('move files reply: ' + JSON.stringify(reply));
						_handleEventClickClearClipboard();
						_reloadCurrentDirectory();
					});					
				
			}else if(haveDirectoriesToCopy){
				
				FileServices
					.moveDirectories(dirIdList, currentDirectory.dirId)
					.then( function( reply ) {
						$log.debug('move directories reply: ' + JSON.stringify(reply));
						_handleEventClickClearClipboard();
						_reloadCurrentDirectory();
					});						
				
			}else{
				
			}			
			
		}

		/**
		 * Build sample md-toast element
		 */
		function _handleEventClickToastTest(event){

			$mdToast.show({
				controller: _toastyController,
				targetEvent: event,
				parent: angular.element(document.body),
				templateUrl: appConstants.contextPath + '/assets/scripts/angular/file/modules/main/partials/sampleToast.jsp',
				//template : 'hi',
				hideDelay: 0,
				position: 'bottom right'
			});

			function _toastyController($scope, $mdToast) {
				$scope.closeToast = function() {
					$mdToast.hide();
				};
                $scope.getUploader = function(){
                    return _uploader();
                };
			}			
			
		}
		
		function _contextMenuTest(ev){
			
			$log.debug('right-click context menu test');
			
			var triggerElement = triggerElement || (ev ? ev.target : $element[0]);
			
			var myCustomMenu = angular.element(
				'<div class="md-open-menu-container md-whiteframe-z2">' +
				'<md-menu-content>' +
				'hello!' +
				'</md-menu-content>' +
				'</div>');

	
			var RightClickMenuCtrl = {
				open: function(event) {
					$mdMenu.show({
						scope: $scope,
						mdMenuCtrl: RightClickMenuCtrl,
						element: myCustomMenu,
						target: triggerElement // used for where the menu animates out of
					});
				}, 
				close: function() { $mdMenu.hide(); },
				positionMode: function() { return { left: 'target', top: 'target' }; },
				offsets: function() { return { top: 0, left: 0 }; }
			};
			
			$mdMenu.show({
				scope: $scope,
				mdMenuCtrl: RightClickMenuCtrl,
				element: myCustomMenu,
				target: triggerElement // used for where the menu animates out of
			});				

			/*
			function _createContextMenuController($scope, $mdMenu) {
				$scope.closeMenu = function() {
					$mdMenu.hide();
				}
				//$scope.positionMode = function() { return { left: 'target', top: 'target' }; }				
			}
			*/
			
		};
	
		var self = this;
		
		/*
		 * External API
		 */
		return {
			doHello : _doHello,
			showContactOptions : _showContactOptions,
			leftNavClose : _leftNavClose,
			rightNavClose : _rightNavClose,
			toggleLeftNav : _buildToggler('MyLeftNav'),
			toggleRightNav : _buildToggler('MyRightNav'),
			isRightNavOpen : _isRightNavOpen,
			toggleRightNavLock : _toggleRightNavLock,
			contextMenuTest : _contextMenuTest,
			notImplemented : _notImplemented,
			sectionTitle : _sectionTitle,
			store : _currentStore,
			storeList : _storeList,
			directory : _currentDirectory,
			uploader : _uploader,
			breadcrumb : _breadcrumb,
			haveChildPathResources : _haveChildPathResources,
			haveClipboardResources : _haveClipboardResources,
			isLoadingPathResource : _isLoadingPathResource,
			isUsingIconView : _isUsingIconView,
			handleEventSwitchResourceView : _handleEventSwitchResourceView,
			handleEventViewStore : _handleEventViewStore,
			handleEventViewStoreSettings : _handleEventViewStoreSettings,
			handleEventViewStoreList : _handleEventViewStoreList,
			handleEventClickTablePathResource : _handleEventClickTablePathResource,
			handleEventClickIconGridPathResource : _handleEventClickIconGridPathResource,
			handleEventDblClickIconGridPathResource : _handleEventDblClickIconGridPathResource,
			handleEventClickBreadcrumb : _handleEventClickBreadcrumb,
			handleEventViewUploadForm : _handleEventViewUploadForm,
			handleEventClickNewFolder : _handleEventClickNewFolder,
			handleEventClearUploadQueue : _handleEventClearUploadQueue,
			handleEventDoUpload : _handleEventDoUpload,
			handleEventClickCancelUpload : _handleEventClickCancelUpload,
			handleEventClickCancelStoreSettings : _handleEventClickCancelStoreSettings,
			handleEventSendSampleStomp : _handleEventSendSampleStomp,
			haveSelectedPathResources : _haveSelectedPathResources,
			handleEventClickDeletePathResources : _handleEventClickDeletePathResources,
			handleEventClickSelectAllPathResources : _handleEventClickSelectAllPathResources,
			handlePathResourceMouseOver: _handlePathResourceMouseOver,
			handleEventClickClearSelectedPathResources : _handleEventClickClearSelectedPathResources,
			handleEventClickCopyPathResources : _handleEventClickCopyPathResources,
			handleEventClickCutPathResources : _handleEventClickCutPathResources,
			handleEventClickPastePathResources : _handleEventClickPastePathResources,
			handleEventClickClearClipboard : _handleEventClickClearClipboard,
			
			handleEventClickToastTest : _handleEventClickToastTest
            
		}

	}

})();