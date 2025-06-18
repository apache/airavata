export default class BooleanExpressionEvaluator {
  /**
   * Context to use for looking up values of variables in expressions.
   * @param {object} context
   */
  constructor(context) {
    this.context = context;
  }
  /**
   * Evaluates boolean expression and returns boolean result.
   * @param {object} expression
   */
  evaluate(expression) {
    const keys = Object.keys(expression);
    if (keys.length > 1) {
      // Implicitly AND together several expressions
      return this.evaluate({
        AND: keys.map((k) => {
          const exp = {};
          exp[k] = expression[k];
          return exp;
        }),
      });
    }
    if (keys.length < 1) {
      throw new Error(
        "Expression does not contain a key: " + JSON.stringify(expression)
      );
    }

    const key = keys[0];
    const value = expression[key];
    if (key === "AND") {
      if (value instanceof Array) {
        const evaluations = value.map((exp) => this.evaluate(exp));
        return evaluations.reduce((acc, curr) => acc && curr);
      } else {
        throw new Error(
          "Unrecognized operand value for AND: " + JSON.stringify(value)
        );
      }
    } else if (key === "OR") {
      if (value instanceof Array) {
        const evaluations = value.map((exp) => this.evaluate(exp));
        return evaluations.reduce((acc, curr) => acc || curr);
      } else {
        throw new Error(
          "Unrecognized operand value for OR: " + JSON.stringify(value)
        );
      }
    } else if (key === "NOT") {
      if (typeof value === "object" && !(value instanceof Array)) {
        return !this.evaluate(value);
      } else {
        throw new Error(
          "Unrecognized operand value for NOT: " + JSON.stringify(value)
        );
      }
    }

    if (typeof value === "object") {
      if (!(key in this.context)) {
        throw new Error(
          "Missing context value for expression " +
            JSON.stringify(expression) +
            " in context " +
            JSON.stringify(this.context)
        );
      }
      const contextValue = this.context[key];
      return this._evaluateComparison(contextValue, value);
    }
  }

  _evaluateComparison(value, comparisonDefinition) {
    const comparison = comparisonDefinition["comparison"];
    if (!comparison) {
      throw new Error(
        "Expression definition is missing 'comparison' property: " +
          JSON.stringify(comparisonDefinition)
      );
    }
    if (comparison === "equals") {
      return value === this._getComparisonValue(comparisonDefinition);
    } else if (comparison === "in") {
      return this._getComparisonValue(comparisonDefinition).includes(value);
    }
    throw new Error("Unrecognized comparison " + JSON.stringify(comparison));
  }

  _getComparisonValue(comparisonDefinition) {
    if (!("value" in comparisonDefinition)) {
      throw new Error(
        "Missing required 'value' property in comparison definition: " +
          JSON.stringify(comparisonDefinition)
      );
    }
    return comparisonDefinition["value"];
  }
}
