import fs from 'node:fs/promises';
import path from 'node:path';
import SwaggerParser from '@apidevtools/swagger-parser';

const contractsDir = path.resolve(process.cwd());

const entries = await fs.readdir(contractsDir, { withFileTypes: true });
const yamls = entries
  .filter((e) => e.isFile())
  .map((e) => e.name)
  .filter((n) => n.endsWith('.yaml') || n.endsWith('.yml'));

if (yamls.length === 0) {
  console.error(`No OpenAPI YAML files found in: ${contractsDir}`);
  process.exit(1);
}

let ok = true;
for (const file of yamls) {
  const fullPath = path.join(contractsDir, file);
  try {
    await SwaggerParser.validate(fullPath);
    console.log(`OK  ${file}`);
  } catch (err) {
    ok = false;
    console.error(`ERR ${file}`);
    console.error(err?.message ?? err);
  }
}

process.exit(ok ? 0 : 2);
