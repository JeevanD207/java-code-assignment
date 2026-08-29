# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
In this code base, different approaches are used for database access. If I were maintaining it, I would prefer to follow one consistent approach. I think using a repository approach would make the code easier to maintain because database-related code can be separated from business logic. Transactions can also be managed properly at the service or use-case level.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Both approaches have their advantages. Using OpenAPI first gives us a clear API contract and documentation, and it helps keep the API implementation consistent. Generating code from the API specification can also reduce differences between teams.Writing the endpoints directly is simpler and faster, especially for smaller features or during the initial development stage. However, if the API is used by multiple teams, I would prefer the OpenAPI-first approach because it provides a clear contract and makes maintenance easier.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would first focus on unit tests for the important business and warehouse rules because they are fast and easy to run. Then I would add integration tests for database operations and REST APIs to make sure the different parts work correctly together.

All tests should be run in the CI pipeline. Also, whenever a bug is found and fixed, I would add a test for that issue so that the same problem does not happen again in the future.

```