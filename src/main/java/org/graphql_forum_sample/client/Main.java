/**
 * 
 */
package org.graphql_forum_sample.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;

public class Main {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(GraphQLClient.class);

	public static void main(String[] args) throws GraphQLRequestPreparationException, GraphQLRequestExecutionException {
		GraphQLClient graphQLClient = new GraphQLClient();
		graphQLClient.execPartialRequests();
		graphQLClient.execFullRequests();
		graphQLClient.execRequestsWithFragments();
		logger.info("");
		logger.info("Normal end of execution");
	}

}
