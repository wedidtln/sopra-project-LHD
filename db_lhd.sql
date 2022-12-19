--
-- PostgreSQL database dump
--

-- Dumped from database version 14.6
-- Dumped by pg_dump version 14.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: lhd; Type: SCHEMA; Schema: -; Owner: -
--
DROP SCHEMA IF EXISTS PUBLIC CASCADE;
DROP SCHEMA IF EXISTS lhd CASCADE ;
CREATE SCHEMA lhd;
SET search_path to lhd;
ALTER DATABASE lhd SET search_path to lhd;
CREATE EXTENSION IF NOT EXISTS btree_gist WITH SCHEMA lhd;

--
-- Name: email; Type: DOMAIN; Schema: lhd; Owner: -
--

CREATE DOMAIN lhd.email AS character varying(254)
	CONSTRAINT email_check CHECK (((VALUE)::text ~ '^[a-zA-Z0-9.!#$%&''*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$'::text));


--
-- Name: check_date_overlap_trigger_function_group_slot(); Type: FUNCTION; Schema: lhd; Owner: -
--

CREATE FUNCTION lhd.check_date_overlap_trigger_function_group_slot() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
       begin
       IF EXISTS(select from slots  s 
           where id=new.id_slot--- we select the timerange from the id in the query
            and exists (
                    select from group_slot gs--- then we check whether there exists an overlapping one
                    join slots s2 on gs.id_slot=s2.id  
                    where gs.id_group = new.id_group 
                    and s2.id <> new.id_slot--- we don't wanna compare the slot specified in the query to itself
                    and s.timerange && s2.timerange  for share--- we use for share to prevent any modification while the trigger is run. 
                       ) 
       for share) 

       THEN
            RAISE unique_violation USING MESSAGE = 'Overlapping timeranges for group : ' || new.id_group;
       END IF;
   RETURN NEW;
       end
       $$;


--
-- Name: check_date_overlap_trigger_function_professor_slot(); Type: FUNCTION; Schema: lhd; Owner: -
--

CREATE FUNCTION lhd.check_date_overlap_trigger_function_professor_slot() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
       begin
       IF EXISTS(select from slots  s
           where id=new.id_slot--- we select the timerange from the id in the query
            and exists (
                    select from professor_slot ls--- then we check whether there exists an overlapping one
                    join slots s2 on ls.id_slot=s2.id
                    where ls.id_professor = new.id_professor
                    and s2.id <> new.id_professor--- we don't wanna compare the slot specified in the query to itself
                    and s.timerange && s2.timerange  for share--- we use for share to prevent any modification while the trigger is run.
                       )
       for share)

       THEN
            RAISE unique_violation USING MESSAGE = 'Overlapping timeranges for group : ' || new.id_professor;
       END IF;
   RETURN NEW;
       end
       $$;


--
-- Name: check_date_overlap_trigger_hook(); Type: FUNCTION; Schema: lhd; Owner: -
--

CREATE FUNCTION lhd.check_date_overlap_trigger_hook() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
       begin
       IF EXISTS(select from slots  s where id=new.id_slot and exists (select from group_slot gs2 join slots s2 on gs2.id_slot=s2.id  where gs2.id_group =new.id_group and s2.id <> new.id_slot and s.timerange && s2.timerange limit 1 for share) limit 1 for share) THEN
                   RAISE EXCEPTION 'Timerange is overlapping between two courses';
               END IF;
           RETURN NEW;
       end
       $$;


--
-- Name: check_date_overlap_trigger_slot_update_group(); Type: FUNCTION; Schema: lhd; Owner: -
--

CREATE FUNCTION lhd.check_date_overlap_trigger_slot_update_group() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
       begin
       IF OLD.TIMERANGE @> NEW.TIMERANGE THEN 
       RETURN NEW; 
       END IF; 
       
       IF EXISTS (select from slots s join group_slot gs  
       on s.id=gs.id_slot 
       where s.id=new.id 
     and exists (
        select * from slots s2 join group_slot gs2 
        on s2.id=gs2.id_slot 
        where gs2.id_group = gs.id_group 
            and s2.id <> new.id 
            and s2.timerange && new.timerange)
       for share)

       THEN
            RAISE unique_violation USING MESSAGE = 'Unavailable group on slot : ' || new.id_slot;
       END IF;
   RETURN NEW;
       end
       $$;


--
-- Name: check_date_overlap_trigger_slot_update_professor(); Type: FUNCTION; Schema: lhd; Owner: -
--

