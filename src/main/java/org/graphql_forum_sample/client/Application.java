package org.graphql_forum_sample.client;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.forum.client.Board;
import org.forum.client.Mutation;
import org.forum.client.Post;
import org.forum.client.PostInput;
import org.forum.client.Topic;
import org.forum.client.TopicPostInput;
import org.graphql_forum_sample.client.subscription.SubscriptionRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;

/**
 * This class contains the functional code that is executed. It uses the GraphQLXxx Spring beans for that. It is started
 * by Spring, once the application context is initialized, as it implements {@link CommandLineRunner}.<br/>
 * It demonstrates how to define and execute queries, with <a href=
 * "https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/client_graphql_repository">GraphQL
 * Repositories</a>. GraphQL Repositories are available since release 1.17
 * 
 * @author etienne-sf
 */
@Component // This annotation marks this class as a Spring bean (prerequisite to make @Autowire annotation work)
public class Application implements CommandLineRunner {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(Application.class);

	@Autowired
	private MyGraphQLRepository myGraphQLRepository;
	@Autowired
	private SubscriptionRequests subscriptions;

	@Override
	public void run(String... args) throws GraphQLRequestExecutionException, GraphQLRequestPreparationException,
			IOException, InterruptedException {
		logger.info("===========================================================================================");
		logger.info("====================  Executing Partial Requests  =========================================");
		logger.info("===========================================================================================");
		List<Board> boards = this.myGraphQLRepository.boards();
		logger.info("Boards read: {}", boards);

		logger.info("===========================================================================================");
		logger.info("====================  Executing Partial Requests, with parameters  ========================");
		logger.info("===========================================================================================");
		List<Topic> topics = this.myGraphQLRepository.topics("Board name 2");
		logger.info("Topics read: {}", topics);

		logger.info("===========================================================================================");
		logger.info("====================  Executing Partial Requests, with parameters and bind parameters =====");
		logger.info("===========================================================================================");
		String memberId = null; // may be null, as it's optional
		String memberName = null; // may be null, as it's optional
		Date since = new Calendar.Builder().setDate(2022, 02 - 1 /* february */, 01).build().getTime();
		List<Topic> topicsSince = this.myGraphQLRepository.topicsSince("Board name 2", memberId, memberName, since);
		logger.info("Topics read: {}", topicsSince);

		logger.info("===========================================================================================");
		logger.info("==================== Executing direct full request, with a fragment =====================");
		logger.info("===========================================================================================");
		boards = this.myGraphQLRepository.fullQueryWithFragment().getBoards();
		logger.info("Boards read: {}", boards);

		logger.info("===========================================================================================");
		logger.info("====================  Executing Partial Requests, with inline Fragment ====================");
		logger.info("===========================================================================================");
		List<Board> boardsWithInlineFragment = this.myGraphQLRepository.boardsWithInlineFragment();
		logger.info("Boards read: {}", boardsWithInlineFragment);

		TopicPostInput topicInput = new TopicPostInput.Builder().withAuthorId("00000000-0000-0000-0000-000000000001")
				.withPubliclyAvailable(true).withDate(new GregorianCalendar(2019, 4 - 1, 30).getTime())
				.withTitle("a title").withContent("Some content").build();
		PostInput postInput = new PostInput.Builder().withFrom(new GregorianCalendar(2018, 3 - 1, 2).getTime())
				.withInput(topicInput).withTopicId("00000000-0000-0000-0000-000000000002").build();

		logger.info("===========================================================================================");
		logger.info("==================== Executing mutation in a Partial Request ==============================");
		logger.info("===========================================================================================");
		Post postFromPartialRequest = this.myGraphQLRepository.createPost(postInput);
		logger.info("Post created: {}", postFromPartialRequest);

		logger.info("===========================================================================================");
		logger.info("==================== Executing mutation in a Full Request =================================");
		logger.info("===========================================================================================");
		Mutation mutation = this.myGraphQLRepository.createPostFullRequest(postInput);
		Post postFromFullRequest = mutation.getCreatePost();
		logger.info("Post created: {}", postFromFullRequest);

		logger.info("");
		logger.info("============================================================================");
		logger.info("======= LET'S EXECUTE A SUBSCRIPTION      ==================================");
		logger.info("============================================================================");
		this.subscriptions.execSubscription();

		logger.info("");
		logger.info("============================================================================");
		logger.info("Normal end of execution");
	}

}
