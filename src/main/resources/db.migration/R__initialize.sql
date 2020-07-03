drop table IF EXISTS ffs_ca.users;

create TABLE ffs_ca.users (
    id SERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL
);

insert into ffs_ca.users (username) values ('admin');

drop table IF EXISTS ffs_ca.auth_state;

create TABLE ffs_ca.auth_state (
    token SERIAL PRIMARY KEY,  -- TODO - set to UUID
    user_id REFERENCES ffs_ca.users (id) ON DELETE CASCADE NOT NULL,
    start_time TIMESTAMP NOT NULL DEFAUTL NOW(),
    reps INTEGER NOT NULL,
    succ_tries INTEGER NOT NULL DEFAULT 0 CHECK (succ_tries >= 0),
    curr_X INTEGER,
    curr_A boolean ARRAY
)
