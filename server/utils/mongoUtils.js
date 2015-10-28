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

var get = function(fn) {
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

var getWatcherData = function(requestParams, tableName) {
    // TODO validations
    if (requestParams.device) {
        var device = requestParams.device;
    }
    if (requestParams.syncid) {
        var syncid = requestParams.syncid;
    }
    if (requestParams.date) {
        var date = requestParams.date;
    }
    if (requestParams.direction) {
        var direction = requestParams.direction;
    }
    if (requestParams.n) {
        var n = requestParams.n;
    }
}

var storeData = function (obj, collection, onSuccess, onError) {
    get(function(db) {
        // store to DB
        db.collection(collection).insert(obj, {'ordered' : false}, function callback(err, doc) {
            if (err) {
                if (onError) {
                    onError(err);
                } else {
                    console.log("Error while adding to " + collection + ": " + err);
                }
            } else {
                onSuccess();
            }
        });
    });
}

module.exports = {
    get : get,
    getWatcherData : getWatcherData,
    storeData : storeData
}