/**
 * Copyright (c) 2016-2017, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1)Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  3)Neither the name of charles-rest nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.amihaiemil.charles.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amihaiemil.charles.aws.AccessKeyId;
import com.amihaiemil.charles.aws.AmazonElasticSearch;
import com.amihaiemil.charles.aws.ElasticSearch;
import com.amihaiemil.charles.aws.EsEndPoint;
import com.amihaiemil.charles.aws.Region;
import com.amihaiemil.charles.aws.SearchQuery;
import com.amihaiemil.charles.aws.SecretKey;
import com.amihaiemil.charles.aws.StAccessKeyId;
import com.amihaiemil.charles.aws.StEsEndPoint;
import com.amihaiemil.charles.aws.StRegion;
import com.amihaiemil.charles.aws.StSecretKey;
import com.amihaiemil.charles.rest.model.SearchResultsPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST interface for charles' logic.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
@Path("/")
public class CharlesResource {

    /**
     * Http request.
     */
    @Context
    private HttpServletRequest servletRequest;

    /**
     * ElasticSearch URL.
     */
    private EsEndPoint esEdp = new StEsEndPoint();

    /**
     * AWS access key.
     */
    private AccessKeyId accesskey = new StAccessKeyId();
    
    /**
     * Aws secret key;
     */
    private SecretKey secretKey = new StSecretKey();
    
    /**
     * Aws ES region.
     */
    private Region reg = new StRegion();
    
    
    public CharlesResource() {
        //for jax-rs
    }

    /**
     * Ctor.
     * @param servletRequest Injected HttpServletRequest
     */
    public CharlesResource (
        final HttpServletRequest servletRequest,
        final AccessKeyId accessKey,
        final SecretKey secret,
        final Region reg,
        final EsEndPoint es
    ) {
        this.servletRequest = servletRequest;
        this.accesskey = accessKey;
        this.reg = reg;
        this.secretKey = secret;
        this.esEdp = es;
    }
    
    /**
     * Endpoint for checking if the service is online.
     * @return ok response.
     */
    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok().entity("Service is online.").build();
    }

    /**
     * Perform a search.
     * @return Http response.
     * @param user Github username.
     * @param repo Github reponame.
     * @param kw Keywords.
     * @param ctg Category.
     * @param index Start displaying results form index.
     * @param size Max number of results on the page.
     * @throws JsonProcessingException 
     */
    @GET
    @Path("/s/{username}/{reponame}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(
        @PathParam("username") String user,
        @PathParam("reponame") String repo,
        @QueryParam("kw") @DefaultValue("") String keywords,
        @QueryParam("ctg") @DefaultValue("page") String category,
        @QueryParam("index") @DefaultValue("0") String index,
        @QueryParam("size") @DefaultValue("10") String size
    ) throws JsonProcessingException {

        int idx = Integer.valueOf(index);
        int nr = Integer.valueOf(size);
        SearchQuery query = new SearchQuery(keywords, category, Integer.valueOf(index), Integer.valueOf(size));
        String indexName = user.toLowerCase() + "x" + repo.toLowerCase();

        ElasticSearch aws = new AmazonElasticSearch(
            indexName, this.accesskey, this.secretKey, this.reg, this.esEdp
        );
        SearchResultsPage results = aws.search(query);
        
        String queryStringFormat = "?kw=%s&ctg=%s&index=%s&size=%s";
        String requestUrl = servletRequest.getRequestURL().toString();
        if(idx == 0) {
            results.setPreviousPage("-");
        } else {
            String queryString = String.format(queryStringFormat, keywords, category, idx - nr, nr);
            results.setPreviousPage(requestUrl + queryString);
        }
        if(idx + nr >= results.getTotalHits()) {
            results.setNextPage("-");
        } else {
            String queryString = String.format(queryStringFormat, keywords, category, idx + nr, nr);
            results.setNextPage(requestUrl + queryString);
        }
        results.setPageNr(idx/nr + 1);
        
        int start = 0;
        List<String> pagesLinks = new ArrayList<String>();
        while(start < results.getTotalHits()) {
            pagesLinks.add(
                requestUrl + String.format(queryStringFormat, keywords, category, start, nr)
            );
            start += nr;
        }
        results.setPages(pagesLinks);
        
        return Response.ok().entity(new ObjectMapper().writeValueAsString(results)).build();
    }

}
