var mongo = require( '../utils/mongoUtils' );
var assert = require('assert');

var ACCOUNTS = process.argv[2]; /*number of accounts*/
var DEVICES = process.argv[3]; /*number of devices per account */
var MESSAGES = process.argv[4]; /*number of messages per device*/
var CALLS = process.argv[5]; /*number of calls per device*/
var GPS = process.argv[6]; /*number of gps records per device*/

console.log('Accounts: ' + ACCOUNTS);
console.log('Devices: ' + DEVICES);
console.log('Messages: ' + MESSAGES);
console.log('Calls: ' + CALLS);
console.log('GPS: ' + GPS);

var accounts = [];
var devices = [];
var messages = [];
var calls = [];
var gps = [];

var truefalse = ['true','false'];

for (var a = 0; a < ACCOUNTS; a++) {
    var account = 'acc'+ a +'@gmail.com';
    mongo.get(function(db) {
        db.collection('accounts').insert({'email' : account});
        for (var d = 0; d < DEVICES; d++) {
            var device = 'dev' + d;
            db.collection('devices').insert({'name' : device, 'account' : account});


            for (var m = 0; m < MESSAGES; m++) {
                messages.push({
                    'tofrom' : '+7 927 771-11-11',
                    'incoming' : truefalse[Math.round(Math.random())],
                    'data' : 'Hello ' + m + '!',
                    'date' : new Date(),
                    'tz' : 'Europe/London',
                    'device_id' : device
                });
            }
            db.collection('messages').insert(messages, function(err, result) {
                messages = [];
                assert.equal(null, err);
                console.log('Messages data generated');
            });


            for (var c = 0; c < CALLS; c++) {
                calls.push({
                    'tofrom' : '+7 927 771-11-11',
                    'incoming' : truefalse[Math.round(Math.random())],
                    'duration' : 90,
                    'date' : new Date(),
                    'tz' : 'Europe/London',
                    'device_id' : device
                });
            }
            db.collection('calls').insert(calls, function(err, result) {
                calls = []; // TODO
                assert.equal(null, err);
                console.log('Calls data generated');
            });


            for (var g = 0; g < GPS; g++) {
                gps.push({
                    'x' : 53.4810538,
                    'y' : 49.4652697,               
                    'date' : new Date(),
                    'tz' : 'Europe/London',
                    'device_id' : device
                });
            }
            db.collection('gps').insert(gps, function(err, result) {
                assert.equal(null, err);
                console.log('GPS data generated');
            });
        }
    });
}

console.log('Generation of data v1 is finished');

/*


db.accounts.drop();
db.devices.drop();
db.messages.drop();
db.calls.drop();
db.gps.drop();


*/