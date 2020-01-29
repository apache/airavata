# tusd

[tusd](https://github.com/tus/tusd) is the reference implementation of the
[tus resumable upload protocol](https://tus.io/). The Airavata Django Portal
integrates with tus to provide better support for large file uploads. Installing
tusd and integrating with the Airavata Django Portal is entirely optional but is
highly recommended if your users will be uploading files larger than, say, 1 GB.

## Installation Notes

!!! note

    Version 1.0 of tusd introduces some breaking changes. The Airavata Django
    Portal has been tested with version 0.13 of tusd.

Install
[version 0.13.3 of tusd](https://github.com/tus/tusd/releases/tag/0.13.3) from
the GitHub releases page.

How you install tusd is up to you, but here are some notes on a specific way to
deploy it.

### Reverse proxy behind Apache httpd

Create a virtual host config file, for example in `/etc/httpd/conf.d/tus.conf`.
(Actual location of config files is OS dependent.)

Contents of tus.conf:

```xml
<VirtualHost *:443>
    ServerName tus.domainname.org
    SSLEngine on

    SSLCertificateChainFile /etc/letsencrypt/live/tus.domainname.org/fullchain.pem
    SSLCertificateFile    /etc/letsencrypt/live/tus.domainname.org/cert.pem
    SSLCertificateKeyFile /etc/letsencrypt/live/tus.domainname.org/privkey.pem

    RequestHeader set X-Forwarded-Proto "https"
    ProxyPreserveHost on

    ProxyPass /files http://localhost:10080/files
    ProxyPassReverse /files http://localhost:10080/files
</VirtualHost>
```

Note this example assumes that the tus server willl be proxied behind an SSL
connection and specifically that the SSL certificate will be generated with
Let's Encrypt.

With this configuration the **TUS_ENDPOINT** will be
`https://tus.domainname.org/files/`

### Systemd unit file

Add the following `tus.service` file to `/etc/systemd/system/`.

```ini
[Unit]
Description=Tusd

[Service]
ExecStart=/home/PORTALUSER/tusd_linux_amd64/tusd -dir /path/to/tus-temp-dir -port 10080 -behind-proxy
User=PORTALUSER
Group=PORTALUSER

[Install]
WantedBy=multi-user.target
```

Replace `PORTALUSER` with the username of the user under which the Django portal
runs.

With this configuration, the **TUS_DATA_DIR** will be `/path/to/tus-temp-dir`

## settings_local.py configuration

Uncomment and set the following settings in settings_local.py, assuming you
created the settings_local.py file by first copying settings_local.py.sample.

-   **TUS_ENDPOINT** - this should be the tus url. Keeping with the example
    above this would be `https://tus.domainname.org/files/`
-   **TUS_DATA_DIR** - this is the directory where tus upload files and metadata
    will be stored. This settings assumes that tusd the tus server used and so
    it assumes that file naming conventions will follow the tusd implementation.
