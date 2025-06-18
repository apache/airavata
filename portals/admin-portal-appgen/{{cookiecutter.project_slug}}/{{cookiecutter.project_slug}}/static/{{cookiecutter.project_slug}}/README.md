# Static files folder

Place your static files (JavaScript, CSS, images, etc.) in this folder. Then
load them in your templates using the static template tag.

```html
{% raw %}
{% load static %}
{% endraw %}

<script src="{{ '{%' }} static '{{ cookiecutter.project_slug }}/some_script.js' %}"></script>
```

See
[Part 6 of the Django tutorial](https://docs.djangoproject.com/en/2.2/intro/tutorial06/)
for more information.
