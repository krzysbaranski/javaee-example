CREATE TABLE public.author
(
  id      SERIAL  NOT NULL,
  name    CHARACTER VARYING,
  version INTEGER NOT NULL,
  surname CHARACTER VARYING,
  CONSTRAINT author_pkey PRIMARY KEY (id)
)
WITH (
OIDS = FALSE
);

ALTER TABLE public.author
  OWNER TO docker;

CREATE TABLE public.book
(
  id      SERIAL  NOT NULL,
  title   CHARACTER VARYING,
  version INTEGER NOT NULL,
  year    INTEGER,
  CONSTRAINT book_pkey PRIMARY KEY (id)
)
WITH (
OIDS = FALSE
);

ALTER TABLE public.book
  OWNER TO docker;

CREATE TABLE public.book_author (
  book_id   INTEGER NOT NULL,
  author_id INTEGER NOT NULL,
  CONSTRAINT book_author_pkey PRIMARY KEY (book_id, author_id)
)
WITH (
OIDS = FALSE
);

ALTER TABLE public.book_author
  OWNER TO docker;
