(function(){

	angular
		.module('home')
		.controller('homeController',[
			'appConstants', 'homeService', 'DirectoryResource', '$mdSidenav', '$mdBottomSheet', '$mdUtil', '$log', '$q', HomeController
			]
		);

	function HomeController( appConstants, homeService, DirectoryResource, $mdSidenav, $mdBottomSheet, $mdUtil, $log, $q) {
    
		// *********************************
		// External API
		// *********************************
		var self = this;
		self.doHello = _doHello;
		self.showContactOptions = _showContactOptions;
		self.leftNavClose = _leftNavClose;
		self.toggleLeftNav = _buildToggler('MyLeftNav');
		self.notImplemented = _notImplemented;
		// resource store methods
		self.storeList = _storeList;
		self.handleEventViewStore = _handleEventViewStore;
		
		// directory methods
		// coming soon...

		// *********************************
		// Internal methods and data 
		// *********************************
		
		// internal models bound to UI
		var _storeList = [{ "name": "empty"}];
		var _currentDirectory = new DirectoryResource({
				name: 'Loading...',
				dateCreated: 'Loading...',
				dateUpdated: 'Loading...'
		});

		//
		// load all resource stores when page loads (asynchronously)
		//
		homeService
			.getResourceStores()
			.then( function( storeData ) {
					if (storeData.error){
						$log.debug("Error, " + storeData.error);
					} else {
						$log.debug("got store data => " + JSON.stringify(storeData));
						_storeList = storeData;
					}
				}
			);
		
		$log.debug('Directory resource name = ' + _currentDirectory.getName());
			
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
			return _storeList;
		}
        
		/**
		 * When user clicks on resource store, fetch store data from service.
		 */
		function _handleEventViewStore(storeId){
			
			$log.debug("View store with id = " + storeId + ". Feature coming soon!");
			
			homeService
				.getResourceStoreById(storeId)
				.then( function( storeData ) {
						if (storeData.error){
							$log.debug("Error, " + storeData.error);
						} else {
							$log.debug("got store data => " + JSON.stringify(storeData));
                            _handleLoadDirectory(storeData.rootDirectoryId);
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
						}
					}
				);
            
        };
		
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

	}

})();