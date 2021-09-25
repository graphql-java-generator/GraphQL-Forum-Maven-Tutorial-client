/**
 * 
 */
package org.graphql_forum_sample.client;

import javax.annotation.PostConstruct;

import org.forum.client.Post;
import org.forum.client.PostInput;
import org.forum.client.util.GraphQLRequest;
import org.forum.client.util.MutationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;

/**
 * Demonstration class, for a GraphQL Spring bean that executes Full Prepared Request. This sample contains only one
 * mutation, but queries works the same.
 * 
 * @author etienne-sf
 */
@Component
public class FullPreparedQueries {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(FullPreparedQueries.class);

	@Autowired
	private MutationExecutor mutationExecutor;

	private GraphQLRequest createPostRequest;

	/**
	 * Thanks to the {@link PostConstruct} annotation, this method is called once all autowired field are set. It's a
	 * kind of constructor for Spring beans.
	 */
	@PostConstruct
	public void init() throws GraphQLRequestPreparationException {
		logger.info("Preparation for PartialPreparedQuery");

		// Preparation of the GraphQL Full requests, that will be used in the execFullRequests() method
		createPostRequest = mutationExecutor
				.getGraphQLRequest("mutation {createPost(post: &postInput) { id date author{id name} title content}}");
	}

	public Post createPost(PostInput postInput) throws GraphQLRequestExecutionException {
		return mutationExecutor.exec(createPostRequest, "postInput", postInput).getCreatePost();
	}
}
