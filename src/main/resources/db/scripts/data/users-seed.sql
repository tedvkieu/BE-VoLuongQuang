-- Seed sample users; passwords will be hashed on app startup
INSERT INTO users (user_id, full_name, email, password, phone, address, role)
VALUES
('UADMIN001', 'Admin VLQ', 'admin@vlq.local', '123456', '0900000001', 'Head Office', 'ADMIN'),
('UCUS0001', 'Nguyen Van A', 'user1@vlq.local', '123456', '0900000002', 'HCMC', 'CUSTOMER'),
('UCUS0002', 'Tran Thi B', 'user2@vlq.local', '123456', '0900000003', 'Hanoi', 'CUSTOMER');

