(function(){
	
	'use strict';
	
	var homeApp = angular.module('home');

	// Create directory resource model
	homeApp.factory('PathResource', [
			'appConstants', '$log', '$q', PathResourceFactory
			]
		);
		
	// Create resource store model
	homeApp.factory('ResourceStore', [
			'appConstants', '$log', '$q', ResourceStoreFactory
			]
		);

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
			}
		};	
		
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

})();