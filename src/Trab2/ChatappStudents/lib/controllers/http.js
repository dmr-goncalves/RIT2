'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _jsonwebtoken = require('jsonwebtoken');

var _jsonwebtoken2 = _interopRequireDefault(_jsonwebtoken);

var _user = require('../models/user.js');

var _user2 = _interopRequireDefault(_user);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

// model for the database data this var is used to access the database
var _secret; // to store the secrect for Jwt authentication
//define the HttpController object with literal object creation

//https://npmjs.org/package/node-jsonwebtoken
var HttpController = {
    setSecret: function setSecret(secret) {
        //=> is a function shorthand the same could be done with setSecret : function (secret) {...}
        _secret = secret;
    },
    /*
    	 * GET home page. First get is responded here with the main page
     */
    SignIn: function SignIn(req, res) {
        var templateData = { //data to set html header (angular module) and page title that is used also for menu
            angularApp: "chatapp",
            pageTitle: "RIT2 Chat"
        };
        res.render('index', templateData);
    },

    map: function map(req, res) {
        var templateData = { //data to set html header (angular module) and page title that is used also for menu
            angularApp: "mapapp",
            pageTitle: "RIT2 map"
        };
        res.render('index', templateData);
    },

    /*
    	 * POST User sign in. User Sign in POST is treated here
     */
    ValidateUser: function ValidateUser(req, res) {
        //TODO validate req.body.username and req.body.password (access mongo db and test)
        console.log("username: " + req.body.username + " password: " + req.body.password);

        _user2.default.findOne({ $and: [{ username: req.body.username }, { password: req.body.password }] }, function (err, User) {
            //if is valid return a token in a JSON response

            if (err) {
                console.error("ERROR: While checking for user with login");
                console.error(err);
            }
            if (User != null) {
                //use jwt.sign to sign some data to create a token on sucess

                var token = _jsonwebtoken2.default.sign(User, _secret, { expiresIn: 3600 * 5 });
                res.json({ token: token });
            } else {
                res.send(401, 'Wrong user or password');
            }
        });
    },

    /*
    	 * POST User registration. User registration POST is treated here
     */
    NewUser: function NewUser(req, res) {
        console.log("received form submission new User");
        //check if user already exists
        _user2.default.findOne({ username: req.body.username }, function (err, ExistingUser) {
            if (err) {
                console.error("ERROR: While accessing database");
                console.error(err);
            }
            if (ExistingUser == null && req.body.name != undefined && req.body.address != undefined && req.body.email != undefined && req.body.avatar != undefined && req.body.username != undefined && req.body.password != undefined) {
                //if user still does not exist
                //create a newUser object instance with the fields defined in the usermodel object

                _user2.default.create({ name: req.body.name, address: req.body.address, email: req.body.email, avatar: req.body.avatar, username: req.body.username, password: req.body.password }, function (err, newUser) {
                    if (err) {
                        console.error("Error on saving new user");
                        console.error(err); // log out to Terminal all errors
                    } else {
                            console.log("Created a new user!");
                            console.log(newUser);
                            res.json(newUser); //sends back the Json with the new stored user
                        }
                });
            } else {
                    console.log("Error regist");
                    res.send(401, 'Error Regist'); //sends in the body of the Http response the string
                    console.log(res.data);
                }
        });
    }
};
//expose the object to be imported
exports.default = HttpController;