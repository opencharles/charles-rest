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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.amihaiemil.camel.Yaml;
import com.amihaiemil.camel.YamlMapping;
import com.amihaiemil.camel.YamlSequence;

/**
 * .charles.yml input parsed with camel library.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.1
 */
public final class CharlesYmlInput implements CharlesYml {

    /**
     * Contents of .charles.yml.
     */
    private YamlMapping yaml;

    /**
     * Ctor.
     * @param yaml .charles.yml.
     * @throws IOException If the input stream cannot be read.
     */
    public CharlesYmlInput(final InputStream yaml) throws IOException {
        this.yaml = Yaml.createYamlInput(yaml).readYamlMapping();
    }

    @Override
    public List<String> commanders() {
        final List<String> commanders = new ArrayList<>();
        final YamlSequence sequence = this.yaml.yamlSequence("commanders");
        if(sequence != null) {
            for(int i=0;i<sequence.size();i++) {
                commanders.add(sequence.string(i));
            }
        }
        return commanders;
    }

    @Override
    public boolean tweet() {
        return Boolean.valueOf(this.yaml.string("tweet"));
    }

    @Override
    public String driver() {
        final String driver = this.yaml.string("driver");
        if(driver != null) {
            return driver;
        } else {
            return "chrome";
        }
    }
}
