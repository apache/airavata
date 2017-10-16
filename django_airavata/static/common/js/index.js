import Pager from './components/Pager.vue'

import Project from './models/Project'

import ProjectService from './services/ProjectService'

import PaginationIterator from './utils/PaginationIterator'

exports.components = {
    Pager: Pager
}

exports.models = {
    Project: Project
}

exports.services = {
    ProjectService: ProjectService
}

exports.utils = {
    PaginationIterator: PaginationIterator
}
