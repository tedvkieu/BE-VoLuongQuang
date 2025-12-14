-- BẢNG product
CREATE TABLE product (
    product_id VARCHAR(50) NOT NULL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    product_group_id VARCHAR(50),
    category_id VARCHAR(50),
    brand_id VARCHAR(50),
    price NUMERIC(10, 2),
    cost_price NUMERIC(10, 2),
    wholesale_price NUMERIC(10, 2),
    discount_percent INT DEFAULT 0,
    stock_quantity INT,
    weight NUMERIC(10, 2),
    unit VARCHAR(50),
    is_featured BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    image_url TEXT,
    url_shopee TEXT,
    url_lazada TEXT,
    url_other TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- BẢNG brand
CREATE TABLE brand (
    brand_id VARCHAR(50) NOT NULL PRIMARY KEY,
    brand_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- BẢNG category
CREATE TABLE category (
    category_id VARCHAR(50) NOT NULL PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- BẢNG product_group
CREATE TABLE product_group (
    gr_prd_id VARCHAR(50) NOT NULL PRIMARY KEY,
    gr_prd_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- BẢNG users
CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    phone VARCHAR(50),
    address TEXT,
    role VARCHAR(50) CHECK (role IN ('CUSTOMER', 'ADMIN', 'STAFF')) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- BẢNG cart
CREATE TABLE cart (
    cart_id SERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- BẢNG cart_items (ĐỔI TÊN prd_id => product_id cho đúng với bảng product)
CREATE TABLE cart_items (
    cart_id INT NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    PRIMARY KEY (cart_id, product_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- RÀNG BUỘC NGOẠI
ALTER TABLE product 
ADD CONSTRAINT fk_brand FOREIGN KEY (brand_id) 
REFERENCES brand(brand_id) ON DELETE SET NULL;

ALTER TABLE product 
ADD CONSTRAINT fk_category FOREIGN KEY (category_id) 
REFERENCES category(category_id) ON DELETE CASCADE;

ALTER TABLE product 
ADD CONSTRAINT fk_product_group FOREIGN KEY (product_group_id) 
REFERENCES product_group(gr_prd_id) ON DELETE SET NULL;

ALTER TABLE cart
ADD CONSTRAINT fk_cart_user FOREIGN KEY (user_id) 
REFERENCES users(user_id) ON DELETE CASCADE;

ALTER TABLE cart_items
ADD CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) 
REFERENCES cart(cart_id) ON DELETE CASCADE;

ALTER TABLE cart_items
ADD CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) 
REFERENCES product(product_id) ON DELETE CASCADE;
