
export default class BaseEnum {
    constructor(name, value) {
        this.name = name;
        this.value = value;
        // immutable
        Object.freeze(this);
    }
    toJSON() {
        return this.value;
    }
    static byName(name) {
        return this.values.find(x => x.name === name);
    }
    static byValue(value) {
        return this.values.find(x => x.value === value);
    }
    // This must be called to initialize static methods on the Enum subclass
    static init(names) {
        const enums = names.map((name, index) => new this(name, index));
        Object.freeze(enums);
        Object.defineProperty(this, 'values', {get: function() { return enums;}});
        this.values.forEach(v => this[v.name] = v);
    }
}
