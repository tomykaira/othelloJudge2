# --- Battle schema

# --- !Ups

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

# --- !Downs

drop sequence if exists battle_seq;

drop table if exists battle;
