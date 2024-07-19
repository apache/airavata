class Logger {
    constructor() {
        this.levels = {
            INFO: 'INFO',
            WARN: 'WARN',
            ERROR: 'ERROR'
        };

        this.colors = {
            INFO: 'color: lightblue',
            WARN: 'color: yellow',
            ERROR: 'color: red'
        };
    }

    getTimestamp() {
        return new Date().toISOString();
    }

    log(level, message) {
        if (!this.levels[level]) {
            throw new Error(`Unknown level: ${level}`);
        }

        const timestamp = this.getTimestamp();
        const color = this.colors[level];

        console.log(`%c[${timestamp}] [${level}]%c ${message}`,
            color, '',
        );
    }

    info(message) {
        this.log(this.levels.INFO, message);
    }

    warn(message) {
        this.log(this.levels.WARN, message);
    }

    error(message) {
        this.log(this.levels.ERROR, message);
    }
}


// Example usage:
export const logger = new Logger();

