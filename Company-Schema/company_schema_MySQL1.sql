create database company;
 
use company;

show tables;

-- ------------------ branch --------------------
create table branch
(branch_no varchar(7),
street varchar(40),
city varchar(20),
state varchar(12),
zip_code numeric(5,0) check (zip_code > 0),
phone_no varchar(13),
primary key (branch_no)
);

-- ------------------ staff --------------------
create table staff
(staff_no varchar(7),
name varchar(40),
position varchar(20),
salary numeric(8,2),
primary key (staff_no)
);

-- ------------------ employs --------------------
create table employs
(staff_no varchar(7),
branch_no varchar(7),
primary key (staff_no),
foreign key (staff_no) references staff(staff_no) on delete cascade,
foreign key (branch_no) references branch(branch_no) on delete cascade
);

-- ------------------ DVD --------------------
create table DVD
(catalog_no varchar(7),
title varchar(200),
category varchar(20),
cost numeric(2,2),
main_actors varchar(200),
director varchar(40),
primary key (catalog_no)
);

-- ------------------ copy --------------------
create table copy
(DVD_no varchar(7),
status boolean,
catalog_no varchar(7),
daily_rental numeric(2,2),
primary key (DVD_no),
foreign key (catalog_no) references DVD(catalog_no) on delete cascade
);

-- ------------------ stock --------------------
create table stock
(DVD_no varchar(7),
branch_no varchar(7),
primary key (DVD_no),
foreign key (DVD_no) references copy(DVD_no) on delete cascade,
foreign key (branch_no) references branch(branch_no) on delete cascade
);

-- ------------------ member --------------------
create table member
(member_no varchar(7),
branch_no varchar(7),
first_name varchar(20),
last_name varchar(20),
address varchar(80),
date_registered date,
primary key (member_no),
foreign key (branch_no) references branch(branch_no) on delete set null
);

-- ------------------ rent --------------------
create table rent
(rental_no varchar(2) check(rental_no >= 1 and rental_no <= 10),
member_no varchar(7),
DVD_no varchar(7),
date_rented date,
date_returned date,
primary key (rental_no, member_no),
foreign key (member_no) references member(member_no) on delete cascade,
foreign key (DVD_no) references copy(DVD_no) on delete cascade
);