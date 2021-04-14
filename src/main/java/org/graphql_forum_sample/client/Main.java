/**
 * 
 */
package org.graphql_forum_sample.client;

import org.forum.client.util.QueryTypeExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.graphql_java_generator.client.GraphQLConfiguration;

@SpringBootApplication(scanBasePackageClasses = { Main.class, GraphQLConfiguration.class, QueryTypeExecutor.class })
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

}
