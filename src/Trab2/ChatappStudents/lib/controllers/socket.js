'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _user = require('../models/user.js');

var _user2 = _interopRequireDefault(_user);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

// model for the database data this var is used to access the database
var clients = []; // array to store clients
/**
 * This module deals with communications from a client via the websocket for real time communication
 * Update 2015
 */

var ioSocket = null; // global store object for websocket
var morada = null;

var SocketController = {

    //starts the socket and waits for connections
    StartSocket: function StartSocket(io, socketioJwt, secret) {

        _user2.default.find({ islogged: true }, { username: 1 }, function (err, onlineUsers) {
            if (err) {
                console.error(err);
            } else {
                if (onlineUsers != null) {

                    for (var i = 0; i < onlineUsers.length; i++) {

                        _user2.default.update({ username: onlineUsers[i].username }, { $set: { islogged: false } }, function (err, User) {
                            //update user islogged
                            if (err) {
                                console.error(err);
                            }
                        });
                    }
                }
            }
        });

        ioSocket = io; // store the websocket object in case you want to send events outside the connection method
        io.set('authorization', socketioJwt.authorize({
            secret: secret,
            handshake: true
        }));

        io.sockets.on('connection', function (socket) {
            // first time it is called is on connection

            //console.log(socket.handshake.decoded_token.username, 'connected'); // shows username in the valid token sent by client

            // defintion and handling of events:
            // new user event sent by client
            socket.on('newUser:username', function (data) {
                /* if client is non-existent store it in the clients array (the object you store is up to you) the id of the socket is
                obtainable in the socket object : socket.id */
                var i = 0;

                while (i < clients.length && clients[i].username != data.username) {
                    i++;
                }
                if (i >= clients.length) {
                    // if client is non-existent store it in clients array and perform rest of logic
                    var client = { // creates new client object to store socket id and username of this connection
                        id: socket.id,
                        username: data.username,
                        address: data.address
                    };
                    clients.push(client); //update database user entry to user logged. Might be done more than once for the same user
                    //check database to see if user already logged
                    _user2.default.findOne({ $and: [{ username: data.username }, { islogged: true }] }, function (err, profile) {
                        if (err) {
                            console.error(err);
                        } else {
                            if (profile == null) {
                                // user is still not logged in
                                _user2.default.update({ username: data.username }, { $set: { islogged: true } }, function (err, User) {
                                    //update user islogged
                                    if (err) {
                                        console.error(err);
                                    } else {
                                        if (User != null) {
                                            //user updated
                                            console.log("User_update: " + User);
                                            console.log("User_update2: " + data.username);
                                        }
                                    }
                                });

                                /* Send the new user the list of current logged users from the database
                                 Query using user.find method with 3 arguments:
                                 1) an object for filtering {islogged:tru}
                                 2) a list of properties to be return, { username: 1 }
                                 3) callback function with (err, results)   err will include any error that occurred results is our resulting array of users */
                                _user2.default.find({ islogged: true }, { username: 1, address: 1, friendlist: 1, avatar: 1 }, function (err, onlineUsers) {
                                    if (err) {
                                        console.error(err);
                                    } else {

                                        // sucess send the array of online users
                                        console.log("retrieved " + onlineUsers.length + " Online users from database");
                                        console.log(onlineUsers); //onlineUsers contains an Object with an id and a username
                                        var Usernames = new Array();
                                        for (i = 0; i < onlineUsers.length; i++) {
                                            if (onlineUsers[i].username != data.username) Usernames.push(onlineUsers[i].username);
                                            if (onlineUsers[i].username == data.username) {
                                                var Morada = onlineUsers[i].address;
                                                var friendlist = onlineUsers[i].friendlist;
                                                var avatar = onlineUsers[i].avatar;
                                            }
                                        }
                                        console.log(Usernames);

                                        socket.emit('init', {
                                            sucess: true,
                                            users: Usernames,
                                            address: Morada,
                                            friendlist: friendlist,
                                            avatar: avatar
                                        });
                                        socket.broadcast.emit('user:join', { username: data.username });
                                    }
                                });
                            }
                        }
                    });
                }
                console.log("new user event received for existing user");
            }); //end event new user received
            ///  TODO ADD other events handling code:

            //when a user leaves broadcast it to other users and update database
            socket.on('disconnect', function () {
                console.log('Got disconnect11!');
                var i = 0;
                var user1;
                for (i = 0; i < clients.length; i++) {
                    if (clients[i].id == socket.id) user1 = clients[i].username;
                }
                i = 0;
                while (i < clients.length && clients[i].username != user1) {
                    i++;
                }if (i < clients.length) {
                    //remove client from array of clients
                    clients.splice(i, 1);
                }
                _user2.default.update({ username: user1 }, { $set: { islogged: false } }, function (err, User) {
                    if (err) {
                        console.error(err);
                    }
                    if (User != null) {
                        //user updated
                        socket.broadcast.emit('user:left', {
                            username: user1
                        });
                    }
                });
            });

            // send a user's message to the destination user
            socket.on('send:message', function (data) {
                // search in clients array the socket id of destination user
                var i = 0;
                while (i < clients.length && clients[i].username != data.dest) {
                    i++;
                }if (i < clients.length) {

                    // found the destination client before the end of the array
                    io.to(clients[i].id).emit('send:message', { // send to destination only
                        user: data.user,
                        message: data.message
                    });
                }
            });

            socket.on('send:message_to group', function (data) {
                // search in clients array the socket id of destination user
                var i = 0;
                var j = 0;
                for (j = 0; j < data.friendlist.length; j++) {
                    i = 0;
                    while (i < clients.length && clients[i].username != data.friendlist[j].name) {
                        i++;
                    }
                    if (i < clients.length) {
                        // found the destination client before the end of the array
                        io.to(clients[i].id).emit('send:message', { // send to destination only
                            user: data.user,
                            message: data.message
                        });
                    }
                }
            });

            //user closed chat event, forward the event to the other party
            socket.on('close:talkingto', function (data) {
                // search in clients array the socket id of the other party
                var i = 0;
                while (i < clients.length && clients[i].username != data.talkingto) {
                    i++;
                }if (clients[i].username == data.talkingto) {
                    io.sockets.connected[clients[i].id].emit('close:talkingto', { // forward to other party the closing event
                        username: data.talkingto, //username in destiny is talkingto in the source
                        talkingto: data.username // talkingto in destiny is username in the source
                    });
                }
            });

            socket.on('open:talkingto', function (data) {

                var i = 0;
                console.log("entrou opentalkingto server");

                while (i < clients.length && clients[i].username != data.talkingto) {
                    i++;
                }if (clients[i].username == data.talkingto) {

                    io.sockets.to(clients[i].id).emit('open:talkingto', { // forward to other party the opening event

                        username: data.talkingto, //username in destiny is talkingto in the source
                        talkingto: data.username // talkingto in destiny is username in the source

                    });
                }
            });

            socket.on('open:deletefriend', function (data) {

                var i = 0;
                console.log("entrou deletefriend server");

                while (i < clients.length && clients[i].username != data.friend) {
                    i++;
                }if (clients[i].username == data.friend) {

                    io.sockets.to(clients[i].id).emit('open:deletefriend', { // forward to other party the opening event

                        friend: data.username });
                }
            });

            //username in destiny is talkingto in the source

            socket.on('open:addfriend', function (data) {

                var i = 0;

                while (i < clients.length && clients[i].username != data.friend) {
                    i++;
                }if (clients[i].username == data.friend) {

                    io.sockets.to(clients[i].id).emit('open:addfriend', { // forward to other party the opening event

                        friend: data.username, //username in destiny is talkingto in the source
                        avatar: data.avatar
                    });
                }
            });

            socket.on('open:emitYes', function (data) {

                var i = 0;

                while (i < clients.length && clients[i].username != data.friend) {
                    i++;
                }if (clients[i].username == data.friend) {

                    io.sockets.to(clients[i].id).emit('open:emitYes', { // forward to other party the opening event
                        friend: data.username, //username in destiny is talkingto in the source
                        logged: data.logged,
                        avatar: data.avatar
                    });
                }
            });

            socket.on('open:emitNo', function (data) {

                var i = 0;

                while (i < clients.length && clients[i].username != data.friend) {
                    i++;
                }if (clients[i].username == data.friend) {

                    io.sockets.to(clients[i].id).emit('open:emitNo', { // forward to other party the opening event
                        friend: data.username });
                }
            });

            //when a user leaves broadcast it to other users and update database
            //username in destiny is talkingto in the source
            socket.on('logout', function (data) {
                console.log("received logout request");

                var i = 0;
                while (i < clients.length && clients[i].username != data.username) {
                    i++;
                }if (i < clients.length) {
                    //remove client from array of clients
                    clients.splice(i, 1);
                }

                for (i = 0; i < data.friendlist.length; i++) {
                    if (data.friendlist[i].name != data.username) {
                        io.sockets.to(clients[i].id).emit('open:online', { // forward to other party the opening event
                            friend: data.username, //username in destiny is talkingto in the source
                            logged: false
                        });
                    }
                }
                _user2.default.update({ username: data.username }, { $set: { islogged: false } }, function (err, User) {
                    if (err) {
                        console.error(err);
                    }
                    if (User != null) {
                        //user updated
                        socket.broadcast.emit('user:left', {
                            username: data.username
                        });
                    }
                });
                _user2.default.update({ username: data.username }, { $set: { friendlist: data.friendlist } }, function (err, User) {
                    if (err) {
                        console.error(err);
                    }
                    if (User != null) {
                        //user updated
                    }
                });
            });

            socket.on('map_talk', function (data) {
                var i = 0;
                while (i < clients.length && clients[i].username != data.talkingto) {
                    i++;
                }if (clients[i].username == data.talkingto) {

                    io.sockets.to(clients[i].id).emit('map_talk', { // forward to other party the opening event

                        username: data.talkingto, //username in destiny is talkingto in the source
                        talkingto: data.username // talkingto in destiny is username in the source

                    });
                }
            });
            socket.on('map_talk2', function (data) {
                morada = data.address;
            });

            if (morada != null) {
                socket.emit('map12', {
                    morada: morada

                });
                morada = null;
            }

            socket.on('isw', function (data) {

                var i = 0;
                while (i < clients.length && clients[i].username != data.talkingto) {
                    i++;
                }if (clients[i].username == data.talkingto) {
                    console.log("server" + data.talkingto);
                    io.sockets.to(clients[i].id).emit('isw', { // forward to other party the opening event

                        username: data.talkingto, //username in destiny is talkingto in the source
                        talkingto: data.username // talkingto in destiny is username in the source

                    });
                }
            });

            socket.on('atuafriend', function (data) {

                var i = 0;
                while (i < clients.length && clients[i].username != data.friend) {
                    i++;
                }if (clients[i].username == data.friend) {
                    io.sockets.to(clients[i].id).emit('atuafriend', { // forward to other party the opening event

                        username: data.friend, //username in destiny is talkingto in the source
                        friend: data.username // talkingto in destiny is username in the source

                    });
                }
            });
            //generic function to send events outside of the socket connection method, for example as a response to an HTTP call

            /*Send_Event: () =
                >
            {
                 if (ioSocket != null) {  // test if the socket was already created (at least one client already connected the websocket)
                    //send whatever data you need.
                }
             }*/
        });
    }
};

//expose the object to be imported
exports.default = SocketController;