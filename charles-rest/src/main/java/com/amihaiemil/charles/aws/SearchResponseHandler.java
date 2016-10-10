package com.amihaiemil.charles.aws;

import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.HttpResponseHandler;
import com.amihaiemil.charles.rest.model.SearchResultsPage;

/**
 * Response handler that parses the search response into a {@link SearchResultsPage}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class SearchResponseHandler implements HttpResponseHandler<SearchResultsPage>{

	@Override
	public SearchResultsPage handle(HttpResponse response) throws Exception {
		return null;
	}

	@Override
	public boolean needsConnectionLeftOpen() {
		return false;
	}

}
