import { services } from "../../js/index";
import ApplicationInterfaceDefinition from "../../js/models/ApplicationInterfaceDefinition";
import GroupResourceProfile from "../../js/models/GroupResourceProfile";
import ApplicationDeploymentDescription from "../../js/models/ApplicationDeploymentDescription";
import BatchQueue from "../../js/models/BatchQueue";
import { createExperiment } from "../../js/utils/ExperimentUtils";

// Mock out 'index' so that RESTful service calls can be mocked
jest.mock("../../js/index");

beforeEach(() => {
  jest.resetAllMocks();
});

test("error thrown when no applicationName given", async () => {
  try {
    expect.assertions(2);
    await createExperiment();
  } catch (e) {
    expect(e).toBeInstanceOf(Error);
    expect(e.message).toEqual(
      "Either applicationInterfaceId or applicationId or applicationName is required"
    );
  }
});

test("error thrown with applicationName doesn't match any interfaces", async () => {
  services.ApplicationInterfaceService.list.mockResolvedValue([
    new ApplicationInterfaceDefinition({
      applicationName: "Foo",
      applicationModules: ["Foo_module1"],
    }),
    new ApplicationInterfaceDefinition({
      applicationName: "Bar",
      applicationModules: ["bar_module1"],
    }),
  ]);
  try {
    expect.assertions(2);
    await createExperiment({ applicationName: "test" });
  } catch (e) {
    expect(e).toBeInstanceOf(Error);
    expect(e.message).toEqual(
      "Could not find application interface named test"
    );
  }
});

test("verify if applicationId and applicationName are given, applicationInterface is loaded with applicationId", async () => {
  services.ApplicationModuleService.getApplicationInterface.mockResolvedValue(
    new ApplicationInterfaceDefinition({
      applicationName: "Foo",
      applicationModules: ["Foo_module1"],
    })
  );
  try {
    expect.assertions(2);
    await createExperiment({
      applicationId: "Foo_module1",
      applicationName: "Foo",
    });
  } catch (e) {
    expect(services.ApplicationModuleService.list).not.toHaveBeenCalled();
    expect(
      services.ApplicationModuleService.getApplicationInterface
    ).toHaveBeenCalledWith({
      lookup: "Foo_module1",
    });
  }
});

test("verify if applicationInterfaceId and applicationId and applicationName are given, applicationInterface is loaded with applicationId", async () => {
  services.ApplicationInterfaceService.retrieve.mockResolvedValue(
    new ApplicationInterfaceDefinition({
      applicationInterfaceId: "Foo_interface1",
      applicationName: "Foo",
      applicationModules: ["Foo_module1"],
    })
  );
  try {
    expect.assertions(3);
    await createExperiment({
      applicationInterfaceId: "Foo_interface1",
      applicationId: "Foo_module1",
      applicationName: "Foo",
    });
  } catch (e) {
    expect(services.ApplicationModuleService.getApplicationInterface).not.toHaveBeenCalled();
    expect(services.ApplicationInterfaceService.list).not.toHaveBeenCalled();
    expect(
      services.ApplicationInterfaceService.retrieve
    ).toHaveBeenCalledWith({
      lookup: "Foo_interface1",
    });
  }
});

test("error thrown when no computeResourceName given", async () => {
  services.ApplicationInterfaceService.list.mockResolvedValue([
    new ApplicationInterfaceDefinition({
      applicationName: "test",
      applicationModules: ["test_module1"],
    }),
    new ApplicationInterfaceDefinition({
      applicationName: "Bar",
      applicationModules: ["bar_module1"],
    }),
  ]);
  try {
    expect.assertions(2);
    await createExperiment({ applicationName: "test" });
  } catch (e) {
    expect(e).toBeInstanceOf(Error);
    expect(e.message).toEqual("computeResourceName is required");
  }
});

test("error thrown when computeResourceName doesn't match any compute resources", async () => {
  services.ApplicationInterfaceService.list.mockResolvedValue([
    new ApplicationInterfaceDefinition({
      applicationName: "test",
      applicationModules: ["test_module1"],
    }),
    new ApplicationInterfaceDefinition({
      applicationName: "Bar",
      applicationModules: ["bar_module1"],
    }),
  ]);
  services.ComputeResourceService.names.mockResolvedValue({
    "compute1.resource.org_id1": "compute1.resource.org",
    "compute2.resource.org_id2": "compute2.resource.org",
  });
  try {
    expect.assertions(2);
    await createExperiment({
      applicationName: "test",
      computeResourceName: "nonexistent.compute.resource.org",
    });
  } catch (e) {
    expect(e).toBeInstanceOf(Error);
    expect(e.message).toEqual(
      "Could not find compute resource with name nonexistent.compute.resource.org"
    );
  }
});

