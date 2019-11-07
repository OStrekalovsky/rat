create database receipts;
use receipts;
create table if not exists States(
    origin  varchar(255) NOT NULL,
    card_number varchar(255) not null,
    sale_date timestamp not null
)ENGINE=InnoDB CHARACTER SET=utf8;
create table if not exists Receipts (
    sale_id int AUTO_INCREMENT PRIMARY KEY,
    card_number  varchar(255) not null ,
    sale_date timestamp not null
)ENGINE=InnoDB CHARACTER SET=utf8;
create table if not exists Products(
    sale_id int not null,
    code integer not null,
    name  varchar(255) not null,
    price float not null,
    count  integer not null,
    FOREIGN KEY (sale_id)
        REFERENCES Receipts (sale_id)
        ON UPDATE RESTRICT ON DELETE CASCADE
)ENGINE=InnoDB CHARACTER SET=utf8;
