(function(){
	
	'use strict';
	
	var homeApp = angular.module('home');

	// Create directory resource model
	homeApp.factory('DirectoryResource', [
			'appConstants', '$log', '$q', DirectoryResourceFactory
			]
		);
	
	function DirectoryResourceFactory(appConstants, $log, $q){
		
		function DirectoryResource(directoryData){
			
			// set defaults
			angular.extend(this, {
				name: 'name not set',
				dateCreated: 'date created not set',
				dateUpdated: 'date updated not set'
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
			getName: function(){
				return this.name;
			},
			setName: function(name){
				this.name = name;
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
		return DirectoryResource;
		
	}

})();