test("error thrown when no GRP found for compute resource", async () => {
  services.ApplicationInterfaceService.list.mockResolvedValue([
    new ApplicationInterfaceDefinition({
      applicationName: "test",
      applicationModules: ["test_module1"],
    }),
    new ApplicationInterfaceDefinition({
      applicationName: "Bar",
      applicationModules: ["bar_module1"],
    }),
  ]);
  services.ComputeResourceService.names.mockResolvedValue({
    "compute1.resource.org_id1": "compute1.resource.org",
    "compute2.resource.org_id2": "compute2.resource.org",
  });
  // Have mock GRP response be an empty list
  services.GroupResourceProfileService.list.mockResolvedValue([]);
  try {
    expect.assertions(2);
    await createExperiment({
      applicationName: "test",
      computeResourceName: "compute1.resource.org",
    });
  } catch (e) {
    expect(e).toBeInstanceOf(Error);
    expect(e.message).toEqual(
      "Couldn't find a group resource profile for compute resource compute1.resource.org_id1"
    );
  }
});

test("error thrown when no deployment found for compute resource", async () => {
  services.ApplicationInterfaceService.list.mockResolvedValue([
    new ApplicationInterfaceDefinition({
      applicationName: "test",
      applicationModules: ["test_module1"],
    }),
    new ApplicationInterfaceDefinition({
      applicationName: "Bar",
      applicationModules: ["bar_module1"],
    }),
  ]);
  services.ComputeResourceService.names.mockResolvedValue({
    "compute1.resource.org_id1": "compute1.resource.org",
    "compute2.resource.org_id2": "compute2.resource.org",
  });
  services.GroupResourceProfileService.list.mockResolvedValue([
    new GroupResourceProfile({
      groupResourceProfileId: "groupResourceProfileId1",
      computePreferences: [
        {
          computeResourceId: "compute1.resource.org_id1",
        },
      ],
    }),
  ]);
  // Just return an empty list
  services.ApplicationDeploymentService.list.mockResolvedValue([]);
  try {
    expect.assertions(3);
    await createExperiment({
      applicationName: "test",
      computeResourceName: "compute1.resource.org",
    });
  } catch (e) {
    expect(services.ApplicationDeploymentService.list).toHaveBeenCalledWith({
      appModuleId: "test_module1",
      groupResourceProfileId: "groupResourceProfileId1",
    });
    expect(e).toBeInstanceOf(Error);
    expect(e.message).toEqual(
      "Couldn't find a deployment for compute resource compute1.resource.org_id1"
    );
  }
});

test("verify that default queue values are used in computationalResourceScheduling", async () => {
  services.ApplicationInterfaceService.list.mockResolvedValue([
    new ApplicationInterfaceDefinition({
      applicationName: "test",
      applicationModules: ["test_module1"],
    }),
    new ApplicationInterfaceDefinition({
      applicationName: "Bar",
      applicationModules: ["bar_module1"],
    }),
  ]);
  services.ComputeResourceService.names.mockResolvedValue({
    "compute1.resource.org_id1": "compute1.resource.org",
    "compute2.resource.org_id2": "compute2.resource.org",
  });
  services.GroupResourceProfileService.list.mockResolvedValue([
    new GroupResourceProfile({
      groupResourceProfileId: "groupResourceProfileId1",
      computePreferences: [
        {
          computeResourceId: "compute1.resource.org_id1",
        },
      ],
    }),
  ]);
  services.ApplicationDeploymentService.list.mockResolvedValue([
    new ApplicationDeploymentDescription({
      appDeploymentId: "appDeploymentId1",
      computeHostId: "compute1.resource.org_id1",
    }),
  ]);
  services.ApplicationDeploymentService.getQueues.mockResolvedValue([
    new BatchQueue({
      queueName: "queue1",
      isDefaultQueue: false,
      defaultCPUCount: 10,
      defaultNodeCount: 11,
      defaultWalltime: 12,
    }),
    new BatchQueue({
      queueName: "queue2",
      isDefaultQueue: true,
      defaultCPUCount: 20,
      defaultNodeCount: 21,
      defaultWalltime: 22,
    }),
  ]);
  services.WorkspacePreferencesService.get.mockResolvedValue({
    most_recent_project_id: "project1",
  });
  const experiment = await createExperiment({
    applicationName: "test",
    computeResourceName: "compute1.resource.org",
  });
  expect(
    experiment.userConfigurationData.computationalResourceScheduling
      .resourceHostId
  ).toBe("compute1.resource.org_id1");
  expect(
    experiment.userConfigurationData.computationalResourceScheduling
      .totalCPUCount
  ).toBe(20);
  expect(
    experiment.userConfigurationData.computationalResourceScheduling.nodeCount
  ).toBe(21);
  expect(
    experiment.userConfigurationData.computationalResourceScheduling
      .wallTimeLimit
  ).toBe(22);
  expect(
    experiment.userConfigurationData.computationalResourceScheduling.queueName
  ).toBe("queue2");
});

