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
    },
  };
});

// beforeEach(() => {
//   jest.resetAllMocks();
// });

test("load experiment by job id when job id matches unique experiment", async () => {
  const spinner = document.createElement("div");
  spinner.id = "airavata-spinner";
  document.body.appendChild(spinner);

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

  // jsdom doesn't implement scrollIntoView so just provide a stubbed implementation
  Element.prototype.scrollIntoView = jest.fn();

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
  expect(services.ExperimentService.retrieve).toHaveBeenCalledWith({
    lookup: experiment.experimentId,
  });
  expect(services.FullExperimentService.retrieve).toHaveBeenCalledWith({
    lookup: experiment.experimentId,
  });
});
