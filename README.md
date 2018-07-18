
# Developer notes

## Allow insecure OAuth callbacks

For local development, [set the OAUTHLIB_INSECURE_TRANSPORT environment variable
to allow insecure OAuth
callbacks](http://requests-oauthlib.readthedocs.io/en/latest/examples/real_world_example.html)
before starting the server:

```
export OAUTHLIB_INSECURE_TRANSPORT=1
```
