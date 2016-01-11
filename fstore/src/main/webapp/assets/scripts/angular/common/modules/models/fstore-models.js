/**
 * Common models use by all fstore modules/apps
 */
(function(){
	
	'use strict';
	
	var fstoreModels;
	
	fstoreModels = angular.module('fstore-models-module', []);
	
	// Create directory resource model
	fstoreModels.factory('PathResource', [ 'appConstants', '$log', '$q', PathResourceFactory ] );
		
	// Create resource store model
	fstoreModels.factory('ResourceStore', [ 'appConstants', '$log', '$q', ResourceStoreFactory ] );

	// Create cms site model
	fstoreModels.factory('CmsSite', [ 'appConstants', '$log', '$q', CmsSiteFactory ] );
	
	// Create clipboard
	fstoreModels.factory('FsClipboard', [ 'appConstants', '$log', '$q', FsClipboardFactory ] );	

	
	function PathResourceFactory(appConstants, $log, $q){
		
		function PathResource(data){
			
			// set defaults
			angular.extend(this, {
				id: 'id not set',
				name: 'name not set',
				description: 'description not set',
				dateCreated: 'date created not set',
				dateUpdated: 'date updated not set',
				isSelected: false,
				children: []
			});
			
			// update defaults with user provided data if we have some
			if(data){
				this.setData(data);
			}
			
		};

		// extend functionality
		PathResource.prototype = {
			setData: function(data){
				angular.extend(this, data);
			},
			getId: function(){
				return this.id;
			},
			setId: function(id){
				this.id = id;
			},			
			getName: function(){
				return this.name;
			},
			setName: function(name){
				this.name = name;
			},
			getDescription: function(){
				return this.description;
			},
			setDescription: function(name){
				this.name = description;
			},			
			getDateCreated: function(){
				return this.dateCreated;
			},
			setDateCreated: function(dateCreated){
				this.dateCreated = dateCreated;
			},
			getDateUpdated: function(){
				return this.dateUpdated;
			},
			setDateUpdated: function(dateUpdated){
				this.dateUpdated = dateUpdated;
			},
			getChildren: function(){
				return this.children;
			},
			setChildren: function(children){
				this.children = children;
			},
			isSelected: function(){
				return isSelected;
			},
			setSelected: function(isSelected){
				this.isSelected = isSelected;
			},
			getHumanReadableSize: function(){
				return _humanFileSize(this.size,true);
			},
			getMimeType: function(){
				return this.mimeType;
			},
			isTextMime: function(){
				if( _stringStartsWith(this.mimeType, "text") || _stringStartsWith(this.mimeType, "TEXT")){
					return true;
				}else{
					return false;
				}
			}
		};
		
		function _stringStartsWith (string, prefix) {
			if(!string || !prefix){
				return false;
			}
		    return string.slice(0, prefix.length) == prefix;
		}		

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
		return PathResource;
		
	}
	
	function ResourceStoreFactory(appConstants, $log, $q){
		
		function ResourceStore(storeData){
			
			// set defaults
			angular.extend(this, {
				id: 'store id not set',
				name: 'name not set',
				description: 'description not set',
				storePath: 'store path not set',
				rootDirectoryId: 'root directory id not set',
				dateCreated: 'date created not set',
				dateUpdated: 'date updated not set'
			});
			
			// update defaults with user provided data if we have some
			if(storeData){
				this.setData(storeData);
			}
			
		};

		// extend functionality
		ResourceStore.prototype = {
			setData: function(storeData){
				angular.extend(this, storeData);
			},
			getId: function(){
				return this.id;
			},
			setId: function(id){
				this.id = id;
			},			
			getName: function(){
				return this.name;
			},
			setName: function(name){
				this.name = name;
			},
			getDescription: function(){
				return this.description;
			},
			setDescription: function(name){
				this.name = description;
			},
			getStorePath: function(){
				return this.storePath;
			},
			setStorePath: function(storePath){
				this.storePath = storePath;
			},
			getRootDirectoryId: function(){
				return this.rootDirectoryId;
			},
			setRootDirectoryId: function(rootDirectoryId){
				this.rootDirectoryId = rootDirectoryId;
			},			
			getDateCreated: function(){
				return this.dateCreated;
			},
			setDateCreated: function(dateCreated){
				this.dateCreated = dateCreated;
			},
			getDateUpdated: function(){
				return this.dateUpdated;
			},
			setDateUpdated: function(dateUpdated){
				this.dateUpdated = dateUpdated;
			}
		};	
		
		// return this
		return ResourceStore;
		
	}

	function CmsSiteFactory(appConstants, $log, $q){
		
		function CmsSite(siteData){
			
			// set defaults
			angular.extend(this, {
				siteId: 'site id not set',
				name: 'name not set',
				description: 'description not set',
				dateCreated: 'date created not set',
				dateUpdated: 'date updated not set'
			});
			
			// update defaults with user provided data if we have some
			if(siteData){
				this.setData(siteData);
			}
			
		};

		// extend functionality
		CmsSite.prototype = {
			setData: function(siteData){
				angular.extend(this, siteData);
			},
			getSiteId: function(){
				return this.siteId;
			},
			setSiteId: function(siteId){
				this.siteId = siteId;
			},			
			getName: function(){
				return this.name;
			},
			setName: function(name){
				this.name = name;
			},
			getDescription: function(){
				return this.description;
			},
			setDescription: function(name){
				this.name = description;
			},
			getDateCreated: function(){
				return this.dateCreated;
			},
			setDateCreated: function(dateCreated){
				this.dateCreated = dateCreated;
			},
			getDateUpdated: function(){
				return this.dateUpdated;
			},
			setDateUpdated: function(dateUpdated){
				this.dateUpdated = dateUpdated;
			}
		};	
		
		// return this
		return CmsSite;
		
	}

	function FsClipboardFactory(appConstants, $log, $q){

		var FsPathResourceOperation = {
			fileIdList : [],
			dirIdList : [],
			sourceDirId : -1,
			targetDirId : -1,
			replaceExisting : true
		};
		
		function FsClipboard(clipboardData){	
			
			// set defaults
			angular.extend(this, {
				operation: { type: '', data: {} }
			});
			
			// update defaults with user provided data if we have some
			if(clipboardData){
				this.setData(clipboardData);
			}
			
		};
		
		function _clear(){
			
			angular.extend(this, {
				operation: { type: '', data: {} }		
			});			
			
		}
		
		function _isEmpty(){
			
			var operation = this.operation;
			
			if(operation.data.fileIdList && operation.data.fileIdList.length > 0 ||
				operation.data.dirIdList && operation.data.dirIdList.length > 0){
					
				return false;
			}
			return true;
		}
		
		function _setOperation(type, fileIdList, dirIdList, sourceDirId, targetDirId, replaceExisting){
			var pathOperation = fromPrototype(FsPathResourceOperation, {
				fileIdList : fileIdList,
				dirIdList : dirIdList,
				sourceDirId : sourceDirId,
				targetDirId : targetDirId,
				replaceExisting : replaceExisting
			});
			var newOperation = {
				type: type,
				data: pathOperation
			}
			this.operation = newOperation;
		}	

		// extend functionality
		FsClipboard.prototype = {
			setData: function(clipboardData){
				angular.extend(this, clipboardData);
			},
			clear : _clear,
			isEmpty : _isEmpty,
			setOperation : _setOperation
		};	
		
		// return this
		return FsClipboard;
		
	}

	var fromPrototype = function(prototype, object) {  
		var newObject = Object.create(prototype);
		for (var prop in object) {
			if (object.hasOwnProperty(prop)) {
				newObject[prop] = object[prop];
			}
		}
		return newObject;
	};	
	
})();