# Adding a Django App

The functionality of the Airavata Django Portal is broken up into separate
Django apps. The apps live in the `django_airavata/apps` directory. When adding
new functionality to the Django portal it may make sense to add it as a new
separate Django app instead of adding it to an existing app. The following steps
document how to do this.

## Create the new Django App

For this example, assume the name of the app is **myapp**. The following also
assumes you have sourced your virtual environment.

```
cd airavata-django-portal
mkdir django_airavata/apps/myapp
python manage.py startapp myapp django_airavata/apps/myapp
```

## Integrating with the Django Portal

### AppConfig settings

Edit the AppConfig so that it extends the AiravataAppConfig and fill in the
required details:

```python
from django_airavata.app_config import AiravataAppConfig


class MyAppConfig(AiravataAppConfig):
    name = 'django_airavata.apps.myapp'
    label = 'django_airavata_myapp'
    verbose_name = 'My App'
    app_order = 10
    url_home = 'django_airavata_myapp:home'
    fa_icon_class = 'fa-bolt'
    app_description = """
        My app for doing stuff in the Airavata Django Portal.
    """
    nav = [
        {
            'label': 'Dashboard',
            'icon': 'fa fa-tachometer-alt',
            'url': 'django_airavata_myapp:dashboard',
            'active_prefixes': ['dashboard']
        },
        # ... additional entries as needed
    ]
```

Some of these are self explanatory, but here are some details on each of these
properties:

- _name_ - this is the python package of the app
- _label_ - this needs to be unique across all installed Django apps. I just
  make this match the _app_name_ in `urls.py`.
- _verbose_name_ - display name of app
- _app_order_ - order of app in the menu listing. Range is 0 - 100. See the
  other Django apps for their values to figure out how to order this app
  relative to them.
- _url_home_ - namespaced url of the "home" page of this app. This will be the
  url used when a user selects this app in a navigational menu.
