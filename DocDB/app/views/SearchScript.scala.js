$(document).ready(function(){
	var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
	var webSocket = new WS("@routes.Application.WebSocket().webSocketURL(request)")


    var receiveEvent = function(event) {
        var data = JSON.parse(event.data)

		if( data.result != null){
			$('#resultDiv').scope().results = eval(data.result);
			$("#resultDiv").scope().$apply();
			/*}else if ( data.loginAttempts != null){
				$("#loginAttempts").html(data.loginAttempts);
			}else if ( data.vipButton != null){
				alert(data.vipButton);
				$("#vipButton").hide(400);
			*/}
	}

    webSocket.onmessage = receiveEvent;

/* jak wysylac do websocketa
		var x = 0;
		webSocket.send(JSON.stringify({"action": "ChangeCards", "cards": x}));
*/

	$("#search").keyup(function(){
		webSocket.send(JSON.stringify({"request": "search", "pattern": $("#search").val(),"limit": "false"}));
	});

	$("h2").css("cursor","pointer");

	$("#content1").prev("h2").click(function(){
		$("#content1").slideToggle(300);
	});
	$("#content2").prev("h2").click(function(){
		$("#content2").slideToggle(300);
	});
	$("#content3").prev("h2").click(function(){
		$("#content3").slideToggle(300);
	});


});
