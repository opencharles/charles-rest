$(document).ready(function() {
    getServiceStatus("https://webapps.amihaiemil.com/charles-rest/api/ping");
});
function getServiceStatus(statusUrl) {
    $.support.cors = true;
	$.ajax({
		type : "GET",
		url : statusUrl,
		success : function(response, status) {
			var lastUpdated;
			if(status == "success") {
                $("#statusBadge").html('<img alt="Status badge" title="The service is up and running" src="img/service status-online-green.svg"/>');
			} else {
			    $("#statusBadge").html('<img alt="Status badge" title="The service is currently offline" src="img/service status-offline-yellow.svg"/>');
			}
		},
		statusCode: {
			404: function() {
			    $("#statusBadge").html('<img alt="Status badge" title="The service is currently offline" src="img/service status-offline-yellow.svg"/>');
			},
			500: function() {
			    $("#statusBadge").html('<img alt="Status badge" title="The service is currently offline" src="img/service status-offline-yellow.svg"/>');
			}
		},
		error : function(e) {
			$("#statusBadge").html('<img alt="Status badge" title="The service is currently offline" src="img/service status-offline-yellow.svg"/>');
		}
	});
}
