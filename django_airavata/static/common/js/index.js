import ApplicationCard from './components/ApplicationCard.vue'
import Autocomplete from './components/Autocomplete.vue'
import AutocompleteTextInput from './components/AutocompleteTextInput.vue'
import NotificationsDisplay from './components/NotificationsDisplay.vue'
import Pager from './components/Pager.vue'
import ShareButton from './components/ShareButton.vue'

import GlobalErrorHandler from './errors/GlobalErrorHandler'

import ListLayout from './layouts/ListLayout.vue'

import Notification from './notifications/Notification'
import NotificationList from './notifications/NotificationList'

import * as utils from './utils'

exports.components = {
  Pager,
  ApplicationCard,
  Autocomplete,
  AutocompleteTextInput,
  NotificationsDisplay,
  ShareButton,
}

exports.errors = {
  GlobalErrorHandler,
}

exports.layouts = {
  ListLayout,
}

exports.notifications = {
  Notification,
  NotificationList,
}

exports.utils = utils;
