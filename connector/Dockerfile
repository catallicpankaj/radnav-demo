FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

COPY connector/pyproject.toml /app/connector/pyproject.toml
COPY connector/src /app/connector/src

RUN python -m pip install --upgrade pip && \
    pip install /app/connector

WORKDIR /app/connector

CMD ["python", "-m", "connector"]
