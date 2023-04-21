/**
 * 
 */
package org.graphql_forum_sample.client.subscription;

import java.util.concurrent.CountDownLatch;

import org.forum.client.Post;

import com.graphql_java_generator.client.SubscriptionCallback;

/**
 * @author etienne-sf
 */
// The class that'll receive the notification from the GraphQL subscription

public class PostSubscriptionCallback implements SubscriptionCallback<Post> {

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
		System.out.println(
				"Received a notification from the 'subscribeToNewPostWithBindValues' subscription, for this post  "
						+ t);
		nbReceivedMessages += 1;
		lastReceivedMessage = t;
		latchNewMessage.countDown();
		// Do something useful with it
	}

	@Override
	public void onClose(int statusCode, String reason) {
		connected = false;
		lastReceivedClose = statusCode + "-" + reason;
		System.out.println("Received onClose: " + lastReceivedClose);
	}

	@Override
	public void onError(Throwable cause) {
		connected = false;
		lastReceivedError = cause;
		System.out.println("Received onError: " + cause);
	}

}
