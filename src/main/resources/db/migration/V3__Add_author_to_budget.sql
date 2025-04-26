alter table budget
add column author_id int;

alter table budget
add constraint fk_author_id
foreign key (author_id)
references author(id)
on delete cascade;
