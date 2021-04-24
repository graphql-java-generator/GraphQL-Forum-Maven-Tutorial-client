package org.graphql_forum_sample.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.graphql_java_generator.exception.GraphQLRequestExecutionException;

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
	PartialPreparedQueries partialPreparedQueries;

	@Override
	public void run(String... args) throws GraphQLRequestExecutionException {
		logger.debug("===========================================================================================");
		logger.debug("====================  Executing Partial Requests  =========================================");
		logger.debug("===========================================================================================");
		partialPreparedQueries.boards();

		logger.debug("===========================================================================================");
		logger.debug("====================  Executing Partial Requests, with parameters  ========================");
		logger.debug("===========================================================================================");
		partialPreparedQueries.topics("Board name 2");

		logger.info("Normal end of execution");
	}

}
