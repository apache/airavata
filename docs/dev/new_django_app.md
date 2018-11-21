# Adding a Django App

The functionality of the Airavata Django Portal is broken up into separate
Django apps. The apps live in the `django_airavata/apps` directory. When
adding new functionality to the Django portal it may make sense to add it as
a new separate Django app instead of adding it to an existing app. The
following steps document how to do this.

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
    url_app_name = label
    app_order = 10
    url_home = 'django_airavata_myapp:home'
    fa_icon_class = 'fa-bolt'
    app_description = """
        My app for doing stuff in the Airavata Django Portal.
    """
```

Some of these are self explanatory, but here are some details on each of
these properties:

- _name_ - this is the python package of the app
- _label_ - this needs to be unique across all installed Django apps. I
  just make this match the _app_name_ in `urls.py`.
- _verbose_name_ - display name of app
- _url_app_name_ - this needs to match the _app_name_ in `urls.py`
- _app_order_ - order of app in the menu listing. Range is 0 - 100. See the
  other Django apps for their values to figure out how to order this app
  relative to them.
- _url_home_ - namespaced url of the "home" page of this app. This will be
  the url used when a user selects this app in a navigational menu.
- _fa_icon_class_ - a FontAwesome icon class. See [the list of available icons
  for v. 4.7](https://fontawesome.com/v4.7.0/icons/).
- _app_description_ - description of this app

### Add AppConfig to INSTALLED_APPS

Edit INSTALLED_APPS in settings.py:

```python
INSTALLED_APPS = [
  # ...
  'django_airavata.apps.myapp.MyAppConfig'
]
```

### Add the apps urls to the site's urls.py

Edit `django_airavata/urls.py` and add the app's urls config:

```python
urlpatterns = [
    url(r'^djadmin/', admin.site.urls),
    url(r'^admin/', include('django_airavata.apps.admin.urls')),
    url(r'^auth/', include('django_airavata.apps.auth.urls')),
    url(r'^workspace/', include('django_airavata.apps.workspace.urls')),
    url(r'^api/', include('django_airavata.apps.api.urls')),
    url(r'^groups/', include('django_airavata.apps.groups.urls')),
    # ... Add the app urls here
    url(r'^myapp/', include('django_airavata.apps.myapp.urls')),
    # ...
    url(r'^home$', views.home, name='home'),
    url(r'^cms/', include(wagtailadmin_urls)),
    url(r'^documents/', include(wagtaildocs_urls)),
    url(r'', include(wagtail_urls)),
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
```

It's important that the app urls are added before the `wagtail_urls` since
wagtail controls those urls.


## App urls.py and base template

Let's add a starter home page and urls.py config for this app.  Create a `urls.py` file in `myapp/`:

```python
from django.conf.urls import url

from . import views

app_name = 'django_airavata_myapp'
urlpatterns = [
    url(r'^home$', views.home, name='home'),
]
```

Add a view function called `home` in views.py:

```python
from django.shortcuts import render


def home(request):
    return render(request, 'django_airavata_myapp/home.html')
```

Create a templates directory called in `myapp` called `templates/django_airavata_myapp/`.

Then create a base template in that directory called `base.html`:

```html

{% extends 'base.html' %}

{% load static %}

{% block css %}
{% comment %}Load any styles sheets that are common across all pages in your app{% endcomment %}
<link rel=stylesheet type=text/css href="{% static 'django_airavata_myapp/dist/common.css' %}">
{% endblock %}

{% block nav-items %}

        {% comment %}Define the left side navigation for drilling into sub pages of your app{% endcomment %}
        <a href="{% url 'django_airavata_myapp:function1' %}"
            class="c-nav__item {% if request.active_nav_item == 'function1' %}is-active{% endif %}"
            data-toggle=tooltip data-placement=right title="Function #1">
            <i class="fa fa-bolt"></i> <span class=sr-only>Function #1</span>
        </a>
        <a href="{% url 'django_airavata_myapp:function2' %}"
            class="c-nav__item {% if request.active_nav_item == 'function2' %}is-active{% endif %}"
            data-toggle=tooltip data-placement=right title="Function #2">
            <i class="fa fa-bolt"></i> <span class=sr-only>Function #2</span>
        </a>
        <a href="{% url 'django_airavata_myapp:function3' %}"
            class="c-nav__item {% if request.active_nav_item == 'function3' %}is-active{% endif %}"
            data-toggle=tooltip data-placement=right title="Function #3">
            <i class="fa fa-bolt"></i> <span class=sr-only>Function #3</span>
        </a>

{% endblock %}

{% block scripts %}
{% comment %}Load any javascript common to all pages in your app{% endcomment %}
<script src="{% static "django_airavata_workspace/dist/common.js" %}"></script>
{% endblock %}
```

Now, create a `home.html` template:

```html

{% extends './base.html' %}

{% load static %}

{% block css %}
{{ block.super }}
{% comment %}Load any CSS specific to this page here, if necessary{% endcomment %}
{% endblock %}

{% block content %}

<h1>Hello World!</h1>

{% endblock content %}

{% block scripts %}
{{ block.super }}
{% comment %}Load any JS specific to this page here, if necessary{% endcomment %}
{% endblock %}
```

Now if you log into the Django portal you should see "My App" in the menu at
the top and clicking on it should display the home page of this app.


## JS build config - Vue.js

Now we'll add JavaScript build config to the app using Vue.js, npm and webpack.

Add a package.json file to the app's directory (i.e., django_airavata/apps/myapp):

```json
{
  "name": "django-airavata-myapp-views",
  "description": "A Vue.js project",
  "version": "1.0.0",
  "author": "Marcus Christie <machristie@apache.org>",
  "private": true,
  "scripts": {
    "dev": "cross-env NODE_ENV=development webpack --progress --hide-modules",
    "watch": "cross-env NODE_ENV=development webpack --watch",
    "build": "cross-env NODE_ENV=production webpack --progress --hide-modules"
  },
  "dependencies": {
    "bootstrap": "^4.0.0-beta.2",
    "bootstrap-vue": "^1.4.1",
    "django-airavata-api": "file:../api",
    "django-airavata-common-ui": "file:../../static/common",
    "vue": "^2.5.17"
  },
  "devDependencies": {
    "babel-core": "^6.0.0",
    "babel-loader": "^7.1.2",
    "babel-preset-env": "^1.5.1",
    "clean-webpack-plugin": "^0.1.17",
    "cross-env": "^3.0.0",
    "css-loader": "^0.25.0",
    "extract-text-webpack-plugin": "^3.0.2",
    "file-loader": "^0.9.0",
    "style-loader": "^0.19.0",
    "vue-loader": "^12.1.0",
    "vue-template-compiler": "^2.3.3",
    "webpack": "^3.1.0",
    "webpack-dev-server": "^2.4.5"
  }
}
```

Add a `.babelrc` to this directory too:

```json
{
  "presets": [
    ["env", { "modules": false }]
  ]
}
```

Now add a `webpack.config.js` file too:

```javascript
var path = require('path')
var webpack = require('webpack')
const ExtractTextPlugin = require("extract-text-webpack-plugin");
const CleanWebpackPlugin = require('clean-webpack-plugin');

module.exports = {
  entry: {
      'home': './static/django_airavata_myapp/js/entry-home',
  },
  output: {
    path: path.resolve(__dirname, './static/django_airavata_myapp/dist/'),
    publicPath: '/static/django_airavata_myapp/dist/',
    filename: '[name].js'
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          loaders: {
          },
          extractCSS: true
          // other vue-loader options go here
        }
      },
      {
        test: /\.js$/,
        loader: 'babel-loader',
        exclude: /node_modules/
      },
      {
          test: /\.css$/,
          use: ExtractTextPlugin.extract({
              fallback: "style-loader",
              use: "css-loader"
          })
      },
      {
        test: /\.(png|jpg|gif|svg)$/,
        loader: 'file-loader',
        options: {
          name: '[name].[ext]?[hash]'
        }
      }
    ]
  },
  resolve: {
    alias: {
      'vue$': 'vue/dist/vue.esm.js'
    }
  },
  devServer: {
    historyApiFallback: true,
    noInfo: true
  },
  performance: {
    hints: false
  },
  devtool: '#eval-source-map',
  plugins: [
      new ExtractTextPlugin("[name].css"),
      new CleanWebpackPlugin(['./static/interactwel_gui/dist']),
      new webpack.optimize.CommonsChunkPlugin({
          name: 'common',
      }),

  ]
}

if (process.env.NODE_ENV === 'production') {
  module.exports.devtool = '#source-map'
  // http://vue-loader.vuejs.org/en/workflow/production.html
  module.exports.plugins = (module.exports.plugins || []).concat([
    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: '"production"'
      }
    }),
    new webpack.optimize.UglifyJsPlugin({
      sourceMap: true,
      compress: {
        warnings: false
      }
    }),
    new webpack.LoaderOptionsPlugin({
      minimize: true
    })
  ])
}
```

You'll customize *entry* by modifying and/or adding additional entry points
and you'll need to modify *output.path* and *output.publicPath* to correspond
to your folder structure.

Now create a static folder for holding javascript code. For this example we
would create `static/django_airavata_myapp/js`. In this folder you can put
the entry points, for example `entry-home.js`.

For each entry point you'll create a template, extending your app's
`base.html` and including that entry points generated css and js file.

For a complete example, see the *workspace* app.
