package rest

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.sources.{BaseRelation, DataSourceRegister, RelationProvider}

class RestDataSource extends RelationProvider
  with DataSourceRegister {
  override def shortName(): String = "rest"

  override def createRelation(
                               sqlContext: SQLContext,
                               parameters: Map[String, String]): BaseRelation = {

    import RESTOptions._

    val restOptions = new RESTOptions(parameters)

    RESTRelation(restOptions)(sqlContext.sparkSession)
  }
}
