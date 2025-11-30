-- Run as postgres superuser
CREATE SCHEMA IF NOT EXISTS rating_engine;
CREATE ROLE ratingadmin WITH LOGIN PASSWORD 'securepassword' CREATEROLE;

GRANT ALL PRIVILEGES ON DATABASE ratingman TO ratingadmin;
GRANT ALL ON SCHEMA rating_engine to ratingadmin;

ALTER SCHEMA rating_engine OWNER TO ratingadmin;