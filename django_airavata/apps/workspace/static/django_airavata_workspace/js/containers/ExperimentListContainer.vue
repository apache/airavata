<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="h4 mb-4">Browse Experiments</h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-body">
            <table class="table table-hover">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Application</th>
                  <th>User</th>
                  <th>Creation Time</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="experiment in experiments"
                  :key="experiment.experimentId"
                >
                  <td>
                    <b-link :href="viewLink(experiment)">{{experiment.name}}</b-link>
                  </td>
                  <td v-if="applicationName(experiment)">{{applicationName(experiment)}}</td>
                  <td
                    v-else
                    class="font-italic text-muted"
                  >N/A</td>
                  <td>{{experiment.userName}}</td>
                  <td>
                    <span :title="experiment.creationTime">{{ fromNow(experiment.creationTime) }}</span>
                  </td>
                  <td>
                    <experiment-status-badge :statusName="experiment.experimentStatus.name" />
                  </td>
                  <td>
                    <!-- if we can't load the application for the experiment
                    (for example, if it was deleted), then user can't edit or
                    clone experiment -->
                    <span v-if="applicationName(experiment)">
                      <b-link
                        v-if="experiment.isEditable && applicationName(experiment)"
                        :href="editLink(experiment)"
                        class="action-link"
                      >Edit
                        <i
                          class="fa fa-edit"
                          aria-hidden="true"
                        ></i>
                      </b-link>
                      <b-link
                        v-else
                        @click="clone(experiment)"
                        class="action-link"
                      >Clone
                        <i
                          class="fa fa-copy"
                          aria-hidden="true"
                        ></i>
                      </b-link>
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
            <pager
              v-bind:paginator="experimentsPaginator"
              v-on:next="nextExperiments"
              v-on:previous="previousExperiments"
            ></pager>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";
import { components as comps } from "django-airavata-common-ui";

import moment from "moment";
import urls from "../utils/urls";

export default {
  props: ["initialExperimentsData"],
  name: "experiment-list-container",
  data() {
    return {
      experimentsPaginator: null,
      applicationInterfaces: {}
    };
  },
  components: {
    pager: comps.Pager,
    "experiment-status-badge": comps.ExperimentStatusBadge
  },
  methods: {
    nextExperiments: function() {
      this.experimentsPaginator.next();
    },
    previousExperiments: function() {
      this.experimentsPaginator.previous();
    },
    fromNow: function(date) {
      return moment(date).fromNow();
    },
    editLink: function(experiment) {
      return urls.editExperiment(experiment);
    },
    viewLink: function(experiment) {
      return urls.viewExperiment(experiment);
    },
    applicationName: function(experiment) {
      if (experiment.executionId in this.applicationInterfaces) {
        if (
          this.applicationInterfaces[experiment.executionId] instanceof
          models.ApplicationInterfaceDefinition
        ) {
          return this.applicationInterfaces[experiment.executionId]
            .applicationName;
        } else if (
          this.applicationInterfaces[experiment.executionId] === null
        ) {
          return null;
        }
      } else {
        const request = services.ApplicationInterfaceService.retrieve(
          {
            lookup: experiment.executionId
          },
          {
            ignoreErrors: true
          }
        )
          .then(result =>
            this.$set(
              this.applicationInterfaces,
              experiment.executionId,
              result
            )
          )
          .catch(() => {
            // Application interface may be deleted
            this.$set(this.applicationInterfaces, experiment.executionId, null);
          });
        this.$set(this.applicationInterfaces, experiment.executionId, request);
      }
      return "...";
    },
    clone(experiment) {
      services.ExperimentService.clone({
        lookup: experiment.experimentId
      }).then(clonedExperiment => {
        urls.navigateToEditExperiment(clonedExperiment);
      });
    }
  },
  computed: {
    experiments: function() {
      return this.experimentsPaginator
        ? this.experimentsPaginator.results
        : null;
    }
  },
  beforeMount: function() {
    services.ExperimentSearchService.list({
      initialData: this.initialExperimentsData
    }).then(result => (this.experimentsPaginator = result));
  }
};
</script>

<style>
</style>
