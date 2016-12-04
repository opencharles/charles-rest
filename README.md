<img alt="logo" src="http://www.amihaiemil.com/images/logo_mic.PNG" width="60" height="60"/>
# charles-github-ejb
[![Build Status](https://travis-ci.org/opencharles/charles-github-ejb.svg?branch=master)](https://travis-ci.org/opencharles/charles-github-ejb)
![Service status](http://charles.amihaiemil.com/img/service status-offline-yellow.svg)
<a href='https://coveralls.io/github/opencharles/charles-github-ejb?branch=master'><img src='https://coveralls.io/repos/github/opencharles/charles-github-ejb/badge.svg?branch=master' alt='Coverage Status' /></a>

#What it is: 

A webapp ``.war`` - codebase behind [Charles Michaels](https://www.github.com/charlesmike) chatbot. It receives notifications read by this [ejb checker](https://github.com/opencharles/mention-notifications-ejb) and takes
actions according to each of them. Any Github account can be used with this project; it's all dictated by the Github auth token used.

Say ``@charlesmike hi there`` in a Github issue comment and see what happens. 
Check out the [website](http://charles.amihaiemil.com) for more details on how to use this service.

## Key technologies
- [phantom js](http://phantomjs.org/) and [Selenium] (http://www.seleniumhq.org/projects/webdriver/) with [GhostDriver](https://github.com/detro/ghostdriver)
- [Elastic search] (https://www.elastic.co/)
- [Amazon Web Services] (https://aws.amazon.com/)
- [Github API] (https://developer.github.com/v3/) (using [jcabi-github](https://github.com/jcabi/jcabi-github/))

## If you wish to install it on your own infrastructure:

The .war should work fine on any Java EE web server. It is not bound by any server-specific property file or other things like that.
Once deployed, it exposes the endpoint POST /api/notifications which accepts a JsonArray of format:

```
[
    ...,
    {
        "repoFullName":"repo/nameHere",
        "issueNumber":123
    },
    ...
]
```
As it is clear, a pipeline between Github Notifications API and this endpoint is needed. You can setup one of your own (respecting the
above mentioned interface) or use
[this](https://github.com/opencharles/mention-notifications-ejb) project.


You will need to set the following system properties. **Pay a lot of attention while configuring these, since everything relies on them**.

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

