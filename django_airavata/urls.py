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
from django.conf.urls import include, url
from django.conf.urls.static import static
from django.contrib import admin
from wagtail.admin import urls as wagtailadmin_urls
from wagtail.core import urls as wagtail_urls
from wagtail.documents import urls as wagtaildocs_urls

from . import views

urlpatterns = [
    url(r'^djadmin/', admin.site.urls),
    url(r'^admin/', include('django_airavata.apps.admin.urls')),
    url(r'^auth/', include('django_airavata.apps.auth.urls')),
    url(r'^workspace/', include('django_airavata.apps.workspace.urls')),
    url(r'^api/', include('django_airavata.apps.api.urls')),
    url(r'^groups/', include('django_airavata.apps.groups.urls')),
    url(r'^home$', views.home, name='home'),
    url(r'^cms/', include(wagtailadmin_urls)),
    url(r'^documents/', include(wagtaildocs_urls)),
    url(r'', include(wagtail_urls)),
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
