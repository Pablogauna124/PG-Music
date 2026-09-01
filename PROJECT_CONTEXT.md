# PG Music — Project Context

Última actualización: 2026-09-01

## Objetivo
PG Music es una aplicación de reproducción de música basada originalmente en SimpMusic. El objetivo es mantener su funcionalidad musical mientras se transforma en una aplicación propia, rápida, fluida y visualmente diferenciada bajo la identidad PG Music.

## Identidad
- Application ID: `com.pablogauna.pgmusic`
- Nombre visible: PG Music
- Tema launcher: `Theme.PGMusic.Launcher`
- Estética: negro OLED, rojo como color principal, interfaz moderna y premium.

## Arquitectura
Base original: SimpMusic.

Tecnologías principales:
- Kotlin
- Gradle
- Jetpack Compose / Compose Multiplatform
- Android Media3
- MPV
- WorkManager
- servicios de reproducción Android
- YouTube / YouTube Music como backend musical

## Principios del proyecto
1. Priorizar estabilidad, rapidez y fluidez.
2. Evitar animaciones permanentes o efectos que degraden el rendimiento.
3. No revertir la identidad PG Music hacia la interfaz original de SimpMusic.
4. No reactivar el actualizador automático original de SimpMusic.
5. Mantener los créditos y licencias open-source correspondientes.

## Sistema de licencias
PG Music utiliza un sistema de activación mediante KEY y comparte infraestructura de licencias con PG StreamFlix.

Productos:
- PG Music
- PG StreamFlix

Los créditos de revendedores son compartidos, pero cada KEY debe pertenecer explícitamente al producto correspondiente. Una KEY de PG StreamFlix no debe activar PG Music y viceversa.

Componentes conocidos:
- `LicenseManager.kt`
- `LicenseActivity.kt`

La licencia debe validarse antes de permitir el acceso normal a la aplicación.

## Panel de licencias
El panel administra licencias, planes, revendedores, créditos y dispositivos para los productos PG.

Funciones requeridas para planes:
- agregar
- editar
- pausar
- eliminar

Funciones requeridas para revendedores:
- crear
- listar
- eliminar
- administrar créditos
- permitir acceso al panel de revendedor
- crear licencias

Problemas históricos que deben verificarse antes de asumir que siguen presentes:
- `permission denied for table license_plans`
- revendedores creados que no aparecían en la lista
- problemas de acceso de revendedores al panel
- celdas/tablas con fondo blanco que ocultaban texto

## Splash
Decisión de diseño:
- fondo negro OLED
- icono PG Music centrado y de buena resolución
- sin video de presentación

El video de presentación fue descartado por problemas de reproducción y temporización. No volver a introducirlo salvo nueva decisión explícita.

## Actualizaciones heredadas
El sistema automático de actualizaciones de SimpMusic fue deshabilitado. PG Music no debe ofrecer actualizaciones oficiales de SimpMusic al usuario.

## Backup
PG Music posee sistema de backup. Ubicación conocida: `Downloads/PG Music`.

Formato esperado:
`pgmusic_backup_<timestamp>.zip`

No romper compatibilidad con backups existentes.

## Interfaz
Objetivo principal: **espectacular + rápida + fluida**.

Continuar eliminando elementos visuales heredados de SimpMusic cuando corresponda, manteniendo siempre las atribuciones legales necesarias.

## Regla para agentes de IA
Antes de modificar el proyecto:
1. Leer este archivo completo.
2. Leer `README.md`.
3. Leer `TODO.md`.
4. Ejecutar `git status`.
5. Comprobar la rama activa.
6. Revisar los últimos commits.
7. No reemplazar funciones existentes sin comprobar primero su uso.
8. No reactivar el actualizador de SimpMusic.
9. Priorizar estabilidad y rendimiento.

Después de cambios importantes:
- realizar un commit descriptivo
- actualizar `TODO.md`
- actualizar `PROJECT_CONTEXT.md` cuando cambie arquitectura, comportamiento importante o decisiones del proyecto

## Nota sobre estado Git
En conversaciones anteriores se registraron distintas ramas y commits durante el desarrollo. El repositorio remoto debe considerarse la fuente de verdad. Antes de continuar, comprobar siempre el estado Git actual en lugar de asumir que un commit histórico sigue siendo HEAD.