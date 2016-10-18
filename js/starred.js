$(document).ready(function() {
	getStarredRepos("charlesmike", starredCallback);
});
function getStarredRepos(user, successCallback) {
	$.support.cors = true;
    $.ajax({
		type : "GET",
		url : "https://api.github.com/users/" + user + "/starred",
		dataType : 'json',
		contentType : "application/json; charset=utf-8",
		headers : {
			Accept : "application/json; charset=utf-8"
		},
		success : function(starred, status) {
			if(status == "success") {
			    successCallback(starred);
			} else {
			    failedGithubCall();
			}
		},
		statusCode: {
			404: function() {
                failedGithubCall();
			},
			500: function() {
                failedGithubCall();
			}
		},
		error : function(e) {
            failedGithubCall();
		}
	});
}

/**
* Call this function in case the call to Github API did not succeed.
**/
function failedGithubCall() {
    $("#nogithub").show();
	$("#githubok").hide();
}

/**
* Do something with the retreived starred repos.
**/
function starredCallback(starred) {
	$("#nogithub").hide();
    $("#githubok").show();
	for(var index in starred) {
	    var author = starred[index].owner.login;
		var repoName = starred[index].name;
		var url = "http://" + repoName;
		if(repoName != author + ".github.io") {
		    url="http://" + author + ".github.io/" + repoName;
		}
	    var starredLink =
		    "<li><p><a title='" + repoName + "'target='_blank' href='" + url + "'>" + url + "</a></p></li>";
	    $('#starred').append(starredLink);
	}
}