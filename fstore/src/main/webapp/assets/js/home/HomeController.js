(function(){

	angular
		.module('home')
		.controller('homeController', [
			'homeService', '$mdSidenav', '$mdBottomSheet', '$mdUtil', '$log', '$q',
			HomeController
		]);

	function HomeController( homeService, $mdSidenav, $mdBottomSheet, $mdUtil, $log, $q) {
    
		var self = this;
		
		// add function to this controller
		self.doHello = doHello;
		self.showContactOptions = showContactOptions;
		self.leftNavClose = leftNavClose;
		self.toggleLeftNav = buildToggler('MyLeftNav');
		self.sampleGrid = sampleGrid;
		
		self.notImplemented = notImplemented;
		
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

		// load sample data
		homeService
			.doLoadTest()
			.then( function( sampleData ) {
					//alert("got sample data => " + sampleData);
				}
			);

		// *********************************
		// Internal methods
		// *********************************

		/**
		 * Say hello
		 */
		function doHello(){
			alert("hello from home controller");
			// $mdSidenav('MyLeftNav').toggle();
			// $mdBottomSheet.hide()
		}
		
		/**
		 * Fetch sample ui-grid
		 */
		function sampleGrid(){
			return sampleGrid;
		}
		
		/**
		 * Build handler to open/close a SideNav; when animation finishes
		 * report completion in console
		 */
		function buildToggler(navID) {
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
		function leftNavClose() {
			$mdSidenav('MyLeftNav').close()
			.then(function () {
				$log.debug("close MyLeftNav is done");
			});
		};

		/**
		 *
		 */
		function notImplemented(){
			alert("Feature implementation is forthcoming.");
		}
		
		/**
		 * Show the bottom sheet
		 */
		function showContactOptions($event) {

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