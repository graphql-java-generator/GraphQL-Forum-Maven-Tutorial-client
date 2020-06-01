/**
 * 
 */
package org.graphql_forum_sample.client;

import com.graphql_java_generator.exception.GraphQLRequestExecutionException;
import com.graphql_java_generator.exception.GraphQLRequestPreparationException;

public class Main {

	public static void main(String[] args) throws GraphQLRequestPreparationException, GraphQLRequestExecutionException {
		GraphQLClient graphQLClient = new GraphQLClient();
		graphQLClient.execPartialRequests();
		graphQLClient.execFullRequests();
		graphQLClient.execRequestsWithFragments();
	}

}
