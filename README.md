<img alt="logo" src="http://www.amihaiemil.com/images/logo_mic.PNG" width="60" height="60"> # charles-github-ejb
[![Build Status](https://travis-ci.org/amihaiemil/charles-github-ejb.svg?branch=master)](https://travis-ci.org/amihaiemil/charles-github-ejb)
![Service status](http://charles.amihaiemil.com/img/service status-offline-yellow.svg)
<a href='https://coveralls.io/github/amihaiemil/charles-github-ejb?branch=master'><img src='https://coveralls.io/repos/github/amihaiemil/charles-github-ejb/badge.svg?branch=master' alt='Coverage Status' /></a>

#What it is: 

This repository holds 2 maven projects:

1) ``charles-github-notifications-ejb`` - an EJB that checks Github notifications regularly (at a set interval of time). When one or more notifications are found, they are sent
to one of the REST endpoints exposed by ``charles-rest`` for further processing.

2) ``charles-rest`` - webapp ``.war`` - codebase behind [Charles Michaels](https://www.github.com/charlesmike) chatbot. It receives notifications read by the above ejb and takes
actions according to each of them. Any Github account can be used with this codebase; it's all dictated by the Github auth token used.

Say ``@charlesmike hi there`` in a Github issue comment and see what happens. 
Check out the [website](http://charles.amihaiemil.com) for more details on how to use this service.


#If you wish to install it on your own infrastructure:

Initially the EJB module was part of the ``.war`` and there was one single deployable. They were split because of clustering issues.
If you deployed the initial architecture in multiple nodes (in a cluster, for load balancing), they would all check the Github notifications at the same time and 
it didn't make much sense. So, the deployment model is now as follows:

    1) The EJB jar is to be deployed alone in a single server
    2) The ``.war`` can be deployed in multple nodes

You will need to set some system properties (e.g. Github access token; token for communication between the EJB and REST endpoint etc).
More to follow on this topic.
