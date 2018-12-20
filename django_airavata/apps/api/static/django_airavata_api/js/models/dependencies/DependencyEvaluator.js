
// TODO: rename: BooleanExpressionEvaluator? There's really nothing related to "app input dependencies" in here.
export default class DependencyEvaluator {

  /**
   * Evaluate all dependency expressions and return an object with the same
   * keys as dependenciesConfig but with the result of each expression.
   *
   * @param inputValues
   * @param dependenciesConfig
   */
  evaluateDependencies(inputValues, dependenciesConfig) {

    const result = {};
    for (const k of Object.keys(dependenciesConfig)) {
      result[k] = this._evaluateExpression(dependenciesConfig[k], inputValues);
    }
    return result;
  }

  /**
   * Evaluates boolean expression for given context and returns boolean result.
   */
  _evaluateExpression(expression, context) {
    console.log("expression: " + JSON.stringify(expression));
    console.log("context: " + JSON.stringify(context));
    const keys = Object.keys(expression);
    if (keys.length > 1) {
      throw new Error("Expression contains more than one key: " + JSON.stringify(expression));
    }
    if (keys.length < 1) {
      throw new Error("Expression does not contain a key: " + JSON.stringify(expression));
    }

    const key = keys[0];
    const value = expression[key];
    if (key === "AND") {
      if (value instanceof Array) {
        const evaluations = value.map(exp => this._evaluateExpression(exp, context));
        return evaluations.reduce((acc, curr) => acc && curr);
      } else if (typeof value === 'object') {
        return this._evaluateExpression(exp, context);
      } else {
        throw new Error("Unrecognized operand value for AND: " + JSON.stringify(value));
      }
    } else if (key === "OR") {
      if (value instanceof Array) {
        const evaluations = value.map(exp => this._evaluateExpression(exp, context));
        return evaluations.reduce((acc, curr) => acc || curr);
      } else if (typeof value === 'object') {
        return this._evaluateExpression(exp, context);
      } else {
        throw new Error("Unrecognized operand value for OR: " + JSON.stringify(value));
      }
    } else if (key === "NOT") {
      if (typeof value === 'object') {
        return !this._evaluateExpression(value, context);
      } else {
        throw new Error("Unrecognized operand value for NOT: " + JSON.stringify(value));
      }
    }

    if (typeof value === 'object') {
      if (!(key in context)) {
        throw new Error("Missing context value for expression " + JSON.stringify(expression) + " in context " + JSON.stringify(context));
      }
      const contextValue = context[key];
      return this._evaluateExpressionType(contextValue, value);
    }
  }

  // TODO: rename: _evaluateExpressionDefinition
  // TODO: rename: _evaluateComparison
  _evaluateExpressionType(value, expressionType) {
    const type = expressionType["type"];
    if (!type) {
      throw new Error("Expression definition is missing 'type' property: " + JSON.stringify(expressionType));
    }
    if (type === "equals") {
      return value === this._getExpressionTypeValue(expressionType);
    }
    throw new Error("Unrecognized expression type " + JSON.stringify(expressionType));
  }

  // TODO: rename: _getExpressionDefinitionValue(expressionDefiniton)
  // TODO: rename: _getComparisonTarget
  _getExpressionTypeValue(expressionType) {

    if (!("value" in expressionType)) {
      throw new Error("Missing required 'value' property in expression definition: " + JSON.stringify(expressionType));
    }
    return expressionType["value"];
  }
}
