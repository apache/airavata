# FUSE Client/Server for Airavata

```sh
make
```
Upon running `make`, two binaries (`bin/server` and `bin/client`) will be created in the `bin/` folder

# Running (Example)
```bash
make run_server
# or
bin/server
```
```bash
make run_client
# or
bin/client -mount <path/to/mountpoint> -serve <path/to/source/folder>
```