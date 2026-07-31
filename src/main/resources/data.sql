INSERT INTO product_entity (id, name, price) VALUES (1, 'Keyboard', 49.99) ON CONFLICT (id) DO NOTHING;
