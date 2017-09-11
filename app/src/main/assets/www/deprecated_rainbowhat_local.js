/**
 * @Deprecated
 * Local script for sending commands to server to execute peripheral events
 * This is now all executed on the serfver.
 */
function setdisplay(txt) {
  sendCommandToServer('cmd', 'rh', { display: txt } );
}

function setledstrip(pos) {
  sendCommandToServer('cmd', 'rh', { ledstrip: pos } )
}

function onButtonEvent(json) {
   var id = json.id;
   var pressed = json.pressed;
   // Implement this function to receive button events.
   onbuttonpressed(id, pressed);
}

