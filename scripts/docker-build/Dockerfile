FROM node:10-alpine

RUN apk update && apk add bash

VOLUME /code

WORKDIR /code

EXPOSE 9000

CMD ["./build_js.sh"]
