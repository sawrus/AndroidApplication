var MongoClient = require('mongodb').MongoClient;
var Server = require('mongodb').Server;
var Events = require('events');
var event = new Events.EventEmitter();

const MONGO_PORT = 20017;
const MONGO_HOST = "localhost";
const MONGO_DB = "spy";

var db = null;

MongoClient.connect( "mongodb://"+ MONGO_HOST +":"+ MONGO_PORT +"/"+ MONGO_DB, 
    {
        server: {
            auto_reconnect : true,
            socketOptions: {
                connectTimeoutMS: 500
            }
        }
    },
    function( err, _db ) {
        if (err) {
            console.log('Error while connecting to MongoDB', err);
            event.emit('error');
        } else {
            console.log('Connected to MongoDB');
            db = _db;
            event.emit('connect');
        }    
    }
);

exports.get = function(fn) {
  if(db) {
    fn(db);
  } else {
    event.on('connect', function() {
      fn(db);
    });
    event.on('error', function() {
      console.log('Unable to use db because of error');
    });
  }
};