- _fa_icon_class_ - a FontAwesome icon class. See
  [the list of available icons for v. 4.7](https://fontawesome.com/v4.7.0/icons/).
- _app_description_ - description of this app
- _nav_ - **optional** provide navigation into sections of the app. The _nav_ is
  optional but is necessary to provide users with a link from the left hand side
  navigation bar to a url in your app.
    - _label_ - textual label, displayed on hover in the side navigation bar
    - _icon_ - FontAwesome icon, see _fa_icon_class_ above
    - _url_ - named or namespaced url
    - _active_prefixes_ - list of strings that come after this app's base url for
        all urls that are considered "active" for this nav item. This is used to
        highlight the currently active nav item in the left side navigation bar. For
        example, let's say the app's base url is "/myapp" and urls belonging to the
        "projects" nav item are of the form "/myapp/projects/`<project_id>`" and
        "/myapp/new-project". Then you would set _active_prefixes_ to
        `["projects", "new-project"]`. These strings can also be [regular
        expressions](https://docs.python.org/3/library/re.html#regular-expression-syntax).

### Add AppConfig to INSTALLED_APPS

Edit INSTALLED_APPS in settings.py:

```python
INSTALLED_APPS = [
  # ...
  'django_airavata.apps.myapp.apps.MyAppConfig'
]
```

### Add Webpack bundle loader config to settings.py

If the new app has Webpack built frontend, then add the following configuration
to WEBPACK_LOADER in settings.py:

```python
...
'MYAPP': {
  'BUNDLE_DIR_NAME': 'django_airavata_myapp/dist/',
  'STATS_FILE': os.path.join(
      BASE_DIR,
      'django_airavata',
      'apps',
      'myapp',
      'static',
      'django_airavata_myapp',
      'dist',
      'webpack-stats.json'),
  'TIMEOUT': 60,
},
...
```

### Add the apps urls to the site's urls.py

Edit `django_airavata/urls.py` and add the app's urls config:

```python
urlpatterns = [
    re_path(r'^djadmin/', admin.site.urls),
    re_path(r'^admin/', include('django_airavata.apps.admin.urls')),
    re_path(r'^auth/', include('django_airavata.apps.auth.urls')),
    re_path(r'^workspace/', include('django_airavata.apps.workspace.urls')),
    re_path(r'^api/', include('django_airavata.apps.api.urls')),
    re_path(r'^groups/', include('django_airavata.apps.groups.urls')),
    re_path(r'^dataparsers/', include('django_airavata.apps.dataparsers.urls')),
    # ... Add the app urls here
    re_path(r'^myapp/', include('django_airavata.apps.myapp.urls')),
    # ...
    path('sdk/', include('airavata_django_portal_sdk.urls')),
    re_path(r'^home$', views.home, name='home'),
    re_path(r'^cms/', include(wagtailadmin_urls)),
    re_path(r'^documents/', include(wagtaildocs_urls)),
    # For testing, developing error pages
    re_path(r'^400/', views.error400),
    re_path(r'^403/', views.error403),
    re_path(r'^404/', views.error404),
    re_path(r'^500/', views.error500),
]
```

## App urls.py and base template

Let's add a starter home page and urls.py config for this app. Create a
`urls.py` file in `myapp/`:

```python
from django.urls import path

from . import views

app_name = 'django_airavata_myapp'
urlpatterns = [
    path('home/', views.home, name='home'),
]
```

Add a view function called `home` in views.py:

```python
from django.shortcuts import render


def home(request):
    return render(request, 'django_airavata_myapp/home.html')
```

Create a templates directory called in `myapp` called
`templates/django_airavata_myapp/`.

Then create a base template in that directory called `base.html`. We'll create
this file assuming that it will load webpack bundles generated by vue-cli:

```django

{% extends 'base.html' %}

{% load static %}
{% load render_bundle from webpack_loader %}

{% block css %}
{% render_bundle 'chunk-vendors' 'css' 'MYAPP' %}
{% comment %}BUT NOTE: if you only have one entry point you won't have a 'chunk-common' bundle so you may need to comment out the next line until you have more than one entry point.{% endcomment %}
{% render_bundle 'chunk-common' 'css' 'MYAPP' %}
{% render_bundle bundle_name 'css' 'MYAPP' %}
{% endblock %}

{% block content %}
<div id="{{ bundle_name }}"/>
{% endblock %}


{% block scripts %}
{% render_bundle 'chunk-vendors' 'js' 'MYAPP' %}
{% comment %}BUT NOTE: if you only have one entry point you won't have a 'chunk-common' bundle so you may need to comment out the next line until you have more than one entry point.{% endcomment %}
{% render_bundle 'chunk-common' 'js' 'MYAPP' %}
{% render_bundle bundle_name 'js' 'MYAPP' %}
{% endblock %}
```

Now, create a `home.html` template:

```html
{% extends './base.html' %}
{% load static %}
{% block css %}
{% comment %}This isn't a Vue.js app, so just turn off loading CSS.{% endcomment %}
{% endblock %}
{% block content %}

<h1>Hello World!</h1>

{% endblock content %}
{% block scripts %}
{% comment %}This isn't a Vue.js app, so just turn off loading JavaScript.{% endcomment %}
{% endblock %}
```

Now if you log into the Django portal you should see "My App" in the menu at the
top and clicking on it should display the home page of this app.

## JS build config - Vue.js

Now we'll add JavaScript build config to the app using Vue.js, npm and webpack.

Add a package.json file to the app's directory (i.e.,
django_airavata/apps/myapp):

```json
{
  "name": "django-airavata-myapp-views",
  "description": "A Vue.js project",
  "version": "1.0.0",
  "author": "Apache Airavata <dev@airavata.apache.org>",
  "private": true,
  "scripts": {
    "serve": "vue-cli-service serve",
    "build": "vue-cli-service build",
    "lint": "vue-cli-service lint",
    "format": "prettier --write ."
  },
  "dependencies": {
    "bootstrap": "^4.0.0-beta.2",
    "bootstrap-vue": "2.0.0-rc.26",
    "django-airavata-api": "link:../api",
    "django-airavata-common-ui": "link:../../static/common",
    "vue": "^2.5.21"
  },
  "devDependencies": {
    "@vue/cli-plugin-babel": "^3.1.1",
    "@vue/cli-plugin-eslint": "^3.1.1",
    "@vue/cli-service": "^3.1.1",
    "babel-eslint": "^10.0.1",
    "eslint": "^5.8.0",
    "eslint-plugin-vue": "^5.0.0-0",
    "prettier": "^2.1.2",
    "vue-template-compiler": "^2.5.21",
    "webpack-bundle-tracker": "^0.4.2-beta"
  },
  "eslintConfig": {
    "root": true,
    "env": {
      "node": true
    },
    "extends": ["plugin:vue/essential", "eslint:recommended"],
    "rules": {},
    "parserOptions": {
      "parser": "babel-eslint"
    }
  },
  "postcss": {
    "plugins": {
      "autoprefixer": {}
    }
  },
  "browserslist": ["> 1%", "last 2 versions", "not dead"]
}
```

Run `yarn` which will install these dependencies and also create a
`yarn.lock` file with locked dependency versions.

Add a `babel.config.js` to this directory too:

```javascript
module.exports = {
  presets: ["@vue/app"]
};
```

Now add a `vue.config.js` file too:

```javascript
const BundleTracker = require("webpack-bundle-tracker");
const path = require("path");

module.exports = {
  publicPath:
    process.env.NODE_ENV === "development"
      ? "http://localhost:9000/static/django_airavata_myapp/dist/"
      : "/static/django_airavata_myapp/dist/",
  outputDir: "./static/django_airavata_myapp/dist",
  pages: {
    home: "./static/django_airavata_myapp/js/entry-home"
    // additional entry points go here ...
  },
  css: {
    loaderOptions: {
      postcss: {
        config: {
          path: __dirname
        }
      }
    }
  },
  configureWebpack: {
    plugins: [
      new BundleTracker({
        filename: "webpack-stats.json",
        path: "./static/django_airavata_myapp/dist/"
      })
    ],
    optimization: {
      /*
       * Force creating a vendor bundle so we can load the 'app' and 'vendor'
       * bundles on development as well as production using django-webpack-loader.
       * Otherwise there is no vendor bundle on development and we would need
       * some template logic to skip trying to load it.
       * See also: https://bitbucket.org/calidae/dejavu/src/d63d10b0030a951c3cafa6b574dad25b3bef3fe9/%7B%7Bcookiecutter.project_slug%7D%7D/frontend/vue.config.js?at=master&fileviewer=file-view-default#vue.config.js-27
       */
      splitChunks: {
        cacheGroups: {
          vendors: {
            name: "chunk-vendors",
            test: /[\\/]node_modules[\\/]/,
            priority: -10,
            chunks: "initial"
          },
          common: {
            name: "chunk-common",
            minChunks: 2,
            priority: -20,
            chunks: "initial",
            reuseExistingChunk: true
          }
        }
      }
    }
  },
  chainWebpack: config => {
    /*
     * Specify the eslint config file otherwise it complains of a missing
     * config file for the ../api and ../../static/common packages
     *
     * See: https://github.com/vuejs/vue-cli/issues/2539#issuecomment-422295246
     */
    config.module
      .rule("eslint")
      .use("eslint-loader")
      .tap(options => {
        options.configFile = path.resolve(__dirname, "package.json");
        return options;
      });
  },
  devServer: {
    port: 9000,
    headers: {
      "Access-Control-Allow-Origin": "*"
    },
    hot: true,
    hotOnly: true
  }
};
```

You'll customize _pages_ by modifying and/or adding additional entry points and
you'll need to modify _publicPath_ and _outputDir_ and the BundleTracker config
to correspond to your folder structure.

Now create a static folder for holding javascript code. For this example we
would create `static/django_airavata_myapp/js`. In this folder you can put the
entry points, for example `entry-home.js`.

For each entry point you'll create a template, extending your app's `base.html`
and including that entry points generated css and js file. See
[_Adding an entry point_](./developing_frontend.md#adding-an-entry-point) for
further instructions.

For a complete example, see the _workspace_ app.

## build_js.sh build script

In the root of the project is a master build script, `build_js.sh`, that
generates a production build of all of the JS frontend code in the project. Add
a line in there for your Django app, like so:

```bash
...
(cd $SCRIPT_DIR/django_airavata/apps/myapp && yarn && yarn run build) || exit 1
```

You can test it by running `./build_js.sh` in the root folder.
