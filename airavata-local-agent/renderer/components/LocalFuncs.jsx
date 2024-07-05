
export function getLocalStgKey(key) {
    if (localStorage) {
        return localStorage.getItem(key);
    }
}