import { utils } from "django-airavata-api";

// Set to true to enable test mode, which will use the test_output_file
// attribute on the Output View Provider class instead of the actual output file
const TEST_MODE = false;

export default class OutputViewDataLoader {
  constructor({ url, experimentId, experimentOutputName, providerId }) {
    this.url = url;
    this.experimentId = experimentId;
    this.experimentOutputName = experimentOutputName;
    this.providerId = providerId;
    this.data = null;
  }

  load(newParams = null) {
    if (newParams && this.data) {
      this.data.interactive = newParams;
    }
    return utils.FetchUtils.get(this.url, {
      "experiment-id": this.experimentId,
      "experiment-output-name": this.experimentOutputName,
      "provider-id": this.providerId,
      "test-mode": TEST_MODE,
      ...this.createInteractiveParams(),
    }).then((resp) => {
      this.data = resp;
      return resp;
    });
  }

  createInteractiveParams() {
    const params = {};
    const meta = {};
    if (this.data && this.data.interactive) {
      this.data.interactive.forEach((p) => {
        params[p.name] = p.value;
        meta[p.name] = {
          type: p.type,
        };
      });
    }
    if (Object.keys(meta).length > 0) {
      // Special _meta query parameter holds type information, which is needed
      // when the type of a parameter can't be inferred when it is missing a
      // default value
      params._meta = JSON.stringify(meta);
    }
    return params;
  }
}
