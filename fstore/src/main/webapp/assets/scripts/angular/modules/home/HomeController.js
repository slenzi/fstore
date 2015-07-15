(function(){

	angular
		.module('home')
		.controller('homeController',[
			'appConstants', 'homeService', 'ResourceStore', 'DirectoryResource', 'FsFileUploader',
			'$state', '$mdSidenav', '$mdBottomSheet', '$mdUtil', '$log', '$q', '$scope', HomeController
			]
		);

	function HomeController( appConstants, homeService, ResourceStore, DirectoryResource, FsFileUploader, 
			$state, $mdSidenav, $mdBottomSheet, $mdUtil, $log, $q, $scope) {
   
		// internal models bound to UI
		var storeList = [{ "name": "empty"}];
		var currentDirectory = new DirectoryResource({
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

		// load all resource stores when page loads (asynchronously)
		homeService
			.getResourceStores()
			.then( function( storeData ) {
					if (storeData.error){
						$log.debug("Error, " + storeData.error);
					} else {
						$log.debug("got store data => " + JSON.stringify(storeData));
						storeList = storeData;
						if(storeData[0]){
							// auto load first store and update ui
							//currentStore.setName(storeData[0].name);
							currentStore.setData(storeData[0]);
							_handleLoadDirectory(storeData[0].rootDirectoryId);
						}
					}
				}
			);
		
		//$log.debug('Directory resource name = ' + currentDirectory.getName());
			
		//$log.debug("here");
			
		// load sample data, regular style
		/*
		var sampleData = homeService.load();
		alert(JSON.stringify(sampleData));
		*/		

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
		}
		
		/**
		 * View settings for current store
		 */
		function _handleEventViewStoreSettings(){
			//alert('View store settings for store ' + currentStore.getId());
			$state.go('home_storeSettings');
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
			alert('You double clicked on a file, id = ' + fileResource.fileId);
		}
		
		/**
		 * Event handler for double-click of directory resource
		 */
		function _handleEventDblClickDirectory(directoryResource){
			alert('You double clicked on a directory, id = ' + directoryResource.dirId);
		}
        
		/**
		 * When user clicks on resource store, fetch store data from service.
		 */
		function _handleEventViewStore(storeId){
			
			$log.debug("View store with id = " + storeId + ".");
			
			homeService
				.getResourceStoreById(storeId)
				.then( function( storeData ) {
						if (storeData.error){
							$log.debug("Error, " + storeData.error);
						} else {
							if(storeData && storeData.rootDirectoryId){
								$log.debug("got store data => " + JSON.stringify(storeData));
								
								currentStore.setName(storeData.name);
								_handleLoadDirectory(storeData.rootDirectoryId);
								_leftNavClose();
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
        function _handleLoadDirectory(dirId){
            
            // fetch directory listing with max depth 1
			homeService
				.getDirectoryListing(dirId, 1)
				.then( function( directoryData ) {
						if (directoryData.error){
							$log.debug("Error, " + directoryData.error);
						} else {
							
							$log.debug("got directory data => " + JSON.stringify(directoryData));
							
							// update view
							currentDirectory.setName(directoryData.name);
							currentDirectory.setChildren(directoryData.children);
							
						}
					}
				);
            
        };
        
        /**
         * Show the upload form.
         */
        function _handleEventViewUploadForm(){
        	$log.debug('view upload form');
        	$state.go('home_upload');
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
			store : _currentStore,
			storeList : _storeList,
			directory : _currentDirectory,
			uploader : _uploader,
			handleEventViewStore : _handleEventViewStore,
			handleEventViewStoreSettings : _handleEventViewStoreSettings,
			handleEventDblClickPathResource : _handleEventDblClickPathResource,
			handleEventViewUploadForm : _handleEventViewUploadForm,
			handleEventClearUploadQueue : _handleEventClearUploadQueue,
			handleEventDoUpload : _handleEventDoUpload
		}

	}

})();