import { utils } from "django-airavata-api";

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
      ...this.createInteractiveParams()
    }).then(resp => {
      this.data = resp;
      return resp;
    });
  }

  createInteractiveParams() {
    const params = {};
    if (this.data && this.data.interactive) {
      this.data.interactive.forEach(p => {
        params[p.name] = p.value;
      });
    }
    return params;
  }
}
