'use strict';

var _express = require('express');

var _express2 = _interopRequireDefault(_express);

var _serveFavicon = require('serve-favicon');

var _serveFavicon2 = _interopRequireDefault(_serveFavicon);

var _errorhandler = require('errorhandler');

var _errorhandler2 = _interopRequireDefault(_errorhandler);

var _morgan = require('morgan');

var _morgan2 = _interopRequireDefault(_morgan);

var _bodyParser = require('body-parser');

var _bodyParser2 = _interopRequireDefault(_bodyParser);

var _methodOverride = require('method-override');

var _methodOverride2 = _interopRequireDefault(_methodOverride);

var _http = require('http');

var _http2 = _interopRequireDefault(_http);

var _mongoose = require('mongoose');

var _mongoose2 = _interopRequireDefault(_mongoose);

var _expressJwt = require('express-jwt');

var _expressJwt2 = _interopRequireDefault(_expressJwt);

var _socketioJwt = require('socketio-jwt');

var _socketioJwt2 = _interopRequireDefault(_socketioJwt);

var _socket = require('socket.io');

var _socket2 = _interopRequireDefault(_socket);

var _path = require('path');

var _path2 = _interopRequireDefault(_path);

var _http3 = require('./controllers/http');

var _http4 = _interopRequireDefault(_http3);

var _socket3 = require('./controllers/socket');

var _socket4 = _interopRequireDefault(_socket3);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

//controller that deals with the Websocket events

// File path utilities to make sure we're using the right type of slash (/ vs \)
//for token based authentication in the websocket
// Wrapper for interacting with MongoDB
// Allows for PUT and DELETE methods to be used in browsers where they are not supported
// Logs each server request to the console
// middleware for tab icon
/**
 * Express app single page site with chat written in ES6 (lattest Java Script Evolution)
 * author: Pedro Amaral updated 2015
 */
/**
 * Import dependencies
 */
var secret = 'this is the secret secret secret 12356'; // password for jwt token

// controller that deals with HTTP requests for the REST API
//websocket communications
//for authentication based in tokens
// Takes information from POST requests and puts it into an object
_http4.default.setSecret(secret); // set the password for jwt token in the HttpController

var app = (0, _express2.default)(); // app for http server
// all environments
app.set('port', process.env.PORT || 3000); // you can change the port to another value here
app.set('views', _path2.default.join(__dirname, '..', 'views')); // sets up the path for the Jade Templates
app.set('view engine', 'jade'); //setup template engine - we're using jade
app.use(_express2.default.static(_path2.default.join(__dirname, '..', 'public'))); // setting up the public dir
app.use((0, _serveFavicon2.default)(_path2.default.join(__dirname, '..', 'public/img/favicon.ico'))); //indicate where to find the tab icon
app.use((0, _morgan2.default)('dev')); // use developer logs
app.use((0, _methodOverride2.default)()); // Allow PUT/DELETE
app.use(_bodyParser2.default.json()); // Parse JSON data and put it into an object which we can access
app.use(_bodyParser2.default.urlencoded({ extended: true }));

//if you wish to restrict the access to some URL /restricted URL currently does not exist it is just an example
app.use('/restricted', (0, _expressJwt2.default)({ secret: secret })); // all routes to /restricted demand authentication

app.use(function (err, req, res, next) {
  // if UnauthorizedError occurs send a 401 code back to client
  if (err.constructor.name === 'UnauthorizedError') {
    res.send(401, 'Unauthorized');
  } else console.error(err.stack);
});

_mongoose2.default.connect('mongodb://localhost:27017/ChatDB'); // Connects to your MongoDB.  Make sure mongod is running!
_mongoose2.default.connection.on('error', function () {
  console.log('MongoDB Connection Error. Please make sure that MongoDB is running.');
  process.exit(1);
});

// development only
if ('development' == app.get('env')) {
  app.use((0, _errorhandler2.default)());
}

//main app page user log in
app.get('/', _http4.default.SignIn);
app.post('/authenticate', _http4.default.ValidateUser); //route to deal with the post of the authentication form
app.post('/newuser', _http4.default.NewUser); //route to deal with the post of the register form
app.get('/map', _http4.default.map);

var server = _http2.default.createServer(app); //HTTP server Object
var io = _socket2.default.listen(server); //WebSocket server Object associated with the same port as HTTP
_socket4.default.StartSocket(io, _socketioJwt2.default, secret); // call StartSocket function in socketController

server.listen(app.get('port'), function () {
  console.log('Express server listening on port' + app.get('port'));
});