CREATE FUNCTION lhd.check_date_overlap_trigger_slot_update_professor() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
       begin
       IF OLD.TIMERANGE @> NEW.TIMERANGE THEN 
       RETURN NEW; 
       END IF; 
       
       IF EXISTS (select from slots s join group_professor gl
       on s.id=gs.id_slot 
       where s.id=new.id 
     and exists (
        select * from slots s2 join group_professor gl2 
        on s2.id=gs2.id_slot 
        where gl2.id_professor = gl.id_professor
            and s2.id <> new.id 
            and s2.timerange && new.timerange)
       for share)

       THEN
            RAISE unique_violation USING MESSAGE = 'Unavailable group on slot : ' || new.id_slot;
       END IF;
   RETURN NEW;
       end
       $$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: users; Type: TABLE; Schema: lhd; Owner: -
--

CREATE TABLE lhd.users (
    id bigint NOT NULL,
    name character varying NOT NULL,
    fname character varying NOT NULL,
    email lhd.email NOT NULL,
    password character varying NOT NULL
);


--
-- Name: admins; Type: TABLE; Schema: lhd; Owner: -
--

CREATE TABLE lhd.admins (
    dpt character varying
)
INHERITS (lhd.users);


--
-- Name: admins_id_seq; Type: SEQUENCE; Schema: lhd; Owner: -
--

ALTER TABLE lhd.admins ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME lhd.admins_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: classrooms; Type: TABLE; Schema: lhd; Owner: -
--

CREATE TABLE lhd.classrooms (
    id bigint NOT NULL,
    name character varying NOT NULL
);


--
-- Name: classrooms_id_seq; Type: SEQUENCE; Schema: lhd; Owner: -
--

ALTER TABLE lhd.classrooms ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME lhd.classrooms_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);
--
-- Name: group_slot; Type: TABLE; Schema: lhd; Owner: -
--

CREATE TABLE lhd.group_slot (
    id_group bigint NOT NULL,
    id_slot bigint NOT NULL
);


--
-- Name: group_user; Type: TABLE; Schema: lhd; Owner: -
--

CREATE TABLE lhd.group_user (
    id_group bigint NOT NULL,
    id_user bigint NOT NULL
);


--
-- Name: groups; Type: TABLE; Schema: lhd; Owner: -
--

CREATE TABLE lhd.groups (
    id bigint NOT NULL,
    name character varying NOT NULL
);


--
-- Name: groups_id_seq; Type: SEQUENCE; Schema: lhd; Owner: -
--

ALTER TABLE lhd.groups ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME lhd.groups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: professor_slot; Type: TABLE; Schema: lhd; Owner: -
--

CREATE TABLE lhd.professor_slot (
    id_professor bigint NOT NULL,
    id_slot bigint NOT NULL
);


--
-- Name: professors; Type: TABLE; Schema: lhd; Owner: -
--

CREATE TABLE lhd.professors (
    title character varying NOT NULL
)
INHERITS (lhd.users);


--
-- Name: professors_id_seq; Type: SEQUENCE; Schema: lhd; Owner: -
--

ALTER TABLE lhd.professors ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME lhd.professors_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: slots; Type: TABLE; Schema: lhd; Owner: -
--

CREATE TABLE lhd.slots (
    id bigint NOT NULL,
    timerange tstzrange,
    classroom bigint,
    memo character varying,
    subject bigint NOT NULL,
    type character varying
);


--
-- Name: slots_id_seq; Type: SEQUENCE; Schema: lhd; Owner: -
--

ALTER TABLE lhd.slots ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME lhd.slots_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: subjects; Type: TABLE; Schema: lhd; Owner: -
--

CREATE TABLE lhd.subjects (
    id bigint NOT NULL,
    name character varying NOT NULL,
    hour_count_max double precision NOT NULL
);


--
-- Name: subject_id_seq; Type: SEQUENCE; Schema: lhd; Owner: -
--

ALTER TABLE lhd.subjects ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME lhd.subject_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: lhd; Owner: -
--

ALTER TABLE lhd.users ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME lhd.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: classrooms classroom_unique; Type: CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.classrooms
    ADD CONSTRAINT classroom_unique UNIQUE (name);


--
-- Name: slots creneau_pkey; Type: CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.slots
    ADD CONSTRAINT creneau_pkey PRIMARY KEY (id);


--
-- Name: groups group_unique; Type: CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.groups
    ADD CONSTRAINT group_unique UNIQUE (name);


--
-- Name: professors lect_pkey; Type: CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.professors
    ADD CONSTRAINT lect_pkey PRIMARY KEY (id);


--
-- Name: group_slot promotion_creneau_pkey; Type: CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.group_slot
    ADD CONSTRAINT promotion_creneau_pkey PRIMARY KEY (id_group, id_slot);


--
-- Name: professor_slot promotion_professors_pkey; Type: CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.professor_slot
    ADD CONSTRAINT promotion_professors_pkey PRIMARY KEY (id_professor, id_slot);


--
-- Name: group_user promotion_usager_pkey; Type: CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.group_user
    ADD CONSTRAINT promotion_usager_pkey PRIMARY KEY (id_group, id_user);


--
-- Name: classrooms salle_pkey; Type: CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.classrooms
    ADD CONSTRAINT salle_pkey PRIMARY KEY (id);


