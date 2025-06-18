# Getting started with Vue.js development

Make sure you have
[the latest version of Node.js LTS installed](https://nodejs.org/en/download/).
You also need to install
[the Yarn 1 (Classic) package manager](https://classic.yarnpkg.com/en/docs/install).

Start the Django portal (`python manage.py runserver`). Navigate to the Django
app directory and run `yarn` and then `yarn` to start up the dev server. Now you
can load the Django app in your browser and as you make code changes they should
automatically be hot-reloaded in the browser. For example, if you wanted to work
on the _workspace_ app's frontend code, you could do

```
cd django_airavata/apps/workspace
yarn
yarn run serve
```

Then in your browser go to
[http://localhost:8000/workspace/dashboard](http://localhost:8000/workspace/dashboard).

Note: after stopping the dev server the portal will still keep trying to load
the app's JS and CSS from the dev server URLs, which it will fail to do. To go
back to a pre dev server state run:

```
yarn run build
```

# Development

## Adding a dependency

If you need to add a JavaScript dependency, run the following:

```
yarn add <name of dependency>
```

This automatically updates the `package.json` file with the added dependency.
The `yarn.lock` file is also updated with a locked version of the added
dependency.

## Adding an entry point

Create an entry point for the Vue app in a new javascript file. The naming
convention is to name the entry point `entry-<name of entry point>.js`. For
example, `entry-something-list.js`. The entry point shouldn't require compiling
a Vue template since we don't include the template compiler in the runtime. The
entry point will generally have the following structure:

```javascript
import { components, entry } from "django-airavata-common-ui";
import SomethingListContainer from "./containers/SomethingListContainer.vue";

entry((Vue) => {
    new Vue({
        render: (h) => h(components.MainLayout, [h(SomethingListContainer)]),
    }).$mount("#something-list");
});
```

If you need to pass data into the Vue app, see below.

vue-cli calls entry points "pages". Edit `vue.config.js` and add an entry to the
"pages" config. For example, to add an entry point with the key "something-list"
and that is defined in the file
"static/django_airavata_myapp/js/entry-something-list.js", you would add:

```javascript
pages: {
  // ...
  "something-list": "static/django_airavata_myapp/js/entry-something-list.js"
}
```

Now you need a template that will load the entry point. For the simple case you
can just use the base.html template and pass in the `bundle_name` which should
equal the page key that you entered in vue.config.js. So in `views.py`, add the
following view function:

```python
@login_required
def something_list(request):
    # request.active_nav_item = ... # update this as appropriate
    return render(request, 'django_airavata_myapp/base.html', {
        'bundle_name': 'something-list'
    })
```

### Passing data through template to the Vue.js app

If you need to pass data from the backend to the frontend Vue.js app, you need
to make that data available to the Django template and then pass it to the
Vue.js app via a data attribute. For example, let's say we have a
_something-view_ and we need to pass _something-id_ to the Vue.js app, we could
do the following:

First, define a URL pattern that allows passing the id in urls.py:

```python
url(r'^something/(?P<something_id>\w+)/$', views.view_something,
    name='view_something'),
```

Then define the view function in views.py:

```python
@login_required
def view_something(request, something_id):
    # request.active_nav_item = ... # update this as appropriate
    return render(request, 'django_airavata_myapp/view_something.html', {
        'bundle_name': 'view-something',
        'something_id': something_id
    })
```

Then create a template that passes the something_id as a data attribute. We'll
name the template view_something.html which will extend the local base.html
template:

```django
{% extends './base.html' %}
{% block content %}
<div id="{{ bundle_name }}" data-something-id="{{ something_id }}"></div>
{% endblock content %}
```

In the entry point, load the data attribute in the `mounted()` hook and pass to
the Vue.js app container via a property:

```javascript
import { components, entry } from "django-airavata-common-ui";
import ViewSomethingContainer from "./containers/ViewSomethingContainer.vue";

entry((Vue) => {
    new Vue({
        render(h) {
            return h(components.MainLayout, [
                h(ViewSomethingContainer, {
                    props: {
                        somethingId: this.somethingId,
                    },
                }),
            ]);
        },
        data() {
            return {
                somethingId: null,
            };
        },
        beforeMount() {
            this.somethingId = this.$el.dataset.somethingId;
        },
    }).$mount("#view-something");
});
```

## Automatically formatting source code

Run `prettier --write .` with the following:

```
yarn format
```

## Recommended tools

-   <https://github.com/vuejs/vue-devtools> - debugging/inspection in Firefox or
    Chrome
-   <https://vuejs.github.io/vetur/> - Vue tooling for Visual Studio Code
