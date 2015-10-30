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

var getDevicesByAccount = function(account, onSuccess, onError) {
    var query = {};
    query.account = account;
    db.collection('devices').find(query, {'name' : true}).toArray(function(err, docs) {
        handleDbResponse(err, docs, 'devices', query, onSuccess, onError);
    });
}

var getWatcherData = function(requestParams, collection, onSuccess, onError) {
    // TODO validations
    var query = {};
    var options = {};

    if (requestParams.device) {
        query.device_id = requestParams.device;
    }
    if (requestParams.syncid) {
        var syncid = requestParams.syncid;
    }
    if (requestParams.date) {
        var date = requestParams.date;
    }
    if (requestParams.direction) {
        var direction = requestParams.direction;
        if (date && direction == 'gte') {
            query.date = {$gte:date};
        } else if (date && direction == 'lte') {
            query.date = {$lte:date};
        } else if (syncid && direction == 'gte') {
            query.syncid = {$gte:syncid}
        } else if (syncid && direction == 'lte') {
            query.syncid = {$lte:syncid}
        }
    }

    if (requestParams.n) {
        var n = requestParams.n;
    }
    if (+n > 0) {
        options.limit = n;
    }
    options.sort = "date";

    console.log(query);

    db.collection(collection).find(query, {}, options).toArray(function(err, docs) {
        handleDbResponse(err, docs, collection, query, onSuccess, onError);
    });
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

function handleDbResponse(err, docs, collection, query, onSuccess, onError) {
    if (err) {
        if (onError) {
            onError()
        } else {
            console.log("Error while selecting from " + collection + ": "
                + query.toString());
        }
    } else {
        onSuccess(docs);
    }
}

module.exports = {
    get : get,
    getDevicesByAccount : getDevicesByAccount,
    getWatcherData : getWatcherData,
    storeData : storeData
}