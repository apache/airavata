import BooleanExpressionEvaluator from "../../../js/models/dependencies/BooleanExpressionEvaluator";

const context = {
  INPUT1: "1",
  INPUT2: "2",
  INPUT3: "3",
};
const booleanExpressionEvaluator = new BooleanExpressionEvaluator(context);

test("throws error when expression is empty", () => {
  expect(() => booleanExpressionEvaluator.evaluate({})).toThrow();
});

test("INPUT1 == 1 AND INPUT2 == 2 is TRUE", () => {
  const result = booleanExpressionEvaluator.evaluate({
    AND: [
      {
        INPUT1: {
          comparison: "equals",
          value: "1",
        },
      },
      {
        INPUT2: {
          comparison: "equals",
          value: "2",
        },
      },
    ],
  });
  expect(result).toBe(true);
});

test("INPUT1 == 2 AND INPUT2 == 2 is FALSE", () => {
  const result = booleanExpressionEvaluator.evaluate({
    AND: [
      {
        INPUT1: {
          comparison: "equals",
          value: "2", // "1" !== "2"
        },
      },
      {
        INPUT2: {
          comparison: "equals",
          value: "2",
        },
      },
    ],
  });
  expect(result).toBe(false);
});

test("INPUT1 == 2 OR INPUT2 == 2 is TRUE", () => {
  const result = booleanExpressionEvaluator.evaluate({
    OR: [
      {
        INPUT1: {
          comparison: "equals",
          value: "2",
        },
      },
      {
        INPUT2: {
          comparison: "equals",
          value: "2",
        },
      },
    ],
  });
  expect(result).toBe(true);
});

test("INPUT1 == 2 OR INPUT2 == 1 is FALSE", () => {
  const result = booleanExpressionEvaluator.evaluate({
    OR: [
      {
        INPUT1: {
          comparison: "equals",
          value: "2",
        },
      },
      {
        INPUT2: {
          comparison: "equals",
          value: "1",
        },
      },
    ],
  });
  expect(result).toBe(false);
});

test("(NOT INPUT1 == 2) AND INPUT2 == 2 is TRUE", () => {
  const result = booleanExpressionEvaluator.evaluate({
    AND: [
      {
        NOT: {
          INPUT1: {
            comparison: "equals",
            value: "2",
          },
        },
      },
      {
        INPUT2: {
          comparison: "equals",
          value: "2",
        },
      },
    ],
  });
  expect(result).toBe(true);
});

// single comparison
test("INPUT1 == 1 is TRUE", () => {
  const result = booleanExpressionEvaluator.evaluate({
    INPUT1: {
      comparison: "equals",
      value: "1",
    },
  });
  expect(result).toBe(true);
});

// single comparison
test("INPUT1 == 2 is FALSE", () => {
  const result = booleanExpressionEvaluator.evaluate({
    INPUT1: {
      comparison: "equals",
      value: "2",
    },
  });
  expect(result).toBe(false);
});

test("non-array given for AND throws Error", () => {
  expect(() =>
    booleanExpressionEvaluator.evaluate({
      AND: "a",
    })
  ).toThrow();
});

test("non-array given for OR throws Error", () => {
  expect(() =>
    booleanExpressionEvaluator.evaluate({
      OR: "a",
    })
  ).toThrow();
});

test("non-object given for NOT throws Error", () => {
  expect(() =>
    booleanExpressionEvaluator.evaluate({
      NOT: [
        {
          INPUT1: {
            comparison: "equals",
            value: 1,
          },
        },
      ],
    })
  ).toThrow();
});

test("referenced variable not in context throws Error", () => {
  expect(() =>
    booleanExpressionEvaluator.evaluate({
      ZINPUT1: {
        comparison: "equals",
        value: 1,
      },
    })
  ).toThrow(/missing context value/i);
});

test("missing 'comparison' property throws Error", () => {
  expect(() =>
    booleanExpressionEvaluator.evaluate({
      INPUT1: {
        value: 1,
      },
    })
  ).toThrow(/missing 'comparison' property/i);
});

test("unrecognized 'comparison' property throws Error", () => {
  expect(() =>
    booleanExpressionEvaluator.evaluate({
      INPUT1: {
        comparison: "foo",
        value: 1,
      },
    })
  ).toThrow(/unrecognized comparison/i);
});

test("Implicitly ANDed INPUT1 == 1 AND INPUT2 == 2 is TRUE", () => {
  const result = booleanExpressionEvaluator.evaluate({
    INPUT1: {
      comparison: "equals",
      value: "1",
    },
    INPUT2: {
      comparison: "equals",
      value: "2",
    },
  });
  expect(result).toBe(true);
});

// 'in' comparison
test("INPUT1 in [1, 2, 3] is TRUE", () => {
  const result = booleanExpressionEvaluator.evaluate({
    INPUT1: {
      comparison: "in",
      value: ["1", "2", "3"],
    },
  });
  expect(result).toBe(true);
});

// 'in' comparison
test("INPUT1 in [4, 5, 6] is FALSE", () => {
  const result = booleanExpressionEvaluator.evaluate({
    INPUT1: {
      comparison: "in",
      value: ["4", "5", "6"],
    },
  });
  expect(result).toBe(false);
});
