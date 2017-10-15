
export default class PaginationIterator {

    constructor(pagedResponse, resultType = null) {
        this.resultType = resultType;
        this.processResponse(pagedResponse);
    }

    next() {
        return fetch(this._next, {
            credentials: 'include'
        })
        .then(response => response.json())
        .then(json => this.processResponse(json));
    }

    hasNext() {
        return this._next != null;
    }

    previous() {
        return fetch(this._previous, {
            credentials: 'include'
        })
        .then(response => response.json())
        .then(json => this.processResponse(json));
    }

    hasPrevious() {
        return this._previous != null;
    }

    processResponse(pagedResponse) {
        this._next = pagedResponse.next;
        this._previous = pagedResponse.previous;
        if (this.resultType) {
            this.results = pagedResponse.results.map(result => new this.resultType(result));
        } else {
            this.results = pagedResponse.results;
        }
        this.offset = pagedResponse.offset;
        this.limit = pagedResponse.limit;
        return this;
    }
}