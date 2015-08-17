/*
Angular HTTP upload module
-sal
*/

(function () {

	'use strict';

	var fsUploadModule;

	// create module
	fsUploadModule = angular.module('fsUpload', []);

	fsUploadModule.value('fsUploadOptions', {
		url: '/',                         // URL file data will be submitted to
		progress: 0,                      // tracks progress for the upload. 0 = 0% and 100 = 100%
		method: 'POST',                   // http method used to submit the upload
		formData: {},                     // optional form data (key-value pairs) to be submitted along with the file data.
		files: {},                        // tracks file to be uploaded. All files added to upload queue will be added to this object.
		headers: {}                       // optional http headers (currently not used.)
	})
	// object for storing file data
	.factory('FsFileItem', ['$log', '$q',
	                               
	    function fsFileItemFactory($log, $q){
		
			function FsFileItem(fileData){
				
				// set defaults
				angular.extend(this, {
					name: 'name not set',  // name of file being uploaded
                    progress: 0,           // tracks progress for the upload. 0 = 0% and 100 = 100%
					file: []               // file being uploaded
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
				},
				getHumanReadableSize(){
					return _humanFileSize(this.file.size,true);
				}
			};

			function _humanFileSize(bytes, si) {
				var thresh = si ? 1000 : 1024;
				if(Math.abs(bytes) < thresh) {
					return bytes + ' B';
				}
				var units = si
					? ['kB','MB','GB','TB','PB','EB','ZB','YB']
					: ['KiB','MiB','GiB','TiB','PiB','EiB','ZiB','YiB'];
				var u = -1;
				do {
					bytes /= thresh;
					++u;
				} while(Math.abs(bytes) >= thresh && u < units.length - 1);
				return bytes.toFixed(1)+' '+units[u];
			}			
			
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
				
			};		

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
			 * Add form value to be submitted along with the file data
			 *
			 * @param key - the name of the form field
			 * @param value - the value of the form field
			 */
			FsFileUploader.prototype.addFormValue = function(key, value){
				
				$log.debug('adding form value, key = ' + key + ', value = ' + value);
				
				this.formData[key] = value;
				
			}
			
			/**
			 * Clear all form data / value
			 */
			FsFileUploader.prototype.clearFormValues = function(){
				
				this.formData = {};
				
			}			
			
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
	
                this.files[fileToAdd.name] = fileItem;
				//this.progress += 1;
				
				//$log.debug('fsUploader: ' + JSON.stringify(this));
				
			};
			
			/**
			 * Removes all files from the upload queue.
			 */
			FsFileUploader.prototype.clearQueue = function(){
				this.files = {};
			};
			
			/**
			 * Check if the upload queue is empty. Returns true if empty, false if not.
			 */
			FsFileUploader.prototype.isQueueEmpty = function(){
				if( Object.keys(this.files).length == 0 ){
					return true;
				}
				return false;
			};			
			
			/**
			 * XML Http Request onprogress event handler
			 *
			 * uploader - the FsFileUploader that's performing the upload
			 * uploadProgressCallback - optional callback for upload progress event
			 * event - the progress event
			 */
			FsFileUploader.prototype.xhrOnProgress = function(uploader, uploadProgressCallback, event){
				
				var progressValue = Math.round(event.lengthComputable ? event.loaded * 100 / event.total : 0);
				
				uploader.progress = progressValue;
				
				// call users progress callback if they provided one
				if(uploadProgressCallback && typeof uploadProgressCallback === 'function'){
					uploadProgressCallback(event);
				}
				
			};
            
			/**
			 * XML Http Request onprogress event handler, per file item
			 *
			 * uploader - the FsFileUploader that's performing the upload
             * fsFileItem - the file item that's currently being uploaded
			 * uploadProgressCallback - optional callback for upload progress event
			 * event - the progress event
			 */
			FsFileUploader.prototype.xhrOnProgressFileItem = function(uploader, fsFileItem, uploadProgressCallback, event){
				
				var progressValue = Math.round(event.lengthComputable ? event.loaded * 100 / event.total : 0);
				
				uploader.progress = progressValue;
                
                fsFileItem.progress = progressValue;
				
				// call users progress callback if they provided one
				if(uploadProgressCallback && typeof uploadProgressCallback === 'function'){
					uploadProgressCallback(event);
				}
				
			};
			
			/**
			 * XML Http Request onload event handler
			 *
			 * uploadCompleteCallback - optional callback for upload complete event
			 */
			FsFileUploader.prototype.xhrOnLoad = function(uploader, uploadCompleteCallback, event){
				
				//uploader.clearQueue();
				
				// call users upload complete callback if they provided one
				if(uploadCompleteCallback && typeof uploadCompleteCallback === 'function'){
					uploadCompleteCallback(event);
				}				
				
			};

			/**
			 * XML Http Request onerror event handler
			 */
			FsFileUploader.prototype.xhrOnError = function(event){
				$log.debug('An error occured while uploading the file data to the server.');
			};

			/**
			 * XML Http Request onabort event handler
			 */
			FsFileUploader.prototype.xhrOnAbort = function(event){
				$log.debug('Upload has been canceled by the user.');
			};			
			
			/**
			 * Uploads all files in the queue, all in one single upload.
			 *
			 * uploadProgressCallback - optional callback for upload progress event
			 * uploadCompleteCallback - optional callback for upload complete event
			 */
			FsFileUploader.prototype.doUpload = function(uploadProgressCallback, uploadCompleteCallback){
				
				_doUploadAsGroup(this, uploadProgressCallback, uploadCompleteCallback);
                
			};
            
			/**
			 * Uploads all files in the queue, as seperate individual uploads.
			 *
			 * uploadProgressCallback - optional callback for upload progress event
			 * individualUploadCompleteCallback - optional callback for upload complete event (for each file)
             * allUploadCompleteCallback - optional callback for once all fioles in the queue have be uploaded.
			 */
			FsFileUploader.prototype.doUploadSingular = function(uploadProgressCallback, individualUploadCompleteCallback, allUploadCompleteCallback){
				
				_doUploadSingular(this, uploadProgressCallback, individualUploadCompleteCallback, allUploadCompleteCallback);
                
			};
            
            /**
             * Uploads all files in the queue as one single upload to the server.
             *
             * fsUploader - reference to the fsUploader object
			 * uploadProgressCallback - optional callback for upload progress event
			 * uploadCompleteCallback - optional callback for upload complete event             
             */
            function _doUploadAsGroup(fsUploader, uploadProgressCallback, uploadCompleteCallback){
                
                $log.debug('Uploading all files in queue as one single upload.');
                
                fsUploader.progress = 0;
                
				var fileNames = Object.keys(fsUploader.files);
				
				if(fileNames.length == 0){
					alert('There are no files in the upload queue. Try adding some files...');
                    return;
				}
				
				var form = new FormData();
				var xhr = new XMLHttpRequest();
				
				// call this.xhrOnProgress for each progress update event. pass this (FsFileUploader)
				// plus uploadProgressCallback method (will be called if user provided one.)
				xhr.upload.addEventListener(
					"progress",
					angular.bind(null, fsUploader.xhrOnProgress, fsUploader, uploadProgressCallback),
					false
				);
				//xhr.upload.onprogress = fsUploader.xhrOnProgress;
				xhr.addEventListener(
					"load",
					angular.bind(null, fsUploader.xhrOnLoad, fsUploader, uploadCompleteCallback),
					false
				);
				//xhr.onload = fsUploader.xhrOnLoad;
				xhr.onerror = fsUploader.xhrOnError;
				xhr.onabort = fsUploader.xhrOnAbort;

				// append file data
				angular.forEach(fileNames, function(fileName, fileIndex) {
					var fsFileItem = fsUploader.files[fileName];
					form.append("file_" + fileIndex, fsFileItem.file);
				}, fsUploader);
			
				// append user form key-values
				var keyNames = Object.keys(fsUploader.formData);
				angular.forEach(keyNames, function(keyName, keyIndex) {
					var keyValue = fsUploader.formData[keyName];
					form.append(keyName, keyValue);
				}, fsUploader);				
			
				$log.debug('Submitting http ' + fsUploader.method + ' to ' + fsUploader.url);
				
				xhr.open(fsUploader.method, fsUploader.url);
				xhr.send(form);                
                
            }
            
            /**
             * Uploads all files in the queue, but each file is treated as a single/seperate upload. (e.g., 5 files equals 5 uploads to the server.)
             *
             * fsUploader - reference to the fsUploader object
			 * uploadProgressCallback - optional callback for upload progress event
			 * individualUploadCompleteCallback - optional callback for upload complete event (for each file)
             * allUploadCompleteCallback - optional callback for once all fioles in the queue have be uploaded.            
             */
            function _doUploadSingular(fsUploader, uploadProgressCallback, individualUploadCompleteCallback, allUploadCompleteCallback){
                
                $log.debug('Uploading all files in queue as seperate, singular uploads.');
                
                fsUploader.progress = 0;
                
				var fileNames = Object.keys(fsUploader.files);
				
				if(fileNames.length == 0){
					alert('There are no files in the upload queue. Try adding some files...');
                    return;
				}
                
                var completeCallback = individualUploadCompleteCallback;
                
				angular.forEach(fileNames, function(fileName, fileIndex) {
                    
                    var fsFileItem = fsUploader.files[fileName];
                    
                    // use upload complete callback if last file
                    if(fileIndex == (fileNames.length - 1)){
                        completeCallback = allUploadCompleteCallback;
                    }
                    
                    _doUploadFileItem(fsUploader, fsFileItem, uploadProgressCallback, completeCallback);
	
				}, fsUploader);
                
                //allUploadCompleteCallback();
                
            }
            
            /**
             * Uploads the single fsFileItem to the server.
             *
             * fsUploader - reference to the fsUploader object
             * fsFileItem - file item model which contains reference the file data object
			 * uploadProgressCallback - optional callback for upload progress event
			 * uploadCompleteCallback - optional callback for upload complete event            
             */
            function _doUploadFileItem(fsUploader, fsFileItem, uploadProgressCallback, uploadCompleteCallback){
                
                fsUploader.progress = 0;
                
				var form = new FormData();
				var xhr = new XMLHttpRequest();
				
				// call this.xhrOnProgress for each progress update event. pass this (FsFileUploader)
				// plus the current fsFileItem being uploaded, and the uploadProgressCallback method (will be called if user provided one.)
				xhr.upload.addEventListener(
					"progress",
					angular.bind(null, fsUploader.xhrOnProgressFileItem, fsUploader, fsFileItem, uploadProgressCallback),
					false
				);
				//xhr.upload.onprogress = fsUploader.xhrOnProgress;
				xhr.addEventListener(
					"load",
					angular.bind(null, fsUploader.xhrOnLoad, fsUploader, uploadCompleteCallback),
					false
				);
				//xhr.onload = fsUploader.xhrOnLoad;
				xhr.onerror = fsUploader.xhrOnError;
				xhr.onabort = fsUploader.xhrOnAbort;
                
                // add file to form data
                form.append("file_0", fsFileItem.file);
                
				// append user form key-values
				var keyNames = Object.keys(fsUploader.formData);
				angular.forEach(keyNames, function(keyName, keyIndex) {
					var keyValue = fsUploader.formData[keyName];
					form.append(keyName, keyValue);
				}, fsUploader);
                
				$log.debug('Submitting http ' + fsUploader.method + ' to ' + fsUploader.url);
				
				xhr.open(fsUploader.method, fsUploader.url);
				xhr.send(form);                 
                
            }			

			// return object prototype
			return FsFileUploader;

		}
	])
	// directive which displays various debug information
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
				fsUploader: '=uploader'
			},
			// display all attributes of the uploader object in a bulleted list
			template: '<ul ng-repeat="(key, value) in fsUploader"><li><b>{{key}}</b> = {{value}}</li></ul>',
			link: function ($scope, element, attributes){
				$scope.$watch('fsUploader.progress', function(updatedUploader) {
					//$log.debug('fsUploader changed = ' + JSON.stringify(updatedUploader))
				});
			}
		};
	}])
	// directive which displays an angular material linear progress bar (requires Angular Material)
	.directive('fsUploadProgress', ['$log', 'FsFileUploader', function($log, FsFileUploader) {
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
				fsUploader: '=uploader'
			},
			// must wrap md-progress-linear directive in a div
			// http://stackoverflow.com/questions/16148086/multiple-directives-directive1-directive2-asking-for-isolated-scope-on
			template:
				'<div>' +
                '   <md-progress-linear class="md-accent" md-mode="determinate" value="{{fsUploader.progress}}">' +
				'   </md-progress-linear>' +
				'</div>',
			link: function ($scope, element, attributes){

			}
		};
	}])
	// directive which displays all files added to the upload queue in a simple <ul><li></li></ul> list.
	.directive('fsUploadQueueSimple', ['$log', 'FsFileUploader', function($log, FsFileUploader) {
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
				fsUploader: '=uploader'
			},
			template:
				'<ul ng-repeat="fsFileItem in fsUploader.files">' +
				'   <li>{{fsFileItem.name}} ({{fsFileItem.getHumanReadableSize()}})</li>' +
				'</ul>',
			link: function ($scope, element, attributes){

			}
		};
	}])
	// directive which displays all files added to the upload queue in a table (angular "smart-table" module)
	.directive('fsUploadQueueTable', ['$log', 'FsFileUploader', function($log, FsFileUploader) {
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
				fsUploader: '=uploader'
			},
			template:
                '<table st-table="fsUploader.files" class="table table-striped">' +
                '    <thead>' +
                '    <tr>' +
                '        <th>Name</th>' +
                '        <th>Size</th>' +
                '        <th>Progress Value</th>' +
                '        <th>Progress Bar</th>' +
                '    </tr>' +
                '    </thead>' +
                '    <tbody>' +
                '    <tr ng-repeat="fsFileItem in fsUploader.files">' +
                '        <td>{{fsFileItem.name}}</td>' +
                '        <td>{{fsFileItem.getHumanReadableSize()}}</td>' +
                '        <td>' +
                '           {{fsFileItem.progress}}% ' +
                '        </td>' +
                '        <td>' +
				'           <div>' +
                '               <md-progress-linear class="md-accent" md-mode="determinate" value="{{fsFileItem.progress}}">' +
				'               </md-progress-linear>' +
				'           </div>' +
                '        </td>' +
                '    </tr>' +
                '    </tbody>' +
                '</table>',
			link: function ($scope, element, attributes){

			}
		};
	}])
	// directive for input type file (opens file select dialog)
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
				
				//$log.debug('typeof(element) = ' + typeof(element));
				//$log.debug('element.html() = ' + element.html());
				//$log.debug('elm.nodeName = ' + elm.nodeName);
				//$log.debug('attributes.uploader = ' + attributes.uploader);
				//$log.debug('Uploader is HTML 5 = ' + uploader.isHTML5);
				
				// angular has no built in support for binding to a file input.
				// https://github.com/angular/angular.js/issues/1375
				// http://stackoverflow.com/questions/17922557/angularjs-how-to-check-for-changes-in-file-input-fields
				element.bind('change', function(event){
					var files = event.target.files;
					for(var i=0; i<files.length; i++){
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
