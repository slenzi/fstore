(function(){
	
	'use strict';
	
	var homeApp;
	
	/**
	 * Initialize home app with 'angular material' and our 'home' module
	 */
	homeApp = angular
			.module('fstoreApp', ['ngMaterial', 'home', 'ui.grid', 'ui.grid.pagination'])
		    .config(function($mdThemingProvider){
		
				// Extend the gray theme with a few different shades
				var lightGrey = $mdThemingProvider.extendPalette('grey', {
					'50':  'EFEFEF',
					'200': 'C6C6C6',
					'400': '8C8C8C',
					'800': '323232'
				});
				
				// Register the new color palette map with the name <code>light-gray</code>
				$mdThemingProvider.definePalette('light-gray', lightGrey);				
		
				// apply our light-gray theme
		    	$mdThemingProvider.theme('default')
		    		.primaryPalette('light-gray', {
                        'default': '50', // by default use shade 50 from the light-gray palette for primary intentions
                        'hue-1': '200', // use shade 100 for the <code>md-hue-1</code> class
                        'hue-2': '400', // use shade 600 for the <code>md-hue-2</code> class
                        'hue-3': '800' // use shade A100 for the <code>md-hue-3</code> class
                    })
		            .accentPalette('red');
					
					// append .dark() to make the theme dark
		
		    }).constant('appConstants', {
				contextPath: '${pageContext.request.contextPath}'
			});
			
})();