--
-- Name: slots slots_classroom_timerange_excl; Type: CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.slots
    ADD CONSTRAINT slots_classroom_timerange_excl EXCLUDE USING gist (classroom WITH =, timerange WITH &&);


--
-- Name: subjects subject_pkey; Type: CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.subjects
    ADD CONSTRAINT subject_pkey PRIMARY KEY (id);


--
-- Name: subjects subject_unique; Type: CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.subjects
    ADD CONSTRAINT subject_unique UNIQUE (name);


--
-- Name: groups user_pkey; Type: CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.groups
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: admins_lower_email_key; Type: INDEX; Schema: lhd; Owner: -
--

CREATE UNIQUE INDEX admins_lower_email_key ON lhd.admins USING btree (lower((email)::text));


--
-- Name: group_slot_id_slot_idx; Type: INDEX; Schema: lhd; Owner: -
--

CREATE INDEX group_slot_id_slot_idx ON lhd.group_slot USING btree (id_slot);


--
-- Name: professors_lower_email_key; Type: INDEX; Schema: lhd; Owner: -
--

CREATE UNIQUE INDEX professors_lower_email_key ON lhd.professors USING btree (lower((email)::text));


--
-- Name: slots_timerange_idx; Type: INDEX; Schema: lhd; Owner: -
--

CREATE INDEX slots_timerange_idx ON lhd.slots USING btree (timerange);


--
-- Name: unqiue_promotion_name; Type: INDEX; Schema: lhd; Owner: -
--

CREATE UNIQUE INDEX unqiue_promotion_name ON lhd.groups USING btree (name);


--
-- Name: users_lower_email_key; Type: INDEX; Schema: lhd; Owner: -
--

CREATE UNIQUE INDEX users_lower_email_key ON lhd.users USING btree (lower((email)::text));


--
-- Name: group_slot check_timerange_overlap; Type: TRIGGER; Schema: lhd; Owner: -
--

CREATE TRIGGER check_timerange_overlap BEFORE INSERT OR UPDATE ON lhd.group_slot FOR EACH ROW EXECUTE FUNCTION lhd.check_date_overlap_trigger_function_group_slot();


--
-- Name: professor_slot ls_check_trigger; Type: TRIGGER; Schema: lhd; Owner: -
--

CREATE TRIGGER ls_check_trigger BEFORE INSERT OR UPDATE ON lhd.professor_slot FOR EACH ROW EXECUTE FUNCTION lhd.check_date_overlap_trigger_function_professor_slot();


--
-- Name: slots update_slot_check_group; Type: TRIGGER; Schema: lhd; Owner: -
--

CREATE TRIGGER update_slot_check_group BEFORE UPDATE ON lhd.slots FOR EACH ROW EXECUTE FUNCTION lhd.check_date_overlap_trigger_slot_update_group();


--
-- Name: slots update_slot_check_professor; Type: TRIGGER; Schema: lhd; Owner: -
--

CREATE TRIGGER update_slot_check_professor BEFORE UPDATE ON lhd.slots FOR EACH ROW EXECUTE FUNCTION lhd.check_date_overlap_trigger_slot_update_professor();


--
-- Name: group_slot fk_creneau; Type: FK CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.group_slot
    ADD CONSTRAINT fk_creneau FOREIGN KEY (id_slot) REFERENCES lhd.slots(id);


--
-- Name: professor_slot fk_creneau; Type: FK CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.professor_slot
    ADD CONSTRAINT fk_creneau FOREIGN KEY (id_slot) REFERENCES lhd.slots(id);


--
-- Name: group_user fk_promotions; Type: FK CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.group_user
    ADD CONSTRAINT fk_promotions FOREIGN KEY (id_group) REFERENCES lhd.groups(id);


--
-- Name: group_slot fk_promotions; Type: FK CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.group_slot
    ADD CONSTRAINT fk_promotions FOREIGN KEY (id_group) REFERENCES lhd.groups(id);


--
-- Name: professor_slot fk_promotions; Type: FK CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.professor_slot
    ADD CONSTRAINT fk_promotions FOREIGN KEY (id_professor) REFERENCES lhd.professors(id);


--
-- Name: group_user fk_user; Type: FK CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.group_user
    ADD CONSTRAINT fk_user FOREIGN KEY (id_user) REFERENCES lhd.users(id);


--
-- Name: slots salle_fkey; Type: FK CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.slots
    ADD CONSTRAINT salle_fkey FOREIGN KEY (classroom) REFERENCES lhd.classrooms(id);


--
-- Name: slots subject_fkey; Type: FK CONSTRAINT; Schema: lhd; Owner: -
--

ALTER TABLE ONLY lhd.slots
    ADD CONSTRAINT subject_fkey FOREIGN KEY (subject) REFERENCES lhd.subjects(id);


--
-- PostgreSQL database dump complete
--

