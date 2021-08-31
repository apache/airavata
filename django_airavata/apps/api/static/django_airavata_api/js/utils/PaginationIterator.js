import FetchUtils from "./FetchUtils";

export default class PaginationIterator {
  constructor(pagedResponse, resultType = null) {
    this.resultType = resultType;
    this.processResponse(pagedResponse);
  }

  next() {
    return FetchUtils.get(this._next).then((json) =>
      this.processResponse(json)
    );
  }

  hasNext() {
    return this._next != null;
  }

  previous() {
    return FetchUtils.get(this._previous).then((json) =>
      this.processResponse(json)
    );
  }

  hasPrevious() {
    return this._previous != null;
  }

  processResponse(pagedResponse) {
    this._next = pagedResponse.next;
    this._previous = pagedResponse.previous;
    if (this.resultType) {
      if (Array.isArray(pagedResponse.results)) {
        this.results = pagedResponse.results.map(
          (result) => new this.resultType(result)
        );
      } else {
        this.results = new this.resultType(pagedResponse.results);
      }
    } else {
      this.results = pagedResponse.results;
    }
    this.offset = pagedResponse.offset;
    this.limit = pagedResponse.limit;
    this.count = pagedResponse.count;
    return this;
  }

  toArray() {
    let results = [].concat(this.results);
    while (this.hasNext()) {
      results = results.concat(this.next().results);
    }
    return results;
  }
}
