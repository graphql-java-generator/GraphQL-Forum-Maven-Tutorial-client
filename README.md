# A sample for the GraphQL Maven plugin (client side)

This Tutorial describes how-to create a GraphQL client application, with the [graphql-maven-plugin](https://github.com/graphql-java-generator/graphql-maven-plugin-project)


The GraphQL Maven plugin helps both on the server and on the client side. You'll find the tutorial for the server side [on this page](../GraphQL-Forum-Maven-Sample-server)


## Schema first

This plugin allows a schema first approach.

This approach is the best approach for APIs: it allows to precisely control the Interface Contract. This contract is the heart of all interface systems.

This tutorial won't describe how to create a GraphQL schema. There are plenty of resources on the net for that, starting with the [official GraphQL site](https://graphql.org/).

## The Forum GraphQL schema

This sample is based on the Forum schema, [available here](https://github.com/graphql-java-generator/GraphQL-Forum-Maven-Sample/blob/master/GraphQL-Forum-Maven-Sample-client/src/main/resources/forum.graphqls)

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

It could be also be used in another folder, like _/src/main/graphql/_. In this case, the schema is not stored in the packaged jar (which is Ok), and you have to use the plugin _schemaFileFolder_ parameter, to indicate where to find this schema.

## The pom file

As a maven plugin, you have to add the plugin the build section of your pom (the full pom [is available here](https://github.com/graphql-java-generator/GraphQL-Forum-Maven-Sample/blob/master/GraphQL-Forum-Maven-Sample-client/pom.xml)):
  

```XML
	
	<build>
...
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.8.1</version>
				<configuration>
					<source>${java.version}</source>
					<target>${java.version}</target>
				</configuration>
			</plugin>
			<plugin>
				<groupId>com.graphql-java-generator</groupId>
				<artifactId>graphql-maven-plugin</artifactId>
				<version>${graphql-maven-plugin.version}</version>
				<executions>
					<execution>
						<goals>
							<goal>graphql</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<mode>client</mode>
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

The compiler must be set to version 1.8 (or higher).

In this plugin declaration:
* The plugin execution is mapped to its graphql goal
* Its mode is set to _client_ (which is actually useless and is set here only for clarity, as _client_ is the mode's default value)
* The plugin generates the GraphQL code in the _packageName_ package (or in the _com.generated.graphql_ if this parameter is not defined)
* The _separateUtilityClasses_ set _true_ allows this separation:
    * All the classes generated directly from the GraphQL schema (object, enum, interfaces, input types...) are generated in _packageName_.
    * All the utility classes are generated in the sub-package _util_
    * This insures to have no collision between the GraphQL code and the GraphQL plugin's utility classes
    * If you set it to _false_, or don't define it, then all classes are generated in the _packageName_ package 
* And we declare the _Date_ scalar implementation. 
    * It is mandatory to give the implementation for each custom scalar defined in the GraphQL schema.
    * You'll find the relevant documentation on the [Plugin custom scalar doc page](https://graphql-maven-plugin-project.graphql-java-generator.com/customscalars.html)  

You can add the _build-helper-maven-plugin_, so that the generated source is automatically added to the build path of your IDE.

The _graphql-java-runtime_ dependency add all necessary dependencies, for the generated code. Of course, its version must be the same as the plugin's version.


## A look at the generated code

Executing a _mvn clean compile_ will generate the client code in the _packageName_ package (or in the _com.generated.graphql_ if this parameter is not defined)

The code is generated in the _/target/generated-sources/graphql-maven-plugin_ folder. And thanks to the _build-helper-maven-plugin_, it should automatically be added to your favorite IDE.

Let's take a look at the generated code:
* The __org.forum.client__ package contains all classes that maps to the GraphQL schema:
    * The classes starting by '__' (two underscores) are the GraphQL introspection classes. These are standard GraphQL types.
    * All other classes are directly the items defined in the forum GraphQL schema, with their fields, getter and setter. All fields are annotated with the GraphQL information necessary on runtime, and the JSON annotations to allow the deserialization of the server response.
* The __org.forum.client.util__ package contains:
    * _CustomScalarXxx_ classes are utility classes for custom scalars: a registry, and one JSON deserializer for each custom scalar defined in the GraphQL schema
    * _GraphQLRequest_ : its a base element to execute GraphQL request (query, mutation or subscription). See below for the details.
    * _QueryTypeExecutor_ , _MutationTypeExecutor_ and _SubscriptionTypeExecutor_ allow to execute the queries, mutations and subscriptions defined in the schema
    * _QueryType_ , _MutationType_ and _SubscriptionType_ are deprecated and will be removed in 2.0 version
    * _XxxResponse_ are deprecated class, that exist only for backward compatibility.
    * _XxxRootResponse_ are the target for deserialization when executing a query or a mutation. 
* The _com.graphql_java_generator_ and its subpackages is the plugin's runtime. It's added to your project, so that your project has __no dependency__ from graphql-java-generator.
    * You can also set the _copyRuntimeSources_ plugin parameter to false, and add the com.graphql-java-generator:graphql-java-runtime dependency, with the exact same version as the plugin version. 

To sum up, you'll use:
* The _GraphQLRequest_ to store a prepared request
* The _QueryType_ , _MutationType_ and _SubscriptionType_ classes to prepare and execute GraphQL requests
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



## Simple Partial Query/Mutation

GraphQL queries and mutations are executed in exactly the same way.

The easiest way is to execute Partial Request. And the most effective is that this request is prepared first.


The code below executes the boards query, as defined in this extract of the GraphQL schema:

```
type QueryType {
    boards: [Board]
[...]
}
```


```Java
public class GraphQLClient {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(GraphQLClient.class);

	QueryTypeExecutor queryExecutor;
	GraphQLRequest boardsRequest;

	/** This constructor prepares the GraphQL requests, so that they can be used by the {@link #exec()} method */
	public GraphQLClient() throws GraphQLRequestPreparationException {
		// Creation of the query executor, for this GraphQL endpoint
		logger.info("Connecting to GraphQL endpoint");
		queryExecutor = new QueryTypeExecutor("http://localhost:8180/graphql");

		// Preparation of the GraphQL requests, that will be used in the exec method
		boardsRequest = queryExecutor
				.getBoardsGraphQLRequest("{id name publiclyAvailable topics {id title date nbPosts}}");
	}

	public void exec() throws GraphQLRequestExecutionException {
		// Let's get, then display, all available boards
		List<Board> boards = queryExecutor.boards(boardsRequest);

		... do something with boards
	}
}
```

And you're done:
* The _GraphQLClient_ constructor prepares the request(s)
* The _exec()_ method executes the query 

Of course, in a real application case, you would prepare more requests

Execution of a __Mutation__ works in exactly the same way.


## Execution of a Query/Mutation with parameters

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
public class GraphQLClient {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(GraphQLClient.class);

	QueryTypeExecutor queryExecutor;
	GraphQLRequest allTopicsRequest;

	/** This constructor prepares the GraphQL requests, so that they can be used by the {@link #exec()} method */
	public GraphQLClient() throws GraphQLRequestPreparationException {
		// Creation of the query executor, for this GraphQL endpoint
		logger.info("Connecting to GraphQL endpoint");
		queryExecutor = new QueryTypeExecutor("http://localhost:8180/graphql");

		// Preparation of the GraphQL requests, that will be used in the exec method
		allTopicsRequest = queryExecutor.getTopicsGraphQLRequest("{id date author {id name} nbPosts title content}");
	}

	public void exec() throws GraphQLRequestExecutionException {
		// Let's get, then display, all topics of one of these boards
		String aBoardName = "Board name 2";
		List<Topic> topics = queryExecutor.topics(allTopicsRequest, aBoardName);
		
		... do something with topics
	}
}
```

The only change is that all the query parameters are parameter of the _queryExecutor.topics(..)_ method. The _topics(..)_ method take care of serializing and sending the board name parameter to the GraphQL server.

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
* memberName. Also optional.
* since. This parameter is mandatory, as it is prefixed by _&_

You can define bind parameters as being optional or mandatory without enforcing what's optional or mandatory in the GraphQL schema :  
* You can define a GraphQL optional parameter as mandatory in your use case. Just prefi the bind parameter by a _&_
* Of course, GraphQL mandatory parameter should be mandatory bind parameters

Here is the code:  

```Java
public class GraphQLClient {

	/** The logger for this class */
	static protected Logger logger = LoggerFactory.getLogger(GraphQLClient.class);

	QueryTypeExecutor queryExecutor;
	GraphQLRequest topicsSinceRequest;

	/** This constructor prepares the GraphQL requests, so that they can be used by the {@link #exec()} method */
	public GraphQLClient() throws GraphQLRequestPreparationException {

		// Creation of the query executor, for this GraphQL endpoint
		logger.info("Connecting to GraphQL endpoint");
		queryExecutor = new QueryTypeExecutor("http://localhost:8180/graphql");

		// Preparation of the GraphQL requests, that will be used in the exec method
		topicsSinceRequest = queryExecutor.getTopicsGraphQLRequest(""//
				+ "{" //
				+ "  id date author {id name} nbPosts title content "//
				+ "  posts(memberId: ?memberId, memberName: ?memberName, since: &since)  {id date title}"//
				+ "}");

	}

	public void exec() throws GraphQLRequestExecutionException { 
		java.util.Date sinceParam = new GregorianCalendar(2018, 3 - 1, 2).getTime();
		List<Topic> topicsSince = queryExecutor.topics(topicsSinceRequest, //
				"Board name 2", // This the query parameter. Depending on the GraphQL schema, there could be others
				"memberId", "00000000-0000-0000-0000-000000000002", //
				// No value is given for the optional memberName parameter
				"since", sinceParam);
		
		... do something with topicsSince
	}
}
```

As there is no provided value for the _memberName_ bind parameter, this parameter is not sent to the server. It's correct as this parameter is optional in both the bind parameter definition in the query (it starts by a _?_ ) and the GraphQL schema.

Please note that:
* If a bind parameter is set for a GraphQL array/list, you'll have to provide a java.util.List<YourObject> instance, where YourObject is the type defined in the GraphQL schema.  
* The _since_ parameter is a custom scalar of type _Date_ . In the pom (or gradle configuration), the custom scalar is declared as being a _java.util.Date_ so the value in your code is a standard java.util.Date. The custom scalar implementation provided in the pom (or gradle configuration) takes care of properly format the code (when executing the request) and read the value (when reading the server response). More information on that in the [custom scalar plugin's doc page](https://graphql-maven-plugin-project.graphql-java-generator.com/customscalars.html).


## Full requests

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
  



## Fragments

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


## To be continued... (Subscriptions)

Subscription are documented on the [GraphQL plugin's web site](https://graphql-maven-plugin-project.graphql-java-generator.com/client_subscription.html) 
