import ApplicationCard from "./components/ApplicationCard.vue";
import Autocomplete from "./components/Autocomplete.vue";
import AutocompleteTextInput from "./components/AutocompleteTextInput.vue";
import ConfirmationDialog from "./components/ConfirmationDialog.vue";
import DeleteButton from "./components/DeleteButton.vue";
import DeleteLink from "./components/DeleteLink.vue";
import NotificationsDisplay from "./components/NotificationsDisplay.vue";
import Pager from "./components/Pager.vue";
import ShareButton from "./components/ShareButton.vue";
import UnsavedChangesGuard from "./components/UnsavedChangesGuard.vue";

import GlobalErrorHandler from "./errors/GlobalErrorHandler";

import ListLayout from "./layouts/ListLayout.vue";

import Notification from "./notifications/Notification";
import NotificationList from "./notifications/NotificationList";

import * as utils from "./utils";

exports.components = {
  Pager,
  ApplicationCard,
  Autocomplete,
  AutocompleteTextInput,
  ConfirmationDialog,
  DeleteButton,
  DeleteLink,
  NotificationsDisplay,
  ShareButton,
  UnsavedChangesGuard
};

exports.errors = {
  GlobalErrorHandler
};

exports.layouts = {
  ListLayout
};

exports.notifications = {
  Notification,
  NotificationList
};

exports.utils = utils;
