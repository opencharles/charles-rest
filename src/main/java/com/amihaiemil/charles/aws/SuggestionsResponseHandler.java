package com.amihaiemil.charles.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.HttpResponseHandler;

public class SuggestionsResponseHandler implements HttpResponseHandler<String[]> {

    @Override
    public String[] handle(HttpResponse response) throws Exception {
        int status = response.getStatusCode();
        if(status < 200 || status >= 300) {
            AmazonServiceException ase = new AmazonServiceException("Unexpected status: " + status);
            ase.setStatusCode(status);
            throw ase;
        }
        return this.extractOptions(response);
    }

    @Override
    public boolean needsConnectionLeftOpen() {
        return false;
    }

    /**
     * Pull out the suggestions from the response json.
     * @param response Json response form ElasticSearch
     * @return String array.
     */
    private String[] extractOptions(HttpResponse response) {
        return new String[]{"universal","unique","university"};
    }
}
