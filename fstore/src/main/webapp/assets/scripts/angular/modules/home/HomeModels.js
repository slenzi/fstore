(function(){
	
	'use strict';
	
	var homeApp = angular.module('home');

	// Create directory resource model
	homeApp.factory('DirectoryResource', [
			'appConstants', '$log', '$q', DirectoryResourceFactory
			]
		);
		
	// Create resource store model
	homeApp.factory('ResourceStore', [
			'appConstants', '$log', '$q', ResourceStoreFactory
			]
		);		
	
	function DirectoryResourceFactory(appConstants, $log, $q){
		
		function DirectoryResource(directoryData){
			
			// set defaults
			angular.extend(this, {
				id: 'directory id not set',
				name: 'name not set',
				description: 'description not set',
				dateCreated: 'date created not set',
				dateUpdated: 'date updated not set',
				children: []
			});
			
			// update defaults with user provided data if we have some
			if(directoryData){
				this.setData(directoryData);
			}
			
		};

		// extend functionality
		DirectoryResource.prototype = {
			setData: function(directoryData){
				angular.extend(this, directoryData);
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
			}			
		};	
		
		// return this
		return DirectoryResource;
		
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

})();