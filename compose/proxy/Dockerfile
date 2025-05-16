FROM docker-registry.x5.ru/nginx:stable-bookworm
ENV PYTHONBUFFERED=1
COPY --from=docker-base-images-prod.x5.ru/x5-certs /x5/certs /usr/local/share/ca-certificates/
COPY . /code
WORKDIR /code
RUN echo "deb http://repo.x5.ru/debian bookworm main" >/etc/apt/sources.list && \
    rm -rf /etc/apt/sources.list.d/debian.sources && \
    apt-get update && \
    apt-get install python3 -y && \
    update-ca-certificates && \
    echo '#!/bin/bash' > /x5key-entrypoint.sh && \
    echo 'while true; do' >> /x5key-entrypoint.sh && \
    echo '    /usr/bin/python3 /code/proxy_controller.py' >> /x5key-entrypoint.sh && \
    echo '    echo "Python script crashed. Restarting..." >&2' >> /x5key-entrypoint.sh && \
    echo '    sleep 2' >> /x5key-entrypoint.sh && \
    echo 'done &' >> /x5key-entrypoint.sh && \
    echo 'exec /docker-entrypoint.sh nginx -g "daemon off;"' >> /x5key-entrypoint.sh && \
    chmod +x /x5key-entrypoint.sh
ENTRYPOINT ["/x5key-entrypoint.sh"]
