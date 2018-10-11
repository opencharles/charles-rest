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
package com.amihaiemil.charles.github;

import java.io.IOException;

import org.slf4j.Logger;

import com.amihaiemil.charles.aws.AmazonElasticSearch;
import com.amihaiemil.charles.aws.ElasticSearch;

/**
 * Step that checks if an index exists in elasticsearch
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version  $Id$
 * @since 1.0.0
 *
 */
public final class IndexExistsCheck  extends PreconditionCheckStep {

    /**
     * AWS elasticsearch repository.
     */
    private ElasticSearch repo;

    /**
     * Constructor.
     * @param index Index name.
     * @param onTrue The step to perform on successful check.
     * @param onFalse the step to perform in unsuccessful check.
     */
    public IndexExistsCheck(String index, Step onTrue, Step onFalse) {
        this(new AmazonElasticSearch(index), onTrue, onFalse);
    }

    /**
     * Constructor.
     * @param repo AWS repository to look into.
     * @param onTrue The step to perform on successful check.
     * @param onFalse The step to perform in unsuccessful check.
     */
    public IndexExistsCheck(ElasticSearch repo, Step onTrue, Step onFalse) {
        super(onTrue, onFalse);
        this.repo = repo;
    }

    @Override
    public void perform(Command command, Logger logger) throws IOException {
        logger.info("Checking if required index exists...");
        boolean exists = this.repo.exists();
        if(exists) {
            logger.info("Index exists - Ok!");
            this.onTrue().perform(command, logger);
        } else {
            logger.warn("The required index does not exist! It may have been deleted already.");
            this.onFalse().perform(command, logger);
        }
    }

}
