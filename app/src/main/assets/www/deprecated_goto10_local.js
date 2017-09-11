/**
 * Script for running locally (in the browser). Deprecated I think in favour of running
 * on the device itself.
 */

function sleep(ms) {
   console.warn("Nasty sleep implementation for learning only, use setTimeout instead.");
   var start = new Date().getTime();
   var end = start;
   while(end < start + ms) {
     end = new Date().getTime();
  }
}

// Send a bundle of JSON to the server to execute commands on the device.
function sendCommandToServer(typ, cls, payload) {
  var json = { 'typ': typ, 'cls': cls, 'payload': payload }
  var jsonString = JSON.stringify(json);
  console.log("sending request: " + jsonString);
  ws.send(jsonString);
}
