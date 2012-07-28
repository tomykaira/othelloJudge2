# --- Program schema

# --- !Ups

create table program (
  email                     varchar(255) not null primary key,
  path                      varchar(255) not null,
  version                   int not null
);

# --- !Downs

drop table if exists program;
