/**
 * 
 */
package org.graphql_forum_sample.client;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.forum.client.Board;
import org.forum.client.Topic;
import org.forum.client.util.GraphQLRequest;
import org.forum.client.util.QueryExecutor;
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
	private QueryExecutor queryExecutor;

	/** Prepared partial requests */
	private GraphQLRequest boardsRequest;

	/** Prepared partial requests, with query parameters */
	private GraphQLRequest topicsRequest;

	/** Prepared partial requests, with both query parameters and bind variables */
	private GraphQLRequest topicsAndPostsRequest;

	/**
	 * Thanks to the {@link PostConstruct} annotation, this method is called once all autowired field are set. It's a
	 * kind of constructor for Spring beans.
	 */
	@PostConstruct
	public void init() throws GraphQLRequestPreparationException {
		logger.info("Preparation for PartialPreparedQuery");

		// Preparation of the GraphQL Partial requests, that will be used in the execPartialRequests() method
		boardsRequest = queryExecutor
				.getBoardsGraphQLRequest("{id name publiclyAvailable topics {id title date nbPosts}}");

		// Preparation of the GraphQL Partial requests, that will be used in the execPartialRequests() method
		topicsRequest = queryExecutor.getTopicsGraphQLRequest("{id date author {id name} nbPosts title content}");

		topicsAndPostsRequest = queryExecutor.getTopicsGraphQLRequest(""//
				+ "{" //
				+ "  id date author {id name} nbPosts title content "//
				+ "  posts(memberId: ?memberId, memberName: ?memberName, since: &since)  {id date title}"//
				+ "}");
	}

	public List<Board> boards() throws GraphQLRequestExecutionException {
		return queryExecutor.boards(boardsRequest);
	}

	/** The topics query has one parameter: the board name */
	public List<Topic> topics(String aBoardName) throws GraphQLRequestExecutionException {
		return queryExecutor.topics(topicsRequest, aBoardName);
	}

	/** The topics query has one parameter: the board name. And the prepared request has three bind parameters */
	public List<Topic> topicsAndPostsRequest(String aBoardName, String memberId, String memberName, Date since)
			throws GraphQLRequestExecutionException {
		return queryExecutor.topics(topicsAndPostsRequest, //
				aBoardName, // This the query parameter. Depending on the GraphQL schema, there could be others
				"memberId", memberId, //
				"memberName", memberName, //
				"since", since);
	}
}
