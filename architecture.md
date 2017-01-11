## Architecture documentation

This document's intention is to give a full overview of code base's structure, flow and main types.
In the text bellow, main types will be written with capital letter and will be linked to their interface or class.

## Entry point

The entry point is REST method ``POST /api/notifications`` (see [here](https://github.com/opencharles/charles-rest/blob/master/src/main/java/com/amihaiemil/charles/rest/NotificationsResource.java#L98)) .
That endpoint listens for an array of notifications formatted as follows:

```
[
    ...,
    {
        "repoFullName":"owner/repoName",
        "issueNumber":123
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

The bot has a [Brain](https://github.com/opencharles/charles-rest/blob/master/src/main/java/com/amihaiemil/charles/github/Brain.java) and at the beginning of each Action, it tries to **understand** the command.
Understanding a command means building up the [Step](https://github.com/opencharles/charles-rest/blob/master/src/main/java/com/amihaiemil/charles/github/Step.java)s to be executed in order to fulfill the command.

If you look inside class Brain you will see that Steps are instantiated one on top of the other, like an umbrella. Basically, for each command a tree-like structure of steps is built.

Please note that currently class **Brain** has grown quite big and needs to be refactored and broken down in smaller pieces.
There is an issue for that [here](https://github.com/opencharles/charles-rest/issues/155).



## Steps

The bot has to execute one or more steps in order to fulfill any command. Always. At a minimum, if the command cannot be understood there is the one step of leaving a comment on the Github issue explaining
that the command wasn't understood and pointing the user to the commands' documentation.

Any Step that the bot takes is (should extend) either a [PreconditionCheckStep](https://github.com/opencharles/charles-rest/blob/master/src/main/java/com/amihaiemil/charles/github/PreconditionCheckStep.java) or a [IntermediaryStep](https://github.com/opencharles/charles-rest/blob/master/src/main/java/com/amihaiemil/charles/github/IntermediaryStep.java). Read the javadocs of those two classes, you'll get the idea quickly.

...more to follow...