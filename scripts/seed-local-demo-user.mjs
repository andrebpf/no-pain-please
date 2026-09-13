// Creates only emulator data. It is safe to run again: existing demo records are preserved.
import { readFile } from 'node:fs/promises';

const projectId = 'no-pain-please-local';
const email = 'demo@no-pain-please.local';
const password = 'demo-local-password';
const auth = `http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1`;
const db = `http://127.0.0.1:8081/v1/projects/${projectId}/databases/(default)/documents`;
const admin = { Authorization: 'Bearer owner', 'Content-Type': 'application/json' };
const plans = JSON.parse(await readFile(new URL('./data/training-plans.json', import.meta.url), 'utf8'));

function value(item) {
  if (item === null) return { nullValue: null };
  if (typeof item === 'string') return { stringValue: item };
  if (typeof item === 'boolean') return { booleanValue: item };
  if (typeof item === 'number') return Number.isInteger(item) ? { integerValue: String(item) } : { doubleValue: item };
  if (Array.isArray(item)) return { arrayValue: { values: item.map(value) } };
  return { mapValue: { fields: Object.fromEntries(Object.entries(item).map(([key, nested]) => [key, value(nested)])) } };
}

async function demoUser() {
  const signIn = () => fetch(`${auth}/accounts:signInWithPassword?key=demo-api-key`, {
    method: 'POST', headers: admin, body: JSON.stringify({ email, password, returnSecureToken: true })
  });
  let response = await signIn();
  if (response.ok) return (await response.json()).localId;

  response = await fetch(`${auth}/accounts:signUp?key=demo-api-key`, {
    method: 'POST', headers: admin, body: JSON.stringify({ email, password, displayName: 'Atleta Demo', returnSecureToken: true })
  });
  if (response.ok) return (await response.json()).localId;
  const body = await response.text();
  if (body.includes('EMAIL_EXISTS')) {
    response = await signIn();
    if (response.ok) return (await response.json()).localId;
  }
  throw new Error(`Não foi possível criar a conta demo: ${response.status} ${body}`);
}

async function createIfMissing(path, document, label) {
  const response = await fetch(`${db}/${path}?currentDocument.exists=false`, {
    method: 'PATCH', headers: admin, body: JSON.stringify({ fields: value(document).mapValue.fields })
  });
  if (response.ok) console.log(`${label}: criado.`);
  else if (response.status === 409) console.log(`${label}: já existe; preservado.`);
  else throw new Error(`${label}: ${response.status} ${await response.text()}`);
}

function dateBefore(days) {
  const today = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'America/Sao_Paulo', year: 'numeric', month: '2-digit', day: '2-digit'
  }).format(new Date());
  const date = new Date(`${today}T12:00:00Z`);
  date.setUTCDate(date.getUTCDate() - days);
  return date.toISOString().slice(0, 10);
}

const uid = await demoUser();
for (const plan of plans) await createIfMissing(`users/${uid}/trainingPlans/${plan.id}`, plan, `Treino ${plan.id}`);

const demoDays = [
  { daysAgo: 12, plan: plans[0], baseWeight: 12.5 },
  { daysAgo: 10, plan: plans[0], baseWeight: 15 },
  { daysAgo: 8, plan: plans[0], baseWeight: 16.5 },
  { daysAgo: 4, plan: plans[0], baseWeight: 17.5 },
  { daysAgo: 2, plan: plans[0], baseWeight: 20 },
  { daysAgo: 1, plan: plans[1], baseWeight: 30 }
];
for (const { daysAgo, plan, baseWeight } of demoDays) {
  const exercises = plan.exercises.map((exercise, exerciseIndex) => ({
    id: exercise.id, weightKg: baseWeight + exerciseIndex * 2.5, completed: true
  }));
  await createIfMissing(`users/${uid}/trainingDays/${dateBefore(daysAgo)}`, {
    date: dateBefore(daysAgo), plan: plan.id, attended: true, exercises
  }, `Histórico do treino ${plan.id} (${dateBefore(daysAgo)})`);
}

console.log(`\nConta de demonstração pronta:\n  e-mail: ${email}\n  senha: ${password}`);
