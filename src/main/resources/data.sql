INSERT INTO role (role_type)
VALUES
    ('ADMIN'),
    ('CUSTOMER'),
    ('DELIVERYMAN'),
    ('MERCHANT');

-- 插入用户数据
INSERT INTO user (username, password, phone, email, address, role_id, user_type)
VALUES
    ('customer1', 'password123', '13800138000', 'customer1@example.com', 'Location 1', 2, 'CUSTOMER'),
    ('customer2', 'password123', '13800138001', 'customer2@example.com', 'Location 2', 2, 'CUSTOMER'),
    ('merchant1', 'password123', '13800138002', 'merchant1@example.com', 'Address 1', 3, 'MERCHANT'),
    ('merchant2', 'password123', '13800138003', 'merchant2@example.com', 'Address 2', 3, 'MERCHANT'),
    ('deliveryman1', 'password123', '13800138004', 'deliveryman1@example.com', 'Location A', 4, 'DELIVERYMAN'),
    ('deliveryman2', 'password123', '13800138005', 'deliveryman2@example.com', 'Location B', 4, 'DELIVERYMAN');

INSERT INTO customer (id)
VALUES
    (1),
    (2);

-- 插入商家数据
INSERT INTO merchant (merchant_name, id)
VALUES
    ('merchant1', 3),
    ('merchant2', 4);

-- 插入外卖员数据
INSERT INTO delivery_man (delivery_man_name, delivery_man_phone, id)
VALUES
    ('deliveryman1', '13800138004', 5),
    ('deliveryman2', '13800138005', 6);

-- 插入菜品数据
INSERT INTO dish (description, image_url, name, price, merchant_id)
VALUES
    ('dish1', 'dish1.png', 'dish1', '10', 3),
    ('dish2', 'dish2.png', 'dish2', '15', 3),
    ('dish3', 'dish3.png', 'dish3', '10', 4),
    ('dish4', 'dish4.png', 'dish4', '15', 4);

-- 插入订单数据
INSERT INTO orders (customer_id, merchant_id, deliveryman_id, delivery_location, order_time, delivery_time, status)
VALUES
    (1, 3, 5, 'Location 1', NOW(), DATE_ADD(NOW(), INTERVAL 30 MINUTE), 'REVIEWED'),
    (1, 3, 5, 'Location 1', NOW(), DATE_ADD(NOW(), INTERVAL 30 MINUTE), 'REVIEWED'),
    (2, 4, 6, 'Location 2', NOW(), DATE_ADD(NOW(), INTERVAL 40 MINUTE), 'REVIEWED'),
    (2, 4, 6, 'Location 2', NOW(), DATE_ADD(NOW(), INTERVAL 40 MINUTE), 'REVIEWED');

