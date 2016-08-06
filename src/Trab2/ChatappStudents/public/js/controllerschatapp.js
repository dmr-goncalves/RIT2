var controllerschatapp = angular.module('controllerschatapp',[]); // module to control the register sign in and listing app

//ng-view
//We have an individual controller for each different client side view of the app

//controller for the register view part of the controllerschatapp modules part of the root module rit2app
controllerschatapp.controller('registerCtrl', function ($scope, $http, $window,$location) { //receives the scope of this controller and http service for calls
  $scope.view = 'Register'; // includes the string name in the scope of this controller
  $scope.isError = false;   // does not show error in page
  $scope.error = '';        // error string starts blank



console.log($scope);
        if($window.sessionStorage.facename!=null && $window.sessionStorage.facename!=undefined) {

          $scope.name = $window.sessionStorage.facename;
          $scope.email = $window.sessionStorage.faceemail;
          $scope.userface = $window.sessionStorage.facename;
          $scope.address = $window.sessionStorage.faceLocale;
   }
  //function that deals with the submit button click
  $scope.register = function (){
      console.log($scope);
	  $http						//$scope.user is the object containing form values
      .post('/newuser', $scope.user) //performs an http post with URL /newuser
      .success(function (data, status, headers, config) { //called if HTTP call was sucessfull
        console.log(data); //data contains the data part of the HTTP response
        if(data.name !=undefined &&  data.address != undefined && data.email != undefined && data.avatar!= undefined && data.username!= undefined && data.password!= undefined ){
          $location.path("/signin");
        }
        else{
          $scope.isError = true;  // show error in page
          $scope.error = 'Error: Error in Regist please try again';
        }
    	   // test data if everything ok you can route the sign in view with $location.path("/signin");
         /* if not you can show error:
           $scope.isError = true;  // show error in page
           $scope.error = 'Error: User name already exists please try again';*/
      })
      .error(function (data, status, headers, config) { //if HTTP response was not OK
      // Handle submit errors (show error in page and whatever actions are needed)
      $scope.isError = true;  // show error in page
      $scope.error = 'Error: Error in Regist please try again';
      });
  };
});

/* Controller for the sign in view callback function receives the $scope, $http, $window, $location and $socket services
 */
controllerschatapp.controller('signinCtrl', function ($scope, $http, $window, $location, $socket) {
	 // if user already logged in previoulsy connect socket and go to chat page without displaying the form
  if ($window.sessionStorage.islogged) {
      $location.path( "/Chat" ); //if already logged go to chat view
	  console.log("re-directing to chat view, login already submitted");
  }
  else{
	  $scope.view = 'Sign In'; //initial view state
	  $scope.isError = false;
  }


    $scope.login = function () {
            console.log($window);
            alert("\n\nPlease Click in the Register button on the right after Log in on Facebook. \n\n                                            Just for fun ;)\n\n");

        FB.login(function(response) {

        if (response.authResponse) {
            console.log('Welcome!  Fetching your information.... ');
            FB.api('/me', 'get', {fields: 'id,name,gender,email,locale,location' }, function(response) {
                console.log('Good to see you, ' + response.name + '.');
                console.log(response);

                $window.sessionStorage.facename=response.name;
                $window.sessionStorage.faceemail=response.email;
                $window.sessionStorage.faceLocale=response.locale;
                $location.path( "/Register" );
            })
        } else {
            console.log('User cancelled login or did not fully authorize.');
        }

    });


}
 /*
   * Function that deals with a click in the submit button
   */
  $scope.submit = function () {
//call HTTP post with form contents
    $http						//$scope.user is the object containing form values
      .post('/authenticate', $scope.user) //performs an http post with URL /ValidateUser
      .success(function (data, status, headers, config) { //called if HTTP call was sucessfull
    //TODO deal with response
    //If success:
     //store token if success so that the authInterceptor service and the socket service can use it for authentication
      $window.sessionStorage.token = data.token;
      $window.sessionStorage.username= $scope.user.username;
      $window.sessionStorage.islogged = true; //to indicate user already logged stays valid until tab is closed
      //Connect socket and re-route to auction view using $location
      $location.path( "/Chat" ); //if already logged go to chat view

    // Erase the token if the user fails to log in since the token is invalid and Handle errors here
      })
      .error(function (data, status, headers, config) { //if HTTP response was not OK;
        $scope.isError = true;  // show error in page
        $scope.error = 'Error: Wrong user or password';
	    });
  };
});

