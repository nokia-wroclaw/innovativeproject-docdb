var kafelekHeight = 175;
var newSearch, webSocket, searchTimer=0, sendLink,allTags;
var geoloc =[], lastLimit=false;
var lat; var lng;
$.fn.exists = function () {
    return this.length !== 0;
}
$(document).ready(function(){
	var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
	webSocket = new WS("@routes.Application.WebSocket().webSocketURL(request)")


    var receiveEvent = function(event) {
        var data = JSON.parse(event.data)

		if( data.result != null){
			$("#resCount").text(data.resultsCount==undefined?"No results":"" + data.resultsCount + " matches found");

			$('#resultDiv').scope().results = eval(data.result);		//wyciągamy rezultaty
			$('#resultDiv').scope().tags = eval(data.tagList);			//wyciągamy tagi
			$("#resultDiv").scope().$apply();							//generujemy html z wyników
			if(typeof localStorage["gridView"] == 'undefined'){			//ustawiamy widok odpowiedni dla użytkownika
				localStorage["gridView"]="true";						//(domyślnie lista) tutaj zmieniać domyślny widok!!!!!!
			}
			if(localStorage["gridView"]=="true"){
				$(".gridable").removeClass("listView").addClass("gridView");//changing to grid
				$(".panel-body.gridView").each(function(){$(this).height(kafelekHeight-$(this).next().height());});//ustawianie wysokości kafelków
			}else if(localStorage["gridView"]=="false"){
				$(".gridable").removeClass("gridView").addClass("listView");//to list
			}

			$(".zipContent").each(function(){
				var thisZip = $(this);
				var link = thisZip.parents(".panel").attr("data-link");
				$.get( "/Preview/"+link, function( data ) {
					thisZip.html( data );
				});
			});

			$("#resultDiv").slideDown();
			rebindEventHandlers();
			if($('#resultDiv').scope().results.length==9 && lastLimit=="false") $('#newSearch').slideDown();
			else $('#newSearch').slideUp();
		}

		if (data.geo != null) {
			var gloc = data.geo;
			$("#geoLoc").html(gloc);
		}
	}

    webSocket.onmessage = receiveEvent;

/* jak wysylac do websocketa
		var x = 0;
		webSocket.send(JSON.stringify({"action": "ChangeCards", "cards": x}));
*/

	$("#search").keyup(function(){
		if (searchTimer) clearTimeout(searchTimer);
		searchTimer = setTimeout('searchRequest("false");',250);
	});

	if (navigator.geolocation) {
		navigator.geolocation.watchPosition(showPosition);
	} else {
		x.innerHTML = "Geolocation is not supported by this browser.";
	}

	function showPosition(position) {
		lat = position.coords.latitude;
		lng = position.coords.longitude;
		send(JSON.stringify({"request": "geolocation", "lat": lat, "lng": lng}));
	}

	newSearch = function (){
		$("#newSearch").slideUp(200);
		searchRequest("true");
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


	$("#linkUpload").keyup(function(e) {
		switch(e.which) {
			case 13: // enter
				address = "takeLink/"+prompt("Wpisz tagi")+","+$('#geoLoc').text();
				$.post( address.replace("# ","").replace("#",""), { link : $("#linkUpload").val() } );
			break;
			default: return; // exit this handler for other keys
		}
		e.preventDefault(); // prevent the default action (scroll / move caret)
	});

	searchFromHash()

});
function rebindEventHandlers(){
	$(".panel-title, .panel-body").unbind("click").unbind("mouseover").unbind("mouseout")
		.mouseover(function() {
			$( this ).find(".panel-title").addClass("panelTitleHover");
			$( this ).find(".panel-body").addClass("panelBodyHover");
		}).mouseout(function() {
			$( this ).find(".panel-title").removeClass("panelTitleHover");
			$( this ).find(".panel-body").removeClass("panelBodyHover");
		});//.click(function(){showPreview($(this).parents(".panel"));});


	$("#resultDiv small").unbind().click(function(e){
		e.preventDefault();
		e.stopPropagation();
		var tagToInsert = $(this).text();
		$("#search").val($("#search").val() +" "+ tagToInsert);
		searchRequest("false");
	});
}

function searchFromHash(){
	var hash = window.location.hash;
	if(hash.indexOf("/Search/")==-1) return;

	var searchText = hash.substring(9).replace("%23","#").replace("%20"," ");
	if($("#search").val() == searchText) return;
	$("#search").val(searchText);
	searchRequest("false");

}

window.onhashchange = searchFromHash;

function searchRequest(limit){
	var searchText = $("#search").val();
	lastLimit=limit;
	window.location.hash  = '/Search/'+searchText.replace("#","%23").replace(" ","%20")
	send(JSON.stringify({"request": "search", "pattern": searchText,"limit": limit}));
}
function send(json){
	if (webSocket.readyState == 1) {
        webSocket.send(json);
    } else {
        setTimeout("send('"+json+"');", 100);
	}
}

function changeToGrid(){
	localStorage["gridView"]="true";
	$("#resultDiv").fadeTo(100,0.01, function(){
		$(".gridable").removeClass("listView").addClass("gridView");
		$(".panel-body.gridable").each(function(){$(this).height(kafelekHeight-$(this).next().height());});//ustawianie wysokości kafelków
	}).fadeTo(300,1);
}
function changeToList(){
	localStorage["gridView"]="false";
	$("#resultDiv").fadeTo(100,0.01,function(){
		$(".gridable").removeClass("gridView").addClass("listView");
		$(".panel-body.gridable").each(function(){$(this).css("height","");});//ustawianie wysokości kafelków
	}).fadeTo(300,1);
}