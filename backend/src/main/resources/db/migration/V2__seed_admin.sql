INSERT INTO user_account (username, password_hash, display_name, role_id)
SELECT 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.R9p5K8xqJ7zqJm5K0u', '系统管理员', id FROM role WHERE name = 'admin';
