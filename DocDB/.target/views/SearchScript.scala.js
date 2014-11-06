$(document).ready(function(){
	var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
	var webSocket = new WS("@routes.Application.WebSocket().webSocketURL(request)")


    var receiveEvent = function(event) {
        var data = JSON.parse(event.data)

		if( data.result != null){
			$("#resultDiv").html(data.result);

		/*}else if ( data.loginAttempts != null){
			$("#loginAttempts").html(data.loginAttempts);
		}else if ( data.vipButton != null){
			alert(data.vipButton);
			$("#vipButton").hide(400);
		*/}
	}

    webSocket.onmessage = receiveEvent;

	// --------------------------------- M O J E 	F U N K C J E ----------------------------------------
/* jak wysylac do websocketa
		var x = 0;
		webSocket.send(JSON.stringify({"action": "ChangeCards", "cards": x}));
*/

	$("#search").keyup(function(){
		webSocket.send(JSON.stringify({"request": "search", "pattern": $("#search").val()}));
	});

});
