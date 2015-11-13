(function(){

	angular
		.module('fstoreAdministrationMain')
		.controller('mainController',[
			'appConstants', 'FileServices', 'ResourceStore', 'PathResource', 'FsClipboard', 'FsFileUploader', 'FsStomp',
			'$state', '$stateParams', '$mdSidenav', '$mdDialog', '$mdMenu', '$mdToast', '$mdBottomSheet', '$mdUtil', '$log', '$q', '$scope', '$rootScope', '$element', MainController
			]
		);

	function MainController(
		appConstants, FileServices, ResourceStore, PathResource, FsClipboard, FsFileUploader, FsStomp, $state, $stateParams, $mdSidenav, $mdDialog, $mdMenu, $mdToast, $mdBottomSheet, $mdUtil, $log, $q, $scope, $rootScope, $element) {
   
   
		/****************************************************************************************
		 * Internal models bound to UI
		 */
		var sectionTitle = "Administration Home";

		var myFsUploader = new FsFileUploader({
			url: appConstants.httpUploadHandler
        });
		var myStomp;
		
		// tracks path resources that have been selected for "copy".
		// they will be copied to a new location when the user performs a "paste"
		//var pathResourceToCopy = [];
		// tracks path resources that have been selected for "cut".
		// they will be moved to a new location when the user performs a "paste"
		//var pathResourceToMove = [];
		
		var clipboard = new FsClipboard({});
		
		// true if right nav is open, false if closed. By default it's closed.
		var isRightNavOpen = false;


		/****************************************************************************************
		 * On application load:  load all resource stores when page loads (asynchronously)
		 */		
		_handleOnPageLoad();

		
		/**
		 * Fetch all resource stores from server and pre-load first one (if one exists)
		 */
		function _handleOnPageLoad(){
			
			//_handleEventViewStoreList();
			
			_doWebSocketTest();
			
		}	
		
		function _doWebSocketTest(){
			$log.debug('peforming websocket test');
			_initSocket();	
		}
		function _initSocket(){
			
			$log.debug('initializing websocket');
			
			myStomp = new FsStomp({
				sockJsUrl: '/fstore/spring/hello'
			});
			myStomp.setDebug(_myStompDebug);
			myStomp.connect(_myStompConnect, _myStompConnectError);
			
		}
		function _handleEventSendSampleStomp(){
			myStomp.send('/app/hello', {}, JSON.stringify({ 'message': 'this is a test' }));
		}
		function _myStompDebug(str){
			$log.debug('my stomp debug = ' + str);	
		}		
		function _myStompConnect(frame){
			var testSubscription = myStomp.subscribe('/topic/tests', _myStompReceiveTestMessages);
			var echoSubscription = myStomp.subscribe('/topic/echos', _myStompReceiveEchoMessages);
			var uploadSubscription = myStomp.subscribe('/topic/uploads', _myStompReceiveUploadMessages);
		}
		function _myStompConnectError(error){
			$log.debug('_onStompConnectError...');
			//$log.debug(error.headers.message);
			$log.debug('error = ' + JSON.stringify(error));
		}
		function _myStompReceiveTestMessages(socketMessage){
			$log.info('STOMP Received = ' + JSON.stringify(socketMessage));
		}
		function _myStompReceiveEchoMessages(socketMessage){
			$log.info('STOMP Received = ' + JSON.stringify(socketMessage));
		}
		function _myStompReceiveUploadMessages(socketMessage){
			
			$log.info('STOMP Received = ' + JSON.stringify(socketMessage));
			
			var uploadMessage = JSON.parse(socketMessage.body);
			
			$log.info('Upload Message Type = ' + uploadMessage.type);
			
			if(uploadMessage.type == 'UPLOAD_PROCESSED'){
				_handleUploadProcessedMessage(uploadMessage);
			}
			
		}
		function _handleUploadProcessedMessage(uploadMessage){
			
			$log.debug("_handleUploadProcessedMessage called.");
			
		}
		
		/**
		 * Say hello
		 */
		function _doHello(){
			alert("hello from main controller");
		}
		
		/**
		 * Get current section title
		 */
		function _sectionTitle(){
			return sectionTitle;
		}
		
		/**
		 * Get reference to fsUploader
		 */
		function _uploader(){
			return myFsUploader;
		}
				
		/**
		 * Build handler to open/close a SideNav; when animation finishes
		 * report completion in console
		 */
		function _buildToggler(navID) {
			var debounceFn = $mdUtil.debounce(function(){
				$mdSidenav(navID)
				.toggle()
				.then(function () {
					$log.debug("toggle " + navID + " is done");
				});
			},300);
			return debounceFn;
		}
		
		/**
		 * Close left-side nav panel
		 */
		function _leftNavClose() {
			$mdSidenav('MyLeftNav').close()
			.then(function () {
				$log.debug("close MyLeftNav is done");
			});
		};
		
		/**
		 * Close right-side nav panel
		 */
		function _rightNavClose() {
			$mdSidenav('MyRightNav').close()
			.then(function () {
				$log.debug("close MyRightNav is done");
			});
		};

		/**
		 * Return true if right nav is open, false otherwise
		 */
		function _isRightNavOpen(){
			return isRightNavOpen;
		};
		
		/**
		 * Toggle right-nav md-is-locked-open attribute
		 */
		function _toggleRightNavLock(){
			isRightNavOpen = !isRightNavOpen;
			$log.debug("toggle right nav lock");
		};

		/**
		 *
		 */
		function _notImplemented(){
			alert("Feature implementation is forthcoming.");
		}

		/**
		 * Build sample md-toast element
		 */
		function _handleEventClickToastTest(event){

			$mdToast.show({
				controller: _toastyController,
				targetEvent: event,
				parent: angular.element(document.body),
				templateUrl: appConstants.contextPath + '/assets/scripts/angular/settings/modules/main/partials/sampleToast.jsp',
				//template : 'hi',
				hideDelay: 0,
				position: 'bottom right'
			});

			function _toastyController($scope, $mdToast) {
				$scope.closeToast = function() {
					$mdToast.hide();
				};
                $scope.getUploader = function(){
                    return _uploader();
                };
			}			
			
		}
		
		function _contextMenuTest(event){
			
			$log.debug('right-click context menu test -> ' + event);
			
			//var triggerElement = triggerElement || (ev ? ev.target : $element[0]);
			
			var myCustomMenu = angular.element(
				'<div class="md-open-menu-container md-whiteframe-z2">' +
				'<md-menu-content>' +
				'hello!' +
				'</md-menu-content>' +
				'</div>');
			
			$log.debug('ready to open');
			
			$mdMenu.show({
				scope: $rootScope.$new(),
				mdMenuCtrl: {
					open: function(evt) {
						$log.debug('open called!');
					}, 
					close: function() { $mdMenu.hide(); },
					positionMode: function() { return { left: 'target', top: 'target' }; },
					offsets: function() { return { top: 200, left: 200 }; }
				},
				element: myCustomMenu//,
				//target: event.target
			});
			
		};
	
		var self = this;
		
		/*
		 * External API
		 */
		return {
			
			doHello : _doHello,

			leftNavClose : _leftNavClose,
			rightNavClose : _rightNavClose,
			toggleLeftNav : _buildToggler('MyLeftNav'),
			toggleRightNav : _buildToggler('MyRightNav'),
			isRightNavOpen : _isRightNavOpen,
			toggleRightNavLock : _toggleRightNavLock,
			
			contextMenuTest : _contextMenuTest,
			
			notImplemented : _notImplemented,
			sectionTitle : _sectionTitle,
		
			uploader : _uploader,
		
		
			handleEventSendSampleStomp : _handleEventSendSampleStomp,
			
			handleEventClickToastTest : _handleEventClickToastTest
            
		}

	}

})();