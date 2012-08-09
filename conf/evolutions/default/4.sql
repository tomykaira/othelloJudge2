# --- Battle schema

# --- !Ups

drop sequence if exists battle_seq;

drop table if exists battle;

create table battle (
  id                        int not null primary key,
  black_mail                varchar(255) not null,
  black_version             int not null,
  white_mail                varchar(255) not null,
  white_version             int not null,
  status                    varchar(255) not null,
  server_output             text not null,
  black_output              text not null,
  white_output              text not null
);

create sequence battle_seq start with 1000;

# --- !Downs

drop sequence if exists battle_seq;

drop table if exists battle;

create table battle (
  id                        int not null primary key,
  challenger_mail           varchar(255) not null,
  challenger_version        int not null,
  opponent_mail             varchar(255) not null,
  opponent_version          int not null,
  status                    varchar(255) not null,
  output                    text not null
);

create sequence battle_seq start with 1000;
