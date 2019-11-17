var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/realtime/");
webSocket.onmessage = function (msg){receiveMsg(JSON.parse(msg.data))}
webSocket.onclose = function () {alert("server dropped you");}
webSocket.onopen = function (){
    var name = "";
    while (name == "") name = prompt("Enter your name");
    sendMessage("join", name);
}

function sendMessage(type, data){
    if (data !== "") {
        webSocket.send(JSON.stringify({type: type, data:data}));
        $("#msg").val("");
        $("#msg").focus();
    }
}

$("#send").click(function (){
    sendMessage("say", $("#msg").val());
});

$("#msg").keypress(function(e){
    if(e.which == 13){
        sendMessage("say", e.target.value);
    }
});