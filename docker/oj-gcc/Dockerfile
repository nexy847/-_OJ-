FROM ubuntu:22.04

RUN apt-get update \
    && apt-get install -y --no-install-recommends build-essential ca-certificates time \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace
