-- Catálogo inicial de ejercicios IAMFIT
-- isActive = true → disponible | false → pendiente de revisión

-- ===== PECHO =====
INSERT INTO exercises (id, name, description, muscle_group, equipment, difficulty, default_sets, default_reps, default_rest_seconds, is_active)
VALUES
(gen_random_uuid(), 'Press de banca con barra', 'Ejercicio fundamental para el pecho. Tumbado en banco plano, baja la barra hasta rozar el pecho y empuja hasta la extensión completa.', 'PECHO', 'BARRA', 'INTERMEDIO', 4, 10, 90, true),
(gen_random_uuid(), 'Press inclinado con mancuernas', 'Trabaja la parte superior del pecho. Banco a 30-45 grados, empuja las mancuernas hacia arriba y al centro.', 'PECHO', 'MANCUERNAS', 'INTERMEDIO', 3, 12, 75, true),
(gen_random_uuid(), 'Aperturas con mancuernas', 'Ejercicio de aislamiento para el pecho. Baja las mancuernas en arco amplio hasta sentir el estiramiento.', 'PECHO', 'MANCUERNAS', 'PRINCIPIANTE', 3, 15, 60, true),
(gen_random_uuid(), 'Fondos en paralelas', 'Trabaja pecho inferior, tríceps y hombros. Baja hasta que los hombros queden por debajo de los codos.', 'PECHO', 'PESO_CORPORAL', 'INTERMEDIO', 3, 12, 90, true),
(gen_random_uuid(), 'Flexiones de brazos', 'Ejercicio básico de empuje. Mantén el cuerpo en línea recta desde cabeza a talones.', 'PECHO', 'PESO_CORPORAL', 'PRINCIPIANTE', 3, 15, 60, true),
(gen_random_uuid(), 'Press declinado con barra', 'Trabaja la parte inferior del pecho. Banco declinado a -15 o -30 grados.', 'PECHO', 'BARRA', 'AVANZADO', 4, 8, 90, true);

-- ===== ESPALDA =====
INSERT INTO exercises (id, name, description, muscle_group, equipment, difficulty, default_sets, default_reps, default_rest_seconds, is_active)
VALUES
(gen_random_uuid(), 'Peso muerto', 'Ejercicio compuesto fundamental. Espalda recta, pecho alto, empuja el suelo con los pies al levantar.', 'ESPALDA', 'BARRA', 'AVANZADO', 4, 6, 120, true),
(gen_random_uuid(), 'Dominadas', 'Desarrolla el dorsal ancho. Cuelga con brazos extendidos y jala hasta que la barbilla supere la barra.', 'ESPALDA', 'PESO_CORPORAL', 'INTERMEDIO', 3, 8, 90, true),
(gen_random_uuid(), 'Remo con barra', 'Para el grosor de la espalda. Inclínate hacia adelante y tira de la barra hacia el abdomen.', 'ESPALDA', 'BARRA', 'INTERMEDIO', 4, 10, 90, true),
(gen_random_uuid(), 'Remo con mancuerna', 'Remo unilateral. Apoya una rodilla en el banco y jala la mancuerna hacia la cadera.', 'ESPALDA', 'MANCUERNAS', 'PRINCIPIANTE', 3, 12, 75, true),
(gen_random_uuid(), 'Jalón al pecho en polea', 'Alternativa a las dominadas. Jala la barra hacia el pecho manteniendo el torso ligeramente inclinado.', 'ESPALDA', 'POLEA', 'PRINCIPIANTE', 3, 12, 75, true),
(gen_random_uuid(), 'Remo en polea baja', 'Trabaja el grosor de la espalda media. Siéntate y jala el accesorio hacia el abdomen.', 'ESPALDA', 'POLEA', 'PRINCIPIANTE', 3, 12, 75, true),
(gen_random_uuid(), 'Buenos días', 'Trabaja la espalda baja y los isquiotibiales. Barra en la espalda, inclínate hacia adelante con la espalda recta.', 'ESPALDA', 'BARRA', 'AVANZADO', 3, 10, 90, true);

