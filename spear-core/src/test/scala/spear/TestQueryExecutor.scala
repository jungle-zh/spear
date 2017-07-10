package spear

import org.mockito.Mockito._

import spear.parsers.QueryExpressionParser.queryExpression
import spear.plans.logical.{LogicalPlan, Optimizer}
import spear.plans.logical.analysis.Analyzer
import spear.plans.physical.PhysicalPlan

class TestQueryExecutor extends QueryExecutor {
  override val catalog: Catalog = new InMemoryCatalog

  override def parse(query: String): LogicalPlan = queryExpression.parse(query).get.value

  override def analyze(plan: LogicalPlan): LogicalPlan = analyzer apply plan

  override def optimize(plan: LogicalPlan): LogicalPlan = optimizer(plan)

  override def plan(plan: LogicalPlan): PhysicalPlan =
    when(mock(classOf[PhysicalPlan]).iterator).thenReturn(Iterator.empty).getMock[PhysicalPlan]

  private val analyzer = new Analyzer(catalog)

  private val optimizer = new Optimizer
}