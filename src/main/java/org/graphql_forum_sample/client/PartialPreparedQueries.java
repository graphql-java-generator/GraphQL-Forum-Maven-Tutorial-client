/**
 * 
 */
package org.graphql_forum_sample.client;

import java.util.List;

import javax.annotation.PostConstruct;

import org.forum.client.Board;
import org.forum.client.util.GraphQLRequest;
import org.forum.client.util.QueryTypeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;

/**
 * @author etienne-sf
 */
@Component
public class PartialPreparedQueries {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(PartialPreparedQueries.class);

	/**
	 * The executor, that allows to execute GraphQL queries. The class name is the one defined in the GraphQL schema,
	 * with the suffix Executor.<BR/>
	 * It is automagically loaded by Spring, from the Beans it has discovered. An error is thrown if no matching bean is
	 * found, of if more than one matching bean is found
	 */
	@Autowired
	QueryTypeExecutor queryExecutor;

	// Partial requests
	GraphQLRequest boardsPartialRequest;

	@PostConstruct
	public void init() throws GraphQLRequestPreparationException {
		logger.info("Preparation for PartialPreparedQuery");

		// Preparation of the GraphQL Partial requests, that will be used in the execPartialRequests() method
		boardsPartialRequest = queryExecutor
				.getBoardsGraphQLRequest("{id name publiclyAvailable topics {id title date nbPosts}}");
	}

	public void execQueryBoards() throws GraphQLRequestExecutionException {
		List<Board> boards = queryExecutor.boards(boardsPartialRequest);
		logger.trace("Boards read: {}", boards);
	}

}
