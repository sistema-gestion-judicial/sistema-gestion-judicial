# Sistema Gestión Judicial

Monorepo de los sistemas del órgano judicial del Proyecto Integrador (UTEZ, 4°E DS).

| Módulo | Sistema |
| --- | --- |
| `oficialia` | SCD-2026, Oficialía de Partes |
| `peritos` | Peritos |
| `audiencias` | Audiencias |
| `tocas` | Tocas (segunda instancia) |
| `common` | Código compartido por los cuatro sistemas |
| `contracts` | Contratos OpenAPI entre sistemas |

## Requisitos

- JDK 21 (Amazon Corretto 21 o cualquier distribución de OpenJDK 21)
- Git

No hace falta instalar Maven: se usa el Maven Wrapper incluido.

## Compilar

    ./mvnw verify        # macOS o Linux
    .\mvnw.cmd verify    # Windows

La guía completa está en `docs/arranque.md`.
