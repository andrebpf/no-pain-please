// Production import: creates the supplied plans only when they do not exist.
// Requires an OAuth token from a project administrator; it never uses a key file.
import { readFile } from 'node:fs/promises';

const [uid, confirmation] = process.argv.slice(2);
const projectId = 'no-pain-please';
const token = process.env.GOOGLE_OAUTH_ACCESS_TOKEN;

if (!uid || !/^[\w-]+$/.test(uid) || confirmation !== '--confirm-production') {
  throw new Error('Usage: GOOGLE_OAUTH_ACCESS_TOKEN=... node scripts/seed-production-training.mjs USER_UID --confirm-production');
}
if (!token) throw new Error('GOOGLE_OAUTH_ACCESS_TOKEN is required.');

const plans = JSON.parse(await readFile(new URL('./data/training-plans.json', import.meta.url), 'utf8'));
const base = `https://firestore.googleapis.com/v1/projects/${projectId}/databases/(default)/documents`;
const headers = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };

function value(input) {
  if (input === null) return { nullValue: null };
  if (typeof input === 'string') return { stringValue: input };
  if (typeof input === 'number') return Number.isInteger(input) ? { integerValue: String(input) } : { doubleValue: input };
  if (typeof input === 'boolean') return { booleanValue: input };
  if (Array.isArray(input)) return { arrayValue: { values: input.map(value) } };
  return { mapValue: { fields: Object.fromEntries(Object.entries(input).map(([key, item]) => [key, value(item)])) } };
}

for (const plan of plans) {
  const path = `${base}/users/${uid}/trainingPlans/${plan.id}`;
  const existing = await fetch(path, { headers });
  if (existing.ok) {
    console.log(`Treino ${plan.id}: já existe; preservado.`);
    continue;
  }
  if (existing.status !== 404) throw new Error(`Não foi possível ler o treino ${plan.id}: ${existing.status} ${await existing.text()}`);

  const response = await fetch(`${path}?currentDocument.exists=false`, {
    method: 'PATCH', headers, body: JSON.stringify({ fields: value(plan).mapValue.fields })
  });
  if (!response.ok) throw new Error(`Não foi possível importar o treino ${plan.id}: ${response.status} ${await response.text()}`);
  console.log(`Treino ${plan.id}: criado no Firestore de produção.`);
}
