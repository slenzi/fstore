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
	// object for storing file data
	.factory('FsFileItem', ['$log', '$q',
	                               
	    function fsFileItemFactory($log, $q){
		
			function FsFileItem(fileData){
				
				// set defaults
				angular.extend(this, {
					name: 'name not set',
					file: []
				});
				
				if(fileData){
					this.setData(fileData);
				}
			};
			
			// extend functionality
			FsFileItem.prototype = {
				setData: function(fileData){
					angular.extend(this, fileData);
				},
				getName: function(){
					return this.name;
				},
				setName: function(name){
					this.name = name;
				},
				getFile: function(){
					return this.file;
				},
				setFile: function(file){
					this.file = file;
				}			
			};	
			
			// return this
			return FsFileItem;
		
		}
	                               
	])
	.factory('FsFileUploader', ['fsUploadOptions', 'FsFileItem', '$http', '$window', '$log',

	    /**
	     * Factory method which returns FsFileUploader object prototype.
	     */
	    function fsUploaderFactory(fsUploadOptions, FsFileItem, $http, $window, $log){

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
			FsFileUploader.prototype.addFile = function(fileToAdd){
				
				if(fileToAdd == null || fileToAdd.length == 0){
					return;
				}
				
				//$log.debug('Adding file [name=' + fileToAdd.name + ', lastModifiedDate=' + fileToAdd.lastModifiedDate + ']');
				
				// create new file item
				var fileItem = new FsFileItem({
					name: fileToAdd.name,
					file: fileToAdd
				});
                
                //$log.debug('fileToAdd = ' + fileToAdd);
                //$log.debug('FsFileItem.file = ' + fileItem.file);
                //$log.debug('Adding FsFileItem = ' + JSON.stringify(fileItem));
	
				// update current list of files in queue and add the new one
				//var currentFiles = this.files;
				//currentFiles[fileToAdd.name] = fileItem;
				//angular.extend(this, {
				//	files: currentFiles 
				//});
                this.files[fileToAdd.name] = fileItem;
				
				//$log.debug('url=' + this.url);
				
				//$log.debug('fsUploader: ' + JSON.stringify(this));
				
			}
			
			/**
			 * Removes all files from the upload queue.
			 */
			FsFileUploader.prototype.clearQueue = function(){
				this.files = {};
			}
			
			/**
			 * Uploads all files in the queue.
			 */
			FsFileUploader.prototype.doUpload = function(){
				
				var fileNames = Object.keys(this.files);
				
				if(fileNames.length == 0){
					alert('There are no files in the upload queue. Try adding some files...');
                    return;
				}
				
				var xhr = new XMLHttpRequest();
				
				xhr.upload.onprogress = function(event) {
					var progress = Math.round(event.lengthComputable ? event.loaded * 100 / event.total : 0);
					$log.debug('XMLHttpRequest upload progres: ' + progress);
				};
				xhr.onload = function() {
					$log.debug('XMLHttpRequest on load');
				};
				xhr.onerror = function() {
					$log.error('XMLHttpRequest on error');
				};
				xhr.onabort = function() {
					$log.warn('XMLHttpRequest on abort');
				};
				
				$log.debug('files = ' + JSON.stringify(this.files));
				
				var form = new FormData();
				
                /*
                fileNames.forEach(function(fileName){
                    var fsFileItem = this.files[fileName];
                    $log.debug('Upload file = ' + JSON.stringify(fsFileItem));
                }, this);
                */
                
				angular.forEach(fileNames, function(fileName, fileIndex) {
					var fsFileItem = this.files[fileName];
					form.append("file_" + fileIndex, fsFileItem.file);
				}, this);
			
				$log.debug('Submitting http POST to ' + this.url);
				
				xhr.open("POST", this.url);
				xhr.send(form);
				
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
                        $log.debug('Add file to uploader queue = ' + files[i]);
						uploader.addFile(files[i]);
					}
					// update parent scope (will update the uploader binded to the fsUploadDebug directive)
					$scope.$parent.$apply();
				});
			}
		};
	}])
	.directive('fsUploadDrop', ['$log','$parse','FsFileUploader', function($log, $parse, FsFileUploader) {
		return {
			/*
			'A' - Attribute - <span ng-sparkline></span>
			'E' - Element   - <ng-sparkline></ng-sparkline>
			'C' - Class     - <span class="ng-sparkline"></span>
			'M' - Comments  - <!-- directive: ng-sparkline -->
			 */
			restrict: 'AE',
			link: function ($scope, element, attributes){
				
				var processDragOver, processDragEnter, processDragOverEnter, uploader;
				
				// get reference to FsFileUploader object (from attribute field)
				uploader = $scope.$eval(attributes.uploader);
				
				// make sure the object in the 'uploader' attribute is actually an instance of our FsFileUploader
				if (!(uploader instanceof FsFileUploader)) {
					throw new TypeError('Uploader must be an instance of FsFileUploader');
				}				
				
				processDragOver = function(event){
					processDragOverEnter(event);
				};
				
				processDragEnter = function(event){
					processDragOverEnter(event);
				};
				
				processDragOverEnter = function(event){
					if (event != null) {
						event.preventDefault();
					}
					//event.dataTransfer.effectAllowed = 'copy';
					return false;					
				}

				element.bind('dragover', processDragOver);
				element.bind('dragenter', processDragEnter);				
				
				return element.bind('drop', function(event) {
					
					var files, name, reader, size, type;
					
					$log.debug('Drop event');
					
					if (event != null) {
						//event.stopPropagation();
						event.preventDefault();
					}
					
					files = event.dataTransfer.files;
					
					for(var i=0; i<files.length; i++){
						uploader.addFile(files[i]);
					}
				
					// update parent scope (will update the uploader binded to the fsUploadDebug directive)
					$scope.$parent.$apply();					
					
					return false;
					
				});				
				
			}
		};		
	}])

	return fsUploadModule;

})();
