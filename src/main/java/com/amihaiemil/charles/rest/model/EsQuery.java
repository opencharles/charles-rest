package com.amihaiemil.charles.rest.model;

import javax.json.JsonObject;

/**
 * Query made to ElasticSearch
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public interface EsQuery {
    
    /**
     * All queries made to elasticsearch are in JSON format.
     * @return Json object representing the query.
     */
    JsonObject toJson();
}
