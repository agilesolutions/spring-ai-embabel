# Spring AI Tool calling 
The goal is to automate tasks that would otherwise require human intervention or explicit programming. For example, a tool can be used to book a flight for a customer interacting with a chatbot, to fill out a form on a web page, or automatically generate an order in an OMS order management system.

## Objective
Tool calling is another option to provide the necessary contextual information to the LLM (Information Retrieval) and allow LLM to execution actions (Taking Action). This enhances the organizations ability to implement AI solutions with real-time contextual data.

<img title="Tool Calling with Spring AI" alt="Alt text" src="/images/tools.png">

1. When we want to make a tool available to the model, we include its definition in the chat request. Each tool definition comprises of a name, a description, and the schema of the input parameters.
2. When the model decides to call a tool, it sends a response with the tool name and the input parameters modeled after the defined 
3. The application is responsible for using the tool name to identify and execute the tool with the provided input parameters.
4. The result of the tool call is processed by the application.
5. The application sends the tool call result back to the model.
6. The model generates the final response using the tool call result as additional context.