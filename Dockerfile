FROM python:3.12-slim

WORKDIR /app

COPY server-fly.py /app/server-fly.py

EXPOSE 4433

CMD ["python", "-u", "server-fly.py"]
