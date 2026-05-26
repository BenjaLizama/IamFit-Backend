-- Catálogo inicial de alimentos IAMFIT
-- Valores nutricionales por 100g

-- ===== CARNES Y PROTEÍNAS =====
INSERT INTO food_items (id, name, name_en, calories, protein, carbohydrates, fat, fiber, sugar, sodium, serving_size_g, food_category, is_verified)
VALUES
(gen_random_uuid(), 'Pechuga de pollo a la plancha', 'Grilled chicken breast', 165.0, 31.0, 0.0, 3.6, 0.0, 0.0, 74.0, 150.0, 'carnes', true),
(gen_random_uuid(), 'Pechuga de pavo', 'Turkey breast', 135.0, 29.0, 0.0, 1.0, 0.0, 0.0, 63.0, 150.0, 'carnes', true),
(gen_random_uuid(), 'Carne molida vacuno 90%', 'Lean ground beef 90%', 176.0, 26.0, 0.0, 8.0, 0.0, 0.0, 75.0, 150.0, 'carnes', true),
(gen_random_uuid(), 'Filete de vacuno', 'Beef sirloin', 207.0, 28.0, 0.0, 10.0, 0.0, 0.0, 64.0, 200.0, 'carnes', true),
(gen_random_uuid(), 'Salmón', 'Salmon', 208.0, 20.0, 0.0, 13.0, 0.0, 0.0, 59.0, 150.0, 'pescados', true),
(gen_random_uuid(), 'Atún en agua', 'Canned tuna in water', 116.0, 26.0, 0.0, 1.0, 0.0, 0.0, 330.0, 100.0, 'pescados', true),
(gen_random_uuid(), 'Merluza', 'Hake', 78.0, 17.0, 0.0, 0.7, 0.0, 0.0, 72.0, 150.0, 'pescados', true),
(gen_random_uuid(), 'Huevo entero', 'Whole egg', 155.0, 13.0, 1.1, 11.0, 0.0, 1.1, 124.0, 60.0, 'huevos', true),
(gen_random_uuid(), 'Clara de huevo', 'Egg white', 52.0, 11.0, 0.7, 0.2, 0.0, 0.7, 166.0, 100.0, 'huevos', true),
(gen_random_uuid(), 'Jamón de pavo', 'Turkey ham', 104.0, 17.0, 2.0, 3.0, 0.0, 1.5, 810.0, 60.0, 'carnes', true);

-- ===== LÁCTEOS =====
INSERT INTO food_items (id, name, name_en, calories, protein, carbohydrates, fat, fiber, sugar, sodium, serving_size_g, food_category, is_verified)
VALUES
(gen_random_uuid(), 'Yogur griego natural', 'Greek yogurt plain', 97.0, 9.0, 3.6, 5.0, 0.0, 3.2, 36.0, 200.0, 'lacteos', true),
(gen_random_uuid(), 'Leche descremada', 'Skim milk', 35.0, 3.4, 5.0, 0.1, 0.0, 5.0, 44.0, 250.0, 'lacteos', true),
(gen_random_uuid(), 'Leche entera', 'Whole milk', 61.0, 3.2, 4.8, 3.3, 0.0, 4.8, 43.0, 250.0, 'lacteos', true),
(gen_random_uuid(), 'Queso cottage', 'Cottage cheese', 98.0, 11.0, 3.4, 4.3, 0.0, 2.7, 364.0, 100.0, 'lacteos', true),
(gen_random_uuid(), 'Queso fresco', 'Fresh cheese', 98.0, 12.0, 2.0, 4.5, 0.0, 1.5, 200.0, 50.0, 'lacteos', true),
(gen_random_uuid(), 'Proteína whey en polvo', 'Whey protein powder', 370.0, 75.0, 8.0, 6.0, 0.0, 4.0, 150.0, 30.0, 'suplementos', true);

