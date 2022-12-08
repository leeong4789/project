use testdb;

/*카테고리 테이블*/
drop table tbl_category;

create table tbl_category(
	c_code int auto_increment primary key,
    c_type varchar(6) not null
);

desc tbl_category;

select * from tbl_category;


/*유저 타입 테이블*/
drop table tbl_user_type;

create table tbl_user_type(
	u_t_code int auto_increment primary key,
    u_type varchar(6) not null
);


desc tbl_user_type;

select * from tbl_user_type;

/*등급 테이블*/
drop table tbl_grade;

create table tbl_grade(
	standard int,
    grade varchar(6) not null primary key
);


desc tbl_grade;

select * from tbl_grade;

/*가게 테이블*/
drop table tbl_store;

create table tbl_store(
	s_code varchar(10) not null primary key,
    s_name varchar(50) not null,
    s_admin varchar(20) not null,
    s_location varchar(200) not null,
    s_tel varchar(20) not null,
    s_c_code int not null,
    s_waiting int default 0,
    s_order int default 0,
    s_rating double default 0,
    s_status boolean default true,
    s_photo varchar(200),
    u_id varchar(20) not null,
    u_pass varchar(255) not null,
    foreign key (s_c_code) references tbl_category(c_code)
);

desc tbl_store;

select * from tbl_store;

/*메뉴 테이블*/
drop table tbl_menu;

create table tbl_menu(
	m_code int,
	s_code varchar(10) not null,
    m_name varchar(50) not null,
    m_price int not null,
    m_photo varchar(200),
    primary key (s_code, m_name),
    foreign key (s_code) references tbl_store(s_code)
);


desc tbl_menu;

select * from tbl_menu;

/* 유저 테이블 */
drop table tbl_user;

create table tbl_user(
	u_code varchar(100) not null primary key, 
    u_name varchar (20) not null,
    u_address varchar(200) not null,
    u_id varchar(20) not null,
    u_pass varchar(255) not null,
    u_adult boolean default false,
    u_date datetime default now(),
    manner int default 0,
    r_count int default 0,
    u_type int default 3,
    u_photo varchar(200),
    u_status boolean default true,
	foreign key (u_type) references tbl_user_type(u_t_code) 
);

desc tbl_user;

select * from tbl_user;


/*즐겨찾기 테이블*/

drop table tbl_liked;

create table tbl_liked(
	s_code varchar(10) not null,
    u_code varchar(100) not null,
    foreign key (s_code) references tbl_store(s_code),
    foreign key (u_code) references tbl_user(u_code),
    primary key(s_code,u_code)
);

desc tbl_liked;

select * from tbl_liked;

/*신고 테이블*/
drop table tbl_report;

create table tbl_report(
	r_code varchar(100) not null primary key,
    suer varchar(100) not null,
    defender varchar(100) not null,
    report_date  datetime default now(),
    r_result varchar(500) not null,
    r_state varchar(10) default '접수수리',
    r_type varchar(10) not null,
    foreign key (suer) references tbl_user(u_code),
    foreign key (defender) references tbl_user(u_code)
);

desc tbl_report;

select * from tbl_report;


/*리뷰 테이블*/
drop table tbl_review;

create table tbl_review(
	s_code varchar(10) not null,
	u_code varchar(100) not null,
    r_content varchar(200),
    r_photo varchar(200),
    r_rating double not null,
    review_date datetime default now(),
    foreign key (s_code) references tbl_store(s_code),
    foreign key (u_code) references tbl_user(u_code),
    primary key(u_code,review_date)
);

select * from tbl_review;

/*유저 삭제 테이블*/

drop table tbl_user_delete;

create table tbl_user_delete(
	d_code varchar(100) not null primary key,
    d_date datetime default now(),
    foreign key (d_code) references tbl_user(u_code)
);

select * from tbl_user_delete;


/*장바구니 테이블*/
drop table tbl_cart;

create table tbl_cart(
	u_code varchar(100) not null,
	s_code varchar(10) not null,
    m_name varchar(50) not null,
    amount int default 0,
	foreign key (u_code) references tbl_user(u_code),
    foreign key (s_code) references tbl_store(s_code),
	primary key(u_code,s_code,m_name)
);

select * from tbl_cart;

desc tbl_cart;







