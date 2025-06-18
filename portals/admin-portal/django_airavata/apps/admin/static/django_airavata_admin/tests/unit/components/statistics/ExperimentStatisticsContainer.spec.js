import { render, fireEvent, within } from "@testing-library/vue";
import "@testing-library/jest-dom";
import ExperimentStatisticsContainer from "@/components/statistics/ExperimentStatisticsContainer.vue";
import Vue from "vue";
import BootstrapVue from "bootstrap-vue";
import VueFlatPickr from "vue-flatpickr-component";

Vue.use(BootstrapVue);
Vue.use(VueFlatPickr);

import { models, services, utils } from "django-airavata-api";
import ExperimentStatus from "django-airavata-api/static/django_airavata_api/js/models/ExperimentStatus";
jest.mock("django-airavata-api", () => {
  const originalModule = jest.requireActual("django-airavata-api");
  return {
    __esModule: true,
    ...originalModule,
    // Mock just the RESTful service calls
    services: {
      ApplicationInterfaceService: {
        list: jest.fn(),
      },
      ExperimentStatisticsService: {
        get: jest.fn(),
      },
      ComputeResourceService: {
        namesList: jest.fn(),
      },
      ExperimentSearchService: {
        list: jest.fn(),
      },
      ExperimentService: {
        retrieve: jest.fn(),
      },
      FullExperimentService: {
        retrieve: jest.fn(),
      },
      GroupResourceProfileService: {
        list: jest.fn(),
      },
      ExperimentArchiveService: {
        get: jest.fn(),
      },
    },
  };
});

beforeEach(() => {
  jest.resetAllMocks();

  const spinner = document.createElement("div");
  spinner.id = "airavata-spinner";
  document.body.appendChild(spinner);

  // jsdom doesn't implement scrollIntoView so just provide a stubbed implementation
  Element.prototype.scrollIntoView = jest.fn();
});

test("load experiment by job id when job id matches unique experiment", async () => {
  // Service call mocks
  services.ApplicationInterfaceService.list.mockResolvedValue([]);
  services.ExperimentStatisticsService.get.mockResolvedValue(
    new utils.PaginationIterator(
      {
        count: 0,
        next: null,
        previous: null,
        results: {
          allExperimentCount: 0,
          completedExperimentCount: 0,
          cancelledExperimentCount: 0,
          failedExperimentCount: 0,
          createdExperimentCount: 0,
          runningExperimentCount: 0,
          allExperiments: [],
          completedExperiments: [],
          failedExperiments: [],
          cancelledExperiments: [],
          createdExperiments: [],
          runningExperiments: [],
        },
        limit: 50,
        offset: 0,
      },
      models.ExperimentStatistics
    )
  );
  services.ComputeResourceService.namesList.mockResolvedValue([]);
  services.ExperimentSearchService.list.mockResolvedValue(
    new utils.PaginationIterator(
      {
        count: 1,
        next: null,
        previous: null,
        results: [{ experimentId: "test-experiment-id" }],
      },
      models.ExperimentSummary
    )
  );
  // Mock just enough of Experiment and FullExperiment to get ExperimentDetailsView to render
  const experiment = new models.Experiment({
    experimentId: "test-experiment-id",
    experimentName: "Test Experiment",
    creationTime: Date.now(),
    experimentStatus: [
      new ExperimentStatus({
        timeOfStateChange: Date.now(),
        state: models.ExperimentState.COMPLETED,
      }),
    ],
  });
  services.ExperimentService.retrieve.mockResolvedValue(experiment);
  services.FullExperimentService.retrieve.mockResolvedValue(
    new models.FullExperiment({
      experimentId: "test-experiment-id",
      experiment,
    })
  );
  services.ExperimentArchiveService.get.mockResolvedValue({
    archived: false,
    archive_name: null,
    created_date: null,
    max_age: 90,
  });

  // The render method returns a collection of utilities to query your component.
  const { findByText, findByPlaceholderText } = render(
    ExperimentStatisticsContainer
  );

  const byJobIDTab = await findByText("By Job ID");

  await fireEvent.click(byJobIDTab);

  const jobIDInputField = await findByPlaceholderText("Job ID");

  await fireEvent.update(jobIDInputField, "12345");

  const loadButton = await within(jobIDInputField.parentElement).findByText(
    "Load"
  );

  await fireEvent.click(loadButton);

  // The job's tab has the job id instead of the normal experiment name
  const jobTab = await findByText("Job 12345");

  expect(jobTab).toBeVisible();

  // Double check that the experiment services were called to load the experiment
  expect(services.ExperimentService.retrieve).toHaveBeenCalledWith(
    {
      lookup: experiment.experimentId,
    },
    {
      ignoreErrors: true,
    }
  );
  expect(services.FullExperimentService.retrieve).toHaveBeenCalledWith({
    lookup: experiment.experimentId,
  });
});

test("Hostname filter only shows compute resources that are configured in a GRP", async () => {
  // Service call mocks
  services.ApplicationInterfaceService.list.mockResolvedValue([]);
  services.ExperimentStatisticsService.get.mockResolvedValue(
    new utils.PaginationIterator(
      {
        count: 0,
        next: null,
        previous: null,
        results: {
          allExperimentCount: 0,
          completedExperimentCount: 0,
          cancelledExperimentCount: 0,
          failedExperimentCount: 0,
          createdExperimentCount: 0,
          runningExperimentCount: 0,
          allExperiments: [],
          completedExperiments: [],
          failedExperiments: [],
          cancelledExperiments: [],
          createdExperiments: [],
          runningExperiments: [],
        },
        limit: 50,
        offset: 0,
      },
      models.ExperimentStatistics
    )
  );
  services.ComputeResourceService.namesList.mockResolvedValue([
    { host_id: "compute4-abcd", host: "d-compute4" },
    { host_id: "compute2-abcd", host: "b-compute2" },
    { host_id: "compute5-abcd", host: "e-compute5" },
    { host_id: "compute3-abcd", host: "c-compute3" },
    { host_id: "compute1-abcd", host: "a-compute1" },
  ]);

  services.GroupResourceProfileService.list.mockResolvedValue([
    new models.GroupResourceProfile({
      computePreferences: [
        new models.GroupComputeResourcePreference({
          computeResourceId: "compute1-abcd",
        }),
        new models.GroupComputeResourcePreference({
          computeResourceId: "compute3-abcd",
        }),
      ],
    }),
    new models.GroupResourceProfile({
      computePreferences: [
        new models.GroupComputeResourcePreference({
          computeResourceId: "compute1-abcd",
        }),
        new models.GroupComputeResourcePreference({
          computeResourceId: "compute4-abcd",
        }),
      ],
    }),
  ]);

  // The render method returns a collection of utilities to query your component.
  const { findByText } = render(ExperimentStatisticsContainer);

  const addFiltersMenu = await findByText("Add Filters");

  await fireEvent.click(addFiltersMenu);

  const hostnameMenuItem = await findByText("Hostname");

  await fireEvent.click(hostnameMenuItem);

  const computeResourcesSelect = await findByText(
    "Select compute resource to filter on"
  );

  const options = computeResourcesSelect.parentElement.options;

  expect(options.length).toBe(4);
  // option 0 is the null one ("Select compute resource to filter on")
  // verify that options 1-3 are compute resources 1, 3, 4. That is, verify that
  // filtering worked and that they were sorted.
  expect(options[1].value).toBe("compute1-abcd");
  expect(options[2].value).toBe("compute3-abcd");
  expect(options[3].value).toBe("compute4-abcd");
});