/* Controller for the chat view callback function receives the $scope, $http, $window, $location and $socket services
 */
controllerschatapp.controller('ChatCtrl', function ($scope, $http, $window, $socket, $location) {
    $socket.connect(); // connect to the Server WebSocket
    $scope.messages = [];	//messages array initialization
	 $scope.showchat = false; // initially view only online users list and not the chat interface
	 $scope.UserName = $window.sessionStorage.username; // show username in view
	 $scope.talkingto = ''; //show a blank strin in the view talkin to label
    $scope.friendlist = [];

	///after getting the partial view from the server and upon controller start


	//emit newUser:username event in every page render
        $socket.emit('newUser:username', { //send new user event via websocket to server containing username
	         username: $window.sessionStorage.username
});

	// Socket event listeners - define action when receiving several different types of messages via the socket
  // These events are suggestions feel free to change them remove and/or create new ones
	$socket.on('init', function (data) { //initial event receives the list of all current online users
		  //handle data
        $scope.Address=data.address;
        $scope.users=data.users;
        $scope.friendlist=data.friendlist;
        $scope.avatar=data.avatar;

        for(i=0;i<$scope.friendlist.length;i++){
          $socket.emit('atuafriend', { //send new user event via websocket to server containing username
             username: $window.sessionStorage.username,
             friend: $scope.friendlist[i].name
  });
}



});

$socket.on('send:message', function (message) { // receiving a message from the server add message to messages scope
    var data={
        user: message.user,
        text: message.message
    };
    $scope.messages.push(data);
    console.log($scope);
    $scope.iswriting=false;

});

$socket.on('user:join', function (data) { // event received when a user logs in the server
    $scope.users.push(data.username);
    console.log(data);

});

$socket.on('close:talkingto', function (data) { // event received when the remote user stops a chat
    $scope.showchat=false;
    $scope.talkingto = ""; //clear talking to Label

});

$socket.on('open:talkingto', function (data) { // event received when the remote user starts a chat
    console.log(data);
    $scope.showchat = true;
    $scope.talkingto = data.talkingto;
});

$socket.on('open:addfriend', function (data) { // event received when the remote user starts a chat
    console.log(data);
    $scope.friend=data.friend;
    $scope.avatf=data.avatar;
    $scope.showfrequest = true;
    $scope.online=true;
    $scope.offline=true;
});
    $socket.on('open:deletefriend', function (data) { // event received when the remote user starts a chat
        console.log(data);
        for (var i = 0; i < $scope.friendlist.length; i++) {

            if ($scope.friendlist[i].name == data.friend) {
                $scope.friendlist.splice(i, 1);
            }
        }
    });

    $socket.on('open:online', function (data) { // event received when the remote user starts a chat
        console.log(data);

        for(i=0;i<$scope.friendlist.length;i++){
            if($scope.friendlist[i].name==data.friend){
                $scope.friendlist[i].logged=data.logged;
            }
        }
    });



    $socket.on('open:emitYes', function (data) { // event received when the remote user starts a chat
    console.log(data);
    $scope.friend=data.friend;
    var friendg = {
        name: data.friend,
        logged:data.logged,
        avatar:data.avatar
    };

    $scope.friendlist.push(friendg);
    $scope.showfAccept=true;
    $scope.showfReject=false;
});

$socket.on('open:emitNo', function (data) { // event received when the remote user starts a chat
    console.log(data);
    $scope.friend=data.friend;
    $scope.showfReject=true;
    $scope.showfAccept=false;
});

$socket.on('user:left', function (data) { // event received when a user logs out the server
    var i = $scope.users.indexOf(data.username);
    console.log("index"+i);
    if(i>-1)
        $scope.users.splice(i, 1);
});
    $socket.on('map_talk', function (data) {

        $socket.emit('map_talk2', {
            address: $scope.Address,
            talkingto: $scope.talkingto
        });
    });
    $socket.on('isw', function (data) { // event received when the remote user starts a chat
        console.log(data+"merda12");
        $scope.iswriting=true;
    });

    $socket.on('atuafriend', function (data) { // event received when the remote user starts a chat
        console.log(data+"merda1223");

        for(i=0;i<$scope.friendlist.length;i++){
            if($scope.friendlist[i].name==data.friend){
                $scope.friendlist[i].logged=true;
              }
            }

    });

// Methods associated with the scope that emit trough the websocket
   //method called when send message button is clicked
    $scope.sendMessage = function () { // sends a message via websocket to the server
        $socket.emit('send:message', {
            dest: $scope.talkingto,
            message: $scope.message,
            user: $window.sessionStorage.username
        });
        // add the message to our model locally to appear in the view
        var data = {
            user: $window.sessionStorage.username,
            text: $scope.message
        };
       $scope.messages.push(data);
        // clear message box
       // $scope.message = '';
    };

    $scope.sendMessage_to_group = function () { // sends a message via websocket to the server
        $socket.emit('send:message_to group', {
            friendlist: $scope.friendlist,
            message: $scope.messagetoall,
            user: $window.sessionStorage.username
        });
        // add the message to our model locally to appear in the view
        var data = {
        user: $window.sessionStorage.username,
        text: $scope.messagetoall
    };
        console.log(data);
    $scope.messages.push(data);
       // $scope.showchat = true;
    // clear message box
        // $scope.message = '';
    };

    //Method called when send close button is clicked
    $scope.closeConnection = function(){  // sends a message to inform the remote user that the chat is over
        $socket.emit('close:talkingto',{
            talkingto: $scope.talkingto,
            username: $window.sessionStorage.username
        });
        //hide the chat interface
        $scope.showchat=false;
        $scope.talkingto = ""; //clear talking to Label
    };

    //scope function called on click of a member of the users list
    $scope.talkto = function(username){
        $socket.emit('open:talkingto',{

            talkingto: username,
            username: $window.sessionStorage.username
        });
        $scope.talkingto = username;
        $scope.showchat=true;
        console.log("clicked in user: " + username);
    };

    //scope function called on click of the logout button
    $scope.logout = function () { //function associated with the logout button receives user.name scope in html form template
        console.log("logout user : " + $window.sessionStorage.username);
   		         //clears session variables
        $window.sessionStorage.islogged="";
        console.log($window.sessionStorage);
        $window.sessionStorage.token="";

        $socket.emit('logout', {
            username: $window.sessionStorage.username,
            friendlist: $scope.friendlist
        });
        console.log($scope.friendlist);
        // Send event to server
       // disconnects WebSocket
        $socket.disconnect();
      // re-route to sign in view
        $location.path( "/signin" );
    };


    $scope.emitYes = function(friend){
        $socket.emit('open:emitYes',{

            friend: friend,
            username: $window.sessionStorage.username,
            logged:true,
            avatar: $scope.avatar
        });

        $scope.showfrequest=false;


        var friendg = {
            name: friend,
            logged:true,
            avatar: $scope.avatf
        };
        $scope.friendlist.push(friendg);
        console.log($scope);
    };


    $scope.emitNo = function(friend){
        $socket.emit('open:emitNo',{

            friend: friend,
            username: $window.sessionStorage.username
        });
        $scope.showfrequest=false;

        console.log($scope);

    };

    $scope.google = function () {

        $socket.emit('map_talk', {
           talkingto: $scope.talkingto
        });

    var myWindow = window.open('/map', "googleMaps", "scrollbars=no,width=400,height=400");
};

    $scope.deletefriend = function(username){

        for(var i=0;i<$scope.friendlist.length;i++){

            if($scope.friendlist[i].name==username) {
                $scope.friendlist.splice(i, 1);
                $socket.emit('open:deletefriend', {
                    friend: username,
                    username: $window.sessionStorage.username
                });
                break;
            }
        }
    };


    $scope.addfriend = function(username){
        $scope.friend = username;
        if($scope.friendlist.length==0){
            $socket.emit('open:addfriend', {

                friend: username,
                username: $window.sessionStorage.username,
                avatar:$scope.avatar

            });
        }else {
            var i = 0;
            var control = false;
            for (i = 0; i < $scope.friendlist.length; i++) {
                if ($scope.friendlist[i].name != username)
                    control = true;
                else {
                    control = false;
                    break;
                }
            }
            if (control == true) {
                $socket.emit('open:addfriend', {

                    friend: username,
                    username: $window.sessionStorage.username,
                    avatar: $scope.avatar

                });
            } else if (control == false) {
                $scope.shownoADD = true;
            }

            console.log("clicked in user: " + username);

        }
    };

    $scope.ok = function(){
        $scope.showfAccept=false;
        $scope.showfReject=false;
        $scope.shownoADD=false;
    }
    $scope.isw = function(){
      console.log("merda1");
      $socket.emit('isw', {
          talkingto: $scope.talkingto,
          username: $window.sessionStorage.username,

      });
    }



});
