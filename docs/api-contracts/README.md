# DenunciaSegura OpenAPI Contracts

Este paquete contiene los contratos OpenAPI (YAML) del backend, publicados como **paquete npm** en **GitHub Packages**.

## Contenido

- `auditoria-api.yaml`
- `auth.api.yaml`
- `denuncias-api.yaml`
- `evidencias-api.yaml`
- `notificaciones-api.yaml`

## Uso desde el frontend

1) Configura el registry de GitHub Packages para el scope `@jotavalla`:

```ini
; .npmrc (en el repo del front)
@jotavalla:registry=https://npm.pkg.github.com
```

2) Autentícate en GitHub Packages (requerido)

GitHub Packages suele responder **404** si no estás autenticado o no tienes permiso, aunque el paquete exista.

Opción A (recomendada): token en `.npmrc`

1. Crea un **Personal Access Token (PAT)** en GitHub con permisos:
  - `read:packages`
  - si el repo/paquete es privado, también `repo`

2. Agrega al `.npmrc` del repo (o a tu `~/.npmrc`):

```ini
@jotavalla:registry=https://npm.pkg.github.com
//npm.pkg.github.com/:_authToken=TU_TOKEN
always-auth=true
```

Opción B: login interactivo

```powershell
npm login --registry=https://npm.pkg.github.com --scope=@jotavalla
```

Para validar que quedaste autenticado:

```powershell
npm whoami --registry=https://npm.pkg.github.com
```

3) Instala el paquete:

```powershell
npm i @jotavalla/denunciasegura-openapi
```

4) Apunta tu generator a un archivo dentro de `node_modules`:

Ejemplo (ajusta a tu tooling):

```powershell
npx @openapitools/openapi-generator-cli generate `
  -i node_modules/@jotavalla/denunciasegura-openapi/auth.api.yaml `
  -g typescript-axios `
  -o src/api/generated
```

## Publicación

Se publica automáticamente cuando se crea un tag `openapi-v*` en este repositorio.

Ejemplo:

```powershell
git tag openapi-v0.1.1
git push origin openapi-v0.1.1
```
