# 🥗 iamfit-meal-planner

Microservicio Spring Boot que genera un plan de comidas semanal personalizado usando IA (OpenAI GPT-4o via Spring AI).

---

## ⚙️ Configuración rápida

### 1. Requisitos
- Java 21
- Maven 3.9+
- Cuenta OpenAI con API key

### 2. Variables de entorno

```bash
export OPENAI_API_KEY=sk-...
```

### 3. Ejecutar

```bash
./mvnw spring-boot:run
```

El servidor arranca en `http://localhost:8080`.

---

## 📡 API

### `POST /api/v1/meal-plan/generate`

**Body (JSON):**

```json
{
  "goal": "Ganar músculo",
  "preferences": ["Alta proteína", "sin gluten"],
  "allergies": ["mariscos", "nueces"],
  "likes": ["pollo", "arroz", "huevos", "batata"],
  "dislikes": ["brócoli", "coliflor"]
}
```

**Respuesta exitosa (200):**

```json
{
  "objetivo": "Ganar músculo",
  "menu": {
    "lunes": {
      "desayuno": "Avena con claras de huevo y arándanos",
      "almuerzo": "Pechuga de pollo a la plancha con arroz integral y ensalada",
      "cena": "Salmón al horno con batata asada",
      "snacks": ["Batido de proteína", "Plátano"]
    },
    "martes": { ... },
    ...
  },
  "recomendaciones_nutricionales": "Apunta a consumir entre 2-2.5g de proteína por kg de peso..."
}
```

**Ejemplo con curl:**

```bash
curl -X POST http://localhost:8080/api/v1/meal-plan/generate \
  -H "Content-Type: application/json" \
  -d '{
    "goal": "Bajar de peso",
    "preferences": ["Mediterráneo"],
    "allergies": [],
    "likes": ["pescado", "verduras", "legumbres"],
    "dislikes": ["mayonesa"]
  }'
```

---

## 🏗️ Arquitectura

```
com.iamfit.demo
├── controller
│   └── MealPlannerController.java   ← REST endpoint POST /generate
├── service
│   └── MealPlannerService.java      ← Lógica: construye prompt, llama IA, parsea JSON
├── dto
│   ├── UserPreferencesRequest.java  ← Input del usuario (validado con @Valid)
│   └── MealPlanResponse.java        ← Mapea el JSON que devuelve la IA
├── config
│   └── AiConfig.java                ← Bean ChatClient de Spring AI
└── exception
    ├── MealPlanGenerationException.java
    └── GlobalExceptionHandler.java  ← ProblemDetail RFC 9457
```

---

## 🔐 Seguridad

- La API key **nunca** se hardcodea en el código — siempre via variable de entorno.
- El modelo está configurado con `response-format: json_object` para forzar salida JSON.
- Se stripean backticks Markdown en caso de que el modelo los incluya accidentalmente.

---

## 🧪 Tests

```bash
./mvnw test
```

---

## 🔄 Objetivos soportados

| Valor en `goal`      | Efecto en el plan                         |
|----------------------|-------------------------------------------|
| `Ganar músculo`      | Alto en proteínas y calorías              |
| `Bajar de peso`      | Déficit calórico, alto en fibra           |
| `Mantener peso`      | Balance calórico según actividad moderada |
