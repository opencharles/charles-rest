package com.amihaiemil.charles.rest.model;

import javax.json.JsonObject;

public final class WebhookNotifications extends Notifications {

	/**
	 * Ctor.
	 * @param all All webhook notifications
	 */
	public WebhookNotifications(final JsonObject... all) {
		for(final JsonObject json : all) {
			super.notifications.add(new IssueCommentNotification(json));
		}
	}

}
