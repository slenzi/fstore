(function(){
	
	'use strict';
	
	/**
	 * Define a range for an ng-repeat loop.
	 *
	 * e.g., <div ng-repeat="n in [10] | makeRange">Do something 0..9: {{n}}</div>
	 */
	angular.module('fsFileManagerMain').filter('makeRange', function() {
        return function(input) {
            var lowBound, highBound;
            switch (input.length) {
            case 1:
                lowBound = 0;
                highBound = parseInt(input[0]) - 1;
                break;
            case 2:
                lowBound = parseInt(input[0]);
                highBound = parseInt(input[1]);
                break;
            default:
                return input;
            }
            var result = [];
            for (var i = lowBound; i <= highBound; i++)
                result.push(i);
            return result;
        };
    });


})();