# Customization HOWTOs

## HOWTO have your custom Django app as the home page after user logs in

By default, the Airavata Django Portal takes the user to the Workspace dashboard
after logging in. You can customize this with the
[LOGIN_REDIRECT_URL](https://docs.djangoproject.com/en/3.2/ref/settings/#login-redirect-url).
In your settings_local.py file you can test this by adding

```
LOGIN_REDIRECT_URL='/my_custom_django_app/home/'
```

but replace the `/my_custom_django_app/home/` with the actual URL to your custom
Django app's home page. (The URL can also be a named URL, see the link above for
more info.)

For deployment, the LOGIN_REDIRECT_URL setting can be added to the Ansible
scripts that generate your portal's settings_local.py file. Some examples:

-   [SimCCS vars.yml](https://github.com/apache/airavata/blob/master/dev-tools/ansible/inventories/scigap/production/host_vars/simccs/vars.yml#L44)
-   [GeoGateway vars.yml](https://github.com/apache/airavata/blob/master/dev-tools/ansible/inventories/scigap/production/host_vars/geo/vars.yml#L41)
