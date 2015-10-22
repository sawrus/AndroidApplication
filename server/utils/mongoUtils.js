var MongoClient = require('mongodb').MongoClient;

const MONGO_PORT = 20017;
const MONGO_HOST = "localhost";
const MONGO_DB = "spy";

var _db;

module.exports = {

    connectToServer: function() {
        MongoClient.connect( "mongodb://"+ MONGO_HOST +":"+ MONGO_PORT +"/"+ MONGO_DB, function( err, db ) {
            if (err) {
                console.log("Error while connecting to MongoDB: " + err);
            } else {
                console.log("Connected to MongoDB");
            }
            _db = db;
            //return _db;
        } );
    },

    getDb: function() {
        return _db;
    },

    closeDb: function() {
        _db.close();
    }

}
