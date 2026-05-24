-- 1. Habilitar la extensión
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Conceder privilegios al usuario personalizado
GRANT ALL PRIVILEGES ON SCHEMA public TO "user";

-- 3. Crear la tabla
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID PRIMARY KEY,
    content TEXT,
    metadata JSONB,
    embedding VECTOR(768)
);

-- 4. Crear el índice
CREATE INDEX IF NOT EXISTS vector_store_embedding_idx
ON vector_store USING hnsw (embedding vector_cosine_ops);