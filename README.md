# A tutorial for the GraphQL Maven plugin (client side)


This Tutorial describes how-to create a GraphQL client application, with the [graphql-maven-plugin](https://github.com/graphql-java-generator/graphql-maven-plugin-project) and the [graphql Gradle plugin](https://github.com/graphql-java-generator/graphql-gradle-plugin-project).


The GraphQL plugin helps both on the server and on the client side. You'll find the tutorials for the server side on the [Maven server tutorial](https://github.com/graphql-java-generator/GraphQL-Forum-Maven-Tutorial-server) and on the [Gradle server tutorial](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server)


## A note about non-spring applications

This samples uses Spring to wire the GraphQL Repositories in the application.

GraphQL Repositories also work with non-spring app. You'll find the needed info in the [client_graphql_repository wiki's page](https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/client_graphql_repository).


## Schema first

This plugin allows a schema first approach.

This approach is the best approach for APIs: it allows to precisely control the Interface Contract. This contract is the heart of all connected systems.

This tutorial won't describe how to create a GraphQL schema. There are plenty of resources on the net for that, starting with the [official GraphQL site](https://graphql.org/).

## The Forum GraphQL schema

This sample is based on the Forum schema, [available here](https://github.com/graphql-java-generator/GraphQL-Forum-Maven-Tutorial-client/blob/master/src/main/resources/forum.graphqls)

This schema contains:

* A custom scalar definition: Date.
    * This allows to define new type to define objet's field. We'll have to provide it's implementation to read and write Date fields. 
* A schema object. This declaration is optional. It allows to define query/mutation/subscription specific names. This schema declares:
    * Query as a query.
    * Mutation as a mutation
    * Subscription as a subscription
    * These types are declared below, as any regular object. Their definition is that of standard Object, but their meaning is very different. These fields are respectively the queries, mutations and subscriptions that you can execute, as a client GraphQL schema that connects to a GraphQL server that implements this schema.
* Four regular GraphQL objects: Member, Board, Topic, Post
    * These are the objects defined in the Object model that can queried (with queries or subscriptions), or inserted/updated (with mutations)
* One enumeration: MemberType
    * Enumeration are a kind of scalar, that allows only a defined list of values. MemberType is one of ADMIN, MODERATOR or STANDARD.
* Three input types: TopicPostInput, TopicInput and PostInput
    * These are objects that are not in the Object model. They may not be returned by queries, mutations or subscriptions. As their name means, they can only be used as field parameters. And regular objects maynot be use as field parameters.

This schema is stored in the _/src/main/resources/_ project folder for convenience. 

It could be also be used in another folder, like _/src/main/graphql/_ . In this case, the schema is not stored in the packaged jar (which is Ok for the Client mode), and you have to use the plugin _schemaFileFolder_ parameter, to indicate where to find this schema.

## The pom.xml and build.gradle files

As a Maven or a Gradle plugin, you have to add the plugin in the build:
* For Maven, you add it in the build section of your pom (here is the [full pom](https://github.com/graphql-java-generator/GraphQL-Forum-Maven-Sample/blob/master/GraphQL-Forum-Maven-Sample-client/pom.xml)):
* For Gradle, you declare the plugin, then configure it (here is the full [build.gradle](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-client/blob/master/build.gradle))
  

Let's first have a look at the Maven **pom.xml** file:

```XML

	<properties>
		<graphql-maven-plugin.version>1.18.6</graphql-maven-plugin.version>
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
					<packageName>org.forum.client</packageName>
					<customScalars>
						<customScalar>
							<graphQLTypeName>Date</graphQLTypeName>
							<javaType>java.util.Date</javaType>
							<graphQLScalarTypeStaticField>com.graphql_java_generator.customscalars.GraphQLScalarTypeDate.Date</graphQLScalarTypeStaticField>
						</customScalar>
					</customScalars>
					<!-- The parameters below change the 1.x default behavior to respect the future 2.x behavior -->
					<copyRuntimeSources>false</copyRuntimeSources>
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
			<artifactId>graphql-java-runtime</artifactId>
			<version>${graphql-maven-plugin.version}</version>
		</dependency>
	</dependencies>
```


Define once the plugin version in the **build.properties** file:

```Groovy
graphQLPluginVersion = 1.18.6
```

Then use this version in the **build.gradle** file:

```Groovy
plugins {
	id "com.graphql_java_generator.graphql-gradle-plugin" version "${graphQLPluginVersion}"
	id 'java'
	id "org.springframework.boot" version "2.4.4"
}

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	// THE VERSION MUST BE THE SAME AS THE PLUGIN's ONE
	implementation "com.graphql-java-generator:graphql-java-client-runtime:${graphQLPluginVersion}"
}

// The line below adds the generated sources as a java source folder in the IDE
sourceSets.main.java.srcDirs += '/build/generated/sources/graphqlGradlePlugin'
sourceSets.main.java.srcDirs += '/build/generated/resources/graphqlGradlePlugin'


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

	// The parameters below change the 1.x default behavior. They are set to respect the default behavior of the future 2.x versions
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
    * You'll find the relevant documentation on the [Plugin custom scalar doc page](https://graphql-maven-plugin-project.graphql-java-generator.com/customscalars.html)  

The generated source is added to the IDE sources, thanks to:
* (for Maven) The _build-helper-maven-plugin_, so that the generated source is automatically added to the build path of your IDE.
* (for Gradle) The _sourceSets.main.java.srcDirs += ..._ line

The _graphql-java-runtime_ dependency add all necessary dependencies, for the generated code. Of course, its version must be the same as the plugin's version.


## A look at the generated code

Don't forget to execute (or re-execute) a full build when you change the plugin configuration, to renegerate the proper code:
* (For Maven) Execute _mvn clean compile_
* (for Gradle) Execute _gradlew clean build_

If you're using eclipse (or other IDE), it may happen (especially with the Gradle plugin, that is less integrated), that you'll have to "Refresh the Gradle plugin" or the "Maven/Update Project...", to make your IDE properly see the generated code.

This will generate the client code in the _packageName_ package (or in the _com.generated.graphql_ if this parameter is not defined)

The code is generated in the :
* (for Maven) _/target/generated-sources/graphql-maven-plugin_ folder. And thanks to the _build-helper-maven-plugin_, it should automatically be added as a source folder to your favorite IDE.
* (for Gradle) _/build/generated/sources/graphqlGradlePlugin_ folder. And thanks to the  _sourceSets.main.java.srcDirs += ..._ line in the _build.gradle_ file, it should automatically be added as a source folder to your favorite IDE.

Let's take a look at the generated code:
* The __org.forum.client__ package contains all classes that maps to the GraphQL schema:
    * The classes starting by '__' (two underscores) are the GraphQL introspection classes. These are standard GraphQL types.
    * All other classes are directly the items defined in the forum GraphQL schema, with their fields, getter and setter. All fields are annotated with the GraphQL information necessary on runtime, and the JSON annotations to allow the deserialization of the server response.
* The __org.forum.client.util__ package contains:
    * _CustomScalarXxx_ classes are utility classes for custom scalars: a registry, and one JSON deserializer for each custom scalar defined in the GraphQL schema
    * _GraphQLRequest_ : its a base element to execute GraphQL request (query, mutation or subscription). See below for the details.
    * _QueryExecutor_ , _MutationExecutor_ and _SubscriptionExecutor_ allow to execute the queries, mutations and subscriptions defined in the schema
    * _Query_ , _Mutation_ and _Subscription_ are deprecated and will be removed in 2.0 version
    * _XxxResponse_ are deprecated class, that exist only for backward compatibility.
    * _XxxRootResponse_ are the target for deserialization when executing a query or a mutation. 
* The _com.graphql_java_generator_ and its subpackages is the plugin's runtime. It's added to your project, so that your project has __no dependency__ from graphql-java-generator.
    * You can also set the _copyRuntimeSources_ plugin parameter to false, and add the com.graphql-java-generator:graphql-java-runtime dependency, with the exact same version as the plugin version. 

To sum up, you'll use:
* The _GraphQLRequest_ to store a prepared request
* The _Query_ , _Mutation_ and _Subscription_ classes to prepare and execute GraphQL requests
* The POJOs in the _org.forum.client_ package to manipulate the data defined in the GraphQL schema 

## Choice 1: Partial or Full requests 

These are concepts proper to the plugin. You'll find more info about Full and Partial request on [this plugins's doc page](https://graphql-maven-plugin-project.graphql-java-generator.com/client.html).

So let's explain that:
* A __Partial__ request is the execution of only one query or mutation at a time. It's easier to use, as the generated method directly returns the POJO instance for the returned type, or throws a [GraphQLRequestExecutionException](https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-java-runtime/apidocs/com/graphql_java_generator/exception/GraphQLRequestExecutionException.html) when an error occurs.
    * The query/mutation/subscription parameters are parameter of the generated java method
    * You provide only the expected response (see below for a sample). For instance to get back from the server the _id_ and _name_ fields, you provide this request string: _"{id name}"_
    * You can use directives only on the fields returned by the server (use full requests if you need others)
    * You can use inline fragments (use full requests if you need named fragments)
    * The execXxx methods of the query/mutation executor returns directly the result of the requests (for instance a _List<Board>_ for _boards_ request of the forum sample)
    * The execXxx methods of the subscription returns a [SubscriptionClient](https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-java-runtime/apidocs/com/graphql_java_generator/client/SubscriptionClient.html), that allows to latter unsubscribe from the subscription.    
* A __Full__ request is the execution of full GraphQL request. This allows to execute several requests at a time.
    * Currently, the plugin doesn't manage aliases. So you can execute several _different_ queries or mutations at at time. But to call several times the same query or mutation (with different parameters for instance), you'll need to execute several requests.
    * Subscription can't be executed by a Full Request, as a callback class must be provided for each subscription.
    * Full requests allows to use named fragments, and directive on the query/mutation/subscription and/or their parameters
    * The execXxx methods of the query/mutation executor returns an instance of the query/mutation GraphQL object. It's up to you to call the relevant getter to retrieve the server's response

## Choice 2: Direct or Prepared requests 

* A __Prepared__ request is a request that is prepared at startup time, and reused at each execution.
    * For internal reason, including bind parameter management, and proper deserialization of interfaces and unions, the plugin needs to parse the GraphQL request. Using Prepared request allows to parse it only once, at preparation time, so it's faster when executing the request.
    * This also allows to check the GraphQL syntax when the request is prepared, typically when the application starts: the earlier you known there is an issue, the best it is to manage it.
* A __Direct__ request is a when you provide the GraphQL query string at execution time.
    * It's easier, as you don't have to call a first preparation method, then store its result, before executing the request
    * It's less efficient, as the request preparation is a little overhead at each execution
    * It's less secure as, as you may discover at execution time, that there is a syntax error in your query string 


## Use of GraphQL Repositories

The plugin helps to define GraphQL requests with __GraphQL Repositories__, that are very alike Spring Repositories.

__GraphQL Repositories__ allows you to define GraphQL requests in a Java interface, without writing any Java code. All the necessary code is executed behind the scene. You'll find all the details in the [client_graphql_repository wiki's page](https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/client_graphql_repository).

They allow:
* The use of Partial and Full requests
* The definition of GraphQL queries, mutations and subscriptions
* Only prepared requests (which is better for performance and safety, as request preparation is done when the application starts)

You'll find the most representative samples in this tutorial.


## Enabling GraphQL Repositories

For use of GraphQL Repositories in non Spring app, please read the [client_graphql_repository wiki's page](https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/client_graphql_repository).

When you create GraphQL Repositories in Spring app, you must declare to the Spring container, where to find the GraphQL Repositories you declared.

This is done through the use of the `@EnableGraphQLRepositories` annotation, on a Spring configuration class. For instance, in a Spring Boot application, it can be added to the main class, like this:

```java
@SpringBootApplication(scanBasePackageClasses = { Main.class, GraphQLConfiguration.class, QueryExecutor.class })
@EnableGraphQLRepositories({ "org.graphql_forum_sample.client" })
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}
}
```

In this sample, the GraphQL Repositories are searched in the `org.graphql_forum_sample.client` package. All sub-packages are searched. You can provide a list of more than one packages, if necessary.


## Simple Partial Query/Mutation

GraphQL queries and mutations are executed in exactly the same way.

The easiest way is to execute Partial Request. And the most effective is that this request is prepared first.


The code below executes the boards query, as defined in this extract of the GraphQL schema:

```
type Query {
    boards: [Board]
[...]
}
```

Let's define a GraphQL Repository in a Java interface, like this:

```Java
@GraphQLRepository
public interface MyGraphQLRepository {

	...   You can defined as many method as you wish in a GraphQL Repository

	/**
	 * Execution of the boards query, with this GraphQL query: {id name publiclyAvailable topics {id title date
	 * nbPosts}}
	 * 
	 * @return The GraphQL server's response, mapped into the POJO generated from the GraphQL schema
	 * @throws GraphQLRequestExecutionException
	 *             Whenever an exception occurs during request execution
	 */
	@PartialRequest(request = "{id name publiclyAvailable topics {id title date nbPosts}}")
	public List<Board> boards() throws GraphQLRequestExecutionException;
}
```

Then, in your code, just declare the use of the GraphQL Repository in order to use it:

```Java
@Component // This annotation marks this class as a Spring bean (prerequisite to make @Autowire annotation work)
public class Application implements CommandLineRunner {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(Application.class);

	@Autowired
	private MyGraphQLRepository myGraphQLRepository;

	@Override
	public void myMethod() throws GraphQLRequestExecutionException {
		logger.info("Boards read: {}", boards);
		
		...
		then do something smart with this result
		...
	}
}
```

And you're done:
* The `@GraphQLRepository` annotation marks the `MyGraphQLRepository` interface being a GraphQL Repository. Each declared method:
    * Must be marked by one of the `@PartialRequest` or  `@FullRequest` annotations
    * The request's parameter of the annotation is mandatory, and allows you to define the GraphQL query
    * All details (and more samples) can be found in the [client_graphql_repository wiki's page](https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/client_graphql_repository).
* Calling any of method of the GraphQL Repository executes the associated request against the GraphQL server.

Execution of __Mutation__ works in exactly the same way. Execution of __Subscription__ is slightly different, and again, you'll find the needed information in the [client_graphql_repository wiki's page](https://github.com/graphql-java-generator/graphql-maven-plugin-project/wiki/client_graphql_repository).


## Execution of a Query/Mutation with parameters

The query/mutation parameters are the parameters that are defined on the field of the query or mutation, in the GraphQL schema.

Let's execute the topics query, that is defined like this:

```
type Query {
[...]
    topics(boardName: String!): [Topic]!
[...]
}
```

The GraphQL Repository contains this additional method:

```java
@GraphQLRepository
public interface MyGraphQLRepository {

	...   You can defined as many method as you wish in a GraphQL Repository

	/**
	 * Execution of the topics query, which has one parameter defined in the GraphQL schema: boardName, that is a
	 * String. If this query had more then one parameter, all its parameters should be parameters of this method, in the
	 * same order a defined in the GraphQL schema, and with the relevant Java type.
	 * 
	 * @param aBoardName
	 * @return
	 * @throws GraphQLRequestExecutionException
	 */
	@PartialRequest(request = "{id date author {id name} nbPosts title content}")
	public List<Topic> topics(String aBoardName) throws GraphQLRequestExecutionException;
}
```

This query is used this way:

```Java
@Component // This annotation marks this class as a Spring bean (prerequisite to make @Autowire annotation work)
public class Application implements CommandLineRunner {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(Application.class);

	@Autowired
	private MyGraphQLRepository myGraphQLRepository;

	@Override
	public void myMethod() throws GraphQLRequestExecutionException {
		List<Topic> topics = myGraphQLRepository.topics("Board name 2");
		
		...
		then do something smart with this result
		...
	}
}
```

The only change is that all the query parameters are parameter of the _queryExecutor.topics(..)_ method. The _topics(..)_ method take care of serializing and sending the board name parameter to the GraphQL server. Then it reads the response, and maps it into the `List<Topic>` response type.

## Execution of a Query/Mutation with bind parameters


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
* memberName. Also optional (prefixed by _?_)
* since. This parameter is mandatory, as it is prefixed by _&_

Of course, mandatory parameter must be given at execution time, whereas optional parameter my remain null.

The GraphQL repository is defined this way:

```java
@GraphQLRepository
public interface MyGraphQLRepository {

	...   You can defined as many method as you wish in a GraphQL Repository

	/**
	 * 
	 * @param boardName
	 *            The parameter of the topics query, as defined in the GraphQL schema
	 * @param memberId
	 *            The bind parameter 'memberId', as defined in this query. It's an optional parameter, as this parameter
	 *            is marked by a '?' character in this query. That is: the provided value may be null, at execution
	 *            time.
	 * @param memberName
	 *            The bind parameter 'memberName', as defined in this query. It's an optional parameter, as this
	 *            parameter is marked by a '?' character in this query. That is: the provided value may be null, at
	 *            execution time.
	 * @param since
	 *            The bind parameter 'since', as defined in this query. It's a mandatory parameter, as this parameter is
	 *            marked by a '&' character in this query. That is: the provided value may NOT be null, at execution
	 *            time.
	 * @throws GraphQLRequestExecutionException
	 *             Whenever an exception occurs during request execution
	 * @return
	 */
	@PartialRequest(requestName = "topics", // The requestName defines the GraphQL defined query. It is mandatory here,
											// as the method has a different name than the query's name in the GraphQL schema
			request = "{id date author {id name} nbPosts title content posts(memberId: ?memberId, memberName: ?memberName, since: &since)  {id date title}}")
	public List<Topic> topicsSince(String boardName, 
			@BindParameter(name = "memberId") String memberId,
			@BindParameter(name = "memberName") String memberName, 
			@BindParameter(name = "since") Date since) throws GraphQLRequestExecutionException;
}
```


Here is the code that calls the GraphQL repository:  

```Java
@Component // This annotation marks this class as a Spring bean (prerequisite to make @Autowire annotation work)
public class Application implements CommandLineRunner {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(Application.class);

	@Autowired
	private MyGraphQLRepository myGraphQLRepository;

	@Override
	public void run(String... args) throws GraphQLRequestExecutionException {
		String memberId = null; // may be null, as it's optional
		String memberName = null; // may be null, as it's optional
		Date since = new Calendar.Builder().setDate(2022, 02 - 1 /* february */, 01).build().getTime();
		
		// Let's call the GraphQL server
		List<Topic> topicsSince = myGraphQLRepository.topicsSince("Board name 2", memberId, memberName, since);
		
		... and do something with the Query's result (topicsSince)
	}
}
```

As there is no provided value for the _memberName_ bind parameter, this parameter is not sent to the server. It's correct as this parameter is optional in both the bind parameter definition in the query (it starts by a _?_ ) and the GraphQL schema.

Please note that:
* If a bind parameter is set for a GraphQL array, the expected type is a java.util.List<YourObject> instance, where YourObject is the type defined in the GraphQL schema (or null, if you don't provide a value for an optional parameter)
* The _since_ parameter is a custom scalar of type _Date_ . In the pom.xml or the build.gradle file, the custom scalar is declared as being a _java.util.Date_ so the value in your code is a standard java.util.Date. The custom scalar implementation provided in the pom.xml or the build.gradle file takes care of properly format the code (when executing the request) and read the value (when reading the server response). More information on that in the [custom scalar plugin's doc page](https://graphql-maven-plugin-project.graphql-java-generator.com/customscalars.html).


## Full requests

The previous samples are all _Partial_ requests. This allows to directly get the result of the query in the response of the GraphQL repository method.

The GraphQL Maven and Gradle plugin also manage _Full_ requests (only for query and mutation, not for subscription).

A full request allows to:
* Execute several queries or mutations into one call toward the server  
* Add directives to the query/mutation itself
* Use GraphQL global fragments into your query (whereas only inline fragments may be used with partial requests)

The main difference between _Partial_ and _Full_ requests, is that the method that executes a full request returns an instance of the Query or Mutation type, as defined the query or mutation is defined in the GraphQL schema. This means:
* That you can have the response for several queries or mutations in one call
    * As alias are not managed yet, you can execute several different queries or several different mutations in a call. But you can not execute several times the same query or mutation in one call.
* You need to call the relevant getter to retrieve the result for each query or mutation that you have executed


Here is a sample of GraphQL Repository method that defines a Full Request:

```Java
@GraphQLRepository
public interface MyGraphQLRepository {

	...   You can defined as many method as you wish in a GraphQL Repository

	/**
	 * A full Request returns the Query or the Mutation type, as it is defined in the GraphQL schema. You'll have then
	 * to use the relevant getter(s) to retrieve the request's result
	 * 
	 * @return
	 */
	@FullRequest(request = "fragment topicFields on Topic {id title date} "
			+ "query{boards{id name publiclyAvailable topics {...topicFields nbPosts}}}")
	public Query fullQueryWithFragment() throws GraphQLRequestExecutionException;
}
```

Then, the using Spring component can call this method like this:

```Java
@Component // This annotation marks this class as a Spring bean (prerequisite to make @Autowire annotation work)
public class Application implements CommandLineRunner {

	@Autowired
	private MyGraphQLRepository myGraphQLRepository;

	@Override
	public void run(String... args) throws GraphQLRequestExecutionException {
		Query query = myGraphQLRepository.fullQueryWithFragment(); // Execution of the GraphQL Request
		List<Board> boards = query.getBoards(); // Retrieval of the result

		.. Do something 
	}

}
```

In this sample, there is a global fragment.
  



## Fragments

You can use fragments in your queries, mutations or subscriptions:
* __Inline fragments__ work with both Partial and Full requests
* __Named fragments__ work only with Full request, as you declare these fragments at the root of the requests. So, named fragments work only with queries and mutations.
    * This limitation will be overcome in the future

Below is a sample of a GraphQL Repository with Fragments:

```Java
@GraphQLRepository
public interface MyGraphQLRepository {

	/**
	 * A full Request returns the Query or the Mutation type, as it is defined in the GraphQL schema. You'll have then
	 * to use the relevant getter(s) to retrieve the request's result
	 * 
	 * @return
	 */
	@FullRequest(request = "fragment topicFields on Topic {id title date} "
			+ "query{boards{id name publiclyAvailable topics {...topicFields nbPosts}}}")
	public Query fullQueryWithFragment() throws GraphQLRequestExecutionException;

	/**
	 * A query with inline fragments
	 * 
	 * @return
	 * @throws GraphQLRequestExecutionException
	 */
	@PartialRequest(requestName = "boards", request = "{id name publiclyAvailable topics {... on Topic {id title date} nbPosts}}")
	List<Board> boardsWithInlineFragment() throws GraphQLRequestExecutionException;
}
```


## Mutations

Mutations work the same way as Queries. 

The only difference is the requestType parameter. Its value is `query` by default. So, for mutation, the `requestType` parameter is mandatory, in both the `@PartialRequest` and `@FullRequest` annotations.

Here is a sample with the mutation, done with a Partial Request, then a Full Request:

```java
@GraphQLRepository
public interface MyGraphQLRepository {

	/**
	 * A mutation sample, within a Partial Request
	 * 
	 * @param postInput
	 *            The post to be created
	 * 
	 * @return The created Post
	 */
	@PartialRequest(request = "{ id date author{id name} title content}", //
			requestType = RequestType.mutation /* default request type for Partial Query is query */)
	Post createPost(PostInput postInput) throws GraphQLRequestExecutionException;

	/**
	 * A mutation sample, within a Full Request
	 * 
	 * @param postInput
	 *            The post to be created
	 * @return The {@link Mutation} type. Calling the {@link Mutation#getCreatePost()} allows to retrieve the return for
	 *         the createPost mutation, that is: the created post
	 */
	@FullRequest(request = "mutation {createPost(post: &postInput) { id date author{id name} title content}}", //
			requestType = RequestType.mutation /* default request type for Partial Query is query */)
	Mutation createPostFullRequest(@BindParameter(name = "postInput") PostInput postInput)
			throws GraphQLRequestExecutionException;
}
```

And the way to use it is:

```java
@Component // This annotation marks this class as a Spring bean (prerequisite to make @Autowire annotation work)
public class Application implements CommandLineRunner {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(Application.class);

	@Autowired
	private MyGraphQLRepository myGraphQLRepository;

	@Override
	public void run(String... args) throws GraphQLRequestExecutionException {

		TopicPostInput topicInput = new TopicPostInput.Builder().withAuthorId("00000000-0000-0000-0000-000000000001")
				.withPubliclyAvailable(true).withDate(new GregorianCalendar(2019, 4 - 1, 30).getTime())
				.withTitle("a title").withContent("Some content").build();
		PostInput postInput = new PostInput.Builder().withFrom(new GregorianCalendar(2018, 3 - 1, 2).getTime())
				.withInput(topicInput).withTopicId("00000000-0000-0000-0000-000000000002").build();

		logger.info("===========================================================================================");
		logger.info("==================== Executing mutation in a Partial Request ==============================");
		logger.info("===========================================================================================");
		Post postFromPartialRequest = myGraphQLRepository.createPost(postInput);

... Do something with the created post (postFromPartialRequest)


		logger.info("===========================================================================================");
		logger.info("==================== Executing mutation in a Full Request =================================");
		logger.info("===========================================================================================");
		Mutation mutation = myGraphQLRepository.createPostFullRequest(postInput);
		Post postFromFullRequest = mutation.getCreatePost();

... Do something with the created post (postFromFullRequest)

	}

}
```

## To be continued... (Subscriptions)

Subscription are documented on the [GraphQL plugin's web site](https://graphql-maven-plugin-project.graphql-java-generator.com/client_subscription.html) 
