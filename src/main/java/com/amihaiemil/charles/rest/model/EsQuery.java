package com.amihaiemil.charles.rest.model;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Query for search.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class EsQuery {

    /**
     * Query the content field.
     */
    private String content;

    /**
     * Query the category (logical type in the index)
     */
    private String category;

    /**
     * Show results from this index.
     */
    private int index;

    /**
     * Show nr of results on a page.
     */
    private int nr;
    
    /**
     * Default ctor.
     */
    public EsQuery() {
        this("", "", 0, 10);
    }
    /**
     * Ctor.
     * @param content
     * @param category
     * @param index
     * @param nr
     */
    public EsQuery(String content, String category, int index, int nr) {
        this.content = content;
        this.category = category;
        this.index = index;
        this.nr = nr;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    /**
     * Turn this to json query for performing a search request
     * (with query in the body; the only way to provide highlighting)
     * <br><br>
     * An example of json query:<br>
     * <pre>
     * {
     *     "from": 10,
     *     "size": 15,
     *     "filter": {
     *         "bool": {
     *             "filter": [{
     *                 "match_phrase_prefix": {
     *                     "textContent": "string here"
     *                  }
     *              },
     *              {
     *                  "type": {
     *                      "value": "tech"
     *                   }
     *              }]
     *         }
     *     }
     * }
     * </pre>
     * 
     * 
     * @return This Query in Json format 
     * @see 
     * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-query.html">
     * ElasticSearch Query
     * </a>
     * @see 
     * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/2.4/term-level-queries.html">
     * Term level queries
     * </a>
     */
    public JsonObject toJson() {
        JsonObject matcher = Json.createObjectBuilder()
            .add(
                "match_phrase_prefix",
                Json.createObjectBuilder().add("textContent", this.content)    
            ).build();
        JsonObject type = Json.createObjectBuilder()
            .add(
                "type",
                Json.createObjectBuilder().add("value", this.category)    
            ).build();
        JsonArray filter = Json.createArrayBuilder().add(matcher).add(type).build();
        JsonObject bool = Json.createObjectBuilder().add(
            "bool", Json.createObjectBuilder().add("filter", filter).build()
        ).build();
        
        JsonObject highlight = Json.createObjectBuilder()
            .add(
                "fields",
                Json.createObjectBuilder()
                    .add(
                        "textContent",
                        Json.createObjectBuilder()
                            .add("fragment_size", 150)
                            .build()
                    )
                    .build()
             ).build();
        
        JsonObject query = Json.createObjectBuilder()
            .add("from", this.index)
            .add("size", this.nr)
            .add("query", bool)
            .add("highlight", highlight)
            .build();
        return query;
    }
    
}
