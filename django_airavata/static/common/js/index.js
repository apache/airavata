import ApplicationCard from "./components/ApplicationCard.vue";
import ApplicationName from "./components/ApplicationName";
import AutocompleteTextInput from "./components/AutocompleteTextInput.vue";
import ClipboardCopyButton from "./components/ClipboardCopyButton.vue";
import ClipboardCopyLink from "./components/ClipboardCopyLink.vue";
import ComputeResourceName from "./components/ComputeResourceName";
import ConfirmationDialog from "./components/ConfirmationDialog.vue";
import DataProductViewer from "./components/DataProductViewer";
import DeleteButton from "./components/DeleteButton.vue";
import DeleteLink from "./components/DeleteLink.vue";
import ExperimentStatusBadge from "./components/ExperimentStatusBadge";
import FavoriteToggle from "./components/FavoriteToggle";
import GatewayGroupsBadge from "./components/GatewayGroupsBadge";
import HumanDate from "./components/HumanDate.vue";
import MainLayout from "./components/MainLayout.vue";
import Pager from "./components/Pager.vue";
import ShareButton from "./components/ShareButton.vue";
import Sidebar from "./components/Sidebar.vue";
import SidebarFeed from "./components/SidebarFeed.vue";
import SidebarHeader from "./components/SidebarHeader.vue";
import UnsavedChangesGuard from "./components/UnsavedChangesGuard.vue";
import Uppy from "./components/Uppy";

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
  ApplicationName,
  AutocompleteTextInput,
  ClipboardCopyButton,
  ClipboardCopyLink,
  ComputeResourceName,
  ConfirmationDialog,
  DataProductViewer,
  DeleteButton,
  DeleteLink,
  ExperimentStatusBadge,
  FavoriteToggle,
  GatewayGroupsBadge,
  HumanDate,
  MainLayout,
  ShareButton,
  Sidebar,
  SidebarFeed,
  SidebarHeader,
  UnsavedChangesGuard,
  Uppy
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

export { components, entry, errors, layouts, mixins, notifications, utils };
