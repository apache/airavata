import logging
from collections.__init__ import OrderedDict
from datetime import datetime

import pytz
from django.conf import settings
from django.http import Http404
from django.http.request import QueryDict
from rest_framework import mixins, pagination, permissions
from rest_framework.response import Response
from rest_framework.reverse import reverse
from rest_framework.utils.urls import remove_query_param, replace_query_param
from rest_framework.viewsets import GenericViewSet

logger = logging.getLogger(__name__)


class GenericAPIBackedViewSet(GenericViewSet):
    # Make lookup_value_regex to any set of non-forward-slash characters. Many
    # Airavata ids contains period ('.') which the default lookup_value_regex
    # in DRF doesn't allow.
    lookup_value_regex = '[^/]+'

    def get_list(self):
        """
        Subclasses must implement.
        """
        raise NotImplementedError()

    def get_instance(self, lookup_value):
        """
        Subclasses must implement.
        """
        raise NotImplementedError()

    def get_queryset(self):
        if isinstance(self, mixins.ListModelMixin):
            return self.get_list()
        else:
            # get_queryset() is invoked whenever a detail extra action route
            # returns a many valued response. For ViewSets that have such
            # actions, return None here so they don't need to provide a
            # get_list() implementation
            return None

    def get_object(self):
        lookup_url_kwarg = self.lookup_url_kwarg or self.lookup_field
        lookup_value = self.kwargs[lookup_url_kwarg]
        inst = self.get_instance(lookup_value)
        if inst is None:
            raise Http404
        self.check_object_permissions(self.request, inst)
        return inst

    @property
    def username(self):
        return self.request.user.username

    @property
    def gateway_id(self):
        return settings.GATEWAY_ID

    @property
    def authz_token(self):
        return self.request.authz_token


class ReadOnlyAPIBackedViewSet(mixins.RetrieveModelMixin,
                               mixins.ListModelMixin,
                               GenericAPIBackedViewSet):
    """
    A viewset that provides default `retrieve()` and `list()` actions.

    Subclasses must implement the following:
    * get_list(self)
    * get_instance(self, lookup_value)
    """
    pass


class APIBackedViewSet(mixins.CreateModelMixin,
                       mixins.RetrieveModelMixin,
                       mixins.UpdateModelMixin,
                       mixins.DestroyModelMixin,
                       mixins.ListModelMixin,
                       GenericAPIBackedViewSet):
    """
    A viewset that provides default `create()`, `retrieve()`, `update()`,
    `partial_update()`, `destroy()` and `list()` actions.

    Subclasses must implement the following:
    * get_list(self)
    * get_instance(self, lookup_value)
    * perform_create(self, serializer) - should return instance with id populated
    * perform_update(self, serializer)
    * perform_destroy(self, instance)
    """
    pass


class APIResultIterator(object):
    """
    Iterable container over API results which allow limit/offset style slicing.
    """

    limit = -1
    offset = 0

    def __init__(self, query_params=None):
        self.query_params = query_params if query_params is not None else QueryDict()

    def get_results(self, limit=-1, offset=0):
        raise NotImplementedError("Subclasses must implement get_results")

    def __iter__(self):
        results = self.get_results(self.limit, self.offset)
        for result in results:
            yield result

    def __getitem__(self, key):
        if isinstance(key, slice):
            self.limit = key.stop - key.start
            self.offset = key.start
            return iter(self)
        else:
            return self.get_results(1, key)


class APIResultPagination(pagination.LimitOffsetPagination):
    """
    Based on DRF's LimitOffsetPagination; Airavata API pagination results don't
    have a known count, so it isn't always possible to know how many pages there
    are.
    """
    default_limit = 10

    def paginate_queryset(self, queryset, request, view=None):
        assert isinstance(
            queryset, APIResultIterator), "queryset is not an APIResultIterator: {}".format(queryset)
        self.query_params = queryset.query_params.copy()
        self.limit = self.get_limit(request)
        if self.limit is None:
            return None

        self.offset = self.get_offset(request)
        self.request = request

        # When a paged view is called from another view (for example, to get the
        # initial data to display), this pagination class needs to know the name
        # of the view being paginated.
        if view and hasattr(view, 'pagination_viewname'):
            self.viewname = view.pagination_viewname

        return list(queryset[self.offset:self.offset + self.limit])

    def get_limit(self, request):
        # If limit <= 0 then don't paginate
        if self.limit_query_param in request.query_params and int(
                request.query_params[self.limit_query_param]) <= 0:
            return None
        return super().get_limit(request)

    def get_paginated_response(self, data):
        has_next_link = len(data) >= self.limit
        return Response(OrderedDict([
            ('next', self.get_next_link() if has_next_link else None),
            ('previous', self.get_previous_link()),
            ('results', data),
            ('limit', self.limit),
            ('offset', self.offset)
        ]))

    def get_next_link(self):
        url = self.get_base_url()
        url = replace_query_param(url, self.limit_query_param, self.limit)

        offset = self.offset + self.limit
        return replace_query_param(url, self.offset_query_param, offset)

    def get_previous_link(self):
        if self.offset <= 0:
            return None

        url = self.get_base_url()
        url = replace_query_param(url, self.limit_query_param, self.limit)

        if self.offset - self.limit <= 0:
            return remove_query_param(url, self.offset_query_param)

        offset = self.offset - self.limit
        return replace_query_param(url, self.offset_query_param, offset)

    def get_base_url(self):
        if hasattr(self, 'viewname'):
            base_url = self.request.build_absolute_uri(reverse(self.viewname))
            if len(self.query_params) > 0:
                base_url += f"?{self.query_params.urlencode()}"
            return base_url
        else:
            return self.request.build_absolute_uri()


def convert_utc_iso8601_to_date(iso8601_utc_string):
    # This is meant to convert a JavaScript `new Date().toJSON()` into a
    # datetime instance
    timestamp = datetime.strptime(
        iso8601_utc_string, "%Y-%m-%dT%H:%M:%S.%fZ")
    timestamp = timestamp.replace(tzinfo=pytz.UTC)
    logger.debug("convert_utc_iso8601_to_date({})={}".format(
        iso8601_utc_string, timestamp))
    return timestamp


class IsInAdminsGroupPermission(permissions.BasePermission):
    message = "User must be member of the Admins or Read Only Admins groups."

    def has_permission(self, request, view):
        # Read Only Admins can make GET requests only
        if request.method in permissions.SAFE_METHODS:
            return (request.is_gateway_admin or
                    request.is_read_only_gateway_admin)
        else:
            return request.is_gateway_admin


class ReadOnly(permissions.BasePermission):
    def has_permission(self, request, view):
        return request.method in permissions.SAFE_METHODS
