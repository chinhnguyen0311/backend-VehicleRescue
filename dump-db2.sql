--
-- PostgreSQL database dump
--

-- Dumped from database version 15.8 (Debian 15.8-1.pgdg110+1)
-- Dumped by pg_dump version 17.0

-- Started on 2026-04-30 17:54:59

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 9 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA public;


--
-- TOC entry 4683 (class 0 OID 0)
-- Dependencies: 9
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS 'standard public schema';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 284 (class 1259 OID 19709)
-- Name: accounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.accounts (
    account_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    username character varying(255) NOT NULL,
    email character varying(255),
    password_hash character varying(255) NOT NULL,
    full_name character varying(255) NOT NULL,
    phone_number character varying(20) NOT NULL,
    refresh_token text,
    role character varying(50) NOT NULL,
    google_id character varying(255),
    avatar_url character varying(2500),
    last_active timestamp with time zone,
    banned_at timestamp with time zone,
    suspended_until timestamp with time zone,
    is_active boolean DEFAULT true,
    email_verified boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    status character varying(20) DEFAULT 'PENDING'::character varying
);


--
-- TOC entry 288 (class 1259 OID 19769)
-- Name: mechanic_services; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mechanic_services (
    mechanic_id uuid NOT NULL,
    service_id uuid NOT NULL,
    custom_price numeric(15,2) NOT NULL
);


--
-- TOC entry 292 (class 1259 OID 36229)
-- Name: mechanic_subscriptions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mechanic_subscriptions (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    mechanic_id uuid NOT NULL,
    current_end_date timestamp without time zone,
    new_end_date timestamp without time zone,
    renewal_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    bill_image_url character varying(2500),
    status character varying(20) DEFAULT 'PENDING'::character varying
);


--
-- TOC entry 287 (class 1259 OID 19749)
-- Name: mechanics; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mechanics (
    mechanic_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    account_id uuid NOT NULL,
    type character varying(50) NOT NULL,
    work_type character varying(50) DEFAULT 'MOBILE'::character varying NOT NULL,
    display_name character varying(255) NOT NULL,
    phone_number character varying(20) NOT NULL,
    description text,
    current_location public.geometry(Point,4326),
    garage_name character varying(255),
    garage_address character varying(500),
    garage_location public.geometry(Point,4326),
    status character varying(50) DEFAULT 'OFFLINE'::character varying,
    rating_score numeric(3,2) DEFAULT 0.00,
    total_reviews integer DEFAULT 0,
    subs_end_date timestamp without time zone,
    is_active_subs boolean DEFAULT false
);


