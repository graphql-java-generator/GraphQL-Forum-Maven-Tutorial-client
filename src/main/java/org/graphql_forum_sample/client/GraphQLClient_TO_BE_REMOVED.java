/**
 * 
 */
package org.graphql_forum_sample.client;

import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;

import org.forum.client.Board;
import org.forum.client.MutationType;
import org.forum.client.Post;
import org.forum.client.PostInput;
import org.forum.client.QueryType;
import org.forum.client.Topic;
import org.forum.client.TopicPostInput;
import org.forum.client.util.GraphQLRequest;
import org.forum.client.util.MutationTypeExecutor;
import org.forum.client.util.QueryTypeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;

//@Component // This marks this class as being a Spring Bean
public class GraphQLClient_TO_BE_REMOVED implements CommandLineRunner {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(GraphQLClient_TO_BE_REMOVED.class);

	/**
	 * The executor, that allows to execute GraphQL queries. The class name is the one defined in the GraphQL schema,
	 * with the suffix Executor.<BR/>
	 * It is automagically loaded by Spring, from the Beans it has discovered. An error is thrown if no matching bean is
	 * found, of if more than one matching bean is found
	 */
	@Autowired
	QueryTypeExecutor queryExecutor;

	/** The executor, that allows to execute GraphQL mutations */
	@Autowired
	MutationTypeExecutor mutationExecutor;

	// Partial requests
	GraphQLRequest boardsPartialRequest;
	GraphQLRequest allTopicsPartialRequest;
	GraphQLRequest topicsSincePartialRequest;

	// Full requests
	GraphQLRequest boardsFullRequest;

	/**
	 * This method is executed by Spring, once the autowired field are set, and before the run method is called
	 * 
	 * @throws GraphQLRequestPreparationException
	 */
	@PostConstruct
	public void init() throws GraphQLRequestPreparationException {
		logger.info("Starting preparation");

		// Creation of the query executor, for this GraphQL endpoint
		logger.info("Connecting to GraphQL endpoint");
		queryExecutor = new QueryTypeExecutor("http://localhost:8180/graphql");
		mutationExecutor = new MutationTypeExecutor("http://localhost:8180/graphql");

		// Preparation of the GraphQL Partial requests, that will be used in the execPartialRequests() method
		boardsPartialRequest = queryExecutor
				.getBoardsGraphQLRequest("{id name publiclyAvailable topics {id title date nbPosts}}");
		allTopicsPartialRequest = queryExecutor.getTopicsGraphQLRequest(//
				"{id date author {id name} nbPosts title content}");
		topicsSincePartialRequest = queryExecutor.getTopicsGraphQLRequest(""//
				+ "{" //
				+ "  id date author {id name} nbPosts title content "//
				+ "  posts(memberId: ?memberId, memberName: ?memberName, since: &since)  {id date title}"//
				+ "}");

		// Preparation of the GraphQL Full requests, that will be used in the execFullRequests() method
		boardsFullRequest = mutationExecutor
				.getGraphQLRequest("mutation {createPost(post: &postInput) { id date author{id name} title content}}");

		logger.info("Preparation finished");
	}

	@Override
	public void run(String... args) throws GraphQLRequestPreparationException {
		logger.info("Starting execution");
		logger.info("Normal end of execution");
	}

	public void execPartialRequests() throws GraphQLRequestExecutionException {
		logger.debug("===========================================================================================");
		logger.debug("====================  Executing Partial Requests  =========================================");
		logger.debug("===========================================================================================");

		// Let's get, then display, all available boards
		List<Board> boards = queryExecutor.boards(boardsPartialRequest);
		logger.trace("Boards read: {}", boards);

		// Let's get, then display, all topics of one of these boards
		String aBoardName = boards.get(1).getName();
		List<Topic> allTopics = queryExecutor.topics(allTopicsPartialRequest, aBoardName);
		logger.trace("Topics of board '{}' are: {}", aBoardName, allTopics);

		// Let's get, then display, all topics of one of these boards since the 2 march 2018
		java.util.Date sinceParam = new GregorianCalendar(2018, 3 - 1, 2).getTime();
		List<Topic> topicsSince = queryExecutor.topics(topicsSincePartialRequest, //
				aBoardName, // This the query parameter. Depending on the GraphQL schema, there could be others
				"memberId", "00000000-0000-0000-0000-000000000002", //
				// No value is given for the optional memberName parameter
				"since", sinceParam);
		logger.trace("Topics of board '{}' for author '00000000-0000-0000-0000-000000000002' since {} are: {}",
				aBoardName, sinceParam, topicsSince);
	}

	public void execFullRequests() throws GraphQLRequestExecutionException {
		logger.debug("===========================================================================================");
		logger.debug("====================  Executing Full Requests  ============================================");
		logger.debug("===========================================================================================");

		// Let's create a dummy postInput parameter, with builders generated for each object by the plugin
		TopicPostInput topicInput = new TopicPostInput.Builder().withAuthorId("00000000-0000-0000-0000-000000000001")
				.withPubliclyAvailable(true).withDate(new GregorianCalendar(2019, 4 - 1, 30).getTime())
				.withTitle("a title").withContent("Some content").build();
		PostInput postInput = new PostInput.Builder().withFrom(new GregorianCalendar(2018, 3 - 1, 2).getTime())
				.withInput(topicInput).withTopicId("00000000-0000-0000-0000-000000000002").build();

		MutationType response = mutationExecutor.exec(boardsFullRequest, "postInput", postInput);
		Post createdPost = response.getCreatePost();
		logger.trace("Created post is {}", createdPost);
	}

	public void execRequestsWithFragments()
			throws GraphQLRequestExecutionException, GraphQLRequestPreparationException {
		logger.debug("===========================================================================================");
		logger.debug("====================  Executing Requests with Fragments ===================================");
		logger.debug("===========================================================================================");

		// A Partial Direct query with an inline fragment.
		List<Board> boards = queryExecutor
				.boards("{id name publiclyAvailable topics {... on Topic {id title date} nbPosts}}");
		logger.trace("Read boards are {}", boards);

		// A Partial Direct query with an inline fragment.
		QueryType response = queryExecutor.exec(""//
				+ "fragment topicFields on Topic {id title date} "
				+ "query{boards{id name publiclyAvailable topics {...topicFields nbPosts}}}");
		boards = response.getBoards();
		logger.trace("Read boards are {}", boards);

	}

}
