/*
Angular module for STOMP messaging over web sockets
-sal
*/

(function () {

	'use strict';

	var fsStompModule;

	// create module
	fsStompModule = angular.module('fstore-stomp-module', []);

	// set default options
	fsStompModule.value('fsStompOptions', {
		sockJsUrl: '',
		sockJsOptions: {},
		sockJsProtocols: { 
			protocols_whitelist: ["websocket", "xhr-streaming", "xdr-streaming", "xhr-polling", "xdr-polling", "iframe-htmlfile", "iframe-eventsource", "iframe-xhr-polling"]
		},
		stompHeaders: {
			//login: 'mylogin',
			//passcode: 'mypasscode',
			//// additional header
			//'client-id': 'my-client-id'			
		},
		connection: {
			sock: null,
			stomp: null
		},
		debug: function(str){
			console.log("FsStomp: " + str);
		}
	})
	.factory('FsStomp', ['fsStompOptions', '$log',

	    /**
	     * Factory method which returns FsFileUploader object prototype.
	     */
	    function fsStompFactory(fsUploadOptions, $log){
		
			var userOptions;
		
			/**
			 * Create instance of FsStomp from object prototype.
			 *
			 * @param {Object} options
			 * @constructor
			 */
			function FsStomp(options){
				
				userOptions = options;
				
				_doInit(this, userOptions);
				
			};
			
			/**
			 * Initialize everything
			 */
			function _doInit(fsStomp, options){
				
				var defaultOptions = angular.copy(fsUploadOptions);

				angular.extend(fsStomp, defaultOptions, options);
				
				fsStomp.connection.sock  = new SockJS(fsStomp.sockJsUrl); // url, protocols, options
				fsStomp.connection.stomp = Stomp.over(fsStomp.connection.sock);
				fsStomp.connection.stomp.debug = fsStomp.debug;
				fsStomp.connection.sock.onclose = _onSocketClose;
				
			}
			
			/**
			 * Handle sockjs close
			 */
			function _onSocketClose(){
				_doSocketReconnect();
			}
			
			/**
			 *  Reinitialize and reconnect.
			 */
			function _doSocketReconnect(){
				setTimeout(_initSocket(this, userOptions), 10000);
			}			
			
			/**
			 * Check if object is a function
			 */
			function isFunction(object){
				return object && typeof object === 'function';
			};
			
			/**
			 * Opens the Stomp (and websockt) connection.
			 *
			 * @param {function} connectCallback
			 */
			FsStomp.prototype.connect = function(connectCallback, connectErrorCallback){
				
				if(!isFunction(connectCallback)){
					$log.error('connectCallback function does not exist, or is not a function.');
					return;
				}
				if(!isFunction(connectErrorCallback)){
					$log.error('connectErrorCallback function does not exist, or is not a function.');
					return;
				}				

				this.connection.stomp.connect(
					this.stompHeaders, connectCallback, connectErrorCallback
				);					
				
			};
			
			/**
			 * Subscribe to a Stomp destination
			 *
			 * @param {string} destination
			 * @param {function} messageCallback
			 */
			FsStomp.prototype.subscribe = function(destination, messageCallback){
				
				if(!isFunction(messageCallback)){
					$log.error('messageCallback function does not exist, or is not a function.');
					return;
				}

				var subscription = this.connection.stomp.subscribe(
					destination, messageCallback
				);
				
				return subscription;
				
			};
			
			/**
			 * Send Stomp message.
			 *
			 * examples:
			 * client.send("/queue/test", {priority: 9}, "Hello, STOMP");
			 * client.send(destination, {}, body);
			 *
			 * @param {string} destination
			 * @param {object} headers
			 * @param {String} body
			 */
			FsStomp.prototype.send = function(destination, headers, body){
				
				this.connection.stomp.send(destination, headers, body);
				
			};
			
			/**
			 * Set the stomp debug function
			 *
			 * @param {function} debugFunc
			 */
			FsStomp.prototype.setDebug = function(debugFunc){
				
				if(!isFunction(debugFunc)){
					$log.error('debugFunc function does not exist, or is not a function.');
					return;
				}
				this.debug = debugFunc;
				this.connection.stomp.debug = this.debug;
				
			};
			
			return FsStomp;
			
		}
		
	]);

	return fsStompModule;

})();	