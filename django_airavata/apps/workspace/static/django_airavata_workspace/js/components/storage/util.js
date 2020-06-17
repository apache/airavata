/**
 * @returns {string} return the user storage path with an ending slash.
 */
export function getStoragePath($route) {
  if ($route) {
    let _storagePath = /~.*$/.exec($route.fullPath);
    if (_storagePath && _storagePath.length > 0) {
      _storagePath = _storagePath[0];
    } else {
      _storagePath = $route.path;
    }

    // Validate to have the ending slash.
    if (!_storagePath.endsWith("/")) {
      _storagePath += "/";
    }

    return _storagePath;
  }
}
