package scraper

import scala.collection.mutable
import scala.reflect.runtime.universe.WeakTypeTag

import scraper.expressions.NamedExpression
import scraper.parser.Parser
import scraper.plans.logical._
import scraper.plans.physical.PhysicalPlan
import scraper.plans.{Optimizer, QueryPlanner, physical}
import scraper.trees.RulesExecutor
import scraper.types.{LongType, TupleType}

trait Catalog {
  def registerRelation(tableName: String, analyzedPlan: LogicalPlan): Unit

  def lookupRelation(tableName: String): LogicalPlan
}

trait Context {
  type QueryExecution <: plans.QueryExecution

  type Catalog <: scraper.Catalog

  private[scraper] def catalog: Catalog

  private[scraper] def analyze: RulesExecutor[LogicalPlan]

  private[scraper] def optimize: RulesExecutor[LogicalPlan]

  private[scraper] def plan: QueryPlanner[LogicalPlan, PhysicalPlan]

  def execute(logicalPlan: LogicalPlan): QueryExecution

  def select(expressions: NamedExpression*): DataFrame

  def q(query: String): DataFrame
}

class LocalContext extends Context {
  type QueryExecution = LocalQueryExecution

  override type Catalog = LocalCatalog

  override private[scraper] val catalog: Catalog = new Catalog

  override private[scraper] val analyze = new Analyzer(catalog)

  override private[scraper] val optimize = new Optimizer

  override private[scraper] val plan = new LocalQueryPlanner

  def lift[T <: Product: WeakTypeTag](data: Traversable[T]): DataFrame =
    new DataFrame(new QueryExecution(LocalRelation(data), this))

  def lift[T <: Product: WeakTypeTag](data: Traversable[T], columnNames: String*): DataFrame = {
    val LocalRelation(rows, schema) = LocalRelation(data)
    new DataFrame(LocalRelation(rows, schema rename (columnNames: _*)), this)
  }

  override def select(expressions: NamedExpression*): DataFrame =
    new DataFrame(Project(SingleRowRelation, expressions), this)

  def range(end: Long): DataFrame = range(0, end)

  def range(begin: Long, end: Long): DataFrame = {
    val rows = (begin until end).map(Row.apply(_))
    val schema = TupleType('id -> LongType.!)
    new DataFrame(LocalRelation(rows, schema), this)
  }

  override def q(query: String): DataFrame = new DataFrame(new Parser().parse(query), this)

  def execute(logicalPlan: LogicalPlan): QueryExecution = new QueryExecution(logicalPlan, this)
}

class LocalCatalog extends Catalog {
  private val tables: mutable.Map[String, LogicalPlan] = mutable.Map.empty

  override def registerRelation(tableName: String, analyzedPlan: LogicalPlan): Unit =
    tables(tableName) = analyzedPlan

  override def lookupRelation(tableName: String): LogicalPlan = tables(tableName)
}

class LocalQueryExecution(val logicalPlan: LogicalPlan, val context: LocalContext)
  extends plans.QueryExecution

class LocalQueryPlanner extends QueryPlanner[LogicalPlan, PhysicalPlan] {
  override def strategies: Seq[Strategy] = Seq(
    BasicOperators
  )

  object BasicOperators extends Strategy {
    override def apply(logicalPlan: LogicalPlan): Seq[PhysicalPlan] = logicalPlan match {
      case child Project projectList =>
        physical.Project(planLater(child), projectList) :: Nil

      case child Filter condition =>
        physical.Filter(planLater(child), condition) :: Nil

      case child Limit n =>
        physical.Limit(planLater(child), n) :: Nil

      case relation @ LocalRelation(data, _) =>
        physical.LocalRelation(data.toIterator, relation.output) :: Nil

      case SingleRowRelation =>
        physical.SingleRowRelation :: Nil

      case _ => Nil
    }
  }
}
