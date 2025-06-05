create table public.tattoo_artists
(
    id           uuid not null default gen_random_uuid(),
    username     text not null,
    display_name text null,
    constraint tattoo_artists_pkey primary key (id),
    constraint tattoo_artists_id_key unique (id),
    constraint tattoo_artists_username_key unique (username)
) TABLESPACE pg_default;

create table public.tattoo_clients
(
    id         uuid not null default gen_random_uuid(),
    first_name text not null,
    last_name  text not null,
    email      text not null,
    constraint tattoo_clients_pkey primary key (id)
) TABLESPACE pg_default;

create table public.tattoo_projects
(
    id                  uuid not null default gen_random_uuid(),
    desired_date        date not null,
    project_description text not null,
    body_part           text not null,
    tattoo_artist_id    uuid not null,
    tattoo_client_id    uuid not null,
    status              text not null,
    constraint tattoo_requests_pkey primary key (id),
    constraint tattoo_requests_tattoo_artist_id_fkey foreign KEY (tattoo_artist_id) references tattoo_artists (id) on update CASCADE,
    constraint tattoo_requests_tattoo_client_id_fkey foreign KEY (tattoo_client_id) references tattoo_clients (id) on update CASCADE
) TABLESPACE pg_default;

create table public.tattoo_references
(
    id                uuid not null default gen_random_uuid(),
    image_path        text not null,
    comment           text null,
    tattoo_project_id uuid null default gen_random_uuid (),
    constraint tattoo_references_pkey primary key (id),
    constraint tattoo_references_tattoo_project_id_fkey foreign KEY (tattoo_project_id) references tattoo_projects (id) on update CASCADE on delete CASCADE
) TABLESPACE pg_default;

create table public.verification_codes
(
    id                uuid not null default gen_random_uuid(),
    code              text not null,
    expires_at        timestamp without time zone not null,
    tattoo_project_id uuid not null,
    constraint verification_codes_pkey primary key (id),
    constraint verification_codes_tattoo_project_id_fkey foreign KEY (tattoo_project_id) references tattoo_projects (id) on update CASCADE on delete CASCADE
) TABLESPACE pg_default;