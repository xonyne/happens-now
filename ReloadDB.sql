DROP DATABASE  happens_now;
CREATE DATABASE happens_now WITH OWNER = postgres;

\connect happens_now postgres;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;
                       
CREATE SEQUENCE public.event_tag_eventtagid_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.event_tag_eventtagid_seq
  OWNER TO postgres;

CREATE SEQUENCE public.location_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.location_id_seq
  OWNER TO postgres;

       
CREATE SEQUENCE public.attending_attending_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.attending_attending_id_seq
  OWNER TO postgres;

CREATE SEQUENCE public.category_category_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.category_category_id_seq
  OWNER TO postgres;



CREATE SEQUENCE public.event_event_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.event_event_id_seq
  OWNER TO postgres;

CREATE SEQUENCE public.interested_interested_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.interested_interested_id_seq
  OWNER TO postgres;




CREATE SEQUENCE public.rating_rating_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.rating_rating_id_seq
  OWNER TO postgres;

    CREATE SEQUENCE public.tag_tagid_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE public.tag_tagid_seq
  OWNER TO postgres;

CREATE TABLE public.location
(
  location_id bigint NOT NULL DEFAULT nextval('location_id_seq'::regclass),
  city character varying(255) NOT NULL,
  country character varying(255) NOT NULL,
  street character varying(255) NOT NULL,
  zip character varying(255) NOT NULL,
  latitude double precision NOT NULL,
  longitude double precision NOT NULL,
  CONSTRAINT location_pk PRIMARY KEY (location_id )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.location
  OWNER TO postgres;


CREATE TABLE public.place
(
  place_id bigint NOT NULL ,
  name character varying(255) NOT NULL,
  location_id bigint NOT NULL,
  CONSTRAINT place_pk PRIMARY KEY (place_id ),
  CONSTRAINT location_fk FOREIGN KEY (location_id)
      REFERENCES public.location (location_id) 
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.place
  OWNER TO postgres;

CREATE TABLE public.category
(
  category_id bigint NOT NULL DEFAULT nextval('category_category_id_seq'::regclass),
  label_key character varying(255) NOT NULL,
  parentcategory_id bigint,
  CONSTRAINT category_pk PRIMARY KEY (category_id ),
  CONSTRAINT parent_category_fk FOREIGN KEY (parentcategory_id)
      REFERENCES public.category (category_id) 
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.category
  OWNER TO postgres;

CREATE TABLE public.event
(
  event_id bigint NOT NULL ,
  startDateTime timestamp without time zone NOT NULL,
  title character varying(255) NOT NULL,
  description character varying,
  endDateTime timestamp without time zone,
  url character varying(255),
  place_id bigint,
  category_id bigint,
  CONSTRAINT event_pk PRIMARY KEY (event_id ),
  CONSTRAINT place_fk FOREIGN KEY (place_id)
      REFERENCES public.place (place_id) 
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT category_fk FOREIGN KEY (category_id)
      REFERENCES public.category (category_id)
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.event
  OWNER TO postgres;


CREATE TABLE public.users
(
  user_id bigint NOT NULL ,
  name character varying(255) NOT NULL,
  username character varying ,
  password character varying ,
  CONSTRAINT user_pk PRIMARY KEY (user_id )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.users
  OWNER TO postgres;

CREATE TABLE public.attending
(
  attending_id bigint NOT NULL DEFAULT nextval('attending_attending_id_seq'::regclass),
  user_id bigint NOT NULL,
  event_id bigint NOT NULL,
  CONSTRAINT attending_pk PRIMARY KEY (attending_id ),
  CONSTRAINT event_id FOREIGN KEY (event_id)
      REFERENCES public.event (event_id) 
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT user_id FOREIGN KEY (user_id)
      REFERENCES public.users (user_id) 
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.attending
  OWNER TO postgres;

CREATE TABLE public.tag
(
  tag_id bigint NOT NULL DEFAULT nextval('tag_tagid_seq'::regclass),
  labelKey character varying NOT NULL,
  CONSTRAINT tag_pk PRIMARY KEY (tag_id )
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.tag
  OWNER TO postgres;

CREATE TABLE public.event_tag
(
  eventtag_id bigint NOT NULL DEFAULT nextval('event_tag_eventtagid_seq'::regclass),
  event_id bigint NOT NULL,
  tag_id bigint NOT NULL,
  CONSTRAINT eventtag_pk PRIMARY KEY (eventtag_id ),
  CONSTRAINT tag_fk FOREIGN KEY (tag_id)
      REFERENCES public.tag (tag_id) 
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.event_tag
  OWNER TO postgres;

CREATE TABLE public.Interested
(
  interested_id bigint NOT NULL DEFAULT nextval('interested_interested_id_seq'::regclass),
  user_id bigint NOT NULL,
  event_id bigint NOT NULL,
  CONSTRAINT interested_pk PRIMARY KEY (interested_id ),
  CONSTRAINT event_fk FOREIGN KEY (event_id)
      REFERENCES public.event (event_id)
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT user_fk FOREIGN KEY (user_id)
      REFERENCES public.users (user_id) 
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.Interested
  OWNER TO postgres;


CREATE TABLE public.rating
(
  rating_id bigint NOT NULL DEFAULT nextval('rating_rating_id_seq'::regclass),
  user_id bigint NOT NULL,
  event_id bigint NOT NULL,
  rating integer,
  CONSTRAINT rating_pk PRIMARY KEY (rating_id ),
  CONSTRAINT event_fk FOREIGN KEY (event_id)
      REFERENCES public.event (event_id) 
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT user_fk FOREIGN KEY (user_id)
      REFERENCES public.users (user_id) 
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.rating
OWNER TO postgres;
