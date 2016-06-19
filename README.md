# charles-github-ejb
[![Build Status](https://travis-ci.org/amihaiemil/charles-github-ejb.svg?branch=master)](https://travis-ci.org/amihaiemil/charles-github-ejb)

This repository holds 2 maven projects:

1) ``charles-github-ejb`` - EJB jar which holds the implementation of the interface between Github and indexing logic (implemented with [charles](https://github.com/amihaiemil/charles)). It can be deployed standalone or as a dependency to other war (see charles-rest)


2) ``charles-rest`` - webapp ``.war`` which offers [charles'](https://github.com/amihaiemil/charles) functionality through a REST interface and also incapsulates ``charles-github-ejb``, reusing implemented logic, extending functionality and reducing deployments.
