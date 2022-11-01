# Rest API as a Data Source Connector in Apache Spark

- The module shared here can be used as a library to leverage the power of Apache Spark processing for data streamed over a REST API. The user can call one or more REST API in a distributed manner for different set of API parameters (for example, query parameters). The result of the API response is returned to the user as a Spark DataFrame.

## Requirements

This library requires Spark 2.2+.

## HTTP Client Dependency

This library leverages[scalaj_http](https://github.com/scalaj/scalaj-http) package.