-- ===== CEREALES Y CARBOHIDRATOS =====
INSERT INTO food_items (id, name, name_en, calories, protein, carbohydrates, fat, fiber, sugar, sodium, serving_size_g, food_category, is_verified)
VALUES
(gen_random_uuid(), 'Arroz blanco cocido', 'Cooked white rice', 130.0, 2.7, 28.0, 0.3, 0.4, 0.0, 1.0, 150.0, 'cereales', true),
(gen_random_uuid(), 'Arroz integral cocido', 'Cooked brown rice', 123.0, 2.7, 26.0, 0.9, 1.8, 0.0, 1.0, 150.0, 'cereales', true),
(gen_random_uuid(), 'Avena en copos', 'Rolled oats', 389.0, 17.0, 66.0, 7.0, 10.0, 1.0, 2.0, 50.0, 'cereales', true),
(gen_random_uuid(), 'Pan integral', 'Whole wheat bread', 247.0, 9.0, 44.0, 3.5, 6.0, 5.0, 472.0, 50.0, 'panaderia', true),
(gen_random_uuid(), 'Pan blanco', 'White bread', 265.0, 8.0, 49.0, 3.2, 2.7, 5.0, 491.0, 50.0, 'panaderia', true),
(gen_random_uuid(), 'Pasta cocida', 'Cooked pasta', 131.0, 5.0, 25.0, 1.1, 1.8, 0.6, 1.0, 180.0, 'cereales', true),
(gen_random_uuid(), 'Quinoa cocida', 'Cooked quinoa', 120.0, 4.4, 21.0, 1.9, 2.8, 0.9, 7.0, 150.0, 'cereales', true),
(gen_random_uuid(), 'Papa cocida', 'Boiled potato', 87.0, 1.9, 20.0, 0.1, 1.8, 0.9, 5.0, 150.0, 'tuberculos', true),
(gen_random_uuid(), 'Camote cocido', 'Boiled sweet potato', 90.0, 2.0, 21.0, 0.1, 3.3, 4.2, 27.0, 150.0, 'tuberculos', true),
(gen_random_uuid(), 'Tortilla de maíz', 'Corn tortilla', 218.0, 5.7, 46.0, 2.5, 6.5, 0.6, 231.0, 50.0, 'panaderia', true);

-- ===== LEGUMBRES =====
INSERT INTO food_items (id, name, name_en, calories, protein, carbohydrates, fat, fiber, sugar, sodium, serving_size_g, food_category, is_verified)
VALUES
(gen_random_uuid(), 'Lentejas cocidas', 'Cooked lentils', 116.0, 9.0, 20.0, 0.4, 7.9, 1.8, 2.0, 150.0, 'legumbres', true),
(gen_random_uuid(), 'Porotos negros cocidos', 'Cooked black beans', 132.0, 8.9, 24.0, 0.5, 8.7, 0.3, 1.0, 150.0, 'legumbres', true),
(gen_random_uuid(), 'Garbanzos cocidos', 'Cooked chickpeas', 164.0, 8.9, 27.0, 2.6, 7.6, 4.8, 7.0, 150.0, 'legumbres', true),
(gen_random_uuid(), 'Porotos blancos cocidos', 'Cooked white beans', 127.0, 8.7, 23.0, 0.5, 6.3, 0.3, 2.0, 150.0, 'legumbres', true);

-- ===== FRUTAS =====
INSERT INTO food_items (id, name, name_en, calories, protein, carbohydrates, fat, fiber, sugar, sodium, serving_size_g, food_category, is_verified)
VALUES
(gen_random_uuid(), 'Plátano', 'Banana', 89.0, 1.1, 23.0, 0.3, 2.6, 12.0, 1.0, 120.0, 'frutas', true),
(gen_random_uuid(), 'Manzana', 'Apple', 52.0, 0.3, 14.0, 0.2, 2.4, 10.0, 1.0, 150.0, 'frutas', true),
(gen_random_uuid(), 'Naranja', 'Orange', 47.0, 0.9, 12.0, 0.1, 2.4, 9.4, 0.0, 150.0, 'frutas', true),
(gen_random_uuid(), 'Frutilla', 'Strawberry', 32.0, 0.7, 7.7, 0.3, 2.0, 4.9, 1.0, 100.0, 'frutas', true),
(gen_random_uuid(), 'Palta', 'Avocado', 160.0, 2.0, 9.0, 15.0, 6.7, 0.7, 7.0, 100.0, 'frutas', true),
(gen_random_uuid(), 'Uvas', 'Grapes', 69.0, 0.7, 18.0, 0.2, 0.9, 15.0, 2.0, 100.0, 'frutas', true),
(gen_random_uuid(), 'Sandía', 'Watermelon', 30.0, 0.6, 7.6, 0.2, 0.4, 6.2, 1.0, 200.0, 'frutas', true),
(gen_random_uuid(), 'Kiwi', 'Kiwi', 61.0, 1.1, 15.0, 0.5, 3.0, 9.0, 3.0, 80.0, 'frutas', true),
(gen_random_uuid(), 'Durazno', 'Peach', 39.0, 0.9, 9.5, 0.3, 1.5, 8.4, 0.0, 150.0, 'frutas', true),
(gen_random_uuid(), 'Arándanos', 'Blueberries', 57.0, 0.7, 14.0, 0.3, 2.4, 10.0, 1.0, 100.0, 'frutas', true);

