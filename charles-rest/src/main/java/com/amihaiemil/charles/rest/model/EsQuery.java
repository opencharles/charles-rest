package com.amihaiemil.charles.rest.model;

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
    private String index;

    /**
     * Show nr of results on a page.
     */
    private String nr;
    
    /**
     * Default ctor.
     */
    public EsQuery() {
        this("", "", "", "");
    }
    /**
     * Ctor.
     * @param content
     * @param category
     * @param index
     * @param nr
     */
    public EsQuery(String content, String category, String index, String nr) {
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

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getNr() {
        return nr;
    }

    public void setNr(String nr) {
        this.nr = nr;
    }
    
}
