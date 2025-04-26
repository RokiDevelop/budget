create table if not exists author
(
    id          serial primary key,
    full_name   varchar(100) not null,
    created_at  timestamp not null default NOW()
);
