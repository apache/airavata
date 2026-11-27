Run linkspan

```
export CS_LINK_URL=ws://localhost:9095/api/v1/processes/5b2a1883-d912-49a6-92c2-507492a95b1e/link

export CS_JUPYTER_PORT=29545
export CS_CONTROL_PORT=29544
export LINKSPAN_LINK_TOKEN=eoWs0jiRlpz2ad7uX9BxH5HwuoFc_h06RgEI7dStbLo

./linkspan --port "$CS_CONTROL_PORT" --workflow workflow.yaml --tunnel-enable --tunnel-mode=link --tunnel-link-args="--url $CS_LINK_URL"
```