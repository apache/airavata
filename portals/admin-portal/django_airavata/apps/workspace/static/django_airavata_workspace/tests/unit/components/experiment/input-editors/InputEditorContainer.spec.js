import { shallowMount } from "@vue/test-utils";
import InputEditorContainer from "@/components/experiment/input-editors/InputEditorContainer.vue";
import { models } from "django-airavata-api";

function factory(config = { initA: "1", initB: null, sync: true }) {
  const inputA = new models.InputDataObjectType({
    name: "A",
    value: config.initA,
  });
  const inputB = new models.InputDataObjectType({
    name: "B",
    value: config.initB,
    metaData: {
      editor: {
        dependencies: {
          show: {
            AND: [
              {
                A: {
                  comparison: "equals",
                  value: "2",
                },
              },
            ],
          },
        },
      },
    },
  });
  // eslint-disable-next-line no-unused-vars
  const experiment = new models.Experiment({
    experimentInputs: [inputA, inputB],
  });
  const wrapper = shallowMount(InputEditorContainer, {
    propsData: {
      value: inputB.value,
      experimentInput: inputB,
    },
    sync: config.sync,
  });
  return {
    wrapper,
    inputA,
    inputB,
    experiment,
  };
}

describe("InputEditorContainer", () => {
  test("new Experiment evaluates dependencies", () => {
    const { inputA, inputB } = factory();
    expect(inputA.show).toBeTruthy();
    expect(inputB.show).toBeFalsy();
  });

  test("When input value changes, other input shows", () => {
    const { inputA, inputB, experiment } = factory();
    expect(inputB.show).toBeFalsy();
    inputA.value = "2";
    experiment.evaluateInputDependencies();
    expect(inputB.show).toBeTruthy();
  });

  test("InputEditorContainer saves value when hiding", () => {
    const { inputA, inputB, experiment, wrapper } = factory({
      initA: "2",
      initB: "3",
    });
    expect(inputB.show).toBeTruthy();
    expect(wrapper.vm.data).toBe("3");
    expect(wrapper.vm.oldValue).toBe(null);
    expect(wrapper.vm.show).toBeTruthy();

    inputA.value = "1";
    experiment.evaluateInputDependencies();
    expect(inputB.show).toBeFalsy();
    // Simulate watcher update since test environment doesn't respond to updates
    // of nested properties of props
    wrapper.setData({ show: inputB.show });
    expect(wrapper.vm.show).toBeFalsy();
    // Should update to data to null and have "3" in oldValue
    expect(wrapper.vm.data).toBe(null);
    expect(wrapper.vm.oldValue).toBe("3");
  });

  test("InputEditorContainer emits null 'input' event when hiding", () => {
    const { inputA, inputB, experiment, wrapper } = factory({
      initA: "2",
      initB: "3",
    });
    expect(wrapper.vm.show).toBeTruthy();

    inputA.value = "1";
    experiment.evaluateInputDependencies();
    expect(inputB.show).toBeFalsy();
    // Simulate watcher update since test environment doesn't respond to updates
    // of nested properties of props
    wrapper.setData({ show: inputB.show });
    expect(wrapper.emitted().input).toBeTruthy();
    expect(wrapper.emitted().input.length).toBe(1);
    expect(wrapper.emitted().input[0]).toEqual([null]);
  });

  test("InputEditorContainer restores values when showing", () => {
    const { inputA, inputB, experiment, wrapper } = factory();
    expect(wrapper.vm.show).toBeFalsy();
    expect(wrapper.vm.oldValue).toBe(null);
    wrapper.setData({ oldValue: "oldValue" });

    inputA.value = "2";
    experiment.evaluateInputDependencies();
    expect(inputB.show).toBeTruthy();
    wrapper.setData({ show: inputB.show });
    expect(wrapper.vm.data).toEqual("oldValue");
  });

  test("InputEditorContainer emits old value 'input' event when showing", () => {
    const { inputA, inputB, experiment, wrapper } = factory();
    expect(wrapper.vm.show).toBeFalsy();
    expect(wrapper.vm.oldValue).toBe(null);
    wrapper.setData({ oldValue: "oldValue" });

    inputA.value = "2";
    experiment.evaluateInputDependencies();
    expect(inputB.show).toBeTruthy();
    wrapper.setData({ show: inputB.show });
    expect(wrapper.emitted().input.length).toBe(1);
    expect(wrapper.emitted().input[0]).toEqual(["oldValue"]);
  });

  test("if hidden from the start, should restore initial value when showing", () => {
    const { inputA, inputB, experiment, wrapper } = factory({
      initB: "initialValue",
    });
    expect(wrapper.vm.show).toBeFalsy();
    expect(wrapper.vm.data).toBe(null);
    expect(wrapper.vm.oldValue).toBe("initialValue");

    inputA.value = "2";
    experiment.evaluateInputDependencies();
    expect(inputB.show).toBeTruthy();
    wrapper.setData({ show: inputB.show });
    expect(wrapper.vm.data).toEqual("initialValue");
    expect(wrapper.vm.oldValue).toEqual("initialValue");
  });

  test("if hidden from the start, should emit 'input' event with null", (done) => {
    // Run test asynchronously so we can capture events that are emitted by VModelMixin watcher
    const { wrapper } = factory({ initB: "initialValue", sync: false });
    expect(wrapper.vm.show).toBeFalsy();
    expect(wrapper.vm.data).toBe(null);
    expect(wrapper.vm.value).toBe("initialValue");
    expect(wrapper.vm.oldValue).toBe("initialValue");

    wrapper.vm.$nextTick(() => {
      expect(wrapper.emitted().input.length).toBe(1);
      expect(wrapper.emitted().input[0]).toEqual([null]);
      done();
    });
  });

  test("if hidden from the start, should emit initial value 'input' event when showing", (done) => {
    // Run test asynchronously so we can capture events that are emitted by VModelMixin watcher
    const { inputA, inputB, experiment, wrapper } = factory({
      initB: "initialValue",
      sync: false,
    });
    expect(wrapper.vm.show).toBeFalsy();
    expect(wrapper.vm.data).toBe(null);
    expect(wrapper.vm.oldValue).toBe("initialValue");

    inputA.value = "2";
    experiment.evaluateInputDependencies();
    expect(inputB.show).toBeTruthy();
    wrapper.setData({ show: inputB.show });
    expect(wrapper.vm.show).toBeTruthy();
    wrapper.vm.$nextTick(() => {
      expect(wrapper.emitted().input.length).toBe(2);
      expect(wrapper.emitted().input[0]).toEqual([null]);
      expect(wrapper.emitted().input[1]).toEqual(["initialValue"]);
      done();
    });
  });
});