test("verify that experiment name is the given name", async () => {
  services.ApplicationInterfaceService.list.mockResolvedValue([
    new ApplicationInterfaceDefinition({
      applicationName: "test",
      applicationModules: ["test_module1"],
    }),
  ]);
  services.ComputeResourceService.names.mockResolvedValue({
    "compute1.resource.org_id1": "compute1.resource.org",
  });
  services.GroupResourceProfileService.list.mockResolvedValue([
    new GroupResourceProfile({
      groupResourceProfileId: "groupResourceProfileId1",
      computePreferences: [
        {
          computeResourceId: "compute1.resource.org_id1",
        },
      ],
    }),
  ]);
  services.ApplicationDeploymentService.list.mockResolvedValue([
    new ApplicationDeploymentDescription({
      appDeploymentId: "appDeploymentId1",
      computeHostId: "compute1.resource.org_id1",
    }),
  ]);
  services.ApplicationDeploymentService.getQueues.mockResolvedValue([
    new BatchQueue({
      queueName: "queue2",
      isDefaultQueue: true,
      defaultCPUCount: 20,
      defaultNodeCount: 21,
      defaultWalltime: 22,
    }),
  ]);
  services.WorkspacePreferencesService.get.mockResolvedValue({
    most_recent_project_id: "project1",
  });
  const experiment = await createExperiment({
    experimentName: "My Experiment",
    applicationName: "test",
    computeResourceName: "compute1.resource.org",
  });
  expect(experiment.experimentName).toBe("My Experiment");
});

test("verify that if no experiment name is given, name is based on experiment name", async () => {
  services.ApplicationInterfaceService.list.mockResolvedValue([
    new ApplicationInterfaceDefinition({
      applicationName: "test",
      applicationModules: ["test_module1"],
    }),
  ]);
  services.ComputeResourceService.names.mockResolvedValue({
    "compute1.resource.org_id1": "compute1.resource.org",
  });
  services.GroupResourceProfileService.list.mockResolvedValue([
    new GroupResourceProfile({
      groupResourceProfileId: "groupResourceProfileId1",
      computePreferences: [
        {
          computeResourceId: "compute1.resource.org_id1",
        },
      ],
    }),
  ]);
  services.ApplicationDeploymentService.list.mockResolvedValue([
    new ApplicationDeploymentDescription({
      appDeploymentId: "appDeploymentId1",
      computeHostId: "compute1.resource.org_id1",
    }),
  ]);
  services.ApplicationDeploymentService.getQueues.mockResolvedValue([
    new BatchQueue({
      queueName: "queue2",
      isDefaultQueue: true,
      defaultCPUCount: 20,
      defaultNodeCount: 21,
      defaultWalltime: 22,
    }),
  ]);
  services.WorkspacePreferencesService.get.mockResolvedValue({
    most_recent_project_id: "project1",
  });
  const experiment = await createExperiment({
    applicationName: "test",
    computeResourceName: "compute1.resource.org",
  });
  // Date string doesn't include seconds, so it should match exactly
  const dateString = new Date().toLocaleString([], {
    dateStyle: "medium",
    timeStyle: "short",
  });
  expect(experiment.experimentName).toBe(`test on ${dateString}`);
});

test("verify that application inputs and outputs are cloned on experiment", async () => {
  services.ApplicationInterfaceService.list.mockResolvedValue([
    new ApplicationInterfaceDefinition({
      applicationName: "test",
      applicationModules: ["test_module1"],
      applicationInputs: [
        {
          name: "appInput1",
        },
        {
          name: "appInput2",
        },
      ],
      applicationOutputs: [
        {
          name: "appOutput1",
        },
        {
          name: "appOutput2",
        },
      ],
    }),
  ]);
  services.ComputeResourceService.names.mockResolvedValue({
    "compute1.resource.org_id1": "compute1.resource.org",
  });
  services.GroupResourceProfileService.list.mockResolvedValue([
    new GroupResourceProfile({
      groupResourceProfileId: "groupResourceProfileId1",
      computePreferences: [
        {
          computeResourceId: "compute1.resource.org_id1",
        },
      ],
    }),
  ]);
  services.ApplicationDeploymentService.list.mockResolvedValue([
    new ApplicationDeploymentDescription({
      appDeploymentId: "appDeploymentId1",
      computeHostId: "compute1.resource.org_id1",
    }),
  ]);
  services.ApplicationDeploymentService.getQueues.mockResolvedValue([
    new BatchQueue({
      queueName: "queue2",
      isDefaultQueue: true,
      defaultCPUCount: 20,
      defaultNodeCount: 21,
      defaultWalltime: 22,
    }),
  ]);
  services.WorkspacePreferencesService.get.mockResolvedValue({
    most_recent_project_id: "project1",
  });
  const experiment = await createExperiment({
    applicationName: "test",
    computeResourceName: "compute1.resource.org",
  });
  expect(
    experiment.experimentInputs.find((i) => i.name === "appInput1")
  ).toBeDefined();
  expect(
    experiment.experimentInputs.find((i) => i.name === "appInput2")
  ).toBeDefined();
  expect(experiment.experimentInputs.length).toBe(2);
  expect(
    experiment.experimentOutputs.find((i) => i.name === "appOutput1")
  ).toBeDefined();
  expect(
    experiment.experimentOutputs.find((i) => i.name === "appOutput2")
  ).toBeDefined();
  expect(experiment.experimentOutputs.length).toBe(2);
});

