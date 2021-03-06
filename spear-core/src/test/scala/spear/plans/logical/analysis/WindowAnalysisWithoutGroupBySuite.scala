package spear.plans.logical.analysis

import spear.expressions._
import spear.expressions.functions._
import spear.expressions.windows._
import spear.plans.logical.{let, table}

class WindowAnalysisWithoutGroupBySuite extends WindowAnalysisTest { self =>
  test("single window function") {
    val w0_? = Window partitionBy 'a orderBy 'b rowsBetween (UnboundedPreceding, 0)
    val `@W: max(a) over w0` = WindowFunctionAlias(max(a) over (w0_? partitionBy a orderBy b))

    checkSQLAnalysis(
      """SELECT max(a) OVER (
        |  PARTITION BY a
        |  ORDER BY b
        |  ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |) AS win_max
        |FROM t
        |""".stripMargin,

      table('t) select ('max('a) over w0_? as 'win_max),

      relation
        window `@W: max(a) over w0`
        select (`@W: max(a) over w0`.attr as 'win_max)
    )
  }

  test("single window function with non-window expressions") {
    val w0_? = Window partitionBy 'a orderBy 'b rowsBetween (UnboundedPreceding, 0)
    val `@W: max(a) over w0` = WindowFunctionAlias(max(a) over (w0_? partitionBy a orderBy b))

    checkSQLAnalysis(
      """SELECT a + max(a) OVER (
        |  PARTITION BY a
        |  ORDER BY b
        |  ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |) as win_max
        |FROM t
        |""".stripMargin,

      table('t) select ('a + ('max('a) over w0_?) as 'win_max),

      relation
        window `@W: max(a) over w0`
        select (a + `@W: max(a) over w0`.attr as 'win_max)
    )
  }

  test("multiple window functions with the same window spec") {
    val w0_? = Window partitionBy 'a orderBy 'b rowsBetween (UnboundedPreceding, 0)
    val w0 = Window partitionBy a orderBy b rowsBetween (UnboundedPreceding, 0)

    val `@W: max(a) over w0` = WindowFunctionAlias(max(a) over w0)
    val `@W: count(b) over w0` = WindowFunctionAlias(count(b) over w0)

    checkSQLAnalysis(
      """SELECT
        |  max(a) OVER (
        |    PARTITION BY a
        |    ORDER BY b
        |    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |  ) AS win_max,
        |  count(b) OVER (
        |    PARTITION BY a
        |    ORDER BY b
        |    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |  ) AS win_count
        |FROM t
        |""".stripMargin,

      table('t) select (
        'max('a) over w0_? as 'win_max,
        'count('b) over w0_? as 'win_count
      ),

      relation.window(
        `@W: max(a) over w0`,
        `@W: count(b) over w0`
      ).select(
        `@W: max(a) over w0`.attr as 'win_max,
        `@W: count(b) over w0`.attr as 'win_count
      )
    )
  }

  test("multiple window functions with the same window spec and non-window expressions") {
    val w0_? = Window partitionBy 'a orderBy 'b rowsBetween (UnboundedPreceding, 0)
    val w0 = Window partitionBy a orderBy b rowsBetween (UnboundedPreceding, 0)

    val `@W: max(a) over w0` = WindowFunctionAlias(max(a) over w0)
    val `@W: count(b) over w0` = WindowFunctionAlias(count(b) over w0)

    checkSQLAnalysis(
      """SELECT
        |  a + max(a) OVER (
        |    PARTITION BY a
        |    ORDER BY b
        |    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |  ) AS c0,
        |  count(b) OVER (
        |    PARTITION BY a
        |    ORDER BY b
        |    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |  ) AS c1
        |FROM t
        |""".stripMargin,

      table('t) select (
        ('a + ('max('a) over w0_?)) as 'c0,
        'count('b) over w0_? as 'c1
      ),

      relation.window(
        `@W: max(a) over w0`,
        `@W: count(b) over w0`
      ).select(
        a + `@W: max(a) over w0`.attr as 'c0,
        `@W: count(b) over w0`.attr as 'c1
      )
    )
  }

  test("multiple window functions with different window specs") {
    val w0_? = Window partitionBy 'a orderBy 'b rowsBetween (UnboundedPreceding, 0)
    val w1_? = Window partitionBy 'b orderBy 'a rangeBetween (-1, 1)

    val w0 = Window partitionBy a orderBy b rowsBetween (UnboundedPreceding, 0)
    val w1 = Window partitionBy b orderBy a rangeBetween (-1, 1)

    val `@W: max(a) over w0` = WindowFunctionAlias(max(a) over w0)
    val `@W: count(b) over w1` = WindowFunctionAlias(count(b) over w1)

    checkSQLAnalysis(
      """SELECT
        |  max(a) OVER (
        |    PARTITION BY a
        |    ORDER BY b
        |    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |  ) AS win_max,
        |  count(b) OVER (
        |    PARTITION BY b
        |    ORDER BY a
        |    RANGE BETWEEN 1 PRECEDING AND 1 FOLLOWING
        |  ) AS win_count
        |FROM t
        |""".stripMargin,

      table('t) select (
        'max('a) over w0_? as 'win_max,
        'count('b) over w1_? as 'win_count
      ),

      relation
        window `@W: max(a) over w0`
        window `@W: count(b) over w1`
        select (
          `@W: max(a) over w0`.attr as 'win_max,
          `@W: count(b) over w1`.attr as 'win_count
        )
    )
  }

