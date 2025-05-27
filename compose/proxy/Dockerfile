FROM nginx:stable-bookworm
ENV PYTHONBUFFERED=1
COPY . /code
WORKDIR /code
RUN apt-get update && \
    apt-get install python3 -y && \
    echo '#!/bin/bash' > /proxy-entrypoint.sh && \
    echo 'while true; do' >> /proxy-entrypoint.sh && \
    echo '    /usr/bin/python3 /code/proxy_controller.py' >> /proxy-entrypoint.sh && \
    echo '    echo "Python script crashed. Restarting..." >&2' >> /proxy-entrypoint.sh && \
    echo '    sleep 2' >> /proxy-entrypoint.sh && \
    echo 'done &' >> /proxy-entrypoint.sh && \
    echo 'exec /docker-entrypoint.sh nginx -g "daemon off;"' >> /proxy-entrypoint.sh && \
    chmod +x /proxy-entrypoint.sh
ENTRYPOINT ["/proxy-entrypoint.sh"]
