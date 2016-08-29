# charles-github-ejb
[![Build Status](https://travis-ci.org/amihaiemil/charles-github-ejb.svg?branch=master)](https://travis-ci.org/amihaiemil/charles-github-ejb)

This repository holds 2 maven projects:

1) ``charles-github-ejb`` - EJB jar which holds the implementation of the interface between Github and indexing logic (implemented with [charles](https://github.com/amihaiemil/charles)). It can be deployed standalone or as a dependency to charles-rest. 

You can use this ejb as a content indexer interacting with your Github account, but for search functionality you need the rest interface. So deploy this ejb by yourself when you only want to index your content and you already have a search mechanism in place. 

There will be some differences in functionality when using this ejb alone:

a) Instead of receiving a rest link to the action logs, the logs will be coppied to a gist and the link to that gist will be returned.

b) When finishing an index command, instead of returning a js &lt;script&gt; to put on the webpage, the link to the ES index will be returned.

2) ``charles-rest`` - webapp ``.war`` which offers [charles'](https://github.com/amihaiemil/charles) functionality through a REST interface and also incapsulates ``charles-github-ejb``, reusing implemented logic, extending functionality and reducing deployments.

Some system properties will have to be set in order to run this successfully. To follow.
