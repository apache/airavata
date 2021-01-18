export default class BaseEnum {
  // TODO: add parameter
  constructor(name, value, writeName = false) {
    this.name = name;
    this.value = value;
    this.writeName = writeName;
    // immutable
    Object.freeze(this);
  }
  toJSON() {
    return this.writeName ? this.name : this.value;
  }
  static byName(name) {
    return this.values.find((x) => x.name === name);
  }
  static byValue(value) {
    return this.values.find((x) => x.value === value);
  }
  // This must be called to initialize static methods on the Enum subclass
  static init(names, writeName = false) {
    const enums = names.map((name, index) => new this(name, index, writeName));
    Object.freeze(enums);
    Object.defineProperty(this, "values", {
      get: function () {
        return enums;
      },
    });
    this.values.forEach((v) => (this[v.name] = v));
  }
}
