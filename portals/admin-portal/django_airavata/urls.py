"""django_airavata_gateway URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.10/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.conf.urls import url, include
    2. Add a URL to urlpatterns:  url(r'^blog/', include('blog.urls'))
"""
from django.conf import settings
from django.conf.urls import include
from django.conf.urls.static import static
from django.contrib import admin
from django.urls import path, re_path
from wagtail.admin import urls as wagtailadmin_urls
from wagtail.core import urls as wagtail_urls
from wagtail.documents import urls as wagtaildocs_urls

from . import views

urlpatterns = [
    re_path(r'^djadmin/', admin.site.urls),
    re_path(r'^admin/', include('django_airavata.apps.admin.urls')),
    re_path(r'^auth/', include('django_airavata.apps.auth.urls')),
    re_path(r'^workspace/', include('django_airavata.apps.workspace.urls')),
    re_path(r'^api/', include('django_airavata.apps.api.urls')),
    re_path(r'^groups/', include('django_airavata.apps.groups.urls')),
    re_path(r'^dataparsers/', include('django_airavata.apps.dataparsers.urls')),
    path('sdk/', include('airavata_django_portal_sdk.urls')),
    re_path(r'^home$', views.home, name='home'),
    re_path(r'^cms/', include(wagtailadmin_urls)),
    re_path(r'^documents/', include(wagtaildocs_urls)),
    # For testing, developing error pages
    re_path(r'^400/', views.error400),
    re_path(r'^403/', views.error403),
    re_path(r'^404/', views.error404),
    re_path(r'^500/', views.error500),
    path('', include('airavata_django_portal_commons.dynamic_apps.urls')),
    path('', include(wagtail_urls)),
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

handler400 = views.error400
handler403 = views.error403
handler404 = views.error404
handler500 = views.error500