test("verify that projectId is copied from preferences", async () => {
  services.ApplicationInterfaceService.list.mockResolvedValue([
    new ApplicationInterfaceDefinition({
      applicationName: "test",
      applicationModules: ["test_module1"],
    }),
  ]);
  services.ComputeResourceService.names.mockResolvedValue({
    "compute1.resource.org_id1": "compute1.resource.org",
  });
  services.GroupResourceProfileService.list.mockResolvedValue([
    new GroupResourceProfile({
      groupResourceProfileId: "groupResourceProfileId1",
      computePreferences: [
        {
          computeResourceId: "compute1.resource.org_id1",
        },
      ],
    }),
  ]);
  services.ApplicationDeploymentService.list.mockResolvedValue([
    new ApplicationDeploymentDescription({
      appDeploymentId: "appDeploymentId1",
      computeHostId: "compute1.resource.org_id1",
    }),
  ]);
  services.ApplicationDeploymentService.getQueues.mockResolvedValue([
    new BatchQueue({
      queueName: "queue2",
      isDefaultQueue: true,
      defaultCPUCount: 20,
      defaultNodeCount: 21,
      defaultWalltime: 22,
    }),
  ]);
  services.WorkspacePreferencesService.get.mockResolvedValue({
    most_recent_project_id: "project1",
  });
  const experiment = await createExperiment({
    applicationName: "test",
    computeResourceName: "compute1.resource.org",
  });
  expect(experiment.projectId).toBe("project1");
});

test("verify that given input values are copied to experiment", async () => {
  services.ApplicationInterfaceService.list.mockResolvedValue([
    new ApplicationInterfaceDefinition({
      applicationName: "test",
      applicationModules: ["test_module1"],
      applicationInputs: [
        {
          name: "appInput1",
          value: "default1",
        },
        {
          name: "appInput2",
        },
      ],
    }),
  ]);
  services.ComputeResourceService.names.mockResolvedValue({
    "compute1.resource.org_id1": "compute1.resource.org",
  });
  services.GroupResourceProfileService.list.mockResolvedValue([
    new GroupResourceProfile({
      groupResourceProfileId: "groupResourceProfileId1",
      computePreferences: [
        {
          computeResourceId: "compute1.resource.org_id1",
        },
      ],
    }),
  ]);
  services.ApplicationDeploymentService.list.mockResolvedValue([
    new ApplicationDeploymentDescription({
      appDeploymentId: "appDeploymentId1",
      computeHostId: "compute1.resource.org_id1",
    }),
  ]);
  services.ApplicationDeploymentService.getQueues.mockResolvedValue([
    new BatchQueue({
      queueName: "queue2",
      isDefaultQueue: true,
      defaultCPUCount: 20,
      defaultNodeCount: 21,
      defaultWalltime: 22,
    }),
  ]);
  services.WorkspacePreferencesService.get.mockResolvedValue({
    most_recent_project_id: "project1",
  });
  const experiment = await createExperiment({
    applicationName: "test",
    computeResourceName: "compute1.resource.org",
    experimentInputs: {
      appInput1: "value1",
      appInput2: "value2",
    },
  });
  expect(
    experiment.experimentInputs.find((i) => i.name === "appInput1").value
  ).toBe("value1");
  expect(
    experiment.experimentInputs.find((i) => i.name === "appInput2").value
  ).toBe("value2");

  // Don't pass appInput1 and take the default value instead
  const experiment2 = await createExperiment({
    applicationName: "test",
    computeResourceName: "compute1.resource.org",
    experimentInputs: {
      // "appInput1": "value1",
      appInput2: "value2",
    },
  });
  expect(
    experiment2.experimentInputs.find((i) => i.name === "appInput1").value
  ).toBe("default1");
  expect(
    experiment2.experimentInputs.find((i) => i.name === "appInput2").value
  ).toBe("value2");
});
