var myLatlng, map, marker;
$(document).ready(function(){

$('#previewModal').on('show.bs.modal', function (event) {
	var panel = $(event.relatedTarget).parents(".panel"); // Button that triggered the modal

	var modal = $(this);
	modal.find('.modal-title').text('File: '+panel.attr("data-file"));
	modal.find("#previewModalDownload").attr("href", "/Download/"+panel.attr("data-link"));
	modal.find("#previewModalPreview").attr("href", "/Preview/"+panel.attr("data-link"));
	modal.find('#previewModalTags').html(panel.find(".panel-footer small").clone());

	if(panel.find("img").exists()){
		modal.find('#previewModalBody').html(panel.find("img").clone());
	}else{
		modal.find('#previewModalBody').html('<object data="/Preview/'+panel.attr("data-link")+'"><a href="/Download/'+panel.attr("data-link")+'"> Your browser can\'t display this type of files.<br /> Click to download this file.</a></object>');
		modal.find('object').height($( window ).height()-200);
	}



	//~ <!----map-->

	//~ var map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
	myLatlng = new google.maps.LatLng(panel.attr("data-lat"),panel.attr("data-lng"));
	console.log(panel.attr("data-lat"));
	console.log(panel.attr("data-lng"));
	
	var mapOptions = {
	  zoom: 10,
	  center: myLatlng
	}
	map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
	// To add the marker to the map, use the 'map' property
	marker = new google.maps.Marker({
		position: myLatlng,
		map: map,
		title:"File source"
	});

})
.on("shown.bs.modal", function (event) {
		google.maps.event.trigger(map, 'resize');
		map.setCenter(myLatlng);
});

});



//~ previewModal
//~ previewModalTitle
//~ previewModalBody
//~ previewModalFooter

//~ links:
//~ previewModalDownload
//~ previewModalPreview

