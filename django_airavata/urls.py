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
    url(r'^dataparsers/', include('django_airavata.apps.dataparsers.urls')),
    url(r'^home$', views.home, name='home'),
    url(r'^cms/', include(wagtailadmin_urls)),
    url(r'^documents/', include(wagtaildocs_urls)),
    # For testing, developing error pages
    url(r'^400/', views.error400),
    url(r'^403/', views.error403),
    url(r'^404/', views.error404),
    url(r'^500/', views.error500),
]

handler400 = views.error400
handler403 = views.error403
handler404 = views.error404
handler500 = views.error500

# Add custom Django app urls patterns
for custom_django_app in settings.CUSTOM_DJANGO_APPS:
    # Custom Django apps may define a url_prefix, otherwise label will be used
    # as url prefix
    url_prefix = getattr(
        custom_django_app,
        'url_prefix',
        custom_django_app.label)
    urlpatterns.append(url(r'^' + url_prefix + '/',
                           include(custom_django_app.name + ".urls")))

urlpatterns += [
    url(r'', include(wagtail_urls)),
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