  test("multiple window functions with different window specs and non-window expressions") {
    val w0_? = Window partitionBy 'a orderBy 'b rowsBetween (UnboundedPreceding, 0)
    val w1_? = Window partitionBy 'b orderBy 'a rangeBetween (-1, 1)

    val w0 = Window partitionBy a orderBy b rowsBetween (UnboundedPreceding, 0)
    val w1 = Window partitionBy b orderBy a rangeBetween (-1, 1)

    val `@W: max(a) over w0` = WindowFunctionAlias(max(a) over w0)
    val `@W: count(b) over w1` = WindowFunctionAlias(count(b) over w1)

    checkSQLAnalysis(
      """SELECT
        |  a + max(a) OVER (
        |    PARTITION BY a
        |    ORDER BY b
        |    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |  ) AS win_max,
        |  count(b) OVER (
        |    PARTITION BY b
        |    ORDER BY a
        |    RANGE BETWEEN 1 PRECEDING AND 1 FOLLOWING
        |  ) AS win_count
        |FROM t
        |""".stripMargin,

      table('t) select (
        ('a + ('max('a) over w0_?)) as 'win_max,
        'count('b) over w1_? as 'win_count
      ),

      relation
        window `@W: max(a) over w0`
        window `@W: count(b) over w1`
        select (
          a + `@W: max(a) over w0`.attr as 'win_max,
          `@W: count(b) over w1`.attr as 'win_count
        )
    )
  }

  test("window function in ORDER BY clause") {
    val w0_? = Window partitionBy 'a orderBy 'b rowsBetween (UnboundedPreceding, 0)
    val w0 = Window partitionBy a orderBy b rowsBetween (UnboundedPreceding, 0)

    val `@W: max(a) over w0` = WindowFunctionAlias(max(a) over w0)

    checkSQLAnalysis(
      """SELECT *
        |FROM t
        |ORDER BY max(a) OVER (
        |  PARTITION BY a
        |  ORDER BY b
        |  ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |)
        |""".stripMargin,

      table('t) select * orderBy ('max('a) over w0_?),

      relation
        select (a, b)
        window `@W: max(a) over w0`
        sort `@W: max(a) over w0`.attr
        select (a, b)
    )
  }

  test("reference window function alias in ORDER BY clause") {
    val w0_? = Window partitionBy 'a orderBy 'b rowsBetween (UnboundedPreceding, 0)
    val w0 = Window partitionBy a orderBy b rowsBetween (UnboundedPreceding, 0)

    val `@W: max(a) over w0` = WindowFunctionAlias(max(a) over w0)
    val win_max = `@W: max(a) over w0`.attr as 'win_max

    checkSQLAnalysis(
      """SELECT max(a) OVER (
        |  PARTITION BY a
        |  ORDER BY b
        |  ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |) AS win_max
        |FROM t
        |ORDER BY win_max
        |""".stripMargin,

      table('t)
        .select('max('a) over w0_? as 'win_max)
        .orderBy('win_max),

      relation
        .window(`@W: max(a) over w0`)
        .select(win_max)
        .sort(win_max.attr)
    )
  }

  test("single window definition") {
    val w0_? = Window partitionBy 'a orderBy 'b rowsBetween (UnboundedPreceding, 0)
    val w0 = Window partitionBy a orderBy b rowsBetween (UnboundedPreceding, 0)

    val `@W: max(a) over w0` = WindowFunctionAlias(max(a) over w0)

    checkSQLAnalysis(
      """SELECT max(a) OVER w0 AS win_max
        |FROM t
        |WINDOW w0 AS (
        |  PARTITION BY a
        |  ORDER BY b
        |  ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |)
        |""".stripMargin,

      let('w0, w0_?) {
        table('t) select ('max('a) over 'w0 as 'win_max)
      },

      relation
        window `@W: max(a) over w0`
        select (`@W: max(a) over w0`.attr as 'win_max)
    )
  }

  test("multiple window definitions") {
    val w0_? = Window partitionBy 'a orderBy 'b rowsBetween (UnboundedPreceding, 0)
    val w1_? = Window partitionBy 'b orderBy 'a rangeBetween (-1, 1)

    val w0 = Window partitionBy a orderBy b rowsBetween (UnboundedPreceding, 0)
    val w1 = Window partitionBy b orderBy a rangeBetween (-1, 1)

    val `@W: max(a) over w0` = WindowFunctionAlias(max(a) over w0)
    val `@W: count(b) over w1` = WindowFunctionAlias(count(b) over w1)

    checkSQLAnalysis(
      """SELECT
        |  max(a) OVER w0 AS win_max,
        |  count(b) OVER w1 AS win_count
        |FROM t
        |WINDOW
        |  w0 AS (
        |    PARTITION BY a
        |    ORDER BY b
        |    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |  ),
        |  w1 AS (
        |    PARTITION BY b
        |    ORDER BY a
        |    RANGE BETWEEN 1 PRECEDING AND 1 FOLLOWING
        |  )
        |""".stripMargin,

      let('w0, w0_?) {
        let('w1, w1_?) {
          table('t) select (
            'max('a) over 'w0 as 'win_max,
            'count('b) over 'w1 as 'win_count
          )
        }
      },

      relation
        window `@W: max(a) over w0`
        window `@W: count(b) over w1`
        select (
          `@W: max(a) over w0`.attr as 'win_max,
          `@W: count(b) over w1`.attr as 'win_count
        )
    )
  }

  test("reference to existing window definition") {
    val w0_? = Window partitionBy 'a orderBy 'b rowsBetween (UnboundedPreceding, 0)
    val w0 = Window partitionBy a orderBy b rowsBetween (UnboundedPreceding, 0)

    val `@W: max(a) over w0` = WindowFunctionAlias(max(a) over w0)
    val `@W: count(b) over w0` = WindowFunctionAlias(count(b) over w0)

    checkSQLAnalysis(
      """SELECT
        |  max(a) OVER w0 AS win_max,
        |  count(b) OVER w1 AS win_count
        |FROM t
        |WINDOW
        |  w0 AS (
        |    PARTITION BY a
        |    ORDER BY b
        |    ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |  ),
        |  w1 AS (w0)
        |""".stripMargin,

      let('w0, w0_?) {
        let('w1, 'w0) {
          table('t) select (
            'max('a) over 'w0 as 'win_max,
            'count('b) over 'w1 as 'win_count
          )
        }
      },

      relation.window(
        `@W: max(a) over w0`,
        `@W: count(b) over w0`
      ).select(
        `@W: max(a) over w0`.attr as 'win_max,
        `@W: count(b) over w0`.attr as 'win_count
      )
    )
  }

  test("window aggregate function containing non-window aggregate function in SELECT") {
    val `@A: count(1)` = AggregateFunctionAlias(count(1))
    val `@W: count(count(1)) over ()` = WindowFunctionAlias(count(`@A: count(1)`.attr) over ())

    checkSQLAnalysis(
      "SELECT count(count(*)) OVER () AS c FROM t",

      table('t) select ('count('count(*)) over () as 'c),

      relation
        aggregate (Nil, `@A: count(1)` :: Nil)
        window `@W: count(count(1)) over ()`
        select (`@W: count(count(1)) over ()`.attr as 'c)
    )
  }

  test("window aggregate function containing non-window aggregate function in ORDER BY") {
    val `@A: count(1)` = AggregateFunctionAlias(count(1))
    val `@W: count(count(1)) over ()` = WindowFunctionAlias(count(`@A: count(1)`.attr) over ())
    val order0 = SortOrderAlias(`@W: count(count(1)) over ()`.attr, "order0")
    val `1 as c` = 1 as 'c

    checkSQLAnalysis(
      "SELECT 1 AS c FROM t ORDER BY count(count(*)) OVER ()",

      table('t) select (1 as 'c) orderBy ('count('count(*)) over ()),

      relation
        aggregate (Nil, `@A: count(1)` :: Nil)
        window `@W: count(count(1)) over ()`
        sort `@W: count(count(1)) over ()`.attr
        select (`1 as c`, order0)
        select `1 as c`.attr
    )
  }
}
