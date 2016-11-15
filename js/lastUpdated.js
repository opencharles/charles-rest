$(document).ready(function() {
	getLastUpdateTimestamp("https://api.github.com/repos/opencharles/charles-github-ejb/commits?sha=gh-pages");
});
function getLastUpdateTimestamp(githubCommitsUri) {
    $.support.cors = true;
	$.ajax({
		type : "GET",
		url : githubCommitsUri,
		dataType : 'json',
		contentType : "application/json; charset=utf-8",
		headers : {
			Accept : "application/json; charset=utf-8"
		},
		success : function(commits, status) {
			var lastUpdated;
			if(status == "success") {
                lastUpdated = new Date(commits[0].commit.committer.date);
			} else {
			    lastUpdated = new Date();
			}
			$("#lastupdated").text(lastUpdated.getDate() + "/" + (lastUpdated.getMonth() + 1) + "/" + lastUpdated.getFullYear());

		},
		statusCode: {
			404: function() {
			    var lastUpdated = new Date();
    			$("#lastupdated").text(lastUpdated.getDate() + "/" + (lastUpdated.getMonth() + 1) + "/" + lastUpdated.getFullYear());
			},
			500: function() {
			    var lastUpdated = new Date();
    			$("#lastupdated").text(lastUpdated.getDate() + "/" + (lastUpdated.getMonth() + 1) + "/" + lastUpdated.getFullYear());
			}
		},
		error : function(e) {
			var lastUpdated = new Date();
			$("#lastupdated").text(lastUpdated.getDate() + "/" + (lastUpdated.getMonth() + 1) + "/" + lastUpdated.getFullYear());
		}
	});
}
