package com.amihaiemil.charles.aws;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.Request;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.HttpResponseHandler;
import com.amihaiemil.charles.rest.model.SearchResult;
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
	public SearchResultsPage handle(HttpResponse response) {
		int status = response.getStatusCode();
		if(status < 200 || status >= 300) {
			AmazonServiceException ase = new AmazonServiceException("Unexpected status: " + status);
			ase.setStatusCode(status);
			throw ase;
		}
		return this.buildResultsPage(response);
	}

	@Override
	public boolean needsConnectionLeftOpen() {
		return false;
	}

	/**
	 * Build the search results page
	 * @param response
	 * @return
	 */
	private SearchResultsPage buildResultsPage(HttpResponse response) {
		SearchResultsPage page = new SearchResultsPage();
        InputStream content = response.getContent();
        JsonObject result = Json.createReader(content).readObject();
        int totalHits = result.getJsonObject("hits").getInt("total");
        if(totalHits != 0) {
        	List<SearchResult> searchResults = new ArrayList<SearchResult>();
            JsonArray hits = result.getJsonObject("hits").getJsonArray("hits");
            for(int i=0; i<hits.size(); i++) {
                JsonObject hitSource = hits.getJsonObject(i).getJsonObject("_source");
                SearchResult res = new SearchResult(
                    //TODO ES settings and logic for highlighting text content.
                    hitSource.getString("url"), "TODO set highlight settings", hitSource.getString("category")
                );
                searchResults.add(res);
            }
            page.setResults(searchResults);
            page.setTotalHits(totalHits);
            this.setPagesInfo(page, response.getRequest());
        } 
        return page;
	}
	
    /**
     * Set the page number, next page, previous page and all pages' links
     * on the results page. Use the original search request since parameters from and size
     * are not part of the response. 
     * @param page
     * @param request
     */
	private void setPagesInfo(SearchResultsPage page, Request<?> request) {
	    //TODO be implemented after highlighting, since request logic might change then.
	}
}