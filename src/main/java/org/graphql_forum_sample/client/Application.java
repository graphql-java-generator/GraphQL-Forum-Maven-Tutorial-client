package org.graphql_forum_sample.client;

import java.util.GregorianCalendar;
import java.util.List;

import org.forum.client.Board;
import org.forum.client.PostInput;
import org.forum.client.Topic;
import org.forum.client.TopicPostInput;
import org.forum.client.util.QueryTypeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;

/**
 * This class contains the functional code that is executed. It uses the GraphQLXxx Spring beans for that. It is started
 * by Spring, once the application context is initialized, as it implements {@link CommandLineRunner}
 * 
 * @author etienne-sf
 */
@Component // This annotation marks this class as a Spring bean
public class Application implements CommandLineRunner {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(Application.class);

	@Autowired
	private QueryTypeExecutor queryExecutor;

	@Autowired
	private PartialPreparedQueries partialPreparedQueries;

	@Autowired
	private FullPreparedQueries FullPreparedQueries;

	@Override
	public void run(String... args) throws GraphQLRequestPreparationException, GraphQLRequestExecutionException {
		logger.info("===========================================================================================");
		logger.info("====================  Executing Partial Requests  =========================================");
		logger.info("===========================================================================================");
		List<Board> boards = partialPreparedQueries.boards();
		logger.info("Boards read: {}", boards);

		logger.info("===========================================================================================");
		logger.info("====================  Executing Partial Requests, with parameters  ========================");
		logger.info("===========================================================================================");
		List<Topic> topics = partialPreparedQueries.topics("Board name 2");
		logger.info("Topics read: {}", topics);

		logger.info("===========================================================================================");
		logger.info("====================  Executing direct partial request, with a fragment  ==================");
		logger.info("===========================================================================================");
		boards = queryExecutor.boards("{id name publiclyAvailable topics {... on Topic {id title date} nbPosts}}");
		logger.info("Boards read: {}", boards);

		logger.info("===========================================================================================");
		logger.info("====================  Executing direct full request, with a fragment  =====================");
		logger.info("===========================================================================================");
		boards = queryExecutor.exec("fragment topicFields on Topic {id title date} "
				+ "query{boards{id name publiclyAvailable topics {...topicFields nbPosts}}}").getBoards();
		logger.info("Boards read: {}", boards);

		logger.info("===========================================================================================");
		logger.info("====================  Executing Full Request, with parameters  ============================");
		logger.info("===========================================================================================");
		TopicPostInput topicInput = new TopicPostInput.Builder().withAuthorId("00000000-0000-0000-0000-000000000001")
				.withPubliclyAvailable(true).withDate(new GregorianCalendar(2019, 4 - 1, 30).getTime())
				.withTitle("a title").withContent("Some content").build();
		PostInput postInput = new PostInput.Builder().withFrom(new GregorianCalendar(2018, 3 - 1, 2).getTime())
				.withInput(topicInput).withTopicId("00000000-0000-0000-0000-000000000002").build();
		FullPreparedQueries.createPost(postInput);

		logger.info("Normal end of execution");
	}

}
