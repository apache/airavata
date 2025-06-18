module.exports = {
  preset: "@vue/cli-plugin-unit-jest",
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/static/django_airavata_admin/src/$1",
    // Ignore style imports. From https://jestjs.io/docs/en/webpack#handling-static-assets
    "\\.(css|less)$":
      "<rootDir>/static/django_airavata_admin/tests/__mocks__/styleMock.js",
  },
};
