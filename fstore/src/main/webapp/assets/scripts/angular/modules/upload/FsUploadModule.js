/*
Angular HTTP upload module
-sal
*/

(function () {

	'use strict';

	var fsUploadModule;

	// create module
	fsUploadModule = angular.module('fsUpload', []);

	// set default options
	fsUploadModule.value('fsUploadOptions', {
		url: '/',
		progress: 0,
		method: 'POST',
		formData: [],
		files: {},
		headers: {}
	})
	.factory('FsFileUploader', ['fsUploadOptions', '$http', '$window', '$log',

	    /**
	     * Factory method which returns FsFileUploader object prototype.
	     */
	    function fsUploaderFactory(fsUploadOptions, $http, $window, $log){

			/**
			 * Create instance of FsFileUploader from object prototype.
			 *
			 * @param {Object} [options]
			 * @constructor
			 */
			function FsFileUploader(options){
				var defaultOptions = angular.copy(fsUploadOptions);
				// extend this object with default options and user provided options
				angular.extend(this, defaultOptions, options);
			}

			/**
			 * Produces a simple hello message.
			 *
			 * @returns {String}
			 */
			FsFileUploader.prototype.hello = function(){
				return 'Hello, use are using ' + this.constructor.name + '. Have a nice day.';
			};

			FsFileUploader.prototype.isHTML5 = !!($window.File && $window.FormData);
			
			/**
			 * Add a file to the upload queue.
			 */
			FsFileUploader.prototype.addFile = function(file){
				
				if(file == null || file.length == 0){
					return;
				}
				
				$log.debug('Adding file [name=' + file.name + ', lastModifiedDate=' + file.lastModifiedDate + ']');
				
				// update current list of files in queue and add new one
				var currentFiles = this.files;
				currentFiles[file.name] = file;
				angular.extend(this, {
					files: currentFiles 
				});
				
				//$log.debug('url=' + this.url);
				
				$log.debug('fsUploader: ' + JSON.stringify(this));
				
			}

			// return object prototype
			return FsFileUploader;

		}
	])
	.directive('fsUploadDebug', ['$log', 'FsFileUploader', function($log, FsFileUploader) {
		return {

			/*
			'A' - Attribute - <span ng-sparkline></span>
			'E' - Element   - <ng-sparkline></ng-sparkline>
			'C' - Class     - <span class="ng-sparkline"></span>
			'M' - Comments  - <!-- directive: ng-sparkline -->
			 */
			restrict: 'AE',
			replace: true,
			scope: {
				//uploaderLocal: '=uploader'
				uploader: '='
			},
			//template: '<span> {{ uploader }} </span>',
			// display all attributes of the uploader object in a bulleted list
			template: '<ul ng-repeat="(key, value) in uploader"><li><b>{{key}}</b> = {{value}}</li></ul>',
			link: function ($scope, element, attributes){
				
				// get reference to FsFileUploader object (from attribute field)
				//var uploader = $scope.$eval(attributes.uploader);
				
				//$log.debug('uploader = ' + uploader);

				// make sure the object in the 'uploader' attribute is actually an instance of our FsFileUploader
				//if (!(uploader instanceof FsFileUploader)) {
				//	throw new TypeError('Uploader must be an instance of FsFileUploader');
				//}				
				
				//$log.debug('here = ' + scope.uploaderObj)
				
				//scope.uploaderObj = uploader;
				
				//scope.uploaderObj = uploader;
				
				//scope.$apply();
				
			}
		};
	}])
	.directive('fsUploadFileSelect', ['$log','$parse','FsFileUploader', function($log, $parse, FsFileUploader) {
		return {

			/*
			'A' - Attribute - <span ng-sparkline></span>
			'E' - Element   - <ng-sparkline></ng-sparkline>
			'C' - Class     - <span class="ng-sparkline"></span>
			'M' - Comments  - <!-- directive: ng-sparkline -->
			*/
			restrict: 'A',

			/*
			element - jQlite object
			*/
			link: function($scope, element, attributes) {

				var elm = element[0]; // convert angular jQlite element to raw DOM element

				// get reference to FsFileUploader object (from attribute field)
				var uploader = $scope.$eval(attributes.uploader);

				// make sure the object in the 'uploader' attribute is actually an instance of our FsFileUploader
				if (!(uploader instanceof FsFileUploader)) {
					throw new TypeError('Uploader must be an instance of FsFileUploader');
				}
				
				$log.debug('typeof(element) = ' + typeof(element));
				$log.debug('element.html() = ' + element.html());
				$log.debug('elm.nodeName = ' + elm.nodeName);
				$log.debug('attributes.uploader = ' + attributes.uploader);
				$log.debug('Uploader is HTML 5 = ' + uploader.isHTML5);
				
				// angular has no built in support for binding to a file input.
				// https://github.com/angular/angular.js/issues/1375
				// http://stackoverflow.com/questions/17922557/angularjs-how-to-check-for-changes-in-file-input-fields
				element.bind('change', function(event){
					$log.debug('files changed!');
					//$log.debug(element.val()));
					var files = event.target.files;
					for(var i=0; i<files.length; i++){
						uploader.addFile(files[i]);
						
						// update parent scope (will update the uploader binded to the fsUploadDebug directive)
						$scope.$parent.$apply();
					}
				});
			}
		};
	}]);

	return fsUploadModule;

})();
