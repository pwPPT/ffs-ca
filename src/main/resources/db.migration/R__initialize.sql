drop table IF EXISTS ffs_ca.users;

create TABLE ffs_ca.users (
    id SERIAL PRIMARY KEY,
    username TEXT NOT NULL
);

insert into ffs_ca.users (username) values ('admin');