// Integration checks use an isolated emulator-only account and remove their own fixtures.
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
const auth = 'http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1';
const db = 'http://127.0.0.1:8081/v1/projects/no-pain-please-local/databases/(default)/documents';
const api = 'http://127.0.0.1:8080/api';
const admin = { Authorization: 'Bearer owner', 'Content-Type': 'application/json' };
const plans = JSON.parse(await readFile(new URL('./data/training-plans.json', import.meta.url), 'utf8'));
const uid = `training-check-${Date.now()}`;
function value(v) {
  if (typeof v === 'string') return { stringValue: v };
  if (typeof v === 'number') return { integerValue: String(v) };
  if (Array.isArray(v)) return { arrayValue: { values: v.map(value) } };
  return { mapValue: { fields: Object.fromEntries(Object.entries(v).map(([k, item]) => [k, value(item)])) } };
}
const signup = await fetch(`${auth}/projects/no-pain-please-local/accounts`, { method: 'POST', headers: admin, body: JSON.stringify({ localId: uid, email: `${uid}@example.test`, password: 'local-test-password' }) });
assert.ok(signup.ok, await signup.text());
const signin = await fetch(`${auth}/accounts:signInWithPassword?key=demo-api-key`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email: `${uid}@example.test`, password: 'local-test-password', returnSecureToken: true }) });
const session = await signin.json();
assert.ok(session.idToken, 'Test account signs in');
const headers = { Authorization: `Bearer ${session.idToken}`, 'Content-Type': 'application/json' };
try {
  const preflight = await fetch(`${api}/training-days/2020-01-02`, {
    method: 'OPTIONS', headers: { Origin: 'http://localhost:4200', 'Access-Control-Request-Method': 'PUT', 'Access-Control-Request-Headers': 'authorization,content-type' }
  });
  assert.equal(preflight.status, 200);
  assert.equal(preflight.headers.get('access-control-allow-origin'), 'http://localhost:4200');
  for (const plan of plans) {
    const seeded = await fetch(`${db}/users/${uid}/trainingPlans/${plan.id}`, { method: 'PATCH', headers: admin, body: JSON.stringify({ fields: value(plan).mapValue.fields }) });
    assert.ok(seeded.ok);
  }
  const loaded = await fetch(`${api}/training-plans`, { headers });
  assert.equal(loaded.status, 200);
  const result = await loaded.json();
  assert.deepEqual(result.map(plan => plan.exercises.length), [6, 6, 7]);
  const date = '2020-01-02';
  const day = { date, plan: 'A', attended: false, exercises: [{ id: 'A-1', weightKg: 42.5, completed: true }] };
  const put = body => fetch(`${api}/training-days/${date}`, { method: 'PUT', headers, body: JSON.stringify(body) });
  let saved = await put(day);
  assert.equal(saved.status, 200); assert.equal((await saved.json()).attended, true);
  saved = await put({ ...day, exercises: [{ id: 'A-1', weightKg: 0, completed: false }] });
  assert.equal(saved.status, 200);
  const listed = await (await fetch(`${api}/training-days`, { headers })).json();
  assert.equal(listed.length, 1, 'Saving twice updates the same day');
  assert.equal(listed[0].exercises[0].weightKg, 0);
  assert.equal(listed[0].exercises[0].completed, false);
  assert.equal((await put({ ...day, exercises: [{ id: 'B-1', weightKg: 10, completed: true }] })).status, 400);
  assert.equal((await put({ ...day, exercises: [{ id: 'A-1', weightKg: -1, completed: true }] })).status, 400);
  assert.equal((await put({ ...day, date: '2999-01-01' })).status, 400);
  assert.equal((await fetch(`${api}/training-days`)).status, 401);
  console.log('PASS: Firebase plans, daily upsert, attendance, zero weights, unchecking, plan membership, invalid weights/dates and authentication.');
} finally {
  for (const plan of plans) await fetch(`${db}/users/${uid}/trainingPlans/${plan.id}`, { method: 'DELETE', headers: admin });
  await fetch(`${db}/users/${uid}/trainingDays/2020-01-02`, { method: 'DELETE', headers: admin });
  await fetch(`${auth}/projects/no-pain-please-local/accounts:delete`, { method: 'POST', headers: admin, body: JSON.stringify({ localId: uid }) });
}
