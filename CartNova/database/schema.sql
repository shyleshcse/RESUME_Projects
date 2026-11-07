-- Optional: Hibernate (ddl-auto=update) creates these tables automatically.
CREATE DATABASE IF NOT EXISTS cartnova;
USE cartnova;

CREATE TABLE users (
  id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  name      VARCHAR(100) NOT NULL,
  email     VARCHAR(150) NOT NULL UNIQUE,
  password  VARCHAR(100) NOT NULL,            -- BCrypt hash
  role      VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER'   -- CUSTOMER | ADMIN
);

CREATE TABLE products (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(200) NOT NULL,
  description VARCHAR(1000),
  price       DECIMAL(10,2) NOT NULL,
  category    VARCHAR(80),
  image_url   VARCHAR(500),
  stock       INT NOT NULL DEFAULT 0
);

CREATE TABLE orders (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT NOT NULL,
  user_email  VARCHAR(150),
  total       DECIMAL(10,2) NOT NULL,
  status      VARCHAR(20) NOT NULL DEFAULT 'PLACED',  -- PLACED | PACKED | SHIPPED | DELIVERED | CANCELLED
  created_at  DATETIME NOT NULL
);

CREATE TABLE order_items (
  order_id      BIGINT NOT NULL,
  product_id    BIGINT NOT NULL,
  product_name  VARCHAR(200),
  price         DECIMAL(10,2),
  quantity      INT NOT NULL,
  FOREIGN KEY (order_id) REFERENCES orders(id)
);
