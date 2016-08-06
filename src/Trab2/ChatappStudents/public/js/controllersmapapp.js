/**
 * Created by André on 12/05/2016.
 */
var controllersmapapp = angular.module('controllersmapapp',[]); // module to control the register sign in and listing app


controllersmapapp.controller('mapCtrl', function  ($scope, $http, $window, $socket, $location) { //receives the scope of this controller and http service for calls
    $scope.view = 'map'; // includes the string name in the scope of this controller
    $scope.isError = false;   // does not show error in page
    $scope.error = '';        // error string starts blank
    $scope.maphide=true;
    $socket.connect(); // connect to the Server WebSocket
    var addr="";
    
    
    $socket.on('map12', function (data) {
        console.log("yghujkm"+data.morada);
        addr=data.morada;

    });


        $scope.a = function() {
            $scope.maphide=false;
            console.log("ssrrrss" + addr);
            geocoder = new google.maps.Geocoder();
            var latlng = new google.maps.LatLng(-34.397, 150.644);
            var myOptions = {
                zoom: 12,
                center: latlng,
                mapTypeControl: true,
                mapTypeControlOptions: {
                    style: google.maps.MapTypeControlStyle.DROPDOWN_MENU
                },
                navigationControl: true,
                mapTypeId: google.maps.MapTypeId.ROADMAP
            };
            map = new google.maps.Map(document.getElementById("googleMap"), myOptions);
            if (geocoder) {
                geocoder.geocode({
                    'address': addr
                }, function (results, status) {
                    if (status == google.maps.GeocoderStatus.OK) {
                        if (status != google.maps.GeocoderStatus.ZERO_RESULTS) {
                            map.setCenter(results[0].geometry.location);

                            var infowindow = new google.maps.InfoWindow({
                                content: '<b>' + addr + '</b>',
                                size: new google.maps.Size(150, 50)
                            });

                            var marker = new google.maps.Marker({
                                position: results[0].geometry.location,
                                map: map,
                                title: addr
                            });
                            google.maps.event.addListener(marker, 'click', function () {
                                infowindow.open(map, marker);
                            });

                        } else {
                            alert("No results found");
                        }
                    } else {
                        alert("Geocode was not successful for the following reason: " + status);
                    }
                });
            }
            google.maps.event.addDomListener(window, 'load', initialize);
        }

});