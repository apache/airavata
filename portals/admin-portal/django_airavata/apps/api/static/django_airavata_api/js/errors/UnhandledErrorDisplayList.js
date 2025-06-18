class UnhandledErrorDisplayList {
  constructor() {
    this.unhandledErrors = [];
  }

  add(unhandledError) {
    this.unhandledErrors.push(unhandledError);
  }

  remove(unhandledError) {
    const i = this.unhandledErrors.indexOf(unhandledError);
    this.unhandledErrors.splice(i, 1);
  }

  get list() {
    return this.unhandledErrors;
  }
}

export default new UnhandledErrorDisplayList();
