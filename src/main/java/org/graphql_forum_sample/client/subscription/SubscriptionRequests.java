package org.graphql_forum_sample.client.subscription;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.forum.client.Member;
import org.forum.client.PostInput;
import org.forum.client.TopicPostInput;
import org.forum.client.util.GraphQLRequest;
import org.forum.client.util.MutationExecutor;
import org.forum.client.util.SubscriptionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.graphql_java_generator.client.SubscriptionClient;
import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;

@Component
public class SubscriptionRequests {

	@Autowired
	MutationExecutor mutationTypeExecutor;

	@Autowired
	SubscriptionExecutor subscriptionTypeExecutor;

	public void execSubscription() throws GraphQLRequestPreparationException, GraphQLRequestExecutionException,
			IOException, InterruptedException {
		// Preparation
		GraphQLRequest subscriptionRequest = subscriptionTypeExecutor
				.getSubscribeToNewPostGraphQLRequest("{id date publiclyAvailable title content author{id name}}");
		GraphQLRequest createPostRequest = mutationTypeExecutor
				.getCreatePostGraphQLRequest("{id date author{id} title content publiclyAvailable}");

		PostSubscriptionCallback postSubscriptionCallback = new PostSubscriptionCallback();
		Member author = new Member();
		author.setId(new UUID(0, 12).toString());
		PostInput postInput = new PostInput();
		postInput.setTopicId(new UUID(0, 22).toString());
		postInput.setInput(getTopicPostInput(author, "Some other content",
				new GregorianCalendar(2020, 11 - 1, 21).getTime(), false, "The good title for a post"));

		////////////////////////////////////////////////////////////////////////////////////////////////////
		// Actual submission of the subscription.
		// The notifications are received by postSubscriptionCallback.onMessage() method
		System.out.println("Submitting the 'subscribeToNewPostWithBindValues' GraphQL subscription");
		SubscriptionClient subscriptionClient = subscriptionTypeExecutor.subscribeToNewPost(subscriptionRequest,
				postSubscriptionCallback, "Board name 1");
		////////////////////////////////////////////////////////////////////////////////////////////////////

		// For this test, we need to be sure that the subscription is active, before creating the post (to be sure that
		// the subscription is active when the post is created, so that we receive the notification for its creation)
		Thread.sleep(1000);

		System.out.println(
				"Creating a post (for which we expect a notification) from this postInput: " + postInput.toString());
		mutationTypeExecutor.createPost(createPostRequest, postInput);

		// Let's wait until we receive the notification
		postSubscriptionCallback.latchNewMessage.await(5, TimeUnit.SECONDS);
		if (postSubscriptionCallback.lastReceivedMessage == null) {
			throw new RuntimeException("The notification for the post creation was not received");
		}

		// We need to free the server resources, at the end
		subscriptionClient.unsubscribe();
	}

	private TopicPostInput getTopicPostInput(Member author, String content, Date date, boolean publiclyAvailable,
			String title) {
		TopicPostInput input = new TopicPostInput();
		input.setAuthorId(author.getId());
		input.setContent(content);
		input.setDate(date);
		input.setPubliclyAvailable(publiclyAvailable);
		input.setTitle(title);

		return input;
	}
}
