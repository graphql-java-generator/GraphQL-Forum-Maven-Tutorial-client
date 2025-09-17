/**
 * 
 */
package org.graphql_forum_sample.client.subscription;

import java.util.concurrent.CountDownLatch;

import org.forum.client.Post;
import org.graphql_forum_sample.client.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphql_java_generator.client.SubscriptionCallback;

/**
 * @author etienne-sf
 */
// The class that'll receive the notification from the GraphQL subscription

public class PostSubscriptionCallback implements SubscriptionCallback<Post> {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(Application.class);

	/** Indicates whether the Web Socket is connected or not */
	public boolean connected = false;

	public int nbReceivedMessages = 0;
	public Post lastReceivedMessage = null;
	public String lastReceivedClose = null;
	public Throwable lastReceivedError = null;

	public CountDownLatch latchNewMessage = new CountDownLatch(1);

	@Override
	public void onConnect() {
		this.connected = true;
	}

	@Override
	public void onMessage(Post t) {
		logger.info("Received a notification from the 'subscribeToNewPostWithBindValues' subscription, for this post  "
				+ t);
		this.nbReceivedMessages += 1;
		this.lastReceivedMessage = t;
		this.latchNewMessage.countDown();
		// Do something useful with it
	}

	@Override
	public void onClose(int statusCode, String reason) {
		this.connected = false;
		this.lastReceivedClose = statusCode + "-" + reason;
		logger.info("Received onClose: " + this.lastReceivedClose);
	}

	@Override
	public void onError(Throwable cause) {
		this.connected = false;
		this.lastReceivedError = cause;
		logger.error("Received onError: " + cause);
	}

}
