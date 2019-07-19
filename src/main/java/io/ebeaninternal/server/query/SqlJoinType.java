package io.ebeaninternal.server.query;

/**
 * Inner join, Outer join or automatic determination based on cardinality and optionality.
 */
public enum SqlJoinType {

  /**
   * It is an inner join.
   */
  INNER("join"),

  /**
   * It is an outer join.
   */
  OUTER("left join"),

  /**
   * It is automatically determined based on cardinality and optionality.
   */
  AUTO("JOIN-TYPE-AUTO-LITERAL-NOT-USED");

  private final String literal;

  SqlJoinType(String literal) {
    this.literal = literal;
  }

  /**
   * Return the actual SQL join literal taking into account the current join type and the 'default
   * join type as per deployment cardinality and optionality'.
   */
  public String getLiteral(SqlJoinType deploymentJoinType) {
    if (this == SqlJoinType.AUTO) {
      return deploymentJoinType.literal;
    }
    return this.literal;
  }

  /**
   * If this is an AUTO join set it to OUTER as we are joining to a Many.
   */
  public SqlJoinType autoToOuter() {
    if (this == AUTO) {
      return OUTER;
    } else {
      return this;
    }
  }

  /**
   * If join is AUTO but deploymentJoinType is OUTER then go into OUTER join mode.
   */
  public SqlJoinType autoToOuter(SqlJoinType deploymentJoinType) {
    if (this == AUTO && deploymentJoinType == OUTER) {
      return OUTER;
    } else {
      return this;
    }
  }

}

