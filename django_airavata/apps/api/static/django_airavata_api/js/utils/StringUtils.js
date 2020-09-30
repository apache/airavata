function sortIgnoreCase(arr, keyFunction) {
  arr.sort((a, b) =>
    keyFunction(a).toLowerCase().localeCompare(keyFunction(b).toLowerCase())
  );
  return arr;
}

export default {
  sortIgnoreCase,
};
