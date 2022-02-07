/**
 * 
 */
package org.graphql_forum_sample.client;

import org.forum.client.util.QueryExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.graphql_java_generator.client.GraphQLConfiguration;
import com.graphql_java_generator.client.graphqlrepository.EnableGraphQLRepositories;

/**
 * This simple class is the minimal code to start a Spring application
 * 
 * @author etienne-sf
 *
 */
@SpringBootApplication(scanBasePackageClasses = { Main.class, GraphQLConfiguration.class, QueryExecutor.class })
@EnableGraphQLRepositories({ "org.graphql_forum_sample.client" })
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

}
