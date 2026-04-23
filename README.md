# TransMilenio — Sistema de Gestión de Rutas

> **Escuela Colombiana de Ingeniería**  
> Desarrollo Orientado por Objetos /
> Natalia Rodriguez - Daniel Villamizar - Sara Arteaga - Julian Tinjaca
---

## Tabla de contenidos

1. [Descripción del problema](#descripción-del-problema)
2. [Estructura del proyecto](#estructura-del-proyecto)
3. [Diagrama de clases](#diagrama-de-clases)
4. [Colecciones utilizadas y justificación](#colecciones-utilizadas-y-justificación)
5. [Servicios implementados](#servicios-implementados)
6. [Servicios de persistencia](#servicios-de-persistencia)
7. [Excepciones](#excepciones)
8. [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)
9. [Ejemplo de uso](#ejemplo-de-uso)

---

## Descripción del problema

Se modela el sistema de transporte masivo **TransMilenio** de Bogotá. El sistema administra tres entidades principales:

- **Troncales**: corredores lineales con nombre, velocidad promedio en metros/minuto y una secuencia de tramos con distancia entre estaciones.
- **Estaciones**: puntos de la red con nombre y un estado de ocupación (`EstadoAlto`, `EstadoMedio`, `EstadoBajo`) que determina su tiempo de espera.
- **Rutas**: servicios identificados por nombre con una lista ordenada de estaciones donde paran.

El sistema expone **7 servicios de consulta** y **3 servicios de persistencia**.

---

## Estructura del proyecto

```
src/
├── transmilenio/
│   ├── TransmilenioException.java     Jerarquía de excepciones del sistema
│   ├── EstadoEstacion.java            Interfaz del Patrón Estado
│   ├── EstadoAlto.java                Estado de ocupación alta  (10 min de espera)
│   ├── EstadoMedio.java               Estado de ocupación media  (5 min de espera)
│   ├── EstadoBajo.java                Estado de ocupación baja   (2 min de espera)
│   ├── Estacion.java                  Entidad estación
│   ├── Tramo.java                     Segmento entre dos estaciones con distancia
│   ├── Troncal.java                   Corredor lineal (serializable)
│   ├── Ruta.java                      Servicio con lista ordenada de paradas
│   └── SistemaTransmilenio.java       Contexto principal con todos los servicios
└── transmilenio/estrategia/
    ├── EstrategiaRuta.java            Interfaz del Patrón Estrategia
    ├── RutaMenorTiempo.java           Estrategia: minimizar tiempo total
    └── RutaMenorParadas.java          Estrategia: minimizar número de paradas
```

---

## Diagrama de clases - Archivo Astah


## Colecciones utilizadas y justificación

| Clase | Atributo | Colección | Justificación |
|---|---|---|---|
| `SistemaTransmilenio` | `estaciones` | `HashMap<String, Estacion>` | Búsqueda por nombre en O(1); es la operación más frecuente del sistema. |
| `SistemaTransmilenio` | `rutas` | `TreeSet<Ruta>` | Mantiene las rutas ordenadas alfabéticamente de forma estructural, sin ordenar en cada consulta (Servicio 2). `Ruta` implementa `Comparable`. |
| `SistemaTransmilenio` | `troncales` | `ArrayList<Troncal>` | Acceso por índice e iteración secuencial para calcular tiempos de viaje entre estaciones. |
| `Troncal` | `estaciones` | `ArrayList<Estacion>` | Acceso por índice en O(1), necesario en `calcularTiempoEntre` para localizar posiciones y sumar tramos intermedios. |
| `Troncal` | `tramos` | `ArrayList<Tramo>` | Mismo motivo: acceso posicional para acumular distancias entre dos índices dados. |
| `Ruta` | `estaciones` | `ArrayList<Estacion>` | Acceso por índice para calcular la diferencia de posiciones entre origen y destino (número de paradas). |

---

## Servicios implementados

### Servicio 1 — Tiempo de espera de una estación

```java
int getTiempoEspera(String nombreEstacion) throws TransmilenioException
```

Retorna el tiempo de espera en minutos de la estación indicada. El cálculo se delega completamente al estado actual de la estación mediante el **Patrón Estado**: la interfaz `EstadoEstacion` tiene tres implementaciones concretas e independientes, cada una con su propio comportamiento encapsulado.

| Estado | Clase | Tiempo de espera |
|---|---|---|
| Alta ocupación | `EstadoAlto` | 10 min |
| Ocupación media | `EstadoMedio` | 5 min |
| Baja ocupación | `EstadoBajo` | 2 min |

Cambiar el estado de una estación con `setEstado(EstadoEstacion)` modifica automáticamente su tiempo de espera sin ningún condicional externo en el código cliente.

---

### Servicio 2 — Rutas ordenadas alfabéticamente

```java
TreeSet<Ruta> getRutasOrdenadas()
```

Devuelve el `TreeSet<Ruta>` interno, que por su naturaleza mantiene las rutas en orden alfabético en todo momento. `Ruta` implementa `Comparable<Ruta>` usando `compareToIgnoreCase` sobre el nombre, por lo que el orden se garantiza estructuralmente sin ningún paso adicional de ordenamiento.

---

### Servicio 3 — Número de paradas entre dos estaciones en una ruta dada

```java
int numeroParadas(String nombreRuta, String origen, String destino)
        throws TransmilenioException
```

Localiza los índices de las dos estaciones dentro del `ArrayList` de la ruta y retorna la diferencia absoluta. Funciona en ambas direcciones (origen→destino y destino→origen). Lanza excepción si la ruta no existe o alguna estación no pertenece a ella.

**Ejemplo:** `Ruta1: A1, A2, A3, A4` → `numeroParadas("Ruta1", "A1", "A4")` = **3**

---

### Servicio 4 — Rutas directas entre dos estaciones (sin transbordo)

```java
List<Ruta> rutasSinTransbordo(String origen, String destino)
        throws TransmilenioException
```

Filtra las rutas que contienen tanto el origen como el destino (usando `pasaPor`), luego las ordena con el siguiente criterio:

1. Por número de paradas (menor a mayor).
2. Alfabéticamente por nombre de ruta como criterio de desempate.

Lanza `TransmilenioException` si no existe ninguna ruta directa entre las estaciones indicadas.

---

### Servicio 5 — Rutas con transbordo entre dos estaciones

```java
List<Ruta> rutasConTransbordo(String origen, String destino)
        throws TransmilenioException
```

Busca combinaciones de **dos rutas** conectadas por una estación de transbordo intermedia. El algoritmo:

1. Recorre las rutas que pasan por el origen (`rutaA`).
2. Para cada estación de `rutaA` como posible punto de transbordo, busca rutas (`rutaB`) que conecten ese transbordo con el destino.
3. Descarta combinaciones donde `rutaA` ya conecta directamente con el destino.
4. Ordena por total de paradas y alfabéticamente.

Lanza excepción si no existe ninguna combinación posible.

---

### Servicio 6 — Tiempo de recorrido de un plan de ruta

```java
double calcularTiempoRecorrido(List<String[]> plan) throws TransmilenioException
```

Recibe el plan como una lista de pares `{nombreEstacion, nombreRuta}`, donde el último par tiene `nombreRuta = null` para indicar el destino final. Calcula el tiempo total de la siguiente manera:

```
Tiempo total = Σ ( espera_en_estacion_i + tiempo_viaje_hacia_siguiente )
             + espera_en_estacion_destino_final
```

El tiempo de viaje entre dos estaciones consecutivas se obtiene llamando a `Troncal.calcularTiempoEntre(e1, e2)`, que suma las distancias de los tramos intermedios y divide por la velocidad promedio de la troncal.

**Formato del plan:**
```java
List<String[]> plan = List.of(
    new String[]{"Portal Norte", "B18"},
    new String[]{"Calle 72",     "B18"},
    new String[]{"El Norte",     null}   // destino final
);
```

---

### Servicio 7 — Mejor ruta (Patrón Estrategia)

```java
Ruta mejorRuta(String origen, String destino) throws TransmilenioException
```

Delega el cálculo a la `EstrategiaRuta` actualmente configurada. La estrategia puede cambiarse en tiempo de ejecución sin modificar `SistemaTransmilenio`, lo que permite agregar nuevos criterios de optimización sin tocar el código existente.

```java
// Estrategia por defecto: menor tiempo total
sistema.setEstrategia(new RutaMenorTiempo(sistema));

// Cambiar a: menor número de paradas
sistema.setEstrategia(new RutaMenorParadas(sistema));
```

| Estrategia | Criterio de optimización |
|---|---|
| `RutaMenorTiempo` | Minimiza: espera en origen + tiempo de viaje + espera en destino |
| `RutaMenorParadas` | Minimiza: el número de tramos recorridos entre origen y destino |

---

## Servicios de persistencia

### Persistencia 1 — Importar ruta desde archivo de texto

```java
void importarRuta(String rutaArchivo) throws TransmilenioException
```

**Formato del archivo:**
```
NombreDeLaRuta
NombreEstacion1
NombreEstacion2
NombreEstacion3
```

- La primera línea es el nombre de la nueva ruta.
- Las líneas siguientes son los nombres de las estaciones en orden de recorrido.
- Todas las estaciones deben existir previamente en el sistema.

Lanza excepción si: el archivo no existe, el archivo está vacío, la ruta ya existe en el sistema, o alguna estación referenciada no se encuentra registrada.

---

### Persistencia 2 — Exportar rutas directas a archivo de texto

```java
void exportarRutasDirectas(String origen, String destino, String rutaArchivo)
        throws TransmilenioException
```

Escribe en un archivo de texto plano los nombres de todas las rutas directas entre las dos estaciones indicadas, una por línea. Es útil para guardar resultados de consultas frecuentes.

**Ejemplo de salida:**
```
Ruta_B18
Ruta_C23
Ruta_F47
```

---

### Persistencia 3 — Salvar y cargar troncal (serialización binaria)

```java
void salvarTroncal(String nombreTroncal, String rutaArchivo) throws TransmilenioException
Troncal cargarTroncal(String rutaArchivo) throws TransmilenioException
```

Serializa la troncal completa (nombre, velocidad, lista de tramos y estaciones) en un archivo binario `.ser` usando `ObjectOutputStream`. La carga se realiza con `ObjectInputStream`. Las clases `Troncal`, `Tramo` y `Estacion` implementan `Serializable` para soportar esta operación.

---

## Excepciones

Todas las excepciones del sistema extienden `TransmilenioException`, que a su vez extiende `Exception` (excepción verificada). Esto obliga al código cliente a manejarlas explícitamente, haciendo el sistema más robusto.

| Constante | Cuándo se lanza |
|---|---|
| `ESTACION_NO_ENCONTRADA` | La estación buscada no existe en el sistema. |
| `RUTA_NO_ENCONTRADA` | La ruta buscada no existe en el sistema. |
| `TRONCAL_NO_ENCONTRADA` | La troncal buscada no existe en el sistema. |
| `ESTACION_NO_EN_RUTA` | La estación no pertenece a la ruta indicada. |
| `SIN_RUTA_DIRECTA` | No hay ninguna ruta directa entre las dos estaciones. |
| `SIN_RUTA_CON_TRANSBORDO` | No hay combinación con transbordo entre las dos estaciones. |
| `SIN_RUTA_POSIBLE` | La estrategia activa no encontró ninguna ruta posible. |
| `ARCHIVO_NO_ENCONTRADO` | El archivo de entrada no existe en la ruta indicada. |
| `ARCHIVO_FORMATO_INVALIDO` | El archivo existe pero su contenido no es válido. |
| `RUTA_YA_EXISTE` | Se intenta importar una ruta con nombre ya registrado. |
| `PLAN_INVALIDO` | El plan de ruta tiene estructura incorrecta. |

---

### Requisitos para ejecutar el proyecto

- Java 
- Eclipse IDE 2022 o superior
- JUnit 5 para las pruebas unitarias

---

## Ejemplo de uso

```java
// 1. Crear el sistema
SistemaTransmilenio sistema = new SistemaTransmilenio();

// 2. Registrar estaciones con su estado inicial
Estacion portalNorte = new Estacion("Portal Norte", new EstadoAlto());
Estacion calle72     = new Estacion("Calle 72",     new EstadoMedio());
Estacion elNorte     = new Estacion("El Norte",     new EstadoBajo());
sistema.agregarEstacion(portalNorte);
sistema.agregarEstacion(calle72);
sistema.agregarEstacion(elNorte);

// 3. Construir troncal con sus estaciones y tramos
Troncal caracas = new Troncal("Caracas", 300.0); // 300 m/min
caracas.agregarEstacion(portalNorte);
caracas.agregarEstacion(calle72);
caracas.agregarEstacion(elNorte);
caracas.agregarTramo(new Tramo(portalNorte, calle72, 1500));
caracas.agregarTramo(new Tramo(calle72, elNorte, 800));
sistema.agregarTroncal(caracas);

// 4. Registrar ruta
Ruta b18 = new Ruta("B18");
b18.agregarEstacion(portalNorte);
b18.agregarEstacion(calle72);
b18.agregarEstacion(elNorte);
sistema.agregarRuta(b18);

// Servicio 1: tiempo de espera
int espera = sistema.getTiempoEspera("Portal Norte"); // → 10 min

// Servicio 2: rutas ordenadas
TreeSet<Ruta> rutas = sistema.getRutasOrdenadas();

// Servicio 3: número de paradas
int paradas = sistema.numeroParadas("B18", "Portal Norte", "El Norte"); // → 2

// Servicio 4: rutas directas
List<Ruta> directas = sistema.rutasSinTransbordo("Portal Norte", "El Norte");

// Servicio 6: tiempo de recorrido de un plan
List<String[]> plan = List.of(
    new String[]{"Portal Norte", "B18"},
    new String[]{"Calle 72",     "B18"},
    new String[]{"El Norte",     null}
);
double tiempo = sistema.calcularTiempoRecorrido(plan);
// → 10 (espera Portal) + 5.0 (viaje 1500m/300) + 5 (espera Calle72)
//   + 2.67 (viaje 800m/300) + 2 (espera El Norte) ≈ 24.67 min

// Servicio 7: mejor ruta con estrategia de menor tiempo (por defecto)
Ruta mejor = sistema.mejorRuta("Portal Norte", "El Norte");

// Cambiar a estrategia de menor paradas
sistema.setEstrategia(new RutaMenorParadas(sistema));
Ruta menosParadas = sistema.mejorRuta("Portal Norte", "El Norte");

// Patrón Estado: cambiar ocupación de una estación en tiempo de ejecución
portalNorte.setEstado(new EstadoBajo());
int nuevaEspera = sistema.getTiempoEspera("Portal Norte"); // → 2 min

// Persistencia 1: importar ruta desde archivo de texto
sistema.importarRuta("nueva_ruta.txt");

// Persistencia 2: exportar rutas directas a archivo
sistema.exportarRutasDirectas("Portal Norte", "El Norte", "rutas_directas.txt");

// Persistencia 3: guardar y cargar troncal
sistema.salvarTroncal("Caracas", "caracas.ser");
Troncal cargada = sistema.cargarTroncal("caracas.ser");
```

---

*Proyecto desarrollado para la asignatura DOPO — Escuela Colombiana de Ingeniería · 2026-1*
