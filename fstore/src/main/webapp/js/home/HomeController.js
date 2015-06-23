(function(){

	angular
		.module('home')
		.controller('HomeController', [
			'homeService', '$mdSidenav', '$mdBottomSheet', '$log', '$q',
			HomeController
		]);

	/**
	 * Main Controller for the Fstore Angular Material app
	 * 
	 * @param $scope
	 * @param $mdSidenav
	 * @param avatarsService
	 * @constructor
	 */
	function HomeController( homeService, $mdSidenav, $mdBottomSheet, $log, $q) {
    
		var self = this;

		// load sample data
		homeService
			.doLoadTest()
			.then( function( sampleData ) {
					alert("got sample data => " + sampleData);
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

			// $mdSidenav('left').toggle();

			// $mdBottomSheet.hide()

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