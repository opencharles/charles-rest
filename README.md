# charles-github-ejb
[![Build Status](https://travis-ci.org/amihaiemil/charles-github-ejb.svg?branch=master)](https://travis-ci.org/amihaiemil/charles-github-ejb)
![Service status](http://charles.amihaiemil.com/img/service status-offline-yellow.svg)
[[Coverage Status](https://coveralls.io/repos/github/amihaiemil/charles-github-ejb/badge.svg?branch=master)](https://coveralls.io/github/amihaiemil/charles-github-ejb?branch=master)

This repository holds 2 maven projects:

1) ``charles-github-ejb`` - Codebase for the Github chatbot [Charles Michael](https://www.github.com/charlesmike)

2) ``charles-rest`` - webapp ``.war`` which wraps the above EJB and also exposes a REST api for retrieving log files, indexing and searching of content.

Say ``@charlesmike hi there`` in a Github issue comment and see what happens. 
Check out the [website](http://charles.amihaiemil.com) for more details on how to use this service.

You can also deploy the ``.war`` on your own infrastructure, with your own chatbot and Elastic Search instance. You just have to set some System properies 
for configuration. More to follow on this topic.
