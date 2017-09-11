/*
 * --- Websocket connection to server ---
 */
var cons = document.getElementById("console");
var ws = new WebSocket("ws://" + window.location.hostname + ":8081");

ws.onclose = function() {
  console.log("Websocket closed.");
};

ws.onopen = function() {
  console.log("Websocket open.");

  // TODO: If an onload ever comes for any other reason we'll screw things up doing this...
  sendCommandToServer('load', '', '');
};
// --------------------------------------

/*
 * --- Event handlers for shortcut keys ---
 */
document.addEventListener('keydown', onKeyDownShortcutHandler, false);
document.addEventListener('keyup', onKeyUpShortcutHandler, false);

function onKeyDownShortcutHandler(e) {

    // Nasty hack to stop shortcut key writing into the editor window.
    if (e.altKey) {
      editor.setReadOnly(true);
    }

    if (e.altKey && e.keyCode == 88) {
       // alt-x == run
       run();
       e.stopPropagation();
    }
    if (e.altKey && e.keyCode == 67) {
        // alt-c == clear
        clearconsole();
        e.stopPropagation();
    }
}

function onKeyUpShortcutHandler(e) {
  if (e.altKey) {
    editor.setReadOnly(false);
  }
}

/**
 * --- Main Actions ---
 */
// Evaluates the script in response to hitting the button or Alt-x
function run() {
    var code = editor.getValue();
    save(code, true);

    // Deprecated. Used to do this to run locally. Not everything is run on the server.
    // eval(code);
}

// Saves the script and then runs it on the server
function runremote() {
    var code = editor.getValue();
    save(code, true);
}

// Clears the onscreen console
function clearconsole() {
    document.getElementById('console').innerHTML='';
}

// Listeners to show log messages onscreen.
console.log=function(msg) {
    cons.innerHTML += "<div class='log'>" + msg + "</div>";
}

console.warn=function(msg) {
    cons.innerHTML += "<div class='warn'>" + msg + "</div>";
}

// Listeners to show error messages onscreen.
window.onerror=function(msg, url, linenumber) {
    cons.innerHTML += "<div class='error'>"
        + "Error line: " + linenumber + " " + msg + "</div>";
}

// Send a bundle of JSON to the server to execute commands on the device.
function sendCommandToServer(typ, cls, payload) {
  var json = { 'typ': typ, 'cls': cls, 'payload': payload }
  var jsonString = JSON.stringify(json);
  console.log("sending request: " + jsonString);
  ws.send(jsonString);
}

// Send code to be saved on the server.
function save(data, andexecute) {
  var json = { 'typ': (andexecute) ? 'execute' : 'save', 'payload': data };
  console.log("saving... " + ((andexecute) ? " and running!" : ""));
  ws.send(JSON.stringify(json));
}

// Callback for receiving events from the server.
ws.onmessage = function (evt) {
  var json = JSON.parse(evt.data);
  console.log("Incoming Websocket message:" + json);
  if (json.typ == 'load') {
     console.log('loading script...');
     var script = json.payload;
     if (script != null) {
       editor.setValue(script);
     }
  } else if (json.typ = 'log') {
     console.log("JS Message: " + json.payload);
  }
};
