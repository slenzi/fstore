(function(){
	
	'use strict';
	
	var mainModule = angular.module('fsCmsMain');
	
	mainModule.factory('CmsSite', [
		'appConstants', '$log', '$q', CmsSiteFactory
		]
	);	

	function CmsSiteFactory(appConstants, $log, $q){
		
		function CmsSite(siteDate){
			
			// set defaults
			angular.extend(this, {
				siteId: 'site id not set',
				name: 'name not set',
				description: 'description not set',
				dateCreated: 'date created not set',
				dateUpdated: 'date updated not set'
			});
			
			// update defaults with user provided data if we have some
			if(siteDate){
				this.setData(siteDate);
			}
			
		};

		// extend functionality
		CmsSite.prototype = {
			setData: function(siteDate){
				angular.extend(this, siteDate);
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

})();