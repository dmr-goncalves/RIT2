'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _mongoose = require('mongoose');

var _mongoose2 = _interopRequireDefault(_mongoose);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

//create a schema for the User Object
var UserSchema = new _mongoose2.default.Schema({
    name: String,
    address: String,
    email: String,
    username: String,
    password: String,
    islogged: Boolean, // indicates sign in status
    friendlist: [{ name: String, logged: Boolean, avatar: Number }],
    avatar: Number,
    createdAt: { type: Date, 'default': Date.now } //stores date of record creation
});

// Expose the model so that it can be imported and used in the controller (to search, delete, etc)
exports.default = _mongoose2.default.model('user', UserSchema);