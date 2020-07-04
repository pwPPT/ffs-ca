drop table IF EXISTS ffs_ca.users;

create TABLE ffs_ca.users (
    id SERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    public_key BIGINT ARRAY NOT NULL
);

-- insert into ffs_ca.users (username) values ('admin');


-- AUTH STATE
drop table IF EXISTS ffs_ca.auth_state;

create TABLE ffs_ca.auth_state (
    token UUID PRIMARY KEY,
    user_id REFERENCES ffs_ca.users (id) ON DELETE CASCADE NOT NULL,
    start_time TIMESTAMP NOT NULL DEFAUTL NOW(),
    reps INTEGER NOT NULL,
    succ_tries INTEGER NOT NULL DEFAULT 0 CHECK (succ_tries >= 0),
    curr_X INTEGER DEFAULT NULL,
    curr_A boolean ARRAY DEFAULT NULL
);

-- SESSIONS
drop table IF EXISTS ffs_ca.sessions;

create TABLE ffs_ca.sessions (
    token UUID PRIMARY KEY,
    user_id REFERENCES ffs_ca.users (id) ON DELETE CASCADE NOT NULL,
    authentication_time TIMESTAMP NOT NULL DEFAULT NOW()
)

-- NOTES
drop table IF EXISTS ffs_ca.notes;

create TABLE ffs_ca.notes (
    id SERIAL PRIMARY KEY,
    user_id REFERENCES ffs_ca.users (id) ON DELETE CASCADE NOT NULL,
    note TEXT NOT NULL
);


-- /api/ca/register endpoint:
-- request payload: {'username': ###, 'public_key': [list of integers]}
INSERT INTO ffs_ca.users (username, public_key) VALUES ($1, ARRAY[$2, $3, ..., $n]);


-- /api/ca/token endpoint:
-- 1. Check if there is existing auth_state for corresponding user, if there is return it's token
-- 2. Otherwise create new state with:
-- CREATE NEW STATE
-- token - generated UUID; user_id from users table; reps - random from range [4, 8]
INSERT INTO ffs_ca.auth_state (token, user_id, reps) VALUES ($1, $2, $3);
-- and return new token


-- /api/ca/X endpoint:
-- 1. Fetch auth_state from DB
SELECT a.token, a.start_time, a.reps, a.succ_tries, a.curr_X, a.curr_A, u.username, u.public_key 
    FROM ffs_ca.auth_state as a 
    JOIN ffs_ca.users as u ON a.user_id = u.id;
-- 2. Check if curr_X != NULL -- True: return BAD REQUEST; Otherwise: step 3
-- 3.     Generate vector A of random values from {0, 1} with length equal to length of public_key vector
-- 4.     SET curr_X (sent by user in request) and curr_A (generated)
UPDATE ffs_ca.auth_state SET curr_X = $1, curr_A = $2
    WHERE token = $3
    RETURNING curr_X, curr_A;
-- 5. Return generated A to the user


-- /api/ca/Y endpoint:
-- 1. Fetch auth_state from DB (select ^^^)
-- 2. Check if curr_X == NULL -- True: return BAD REQUEST; Otherwise: step 3
-- 3. Verify user (procedure:
--     def verify(curr_X, Y):
--        values = [s**i for (s, i) in zip(public_key, A)]
--        Y1 = (curr_X * reduce(lambda i, j: (i * j) % N, values)) % N
--        return Y1 == (Y ** 2) % N
--    ) if verify(curr_X, Y) == True : 
--            3a. increment succ_tries in DB,
                UPDATE ffs_ca.auth_state SET succ_tries = succ_tries + 1 WHERE token = $1;
--                 if succ_tries == reps:  
--                     - delete record from auth_state table:
                       DELETE FROM ffs_ca.auth_state WHERE token = $1;
--                     - create new record in sessions table:
                       INSERT INTO ffs_ca.sessions (token, user_id) VALUES ($1, $2);
--                     - return {'repeat': False, 'is_authenticated': True, 'session_id': token}
--                  otherwise:
--                     - update record from auth_state table - set curr_X and curr_A = NULL
                       UPDATE ffs_ca.auth_state SET curr_X = NULL, curr_A = NULL WHERE token = $1;
--                     - return {'repeat': True, 'is_authenticated': False, 'session_id': null}
--      if verify(curr_X, Y) == False :
--            3b. - delete record from auth_state table (delete stmt ^^^)
--                - return {'repeat': False, 'is_authenticated': False, 'session_id': null}


-- /api/ca/notes endpoint POST: (add new note)
-- req payload: {'session_id': ###, 'note': ###}
-- check if session exist and is not expired:
SELECT token, user_id, authentication_time 
    FROM ffs_ca.sessions 
    WHERE token = $1 AND authentication_time >= NOW() - INTERVAL '20 minutes';
-- if there is session:
INSERT INTO ffs_ca.notes (user_id, note) VALUES ($1, $2);
-- return 200 OK
-- if there is no session return UNAUTHORIZED


-- /api/ca/notes endpoint GET: (list all notes)
-- check session ^^^
-- if session exists:
SELECT note FROM ffs_ca.notes WHERE user_id = $1;
-- return {"notes": [list of notes]}
-- if session does not exist:
-- return UNAUTHORIZED