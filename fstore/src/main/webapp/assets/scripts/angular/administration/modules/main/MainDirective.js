(function(){
	
	'use strict';
	
	// fetch main module
	var mainModule = angular.module('fstoreAdministrationMain');
	
	/**
	 * Handle single click when also using ng-dblclick. Allows for both
	 * single and double click on the same element.
	 * 
	 * Thanks to Rob:
	 * http://stackoverflow.com/questions/20444409/handling-ng-click-and-ng-dblclick-on-the-same-element-with-angularjs
	 */
	mainModule.directive('sglclick', ['$parse', function($parse) {
		return {
			restrict: 'A',
			link: function(scope, element, attr) {
				var fn = $parse(attr['sglclick']);
				var delay = 300, clicks = 0, timer = null;
				element.on('click', function (event) {
					clicks++;  //count clicks
					if(clicks === 1) {
						timer = setTimeout(function() {
							scope.$apply(function () {
								fn(scope, { $event: event });
							}); 
							clicks = 0;             //after action performed, reset counter
						}, delay);
					} else {
						clearTimeout(timer);    //prevent single-click action
						clicks = 0;             //after action performed, reset counter
					}
				});
			}
		};
	}])
    /**
     * right-click directive
     */
	.directive('ngRightClick', ['$parse', '$log', function($parse, $log) {
		
	    return function(scope, element, attrs) {
	        var fn = $parse(attrs.ngRightClick);
	        element.bind('contextmenu', function(event) {
	            scope.$apply(function() {
	                event.preventDefault();
	                fn(scope, {$event:event});
	            });
	        });
	    };		
		
	}]);		


})();