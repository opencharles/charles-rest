$(document).ready(function() {
	getContributors(
	    [
		    "https://api.github.com/repos/opencharles/charles-rest/contributors",
			"https://api.github.com/repos/opencharles/charles/contributors"
		], contributorsCallback
	);
});
function getContributors(contributorsUrls, successCallback) {
	$.support.cors = true;
	for (var index in contributorsUrls) {
        $.ajax({
		    type : "GET",
		    url : contributorsUrls[index],
		    dataType : 'json',
		    contentType : "application/json; charset=utf-8",
		    headers : {
			    Accept : "application/json; charset=utf-8"
		    },
		    success : function(contributors, status) {
			    if(status == "success") {
			        successCallback(contributors);
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
}

/**
* Call this function in case the call to Github API did not succeed.
**/
function failedGithubCall() {
    $("#nogithub").show();
	$("#githubok").hide();
}

/**
* Do something with the retreived contributors.
**/
function contributorsCallback(contributors) {
	$("#nogithub").hide();
    $("#githubok").show();
	for(var index in contributors) {
		var authorBubbleId = "#" + contributors[index].login;
		if($(authorBubbleId).length == 0) {//Check if the contributor wasn't already added on the page with a previous call.
	        var authorBubble =
		        "<li><a id='" + contributors[index].login + "' title='" + contributors[index].login + "'target='_blank' href='" + contributors[index].html_url + "'><img class='bubbleAvatar' src='" + contributors[index].avatar_url + "'></a></li>";
			$('#contributors').append(authorBubble);
		}
	}
}
