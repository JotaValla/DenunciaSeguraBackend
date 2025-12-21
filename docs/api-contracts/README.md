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

2) Instala el paquete:

```powershell
npm i @jotavalla/denunciasegura-openapi
```

3) Apunta tu generator a un archivo dentro de `node_modules`:

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
