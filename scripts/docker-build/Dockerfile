FROM node:10-alpine

RUN apk update && apk add bash

VOLUME /code

WORKDIR /code

CMD ["./build_js.sh"]
