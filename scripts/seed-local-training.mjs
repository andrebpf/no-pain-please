// Explicitly targets the local emulator; never uses production credentials.
import { readFile } from 'node:fs/promises';
const uid = process.argv[2];
if (!uid || !/^[\w-]+$/.test(uid)) throw new Error('Usage: node scripts/seed-local-training.mjs USER_UID');
const plans = JSON.parse(await readFile(new URL('./data/training-plans.json', import.meta.url), 'utf8'));
const base = 'http://127.0.0.1:8081/v1/projects/no-pain-please-local/databases/(default)/documents';
function value(v) {
  if (typeof v === 'string') return { stringValue: v };
  if (typeof v === 'number') return { integerValue: String(v) };
  if (Array.isArray(v)) return { arrayValue: { values: v.map(value) } };
  return { mapValue: { fields: Object.fromEntries(Object.entries(v).map(([k, item]) => [k, value(item)])) } };
}
for (const plan of plans) {
  const path = `${base}/users/${uid}/trainingPlans/${plan.id}`;
  const existing = await fetch(path, { headers: { Authorization: 'Bearer owner' } });
  if (existing.ok) { console.log(`Treino ${plan.id}: já existe; preservado.`); continue; }
  if (existing.status !== 404) throw new Error(`Read failed: ${existing.status}`);
  const response = await fetch(`${path}?currentDocument.exists=false`, {
    method: 'PATCH', headers: { 'Content-Type': 'application/json', Authorization: 'Bearer owner' }, body: JSON.stringify({ fields: value(plan).mapValue.fields })
  });
  if (!response.ok) throw new Error(`Seed failed: ${response.status} ${await response.text()}`);
  console.log(`Treino ${plan.id}: salvo no Firestore local.`);
}
