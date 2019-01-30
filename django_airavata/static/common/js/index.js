import ApplicationCard from "./components/ApplicationCard.vue";
import AutocompleteTextInput from "./components/AutocompleteTextInput.vue";
import ClipboardCopyButton from "./components/ClipboardCopyButton.vue";
import ClipboardCopyLink from "./components/ClipboardCopyLink.vue";
import ConfirmationDialog from "./components/ConfirmationDialog.vue";
import DeleteButton from "./components/DeleteButton.vue";
import DeleteLink from "./components/DeleteLink.vue";
import NotificationsDisplay from "./components/NotificationsDisplay.vue";
import Pager from "./components/Pager.vue";
import ShareButton from "./components/ShareButton.vue";
import UnsavedChangesGuard from "./components/UnsavedChangesGuard.vue";

import GlobalErrorHandler from "./errors/GlobalErrorHandler";
import ValidationErrors from "./errors/ValidationErrors";

import ListLayout from "./layouts/ListLayout.vue";

import VModelMixin from "./mixins/VModelMixin";

import Notification from "./notifications/Notification";
import NotificationList from "./notifications/NotificationList";

import * as utils from "./utils";

import entry from "./entry";

const components = {
  Pager,
  ApplicationCard,
  AutocompleteTextInput,
  ClipboardCopyButton,
  ClipboardCopyLink,
  ConfirmationDialog,
  DeleteButton,
  DeleteLink,
  NotificationsDisplay,
  ShareButton,
  UnsavedChangesGuard
};

const errors = {
  GlobalErrorHandler,
  ValidationErrors
};

const layouts = {
  ListLayout
};

const mixins = {
  VModelMixin
};

const notifications = {
  Notification,
  NotificationList
};

export default {
  components,
  entry,
  errors,
  layouts,
  mixins,
  notifications,
  utils
};

export {
  components,
  entry,
  errors,
  layouts,
  mixins,
  notifications,
  utils
};