-- ===== HOMBROS =====
INSERT INTO exercises (id, name, description, muscle_group, equipment, difficulty, default_sets, default_reps, default_rest_seconds, is_active)
VALUES
(gen_random_uuid(), 'Press militar con barra', 'Ejercicio principal de hombros. De pie o sentado, empuja la barra desde los hombros hasta la extensión completa.', 'HOMBROS', 'BARRA', 'INTERMEDIO', 4, 8, 90, true),
(gen_random_uuid(), 'Press con mancuernas sentado', 'Variante del press militar. Permite mayor rango de movimiento y trabajar cada lado de forma independiente.', 'HOMBROS', 'MANCUERNAS', 'PRINCIPIANTE', 3, 12, 75, true),
(gen_random_uuid(), 'Elevaciones laterales', 'Aísla el deltoides lateral. Eleva los brazos lateralmente hasta la altura de los hombros sin usar impulso.', 'HOMBROS', 'MANCUERNAS', 'PRINCIPIANTE', 3, 15, 60, true),
(gen_random_uuid(), 'Elevaciones frontales', 'Trabaja el deltoides anterior. Eleva las mancuernas al frente hasta la altura de los hombros.', 'HOMBROS', 'MANCUERNAS', 'PRINCIPIANTE', 3, 12, 60, true),
(gen_random_uuid(), 'Face pulls en polea', 'Trabaja el deltoides posterior y los rotadores. Jala el accesorio de cuerda hacia la cara.', 'HOMBROS', 'POLEA', 'PRINCIPIANTE', 3, 15, 60, true),
(gen_random_uuid(), 'Pájaros con mancuernas', 'Aísla el deltoides posterior. Inclinado hacia adelante, eleva las mancuernas lateralmente.', 'HOMBROS', 'MANCUERNAS', 'PRINCIPIANTE', 3, 15, 60, true);

-- ===== BÍCEPS =====
INSERT INTO exercises (id, name, description, muscle_group, equipment, difficulty, default_sets, default_reps, default_rest_seconds, is_active)
VALUES
(gen_random_uuid(), 'Curl de bíceps con barra', 'Ejercicio básico de bíceps. De pie, flexiona los codos sin mover los hombros.', 'BICEPS', 'BARRA', 'PRINCIPIANTE', 3, 12, 60, true),
(gen_random_uuid(), 'Curl con mancuernas alterno', 'Trabaja cada bíceps de forma independiente. Alterna los brazos en cada repetición.', 'BICEPS', 'MANCUERNAS', 'PRINCIPIANTE', 3, 12, 60, true),
(gen_random_uuid(), 'Curl martillo', 'Trabaja el braquial y braquiorradial. Agarre neutro con las palmas mirando hacia adentro.', 'BICEPS', 'MANCUERNAS', 'PRINCIPIANTE', 3, 12, 60, true),
(gen_random_uuid(), 'Curl en polea baja', 'Mantiene tensión constante en el bíceps durante todo el recorrido.', 'BICEPS', 'POLEA', 'PRINCIPIANTE', 3, 15, 60, true),
(gen_random_uuid(), 'Curl concentrado', 'Máximo aislamiento del bíceps. Apoya el codo en la cara interna del muslo.', 'BICEPS', 'MANCUERNAS', 'INTERMEDIO', 3, 12, 60, true);

-- ===== TRÍCEPS =====
INSERT INTO exercises (id, name, description, muscle_group, equipment, difficulty, default_sets, default_reps, default_rest_seconds, is_active)
VALUES
(gen_random_uuid(), 'Extensión de tríceps en polea', 'Ejercicio de aislamiento para tríceps. Codos pegados al cuerpo, extiende los brazos hacia abajo.', 'TRICEPS', 'POLEA', 'PRINCIPIANTE', 3, 15, 60, true),
(gen_random_uuid(), 'Press francés con barra', 'Trabaja el tríceps largo. Tumbado, baja la barra hacia la frente flexionando solo los codos.', 'TRICEPS', 'BARRA', 'INTERMEDIO', 3, 12, 75, true),
(gen_random_uuid(), 'Extensión sobre la cabeza con mancuerna', 'Estira el tríceps largo. Sostén la mancuerna sobre la cabeza y baja por detrás.', 'TRICEPS', 'MANCUERNAS', 'PRINCIPIANTE', 3, 12, 60, true),
(gen_random_uuid(), 'Fondos en banco', 'Ejercicio de peso corporal para tríceps. Manos en el banco detrás, baja el cuerpo flexionando los codos.', 'TRICEPS', 'PESO_CORPORAL', 'PRINCIPIANTE', 3, 15, 60, true),
(gen_random_uuid(), 'Press de tríceps en polea cuerda', 'Variante con cuerda que permite separar las manos al final del movimiento.', 'TRICEPS', 'POLEA', 'PRINCIPIANTE', 3, 15, 60, true);

