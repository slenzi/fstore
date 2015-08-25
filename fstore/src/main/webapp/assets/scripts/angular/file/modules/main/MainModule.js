(function(){
	
	'use strict';

	var mainModule;
	
	/**
	 * Create main module with following dependencies:
	 * 
	 * fsUpload - for multi-part HTTP upload
	 * ngMaterial - angular material UI
	 */
	mainModule = angular.module('fsFileManagerMain', [ 'fsUpload', 'ngMaterial' ]);

})();