--
-- TOC entry 286 (class 1259 OID 19733)
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications (
    notification_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    account_id uuid NOT NULL,
    type character varying(50) NOT NULL,
    title character varying(255) NOT NULL,
    message text NOT NULL,
    related_id uuid,
    related_type character varying(50),
    is_read boolean DEFAULT false,
    is_sent boolean DEFAULT false,
    read_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- TOC entry 291 (class 1259 OID 19828)
-- Name: reports; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reports (
    report_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    order_id uuid,
    reported_by_type character varying(50) NOT NULL,
    reporter_phone character varying(20) NOT NULL,
    reason_category character varying(100) NOT NULL,
    content text NOT NULL,
    status character varying(50) DEFAULT 'PENDING'::character varying,
    admin_note text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    reported_target character varying(50),
    target_phone character varying(20)
);


--
-- TOC entry 289 (class 1259 OID 19784)
-- Name: rescue_orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rescue_orders (
    order_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    customer_name character varying(255) NOT NULL,
    customer_phone character varying(20) NOT NULL,
    customer_location public.geometry(Point,4326) NOT NULL,
    mechanic_id uuid,
    service_id uuid,
    status character varying(50) DEFAULT 'REQUESTED'::character varying,
    mechanic_name character varying(255),
    completed_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    customer_address text
);


--
-- TOC entry 290 (class 1259 OID 19805)
-- Name: reviews; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reviews (
    review_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    order_id uuid NOT NULL,
    mechanic_id uuid NOT NULL,
    rating integer,
    review text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT reviews_rating_check CHECK (((rating >= 1) AND (rating <= 5)))
);


--
-- TOC entry 285 (class 1259 OID 19725)
-- Name: services; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.services (
    service_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(255) NOT NULL,
    icon_url character varying(500),
    base_price numeric(15,2) NOT NULL
);


--
-- TOC entry 4496 (class 2606 OID 19724)
-- Name: accounts accounts_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_email_key UNIQUE (email);


--
-- TOC entry 4498 (class 2606 OID 19720)
-- Name: accounts accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_pkey PRIMARY KEY (account_id);


--
-- TOC entry 4500 (class 2606 OID 19722)
-- Name: accounts accounts_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_username_key UNIQUE (username);


--
-- TOC entry 4510 (class 2606 OID 19773)
-- Name: mechanic_services mechanic_services_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mechanic_services
    ADD CONSTRAINT mechanic_services_pkey PRIMARY KEY (mechanic_id, service_id);


--
-- TOC entry 4520 (class 2606 OID 36238)
-- Name: mechanic_subscriptions mechanic_subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mechanic_subscriptions
    ADD CONSTRAINT mechanic_subscriptions_pkey PRIMARY KEY (id);


--
-- TOC entry 4506 (class 2606 OID 19763)
-- Name: mechanics mechanics_account_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mechanics
    ADD CONSTRAINT mechanics_account_id_key UNIQUE (account_id);


--
-- TOC entry 4508 (class 2606 OID 19761)
-- Name: mechanics mechanics_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mechanics
    ADD CONSTRAINT mechanics_pkey PRIMARY KEY (mechanic_id);


--
-- TOC entry 4504 (class 2606 OID 19743)
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (notification_id);


--
-- TOC entry 4518 (class 2606 OID 19837)
-- Name: reports reports_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reports
    ADD CONSTRAINT reports_pkey PRIMARY KEY (report_id);


--
-- TOC entry 4512 (class 2606 OID 19794)
-- Name: rescue_orders rescue_orders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rescue_orders
    ADD CONSTRAINT rescue_orders_pkey PRIMARY KEY (order_id);


--
-- TOC entry 4514 (class 2606 OID 19817)
-- Name: reviews reviews_order_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_order_id_key UNIQUE (order_id);


--
-- TOC entry 4516 (class 2606 OID 19815)
-- Name: reviews reviews_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_pkey PRIMARY KEY (review_id);


--
-- TOC entry 4502 (class 2606 OID 19732)
-- Name: services services_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.services
    ADD CONSTRAINT services_pkey PRIMARY KEY (service_id);


--
-- TOC entry 4522 (class 2606 OID 19764)
-- Name: mechanics fk_mechanic_account; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mechanics
    ADD CONSTRAINT fk_mechanic_account FOREIGN KEY (account_id) REFERENCES public.accounts(account_id) ON DELETE CASCADE;


--
-- TOC entry 4523 (class 2606 OID 19774)
-- Name: mechanic_services fk_ms_mechanic; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mechanic_services
    ADD CONSTRAINT fk_ms_mechanic FOREIGN KEY (mechanic_id) REFERENCES public.mechanics(mechanic_id) ON DELETE CASCADE;


--
-- TOC entry 4524 (class 2606 OID 19779)
-- Name: mechanic_services fk_ms_service; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mechanic_services
    ADD CONSTRAINT fk_ms_service FOREIGN KEY (service_id) REFERENCES public.services(service_id) ON DELETE CASCADE;


--
-- TOC entry 4521 (class 2606 OID 19744)
-- Name: notifications fk_noti_account; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fk_noti_account FOREIGN KEY (account_id) REFERENCES public.accounts(account_id) ON DELETE CASCADE;


--
-- TOC entry 4525 (class 2606 OID 19795)
-- Name: rescue_orders fk_order_mechanic; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rescue_orders
    ADD CONSTRAINT fk_order_mechanic FOREIGN KEY (mechanic_id) REFERENCES public.mechanics(mechanic_id) ON DELETE SET NULL;


--
-- TOC entry 4526 (class 2606 OID 19800)
-- Name: rescue_orders fk_order_service; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rescue_orders
    ADD CONSTRAINT fk_order_service FOREIGN KEY (service_id) REFERENCES public.services(service_id) ON DELETE SET NULL;


--
-- TOC entry 4529 (class 2606 OID 19838)
-- Name: reports fk_report_order; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reports
    ADD CONSTRAINT fk_report_order FOREIGN KEY (order_id) REFERENCES public.rescue_orders(order_id) ON DELETE CASCADE;


--
-- TOC entry 4527 (class 2606 OID 19823)
-- Name: reviews fk_review_mechanic; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT fk_review_mechanic FOREIGN KEY (mechanic_id) REFERENCES public.mechanics(mechanic_id) ON DELETE CASCADE;


--
-- TOC entry 4528 (class 2606 OID 19818)
-- Name: reviews fk_review_order; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES public.rescue_orders(order_id) ON DELETE CASCADE;


--
-- TOC entry 4530 (class 2606 OID 36239)
-- Name: mechanic_subscriptions fk_subscription_mechanic; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mechanic_subscriptions
    ADD CONSTRAINT fk_subscription_mechanic FOREIGN KEY (mechanic_id) REFERENCES public.mechanics(mechanic_id) ON DELETE CASCADE;


-- Completed on 2026-04-30 17:54:59

--
-- PostgreSQL database dump complete
--

