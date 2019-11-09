-- create database rat;
use rat;
create table if not exists States(
    origin  varchar(255) NOT NULL,
    offset int not null,
    PRIMARY KEY (origin, offset)
)ENGINE=InnoDB CHARACTER SET=utf8;
create table if not exists Receipts (
    sale_id int AUTO_INCREMENT PRIMARY KEY,
    card_number varchar(255) not null,
    sale_date timestamp not null,
    INDEX (sale_date)
)ENGINE=InnoDB CHARACTER SET=utf8;
create table if not exists Products(
    sale_id int not null,
    INDEX (sale_id),
    code integer not null,
    INDEX (code),
    name  varchar(255) not null,
    price float not null,
    count  integer not null,
    FOREIGN KEY (sale_id)
        REFERENCES Receipts (sale_id)
        ON UPDATE RESTRICT ON DELETE CASCADE
)ENGINE=InnoDB CHARACTER SET=utf8;
