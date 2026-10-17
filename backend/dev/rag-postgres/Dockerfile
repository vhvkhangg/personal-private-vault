FROM postgres:18.6@sha256:5a5a84b19854a9ffaa54082c166ff4ec27473a361e496e5ea167f298f2da9722 AS build
RUN apt-get update && apt-get install -y --no-install-recommends build-essential ca-certificates curl postgresql-server-dev-18="$PG_VERSION" \
    && curl --fail --location --max-time 60 -o /tmp/pgvector.tar.gz \
       https://codeload.github.com/pgvector/pgvector/tar.gz/8ee86c96f0fd72390f890aa8a336fda6d3ab4c6c \
    && echo "d076a3098010905fd60256649327809651f6288327db6413f0938305f62ea299  /tmp/pgvector.tar.gz" | sha256sum -c - \
    && mkdir /tmp/pgvector && tar -xzf /tmp/pgvector.tar.gz --strip-components=1 -C /tmp/pgvector \
    && make -C /tmp/pgvector OPTFLAGS="" && make -C /tmp/pgvector install

FROM postgres:18.6@sha256:5a5a84b19854a9ffaa54082c166ff4ec27473a361e496e5ea167f298f2da9722
COPY --from=build /usr/lib/postgresql/18/lib/vector.so /usr/lib/postgresql/18/lib/vector.so
COPY --from=build /usr/share/postgresql/18/extension/vector* /usr/share/postgresql/18/extension/
LABEL org.opencontainers.image.title="PPV Phase 17 PostgreSQL 18.6 with pgvector 0.8.6"
LABEL ppv.pgvector.commit="8ee86c96f0fd72390f890aa8a336fda6d3ab4c6c"