-- ===== VERDURAS =====
INSERT INTO food_items (id, name, name_en, calories, protein, carbohydrates, fat, fiber, sugar, sodium, serving_size_g, food_category, is_verified)
VALUES
(gen_random_uuid(), 'Espinaca cruda', 'Raw spinach', 23.0, 2.9, 3.6, 0.4, 2.2, 0.4, 79.0, 100.0, 'verduras', true),
(gen_random_uuid(), 'Brócoli cocido', 'Cooked broccoli', 35.0, 2.4, 7.2, 0.4, 3.3, 1.7, 41.0, 150.0, 'verduras', true),
(gen_random_uuid(), 'Zanahoria cruda', 'Raw carrot', 41.0, 0.9, 10.0, 0.2, 2.8, 4.7, 69.0, 100.0, 'verduras', true),
(gen_random_uuid(), 'Tomate', 'Tomato', 18.0, 0.9, 3.9, 0.2, 1.2, 2.6, 5.0, 100.0, 'verduras', true),
(gen_random_uuid(), 'Lechuga', 'Lettuce', 15.0, 1.4, 2.9, 0.2, 1.3, 1.5, 28.0, 100.0, 'verduras', true),
(gen_random_uuid(), 'Pepino', 'Cucumber', 15.0, 0.7, 3.6, 0.1, 0.5, 1.7, 2.0, 100.0, 'verduras', true),
(gen_random_uuid(), 'Pimentón rojo', 'Red bell pepper', 31.0, 1.0, 6.0, 0.3, 2.1, 4.2, 4.0, 100.0, 'verduras', true),
(gen_random_uuid(), 'Cebolla', 'Onion', 40.0, 1.1, 9.3, 0.1, 1.7, 4.2, 4.0, 100.0, 'verduras', true),
(gen_random_uuid(), 'Champiñones', 'Mushrooms', 22.0, 3.1, 3.3, 0.3, 1.0, 2.0, 5.0, 100.0, 'verduras', true),
(gen_random_uuid(), 'Zapallo italiano', 'Zucchini', 17.0, 1.2, 3.1, 0.3, 1.0, 2.5, 8.0, 100.0, 'verduras', true);

-- ===== FRUTOS SECOS Y GRASAS SALUDABLES =====
INSERT INTO food_items (id, name, name_en, calories, protein, carbohydrates, fat, fiber, sugar, sodium, serving_size_g, food_category, is_verified)
VALUES
(gen_random_uuid(), 'Almendras', 'Almonds', 579.0, 21.0, 22.0, 50.0, 12.5, 4.4, 1.0, 30.0, 'frutos_secos', true),
(gen_random_uuid(), 'Nueces', 'Walnuts', 654.0, 15.0, 14.0, 65.0, 6.7, 2.6, 2.0, 30.0, 'frutos_secos', true),
(gen_random_uuid(), 'Maní', 'Peanuts', 567.0, 26.0, 16.0, 49.0, 8.5, 4.7, 18.0, 30.0, 'frutos_secos', true),
(gen_random_uuid(), 'Aceite de oliva', 'Olive oil', 884.0, 0.0, 0.0, 100.0, 0.0, 0.0, 2.0, 10.0, 'aceites', true),
(gen_random_uuid(), 'Mantequilla de maní', 'Peanut butter', 588.0, 25.0, 20.0, 50.0, 6.0, 9.0, 469.0, 32.0, 'frutos_secos', true);

-- ===== BEBIDAS =====
INSERT INTO food_items (id, name, name_en, calories, protein, carbohydrates, fat, fiber, sugar, sodium, serving_size_g, food_category, is_verified)
VALUES
(gen_random_uuid(), 'Agua', 'Water', 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 250.0, 'bebidas', true),
(gen_random_uuid(), 'Jugo de naranja natural', 'Fresh orange juice', 45.0, 0.7, 10.0, 0.2, 0.2, 8.4, 1.0, 250.0, 'bebidas', true),
(gen_random_uuid(), 'Café negro', 'Black coffee', 2.0, 0.3, 0.0, 0.0, 0.0, 0.0, 2.0, 240.0, 'bebidas', true),
(gen_random_uuid(), 'Té verde', 'Green tea', 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 240.0, 'bebidas', true);