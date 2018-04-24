
# Developer notes

## Allow insecure OAuth callbacks

For local development, [set the OAUTHLIB_INSECURE_TRANSPORT environment variable
to allow insecure OAuth
callbacks](http://requests-oauthlib.readthedocs.io/en/latest/examples/real_world_example.html)
before starting the server:

```
export OAUTHLIB_INSECURE_TRANSPORT=1
```

## USING CMS

#### Logging in to CMS Dashboard

1. Make sure you are on the homepage of the website. Now, append `/cms` to the url and click ENTER/GO
2. You will be redirected to a Login Interface for Wagtail which looks like shown below:

![wagtail login][logo]

[logo]: https://github.com/stephenpaul2727/airavata-django-portal/blob/cms/docimages/wagtail-login.png "Wagtail Login"

3. Input your Username and Password and click `Sign In`. If you are authorized by gateway admin to access the CMS dashboard, you will be redirected to the Wagtail dashboard which should look like shown below:


![wagtail dashboard][logo]

[logo]: https://github.com/stephenpaul2727/airavata-django-portal/blob/cms/docimages/wagtail-dashboard.png "Wagtail Dashboard"


#### CMS Dashboard Overview

![wagtail dashboard_overview][logo]

[logo]: https://github.com/stephenpaul2727/airavata-django-portal/blob/cms/docimages/wagtail-dashboard-overview.png "Wagtail Dashboard Overview"


#### Creating a New Page



#### Deleting a Page



#### Modifying a Page content

