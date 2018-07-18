
export function getProperty(obj, props) {
    if (typeof props === 'string') {
        return obj[props];
    } else if (typeof props === 'object' && props instanceof Array) { // Array
        return props.reduce((o, prop) => o && prop in o ? o[prop] : undefined, obj);
    }
}