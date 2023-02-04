CREATE TABLE Users(
    username VARCHAR NOT NULL UNIQUE,
    email VARCHAR NOT NULL,
    date_of_creation TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (username)
);
