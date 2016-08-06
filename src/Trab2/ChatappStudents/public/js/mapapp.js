/**
 * Created by André on 12/05/2016.
 */

var mapapp = angular.module('mapapp', ['ngRoute','controllersmapapp']);
//controllersrit2.js containscontrollers code for the views
/*
 *Callback for the config method of the module to configure the routes to the different partial client side views
 * within the signin server side view
 */
mapapp.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
        when('/map', {
            templateUrl: 'partials/map.html', controller: 'mapCtrl'  //register view controller either here or in the template itself
        }).
        // If invalid route, just redirect to the main signin view
        otherwise({ redirectTo: '/map' });
    }]);

/*
 * Function to parse the user profile from base64
 */
function url_base64_decode(str) {
    var output = str.replace('-', '+').replace('_', '/');
    switch (output.length % 4) {
        case 0:
            break;
        case 2:
            output += '==';
            break;
        case 3:
            output += '=';
            break;
        default:
            throw 'Illegal base64url string!';
    }
    return window.atob(output);
}

/*
 * Service to provide an authentication interceptor that will be used in all calls to the $http service to fill the authentication
 * header with the token
 */
mapapp.factory('authInterceptor', function ($rootScope, $q, $window) {
    return {
        request: function (config) {
            config.headers = config.headers || {};
            if ($window.sessionStorage.token) {
                config.headers.Authorization = 'Bearer ' + $window.sessionStorage.token; //sets the http Authorization with the signed token
            }
            return config;
        },
        responseError: function (rejection) {
            if (rejection.status === 401) {
                // handle the case where the user is not authenticated
            }
            return $q.reject(rejection);
        }
    };
});

//Adds the interceptor service to the $httpProvider service
mapapp.config(function ($httpProvider) {
    $httpProvider.interceptors.push('authInterceptor');
});

/*Service to wrap the socket object returned by Socket.io it wraps only the emit and on methods of the Socket.io API
 * we can then use it in an angular controller via this service. (service must be included in the controller definition
 */
mapapp.factory('$socket', function ($rootScope, $window) {
    var socket;
    return {
        connect: function(){
            socket = io.connect('', {'force new connection': true ,
                query: 'token=' + $window.sessionStorage.token
            });
            console.log('socket connected');
        },
        disconnect: function(){
            console.log('Socket Disconnecting');
            socket.disconnect();
        },
        on: function (eventName, callback) {
            socket.on(eventName, function () { // the arguments in the callback function() stored in args contain the sent data
                var args = arguments;
                console.log("received event " + eventName + "with arguments " + args);
                $rootScope.$apply(function () { // this is to apply the data to the scope, how this is done depends on the how the function is writen in the actual call in the controller
                    callback.apply(socket, args);
                });
            });
        },
        emit: function (eventName, data, callback) {
            socket.emit(eventName, data, function () {
                var args = arguments;
                console.log("sent event " + eventName + "with arguments " + args);
                $rootScope.$apply(function () {
                    if (callback) {
                        callback.apply(socket, args);
                    }
                });
            })
        }
    };
});