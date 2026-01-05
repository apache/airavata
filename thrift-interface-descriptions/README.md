## Thrift IDL + stub generation

Airavata’s Thrift IDLs live in `thrift-interface-descriptions/`.

### Requirements

- **Apache Thrift compiler**: this repo’s generator script requires **Thrift `0.22.0`** (see `generate-thrift-stubs.sh`).

### Generate Java stubs (recommended)

From the `airavata/` repo root:

```bash
./thrift-interface-descriptions/generate-thrift-stubs.sh java
```

That script will generate and sync Java sources into:

- `modules/airavata-api/src/main/java` (models + CPI/service stubs)

To generate all supported languages at once:

```bash
./thrift-interface-descriptions/generate-thrift-stubs.sh all
```

### Java stubs via Maven (module build)

The `modules/thrift-api` build also generates Java sources from `thrift-interface-descriptions/stubs_java.thrift` during `generate-sources` (requires `thrift` on `PATH`):

```bash
mvn -pl modules/thrift-api -DskipTests generate-sources
```


