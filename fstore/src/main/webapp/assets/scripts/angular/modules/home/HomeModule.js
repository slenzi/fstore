(function(){
	
	'use strict';

	var homeModule;
	
	/**
	 * Create home module with following dependencies:
	 * 
	 * fsUpload - for multi-part HTTP upload
	 * ngMaterial - angular material UI
	 */
	homeModule = angular.module('home', [ 'fsUpload', 'ngMaterial' ]);

})();
