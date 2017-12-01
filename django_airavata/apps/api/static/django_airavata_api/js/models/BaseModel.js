
export default class BaseModel {
    constructor(data = {}) {
    }
    copyData(data) {
        for (let prop in this) {
            this[prop] = data[prop] || null;
        }
    }
}
