<img alt="logo" src="http://www.amihaiemil.com/images/logo_mic.PNG" width="60" height="60"/>
# charles-github-ejb
[![Build Status](https://travis-ci.org/opencharles/charles-github-ejb.svg?branch=master)](https://travis-ci.org/opencharles/charles-github-ejb)
![Service status](http://charles.amihaiemil.com/img/service status-offline-yellow.svg)
<a href='https://coveralls.io/github/opencharles/charles-github-ejb?branch=master'><img src='https://coveralls.io/repos/github/opencharles/charles-github-ejb/badge.svg?branch=master' alt='Coverage Status' /></a>

#What it is: 

This repository holds 2 maven projects:

1) ``charles-github-notifications-ejb`` - an EJB that checks Github notifications regularly (at a set interval of time). When one or more notifications are found, they are sent
to one of the REST endpoints exposed by ``charles-rest`` for further processing.

2) ``charles-rest`` - webapp ``.war`` - codebase behind [Charles Michaels](https://www.github.com/charlesmike) chatbot. It receives notifications read by the above ejb and takes
actions according to each of them. Any Github account can be used with this codebase; it's all dictated by the Github auth token used.

Say ``@charlesmike hi there`` in a Github issue comment and see what happens. 
Check out the [website](http://charles.amihaiemil.com) for more details on how to use this service.

## Key technologies
- [phantom js](http://phantomjs.org/) and [Selenium] (http://www.seleniumhq.org/projects/webdriver/) with [GhostDriver](https://github.com/detro/ghostdriver)
- [Elastic search] (https://www.elastic.co/)
- [Amazon Web Services] (https://aws.amazon.com/)
- [Github API] (https://developer.github.com/v3/) (using [jcabi-github](https://github.com/jcabi/jcabi-github/))

## If you wish to install it on your own infrastructure:

Initially the EJB module was part of the ``.war`` and there was one single deployable. They were split because of clustering issues.
If you deployed the initial architecture in multiple nodes (in a cluster, for load balancing), they would all check the Github notifications at the same time and 
it didn't make much sense. So, the deployment model is now as follows:

    1) The EJB jar is to be deployed alone in a single server
    2) The ``.war`` can be deployed in multple nodes

You will need to set the following system properties. **Pay a lot of attention while configuring these, since everything relies on them**.

## EJB notifications checker sys props
<table>
  <tr>
    <th>Name</th><th>Value</th><th>Description</th>
  </tr>
  <tr>
    <td>checks.interval.minutes</td>
    <td>integer</td>
    <td><b>Optional</b>. Minutes that should <br> pass between checks. Defaults to 2.</td>
  </tr>
  <tr>
    <td>charles.rest.endpoint</td>
    <td>**domain**/**charles-rest-context-root**/api/notifications</td>
    <td><b>Mantadory</b>. Rest endpoint from charles-rest<br>where the found notifications should be sent for handling.</td>
  </tr>
  <tr>
    <td>github.auth.token</td>
    <td>string</td>
    <td><b>Mantadory</b>. Github agent's access token. It should only have permissions to check notifications, star repos and
    post comments. <b>Do not give full permissions!</b></td>
  </tr>
</table>

## Rest api sys props
<table>
  <tr>
    <th>Name</th><th>Value</th><th>Description</th>
  </tr>
  <tr>
    <td>LOG_ROOT</td>
    <td>string</td>
    <td><b>Optional</b>. Place where the log files will be stored. Defaults to . (dot)</td>
  </tr>
  <tr>
    <td>charles.rest.logs.endpoint</td>
    <td>**domain**/**charles-rest-context-root**/api/logs</td>
    <td><b>Mantadory</b>. Rest endpoint from charles-rest<br>that returns the log of an action.</td>
  </tr>
  <tr>
    <td>github.auth.token</td>
    <td>string</td>
    <td><b>Mantadory</b>. Github agent's access token. <b>Must be the same as for the EJB checker</b></td>
  </tr>
  <tr>
    <td>phantomjsExec</td>
    <td>string</td>
    <td><b>Optional</b>. Location of phantomjs executable on the server. Defaults to <b>/usr/local/bin/phantomjs</b></td>
  </tr>
  <tr>
    <td>aws.es.endpoint</td>
    <td>string</td>
    <td><b>Mandatory</b>. Endpoint of AWS elasticsearch service</td>
  </tr>
  <tr>
    <td>aws.es.region</td>
    <td>string</td>
    <td><b>Mandatory</b>. Region of AWS elasticsearch service</td>
  </tr>
  <tr>
    <td>aws.accessKeyId</td>
    <td>string</td>
    <td><b>Mandatory</b>. AWS access key id</td>
  </tr>
  <tr>
    <td>aws.secretKey</td>
    <td>string</td>
    <td><b>Mandatory</b>. AWS secret key</td>
  </tr>
  
</table>