-- ===== PIERNAS =====
INSERT INTO exercises (id, name, description, muscle_group, equipment, difficulty, default_sets, default_reps, default_rest_seconds, is_active)
VALUES
(gen_random_uuid(), 'Sentadilla con barra', 'El ejercicio rey del tren inferior. Pies a la anchura de los hombros, baja hasta que los muslos queden paralelos al suelo.', 'PIERNAS', 'BARRA', 'INTERMEDIO', 4, 8, 120, true),
(gen_random_uuid(), 'Prensa de piernas', 'Alternativa a la sentadilla con menos carga en la columna. Pies a la anchura de los hombros en la plataforma.', 'PIERNAS', 'MAQUINA', 'PRINCIPIANTE', 4, 12, 90, true),
(gen_random_uuid(), 'Zancadas con mancuernas', 'Trabaja cuádriceps, glúteos y equilibrio. Da un paso largo y baja la rodilla trasera casi al suelo.', 'PIERNAS', 'MANCUERNAS', 'PRINCIPIANTE', 3, 12, 75, true),
(gen_random_uuid(), 'Curl femoral en máquina', 'Aísla los isquiotibiales. Tumbado boca abajo, flexiona las rodillas hasta que los talones toquen los glúteos.', 'PIERNAS', 'MAQUINA', 'PRINCIPIANTE', 3, 12, 75, true),
(gen_random_uuid(), 'Extensión de cuádriceps en máquina', 'Aísla los cuádriceps. Sentado, extiende las rodillas hasta la posición recta.', 'PIERNAS', 'MAQUINA', 'PRINCIPIANTE', 3, 15, 60, true),
(gen_random_uuid(), 'Sentadilla goblet con kettlebell', 'Variante de sentadilla sosteniendo el kettlebell frente al pecho. Ideal para principiantes.', 'PIERNAS', 'KETTLEBELL', 'PRINCIPIANTE', 3, 15, 75, true),
(gen_random_uuid(), 'Peso muerto rumano', 'Trabaja isquiotibiales y glúteos. Baja la barra deslizándola por las piernas manteniendo la espalda recta.', 'PIERNAS', 'BARRA', 'INTERMEDIO', 3, 10, 90, true),
(gen_random_uuid(), 'Sentadilla búlgara', 'Sentadilla unilateral con el pie trasero elevado. Alta demanda de equilibrio y fuerza.', 'PIERNAS', 'MANCUERNAS', 'AVANZADO', 3, 10, 90, true);

-- ===== GLÚTEOS =====
INSERT INTO exercises (id, name, description, muscle_group, equipment, difficulty, default_sets, default_reps, default_rest_seconds, is_active)
VALUES
(gen_random_uuid(), 'Hip thrust con barra', 'Ejercicio principal para glúteos. Apoya la espalda en un banco y empuja la barra hacia arriba con las caderas.', 'GLUTEOS', 'BARRA', 'INTERMEDIO', 4, 12, 90, true),
(gen_random_uuid(), 'Patada de glúteo en polea', 'Aísla el glúteo mayor. De pie frente a la polea, extiende la pierna hacia atrás.', 'GLUTEOS', 'POLEA', 'PRINCIPIANTE', 3, 15, 60, true),
(gen_random_uuid(), 'Puente de glúteos', 'Variante del hip thrust sin banco. Tumbado boca arriba, eleva las caderas apretando los glúteos.', 'GLUTEOS', 'PESO_CORPORAL', 'PRINCIPIANTE', 3, 20, 60, true),
(gen_random_uuid(), 'Abducción de cadera en máquina', 'Trabaja el glúteo medio. Sentado, abre las piernas contra la resistencia de la máquina.', 'GLUTEOS', 'MAQUINA', 'PRINCIPIANTE', 3, 15, 60, true);

