## Architecture documentation

This document's intention is to give a full overview of the code base's structure, flow and main types.
In the text bellow, main types will be written with capital letter and will be linked to their interface or class.

 * [Entry point](#entry-point)
 * [Taking action about a notification](#taking-action-about-a-notification)
 * [Understanding a Command](#understanding-a-command)
 * [Steps](#steps)
 * [Other details](#other-details)
 
## Entry point

The entry point is REST method ``POST /api/notifications`` (see [here](https://github.com/opencharles/charles-rest/blob/master/src/main/java/com/amihaiemil/charles/rest/NotificationsResource.java#L98)) .
That endpoint listens for an array of notifications formatted as follows:

```
[
    ...,
    {
        "repoFullName":"owner/repoName",
        "issueNumber":128
    },
    ...
]
```

## Taking action about a notification

For each received notification, the bot starts an [Action](https://github.com/opencharles/charles-rest/blob/master/src/main/java/com/amihaiemil/charles/github/Action.java). If more notifications are received at the same time, then the actions are executed in parallel.
This is achieved via EJB's [Asynchronous](http://docs.oracle.com/javaee/6/tutorial/doc/gkkqg.html) annotation - if you look closely, you'll notice that [NotificationsResource](https://github.com/opencharles/charles-rest/blob/master/src/main/java/com/amihaiemil/charles/rest/NotificationsResource.java) is both a REST resource and an EJB.

Each **Action** builds its own logger so the application logs each action in a different log file (in most of the commands, we let the user inspect the log to see if everything
went fine)

## Understanding a Command

A notification contains only the repo and the issue where the bot has been mentioned. To find the command, the bot searches for the **last** (most recent) comment in the issue
where it's been mentioned. Only the last mentioning comment will be treated as a command, in order to avoid spamming.

The bot has a set of knowledges (implementations of Knowledge), which it uses in order to try and "understand" a command.
Understanding a command means building the tree of steps which are to be executed in order to fulfil the command.

First, the bot puts together all its knowledges, then it uses them to find the proper steps. The last Knowledge in the chain is Confused, which means
the bot didn't manage to categorize the command and it will leave a Github reply informing the commander about the sitiuation:

```java
    final Knowledge knowledge = new Conversation(      //it has a conversation based on a command
        new Hello(                                     //it can say hello
            new IndexSiteKn(                           //it can index a whole website
                new IndexSitemapKn(                    //it can index a site following a sitemap
                    new IndexPageKn(                   //it can index a single page
                        new DeleteIndexKn(             //it can delete a whole index
                            new DeletePageKn(          //it can delete a single page from an index
                                new Confused()         //it may be confused, not understanding your command.
                            )
                        )
                    )
                )
            )
        )
    );
```


## Steps

The bot has to execute one or more steps in order to fulfill any command. Always. At a minimum, if the command cannot be understood there is the one step of leaving a comment on the Github issue explaining
that the command wasn't understood and pointing the user to the commands' documentation.

Any Step that the bot takes is (should extend) either a [PreconditionCheckStep](https://github.com/opencharles/charles-rest/blob/master/src/main/java/com/amihaiemil/charles/github/PreconditionCheckStep.java) or a [IntermediaryStep](https://github.com/opencharles/charles-rest/blob/master/src/main/java/com/amihaiemil/charles/github/IntermediaryStep.java). Read the javadocs of those two classes, you'll get the idea quickly.

Once the command has been "understood" and the Step-tree is built, the steps are executed within an Action.
If there is an error during steps' execution, a comment is delivered explaining the something went wrong and ofering a link to the logs.

## Other details

 * **Interaction with the Github API**:

    For communication with the Github API, the library [jcabi-github](https://github.com/jcabi/jcabi-github) is used.
    This library encapsulates all the HTTP calls made to the API and provides very good abstractions and a convenient fluency in method calls.

    Also, the library provides a **mock implementation of the Github API**, which is **used when unit testing** the code (in order to avoid calling the
    real API at each unit test run).
 * **More languages**:

    The bot should "speak" more languages. See how the [Language](https://github.com/opencharles/charles-rest/blob/master/src/main/java/com/amihaiemil/charles/github/Language.java)
    class is used (type hierarchy and use by class Brain)
