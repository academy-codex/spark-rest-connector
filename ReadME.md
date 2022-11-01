# Rest API as a Data Source Connector in Apache Spark

- The module shared here can be used as a library to leverage the power of Apache Spark processing for data streamed over a REST API. The user can call one or more REST API in a distributed manner for different set of API parameters (for example, query parameters). The result of the API response is returned to the user as a Spark DataFrame.

## Requirements

This library requires Spark 2.2+.

## HTTP Client Dependency

This library leverages[scalaj_http](https://github.com/scalaj/scalaj-http) package.

## Features
This library allows multiple calls, in parallel, to a target REST based micro service for a set of different input parameters. These parameters can be passed as a Temporary Spark Table where the column names of the table should be same as the keys of the target API. Each row in the table, and corresponding combination of parameter values, will be used to make one API call. The result from the multiple calls to the API is returned as a Spark DataFrameof with the schema structure matching that of the target API's response.

All datatypes are converted to string while generating the payload. Complex datatypes (objects) are also supported as payload; just ensure to **stringify the object to json string** (use [to_json()](https://spark.apache.org/docs/2.4.0/api/sql/#to_json) function in Spark SQL).

This library supports several options for calling the target REST service:
* `url`: This is the uri of the target micro service. You can also provide the common parameters (those that don't vary with each API call) in this url. This is a mandatory parameter.
* `input`: You need to pass the name of the Temporary Spark Table which contains the input parameters set. This is a mandatory parameter too. If this table contains complex datatypes which needs to be sent as the payload, then ensure to convert the object to json string before passing it here.
* `method`: The supported http/https method. Possible types supported right now are `POST`, and `GET`. Default is `POST`
* `userId` : The userId in case the target API needs basic authentication.
* `userPassword` : The password in case the target API needs basic authentication
* `oauthToken` : This can be used for bearer token based authorization. This is the value for the `Authorization` key passed in the HEADER. So you can pass `Bearer <<access_token>>` as its value.
* `partitions`: Number of partition to be used to increase parallelism. Default is 2.
* `connectionTimeout` : In case the target API needs high time to connect. Default is 1000 (in ms)
* `readTimeout` : In case the target API returns large volume of data which needs more read time. Default is 5000 (in ms)
* `schemaSamplePcnt` : Percentage of records in the input table to be used to infer the schema. The default is "30" and minimum is 3. Increase this number in case you are getting an error or the schema is not propery inferred.
* `callStrictlyOnce` : This value can be used to ensure the backend API is called only once for each set of input parameters. The default is "N", allowing the back end API to get called for multiple times - once for inferring the schema and then for other operations. If this value is set to "Y" the backend API would be called only once (during infering the schema) for all of the input parameter sets and would be cached. This option is useful when the target APIs are paid service or does not support calls per day/per hour beyond certain number. However, the results would be cached which will increase the memory usage.

## Typical Structure of the Dataframe returned by Rest Data Source

The dataframe created by this REST Data Source will return a set of Rows of the same Structure. The Structure will contain the input fields that were used for the API call as well as the returned output under a new column named 'output'. Whatever gets returned in 'output' is specific to the target REST API. It's structure can be easily obtained by printSchema method of Dataframe.
