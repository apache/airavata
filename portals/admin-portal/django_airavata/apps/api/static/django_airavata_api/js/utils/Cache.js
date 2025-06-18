const DEFAULT_EXPIRATION_TIME_MS = 5 * 60 * 1000;

class CacheEntry {
  constructor(value, expireDate) {
    this._value = value;
    this._expireDate = expireDate;
  }

  get value() {
    return this._value;
  }

  get isExpired() {
    return this._expireDate.getTime() < Date.now();
  }
}

export default class Cache {
  constructor() {
    this._cache = {};
  }

  get(key) {
    if (this.has(key)) {
      const cacheEntry = this._cache[key];
      return cacheEntry.value;
    } else {
      return null;
    }
  }

  put({
    key,
    value,
    expireDate = new Date(Date.now() + DEFAULT_EXPIRATION_TIME_MS),
  }) {
    this._cache[key] = new CacheEntry(value, expireDate);
  }

  has(key) {
    if (this._cache.hasOwnProperty(key)) {
      const cacheEntry = this._cache[key];
      if (cacheEntry.isExpired) {
        delete this._cache[key];
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }
}
