var newSearch, webSocket;
$(document).ready(function(){
	var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
	webSocket = new WS("@routes.Application.WebSocket().webSocketURL(request)")


    var receiveEvent = function(event) {
        var data = JSON.parse(event.data)

		if( data.result != null){
			$("#resCount").text("I found " + data.resultsCount + " matches");
			$('#resultDiv').scope().results = eval(data.result);
			$("#resultDiv").scope().$apply();
			rebindEventHandlers();
		}
	}

    webSocket.onmessage = receiveEvent;

/* jak wysylac do websocketa
		var x = 0;
		webSocket.send(JSON.stringify({"action": "ChangeCards", "cards": x}));
*/

	$("#search").keyup(function(){
		searchRequest("false");
	});
	if (navigator.geolocation) {
		navigator.geolocation.watchPosition(showPosition);
	} else {
		x.innerHTML = "Geolocation is not supported by this browser.";
	}
	var lat;
	var lng;
	function showPosition(position) {
		lat = position.coords.latitude;
		lng = position.coords.longitude;
		webSocket.send(JSON.stringify({"request": "geolocation", "lat": lat, "lng": lng}));
	}

	newSearch = function (){
		searchRequest("true");
		//webSocket.send(JSON.stringify({"request": "search", "pattern": $("#search").val(), "limit": "true"}));
	}

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
function rebindEventHandlers(){
	$("#resultDiv small").unbind().click(function(e){
		e.preventDefault();
		e.stopPropagation();
		var tagToInsert = $(this).text();
		$("#search").val($("#search").val() +" "+ tagToInsert);
		searchRequest("false");
	});
}

function searchRequest(limit){
	webSocket.send(JSON.stringify({"request": "search", "pattern": $("#search").val(),"limit": limit}));
}