COPY product(
    product_id,
    product_name,
    product_group_id,
    category_id,
    brand_id,
    price,
    cost_price,
    wholesale_price,
    discount_percent,
    stock_quantity,
    weight,
    unit,
    is_featured,
    is_active,
    image_url,
    description,
    created_at,
    updated_at
)
FROM 'D:/Project/My-Project/voluongquang/be-voluongquang/src/main/resources/db/scripts/data/product.csv'
DELIMITER ','
CSV HEADER
ENCODING 'UTF8';
