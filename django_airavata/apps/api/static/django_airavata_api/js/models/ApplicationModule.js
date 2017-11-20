import BaseModel from './BaseModel';

export default class ApplicationModule extends BaseModel {
    constructor(data = {}) {
        super(data);
        this.appModuleId = null;
        this.appModuleName = null;
        this.appModuleVersion = null;
        this.appModuleDescription = null;
        this.copyData(data);
    }

    validateForCreate() {
        if (this.appModuleName === null || this.appModuleName.trim() === "") {
            return {
                name: ["Please provide a name."]
            }
        }
        return null;
    }

    toJSONForCreate() {
        // Remaining fields just get defaulted
        return JSON.stringify(this, ["appModuleName", "appModuleVersion", "appModuleDescription"]);
    }

    toJSONForUpdate() {
        return JSON.stringify(this, ["appModuleId", "appModuleName", "appModuleVersion", "appModuleDescription"]);
    }
}
