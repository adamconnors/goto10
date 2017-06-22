// TODO: May need to namespace this at some point.
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