-- ===== CORE =====
INSERT INTO exercises (id, name, description, muscle_group, equipment, difficulty, default_sets, default_reps, default_rest_seconds, is_active)
VALUES
(gen_random_uuid(), 'Plancha isométrica', 'Ejercicio fundamental de core. Apoya antebrazos y pies, mantén el cuerpo en línea recta.', 'CORE', 'PESO_CORPORAL', 'PRINCIPIANTE', 3, 45, 60, true),
(gen_random_uuid(), 'Crunch abdominal', 'Contrae el abdomen elevando los hombros del suelo. No jales del cuello.', 'CORE', 'PESO_CORPORAL', 'PRINCIPIANTE', 3, 20, 45, true),
(gen_random_uuid(), 'Elevación de piernas colgado', 'Trabaja el abdomen inferior. Cuelga de la barra y eleva las piernas hasta la cadera.', 'CORE', 'PESO_CORPORAL', 'INTERMEDIO', 3, 12, 75, true),
(gen_random_uuid(), 'Rueda abdominal', 'Ejercicio avanzado de core. Desde rodillas, extiende los brazos hacia adelante con la rueda y vuelve.', 'CORE', 'PESO_CORPORAL', 'AVANZADO', 3, 10, 90, true),
(gen_random_uuid(), 'Mountain climbers', 'Ejercicio dinámico de core y cardio. En posición de plancha, alterna las rodillas hacia el pecho.', 'CORE', 'PESO_CORPORAL', 'INTERMEDIO', 3, 20, 60, true),
(gen_random_uuid(), 'Plancha lateral', 'Trabaja el core lateral. Apoya un antebrazo y el borde del pie, mantén la cadera elevada.', 'CORE', 'PESO_CORPORAL', 'PRINCIPIANTE', 3, 30, 60, true),
(gen_random_uuid(), 'Dead bug', 'Ejercicio de estabilización del core. Tumbado boca arriba, extiende brazos y piernas opuestos simultáneamente.', 'CORE', 'PESO_CORPORAL', 'PRINCIPIANTE', 3, 12, 60, true);

-- ===== CARDIO =====
INSERT INTO exercises (id, name, description, muscle_group, equipment, difficulty, default_sets, default_reps, default_rest_seconds, is_active)
VALUES
(gen_random_uuid(), 'Burpees', 'Ejercicio de cuerpo completo y cardio. Combina sentadilla, flexión y salto.', 'CARDIO', 'PESO_CORPORAL', 'INTERMEDIO', 3, 15, 90, true),
(gen_random_uuid(), 'Saltos de tijera', 'Cardio de bajo impacto técnico. Salta abriendo y cerrando piernas y brazos simultáneamente.', 'CARDIO', 'PESO_CORPORAL', 'PRINCIPIANTE', 3, 30, 60, true),
(gen_random_uuid(), 'Salto a la cuerda', 'Cardio de alta intensidad. Mantén los codos cerca del cuerpo y salta con los pies juntos.', 'CARDIO', 'PESO_CORPORAL', 'PRINCIPIANTE', 3, 60, 60, true),
(gen_random_uuid(), 'Sprint en cinta', 'Cardio de alta intensidad. Alterna 30 segundos de sprint con 30 de descanso activo.', 'CARDIO', 'MAQUINA', 'INTERMEDIO', 8, 30, 30, true),
(gen_random_uuid(), 'Remo en máquina', 'Cardio de bajo impacto con trabajo de espalda. Mantén la espalda recta durante el movimiento.', 'CARDIO', 'MAQUINA', 'PRINCIPIANTE', 3, 60, 60, true);

-- ===== CUERPO COMPLETO =====
INSERT INTO exercises (id, name, description, muscle_group, equipment, difficulty, default_sets, default_reps, default_rest_seconds, is_active)
VALUES
(gen_random_uuid(), 'Clean and press', 'Movimiento olímpico completo. Levanta la barra desde el suelo hasta los hombros y luego sobre la cabeza.', 'CUERPO_COMPLETO', 'BARRA', 'AVANZADO', 4, 6, 120, true),
(gen_random_uuid(), 'Kettlebell swing', 'Movimiento balístico que trabaja glúteos, isquiotibiales y core. Impulsa el kettlebell hacia adelante con las caderas.', 'CUERPO_COMPLETO', 'KETTLEBELL', 'INTERMEDIO', 4, 15, 60, true),
(gen_random_uuid(), 'Thruster con mancuernas', 'Combina sentadilla frontal y press. Baja en sentadilla y al subir empuja las mancuernas sobre la cabeza.', 'CUERPO_COMPLETO', 'MANCUERNAS', 'INTERMEDIO', 3, 12, 90, true),
(gen_random_uuid(), 'Turkish get up', 'Movimiento funcional completo. Desde tumbado, levántate sosteniendo el kettlebell con un brazo extendido.', 'CUERPO_COMPLETO', 'KETTLEBELL', 'AVANZADO', 3, 5, 120, true);