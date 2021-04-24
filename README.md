

This Tutorial describes how-to create a GraphQL client application, with the [graphql-maven-plugin](https://github.com/graphql-java-generator/graphql-maven-plugin-project) and the [graphql Gradle plugin](https://github.com/graphql-java-generator/graphql-gradle-plugin-project).


The GraphQL plugin helps both on the server and on the client side. You'll find the tutorials for the server side on the [Maven server tutorial](https://github.com/graphql-java-generator/GraphQL-Forum-Maven-Tutorial-server) and on the [Gradle server tutorial](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server)


# Schema first

This plugin allows a schema first approach.

This approach is the best approach for APIs: it allows to precisely control the Interface Contract. This contract is the heart of all connected systems.

This tutorial won't describe how to create a GraphQL schema. There are plenty of resources on the net for that, starting with the [official GraphQL site](https://graphql.org/).

# The Forum GraphQL schema

This sample is based on the Forum GraphQL schema, [available here](https://github.com/graphql-java-generator/GraphQL-Forum-Maven-Tutorial-client/blob/master/src/main/resources/forum.graphqls)

This schema contains:

* A custom scalar definition: Date.
    * This allows to define new type to define objet's field. We'll have to provide it's implementation to read and write Date fields. 
* A schema object. This declaration is optional. It allows to define query/mutation/subscription specific names. This schema declares:
    * QueryType as a query.
    * MutationType as a mutation
    * SubscriptionType as a subscription
    * These types are declared below, as any regular object. Their definition is that of standard Object, but their meaning is very different. These fields are respectively the queries, mutations and subscriptions that you can execute, as a client GraphQL schema that connects to a GraphQL server that implements this schema.
* Four regular GraphQL objects: Member, Board, Topic, Post
    * These are the objects defined in the Object model that can queried (with queries or subscriptions), or inserted/updated (with mutations)
* One enumeration: MemberType
    * Enumeration are a kind of scalar, that allows only a defined list of values. MemberType is one of ADMIN, MODERATOR or STANDARD.
* Three input types: TopicPostInput, TopicInput and PostInput
    * These are objects that are not in the Object model. They may not be returned by queries, mutations or subscriptions. As their name means, they can only be used as field parameters. And regular objects maynot be use as field parameters.

This schema is stored in the _/src/main/resources/_ project folder for convenience. 

It could be also be used in another folder, like _/src/main/graphql/_ . In this case, the schema is not stored in the packaged jar (which is Ok for the Client mode), and you have to use the plugin _schemaFileFolder_ parameter, to indicate where to find this schema.

# The pom.xml and build.gradle files

As a Maven or a Gradle plugin, you have to add the plugin in the build:
* For Maven, you add it in the build section of your pom (here is the [full pom](https://github.com/graphql-java-generator/GraphQL-Forum-Maven-Tutorial-client/blob/master/pom.xml)):
* For Gradle, you declare the plugin, then configure it (here is the full [build.gradle](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-client/blob/master/build.gradle))
  

Let's first have a look at the Maven **pom.xml** file:

```XML
<?xml version="1.0"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd"
	xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.graphql-java-generator</groupId>
	<artifactId>GraphQL-Forum-Maven-Sample-client</artifactId>
	<version>1.0-SNAPSHOT</version>

	<properties>
		<graphql-maven-plugin.version>1.14.1</graphql-maven-plugin.version>
	</properties>
	
	<build>
...
			<plugin>
				<groupId>com.graphql-java-generator</groupId>
				<artifactId>graphql-maven-plugin</artifactId>
				<version>${graphql-maven-plugin.version}</version>
				<executions>
					<execution>
						<goals>
							<goal>generateClientCode</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<!-- Let's configure the GraphQL Gradle Plugin: -->
					<!-- All available parameters are described here: -->
					<!-- https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-maven-plugin/generateClientCode-mojo.html -->
					<packageName>org.forum.client</packageName>
					<customScalars>
						<customScalar>
							<graphQLTypeName>Date</graphQLTypeName>
							<javaType>java.util.Date</javaType>
							<graphQLScalarTypeStaticField>com.graphql_java_generator.customscalars.GraphQLScalarTypeDate.Date</graphQLScalarTypeStaticField>
						</customScalar>
					</customScalars>
					<!-- The parameters below change the 1.x default behavior. They are set to respect the behavior of the future 2.x versions -->
					<generateDeprecatedRequestResponse>false</generateDeprecatedRequestResponse>
					<separateUtilityClasses>true</separateUtilityClasses>
					<skipGenerationIfSchemaHasNotChanged>true</skipGenerationIfSchemaHasNotChanged>
				</configuration>
			</plugin>
...
		</plugins>
	</build>

	<dependencies>
		<!-- Dependencies for GraphQL -->
		<dependency>
			<groupId>com.graphql-java-generator</groupId>
			<artifactId>graphql-java-client-dependencies</artifactId>
			<type>pom</type>
			<version>${graphql-maven-plugin.version}</version>
		</dependency>
	</dependencies>

</project>
```

Then the Gradle **build.gradle** file:

```Groovy
plugins {
	id "com.graphql_java_generator.graphql-gradle-plugin" version "1.14.1"
	id 'java'
}

repositories {
	jcenter()
	mavenCentral()
}

dependencies {
	// The graphql-java-runtime module agregates all dependencies for the generated code, including the plugin runtime
	implementation "com.graphql-java-generator:graphql-java-runtime:1.14.1" // This MUST BE the same version as the graphql-gradle-plugin one
	implementation "org.apache.logging.log4j:log4j-slf4j-impl:2.12.1"
}

// The line below makes the GraphQL plugin be executed before Java compiles, so that all sources are generated on time
compileJava.dependsOn generateClientCode

// The line below adds the generated sources as a java source folder
sourceSets.main.java.srcDirs += '/build/generated/graphql-maven-plugin'

// Let's configure the GraphQL Gradle Plugin:
// All available parameters are described here: 
// https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-maven-plugin/generateClientCode-mojo.html
generateClientCodeConf {
	packageName = 'org.forum.client'
	customScalars = [ [
			graphQLTypeName: "Date",
			javaType: "java.util.Date",
			graphQLScalarTypeStaticField: "com.graphql_java_generator.customscalars.GraphQLScalarTypeDate.Date"
	] ]

	// The parameters below change the 1.x default behavior to respect the future 2.x behavior
	copyRuntimeSources = false
	generateDeprecatedRequestResponse = false
	separateUtilityClasses = true
}
```

The java version must be set to version 1.8 (or higher).

In this plugin declaration:
* (for Maven only) The plugin execution is mapped to its _generateClientCode_ goal
* The plugin generates the GraphQL code in the _packageName_ package (or in the _com.generated.graphql_ if this parameter is not defined)
* The _separateUtilityClasses_ set _true_ allows this separation:
    * All the classes generated directly from the GraphQL schema (object, enum, interfaces, input types...) are generated in _packageName_.
    * All the utility classes are generated in the sub-package _util_
    * This insures to have no collision between the GraphQL code and the GraphQL plugin's utility classes
    * If you set it to _false_, or don't define it, then all classes are generated in the _packageName_ package 
* And we declare the _Date_ scalar implementation. 
    * It is mandatory to give the implementation for each custom scalar defined in the GraphQL schema.
    * You'll find the relevant documentation on the [Plugin custom scalar doc page](https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/usage_customscalars)

The generated source is added to the IDE sources, thanks to:
* (for Maven) The _build-helper-maven-plugin_, so that the generated source is automatically added to the build path of your IDE.
* (for Gradle) The _sourceSets.main.java.srcDirs += ..._ line

The _graphql-java-runtime_ dependency add all necessary dependencies, for the generated code. Of course, its version must be the same as the plugin's version.


# A look at the generated code

Don't forget to execute (or re-execute) a full build when you change the plugin configuration, to renegerate the proper code:
* (For Maven) Execute _mvn clean compile_
* (for Gradle) Execute _gradlew clean build_

This will generate the client code in the _packageName_ package (or in the _com.generated.graphql_ if this parameter is not defined)

The code is generated in the :
* (for Maven) _/target/generated-sources/graphql-maven-plugin_ folder. And thanks to the _build-helper-maven-plugin_, it should automatically be added as a source folder to your favorite IDE.
* (for Gradle) _/build/generated-sources/generateClientCode_ folder. And thanks to the  _sourceSets.main.java.srcDirs += ..._ line in the _build.gradle_ file, it should automatically be added as a source folder to your favorite IDE.

Let's take a look at the generated code:
* The __org.forum.client__ package contains all classes that maps to the GraphQL schema:
    * The classes starting by '__' (two underscores) are the GraphQL introspection classes. These are standard GraphQL types. You can find more information on this on the [introspection page](https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/client_introspection)
    * All other classes are directly the items defined in the forum GraphQL schema, with their fields, getter and setter. All fields are annotated with the GraphQL information necessary on runtime, and the JSON annotations to allow the deserialization of the server response.
* The __org.forum.client.util__ package contains:
    * `CustomScalarXxx` classes are utility classes for custom scalars: a registry, and one JSON deserializer for each custom scalar defined in the GraphQL schema
    * `GraphQLRequest` : its a base element to execute GraphQL request (query, mutation or subscription). See below for the details.
    * `QueryTypeExecutor` , `MutationTypeExecutor` and `SubscriptionTypeExecutor` allow to execute the queries, mutations and subscriptions defined in the schema
    * `QueryType` , `MutationType` and `SubscriptionType` are deprecated and will be removed in 2.0 version
    * `XxxResponse` are deprecated class, that exist only for backward compatibility.
    * `XxxRootResponse` are the target for deserialization when executing a query or a mutation. 
* The _com.graphql_java_generator_ and its subpackages is the plugin's runtime. It's added to your project, so that your project has __no dependency__ from graphql-java-generator.
    * You can also set the _copyRuntimeSources_ plugin parameter to false, and add the com.graphql-java-generator:graphql-java-runtime dependency, with the exact same version as the plugin version. 

To sum up, you'll use:
* The `GraphQLRequest` to store a prepared request
* The `QueryTypeExecutor` , `MutationTypeExecutor` and `SubscriptionTypeExecutor` classes to prepare and execute GraphQL requests
* The POJOs in the _org.forum.client_ package to manipulate the data defined in the GraphQL schema 


# Let's structure our application

## Overview

Since the 1.12 version, the recommended way to use the generated client code, is to use it as a Spring Boot App. You'll find tons of documentation about Spring on the net. You can start by [their site](https://spring.io/) and the description of their [Core Framework](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html).

More precisely, this tutorial application uses Spring Boot. And you can find more information on that [on this page](https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html).

The main reason for that, is to benefit of:
* The IoC (Inversion of Control) and DI (Dependency Injection). This allows to declare the component each class needs. And let the Spring container initialize the relevant beans. 
* The default beans, and the capacity to override them, by declaring a new Spring Bean, and mark it with the `@Primary` annotation
* Use all the [Spring Security](https://spring.io/projects/spring-security) capability, including OAuth2, OpenID Connect, SAML... without updating your code! Just declare the relevant Spring beans.
* Use the [Spring Boot](https://spring.io/projects/spring-boot) autoconfiguration tools, thanks to the _application.properties_ or _application.yml_ file.

To demonstrate how to decouple the application, thanks to Spring, and to use GraphQL, based on the plugin's generated code, this tutorial structures the application with these components:
* The `Main` class contains the `main(...)` method. It is responsible to start the Spring container and to define where to load the Spring beans
* The _application.yml_ file contains the configuration properties for the application
* The `Application` class contains the executable part of the application. It can be merged with the `Main` class
* The `GraphQLXxx` classes are Spring beans, that demonstrates GraphQL capabilities. Spring loads them into the `Application` class, so that the functional code can use these components.

## The `Main` class


As all application, you need a java `Main` class. Its role is just to configure and start the Spring Container. So it's a pretty simple class:

```java
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
```

The _scanBasePackageClasses_ parameter indicates to Spring where to look for Spring beans. These beans will searched into all the packages of the given classes, and their subpackages. So, here, the Sprint beans will be searched in the GraphQL runtime (thanks to the `GraphQLConfiguration` class), in the generated code (thanks to the `QueryTypeExecutor` class) and in your code, that is: in the `Main`'s class package, and all its subpackages.

In other words: put your Main class at the root of your application's packages.

Please note that you can declare additional Spring Beans, here, with the `@Bean` annotation. More information on this in the [Spring Core Framework documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html).


## The _application.yml` file


The second, and very important, thing to do, is to configure the application. When the application starts, Spring Boot searches for an _application.properties_ or an _application.yml_ file on the classpath. It contains all the information that will be used to configure your application: the GraphQL endpoint, any security configuration (OAuth2...), any configuration that would be specific to your application...

You can put tons of configuration, here. 

To start with, you need no special information there. But, as we'll connect to a GraphQL server very soon, let's configure its URL:

```yaml
graphql:
  endpoint:
    url: http://localhost:8180/graphql

#We don't need the web server to start (it would start due to e the Spring reactive dependencies)
spring:
  main:
    web-application-type: none
```


## The `Application` class

This class is the main entry point of your code. It can be us

Alone, this class won't do anything. The idea, then is to create a class that Spring will instanciate and run. In order to do that, we'll use a Spring Bean of type [CommandLineRunner](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/CommandLineRunner.html)

This class will look like this:

```java
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/*
 * This class contains the functional code that is executed. It uses the GraphQLXxx Spring beans for that. It is started
 * by Spring, once the application context is initialized, as it implements {@link CommandLineRunner}
 */
@Component // This annotation marks this class as a Spring bean
public class Application implements CommandLineRunner {

	@Override
	public void run(String... args) throws Exception {
		// The classical minimum app ;)    (no GraphQL yet)
		System.out.println("Hello world");
	}

}
```

At this point, your "Hello World" applications works. So you can start your application:
* Java will execute the `Main.main()` method
* It will start the Spring container
* Spring loads the `Application` Spring bean, as it is marked by the `@Component` annotation. 
* As it's a `CommandLineRunner`, Spring executes its `run()` method, and wait for it to finish.
* Then all the job is done, and Spring returns: the application quits.


# Executing GraphQL request: two choices to do

So, we can now do some more useful treatment in our code. We'll make this `run()` method calls other Spring Beans, that will execute GraphQL requests.

## GraphQL choice 1: Partial or Full requests 

These are concepts proper to the plugin. You'll find more info about Full and Partial request on [this plugins's doc page](https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/client_exec_graphql_requests).

So let's explain that:
* A __Partial__ request is the execution of only one query or mutation at a time. It's easier to use, as the generated method directly returns the POJO instance for the returned type, or throws a [GraphQLRequestExecutionException](https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-java-runtime/apidocs/com/graphql_java_generator/exception/GraphQLRequestExecutionException.html) when an error occurs.
    * The query/mutation/subscription parameters are java parameters of the generated java method
    * You provide only the expected GraphQL response (see below for a sample). For instance to get back from the server the _id_ and _name_ fields, you provide this request string: _"{id name}"_
    * You can use GraphQL directives only on the fields returned by the server (use full requests if you need directives on the query/mutation/subscription itself)
    * You can use GraphQL inline fragments (use full requests if you need named fragments)
    * The execXxx methods of the query/mutation executor return directly the result of the requests (for instance a `List<Board>` for _boards_ request of the forum sample)
    * The execXxx methods of the subscription executor return a [SubscriptionClient](https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-java-runtime/apidocs/com/graphql_java_generator/client/SubscriptionClient.html), that allows to latter unsubscribe from the subscription.
        * For subscription, you provide a callback class, that will receive each notification for this subscription. This callback is an instance of `[SubscriptionCallback](https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-java-runtime/apidocs/com/graphql_java_generator/client/SubscriptionCallback.html)`
* A __Full__ request is the execution of a standard GraphQL request, as you would write it in graphiql for instance. This allows to execute several requests at a time, or to use GraphQL variables.
    * Full requests allows to execute several queries or mutations at a time, provided that these are different fields in the query or mutation
    * Currently, the plugin doesn't manage aliases: you can execute several _different_ queries or mutations at at time. But to call several times the same query or mutation (with different parameters for instance), you'll need to execute several requests.
    * Queries, mutations and subscriptions can be executed with a Full Request
    * Full requests allows to use GraphQL named fragments, and GraphQL directives on the query/mutation/subscription and/or their parameters
    * Full requests allows to use GraphQL variables
    * The execXxx methods of the query/mutation executor return an instance of the query/mutation GraphQL object. It's up to you to call the relevant getter to retrieve the server's response
    * The execSubscription methods return a [SubscriptionClient](https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-java-runtime/apidocs/com/graphql_java_generator/client/SubscriptionClient.html), that allows to latter unsubscribe from the subscription.
        * When calling the execSubscription, you provide a callback that will receive the notifications. This callback must implement `[SubscriptionCallback](https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-java-runtime/apidocs/com/graphql_java_generator/client/SubscriptionCallback.html)`

## GraphQL choice 2: Direct or Prepared requests 

* A __Prepared__ request is a request that is prepared at startup time, and reused at each execution.
    * For internal reason, including bind parameter management, and proper deserialization of interfaces and unions, the plugin needs to parse the GraphQL request. Using Prepared request allows to parse it only once, at preparation time. So it's faster when executing the same request several times.
    * This also allows to check the GraphQL syntax when the request is prepared, typically when the application starts: the earlier you known there is an issue, the best it is to manage it.
* A __Direct__ request is when you provide the GraphQL query string at execution time.
    * It's easier, as you don't have to call a first preparation method, then store its result, before executing the request
    * It's less efficient, as the request preparation is a little overhead at each execution
    * It's less secure as, as you may discover at execution time, that there is a syntax error in your query string



# Sample of a Partial Query/Mutation

GraphQL queries and mutations are executed in exactly the same way.

The easiest way is to execute Partial Request. And the most effective is that this request is prepared first.


The code below executes the boards query, as defined in this extract of the GraphQL schema:

```graphql
type QueryType {
    boards: [Board]
[...]
}
```

There are plenty of ways to develop an application. In this tutorial, we separate the GraphQL query execution, from the rest of the code, by placing all the GraphQL stuff in separate classes. Of course, you can find it irrelevant for your use cases, and do it another way.

So, let's create a `PartialPreparedQueries` that is responsible for preparing, then expose the methods to execute some GraphQL queries.

```Java
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

	// Prepared partial requests
	GraphQLRequest boardsRequest;

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
	}

	public void boards() throws GraphQLRequestExecutionException {
		List<Board> boards = queryExecutor.boards(boardsRequest);
		logger.trace("Boards read: {}", boards);
	}
}
```

This class is a Spring bean. It's loaded like other Spring beans: with the `@Autowired` annotation (see also the Spring doc for other such annotations).

You can use it in your `Application` class like this:

```java
@Component
public class Application implements CommandLineRunner {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(Application.class);

	@Autowired
	PartialPreparedQueries partialPreparedQueries;

	@Override
	public void run(String... args) throws GraphQLRequestExecutionException {

		...

		partialPreparedQueries.boards();

		...

	}
}
```


And you're done:
* The `PartialPreparedQueries` Spring bean is loaded by Spring. Spring calls its `init()` method once all autowired fields are set, as it is marked by the `@PostConstruct` constructor prepares the request(s)
* The _exec()_ method executes the query 

Of course, in a real application case, you would prepare more requests

The execution of a __Mutation__ works in exactly the same way.


# Execution of a Query/Mutation with parameters

The query/mutation parameters are the parameters that are defined on the field of the query or mutation, in the GraphQL schema.

Let's execute the topics query, that is defined like this:

```
type QueryType {
[...]
    topics(boardName: String!): [Topic]!
[...]
}
```

This query is used this way:

```Java
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

...

	/** Prepared partial requests, with parameters */
	GraphQLRequest topicsRequest;

	@PostConstruct
	public void init() throws GraphQLRequestPreparationException {
		logger.info("Preparation for PartialPreparedQuery");

...

		// Preparation of the GraphQL Partial requests, that will be used in the execPartialRequests() method
		topicsRequest = queryExecutor.getTopicsGraphQLRequest("{id date author {id name} nbPosts title content}");
	}

...

	/** The topics query has one parameter: the board name */
	public void topics(String aBoardName) throws GraphQLRequestExecutionException {
		List<Topic> topics = queryExecutor.topics(topicsRequest, aBoardName);
		logger.trace("Topics read: {}", topics);
	}
}
```

The only change is that all the query parameters are parameters of the `queryExecutor.topics(..)` method. The `topics(..)` method takes care of serializing and sending the board name parameter to the GraphQL server.

# Execution of a Query/Mutation with bind parameters

The _posts_ field of the _Topic_ object accepts three parameters:

```
type Topic {
    id: ID!
    date: Date!
    author: Member!
    publiclyAvailable: Boolean
    nbPosts: Int
    title: String!
    content: String
    posts(memberId: ID, memberName: String, since: Date!): [Post]!
}
```

So we can update the previous sample, by querying the topic's posts. We'll define these bind parameters:
* memberId. It's defined as optional, as it is prefixed by _?_
* memberName. Also optional.
* since. This parameter is mandatory, as it is prefixed by _&_

You can define bind parameters as being optional or mandatory without enforcing what's optional or mandatory in the GraphQL schema :  
* You can define a GraphQL optional parameter as mandatory in your use case. Just prefi the bind parameter by a _&_
* Of course, GraphQL mandatory parameter should be mandatory bind parameters

Here is the code:

```Java
@Component
public class PartialPreparedQueries {

	static protected Logger logger = LoggerFactory.getLogger(PartialPreparedQueries.class);

	@Autowired
	QueryTypeExecutor queryExecutor;

	...


	GraphQLRequest topicsAndPostsRequest;

	@PostConstruct
	public void init() throws GraphQLRequestPreparationException {

		...
		
		topicsAndPostsRequest = queryExecutor.getTopicsGraphQLRequest(""//
				+ "{" //
				+ "  id date author {id name} nbPosts title content "//
				+ "  posts(memberId: ?memberId, memberName: ?memberName, since: &since)  {id date title}"//
				+ "}");
	}

...

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
```

If a parameter value is null, it won't be sent to the server. It's correct as this parameter is optional in both the bind parameter definition in the query (it starts by a _?_ ) and the GraphQL schema.

Please note that:
* If a bind parameter is set for a GraphQL array/list, you'll have to provide a java.util.List<YourObject> instance, where YourObject is the type defined in the GraphQL schema.  
* The _since_ parameter is a custom scalar of type _Date_ . In the pom.xml or the build.gradle file, the custom scalar is declared as being a _java.util.Date_ so the value in your code is a standard java.util.Date. The custom scalar implementation provided in the pom.xml or the build.gradle file takes care of properly format the code (when executing the request) and read the value (when reading the server response). More information on that in the [custom scalar plugin's doc page](https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/usage_customscalars).


# Full requests

The above samples are all _Partial_ requests.

The GraphQL Maven and Gradle plugin also manage _Full_ requests (only for query and mutation, not for subscription).

A full request allows to:
* Execute several queries into one call toward the server  
* Add directives to the query/mutation itself
* Use GraphQL global fragments into your query (inline fragment are usable with partial requests as well)

The main difference between _Partial_ and _Full_ requests, is that the method that executes a full request returns an instance of the QueryType or MutationType, as defined the query or mutation is defined in the GraphQL schema. This means:
* That you can have the response for several queries or mutations in one call
    * As alias are not managed yet, you can execute several different queries or several different mutations in a call. But you can not execute several times the same query or mutation in one call.
* You need to call the relevant getter to retrieve the result for each query or mutation that you have executed

Here is a sample:

```Java
public class GraphQLClient {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(GraphQLClient.class);

	MutationTypeExecutor mutationExecutor;
	GraphQLRequest boardsFullRequest;

	/**
	 * This constructor prepares the GraphQL requests, so that they can be used by the {@link #execPartialRequests()}
	 * method
	 */
	public GraphQLClient() throws GraphQLRequestPreparationException {

		// Creation of the query executor, for this GraphQL endpoint
		logger.info("Connecting to GraphQL endpoint");
		mutationExecutor = new MutationTypeExecutor("http://localhost:8180/graphql");

		// Preparation of the GraphQL Full requests, that will be used in the execFullRequests() method
		boardsFullRequest = mutationExecutor
				.getGraphQLRequest("mutation {createPost(post: &postInput) { id date author{id name} title content}}");
	}

	public void execFullRequests() throws GraphQLRequestExecutionException {
		// Let's create a dummy postInput parameter, with builders generated for each object by the plugin
		TopicPostInput topicInput = new TopicPostInput.Builder().withAuthorId("00000000-0000-0000-0000-000000000001")
				.withPubliclyAvailable(true).withDate(new GregorianCalendar(2019, 4 - 1, 30).getTime())
				.withTitle("a title").withContent("Some content").build();
		PostInput postInput = new PostInput.Builder().withFrom(new GregorianCalendar(2018, 3 - 1, 2).getTime())
				.withInput(topicInput).withTopicId("00000000-0000-0000-0000-000000000002").build();

		MutationType response = mutationExecutor.exec(boardsFullRequest, "postInput", postInput);
		Post createdPost = response.getCreatePost();

		... Do something with createdPost
	}
}
```

In this sample, there is one bind parameter, which is the mutation parameter. You can note that it's a GraphQL input type.
  



# Fragments

You can use fragments in your queries, mutations or subscriptions:
* __Inline fragments__ work with both Partial and Full requests
* __Named fragments__ work only with Full request, as you declare these fragments at the root of the requests. So, named fragments work only with queries and mutations.
    * This limitation will be overcome in the future

Below is a sample of a partial direct query with an inline fragment:

```Java
	List<Board> boards = queryExecutor
				.boards("{id name publiclyAvailable topics {... on Topic {id title date} nbPosts}}");
	
	... Do something with boards
```

Below is another sample, with a full direct query with a named fragment:

```Java
import org.forum.client.Board;
import org.forum.client.QueryType;

	QueryType response = queryExecutor.exec(
			"fragment topicFields on Topic {id title date} " +
			"query{boards{id name publiclyAvailable topics {...topicFields nbPosts}}}");
	List<Board> boards = response.getBoards();
	
	... Do something with boards
```


# Subscriptions

The Subscriptions works in the same way. 

You'll find the relevant document on the [GraphQL plugin's wiki](https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/client_subscription) 
