(function(){
	
	'use strict';
	
	var homeApp;
	
	/**
	 * Initialize home app with 'angular material' and our 'home' module
	 */
	homeApp = angular
			.module('fstoreApp', ['ngMaterial', 'home', 'ui.grid', 'ui.grid.pagination'])
		    .config(function($mdThemingProvider){
		
		    	$mdThemingProvider.theme('default')
		    		.primaryPalette('grey', {
                        'default': '50', // by default use shade 400 from the pink palette for primary intentions
                        'hue-1': '200', // use shade 100 for the <code>md-hue-1</code> class
                        'hue-2': '400', // use shade 600 for the <code>md-hue-2</code> class
                        'hue-3': '800' // use shade A100 for the <code>md-hue-3</code> class
                    })
		            .accentPalette('red');
					
					// append .dark() to make the theme dark
		
		    });
			
})();