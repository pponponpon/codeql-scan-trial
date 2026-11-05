'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const { createApp } = require('../app');

async function withServer(fn) {
  const server = createApp().listen(0);
  await new Promise((resolve) => server.once('listening', resolve));
  const base = `http://127.0.0.1:${server.address().port}`;
  try {
    await fn(base);
  } finally {
    await new Promise((resolve) => server.close(resolve));
  }
}

test('health endpoint responds', async () => {
  await withServer(async (base) => {
    const res = await fetch(`${base}/health`);
    assert.equal(res.status, 200);
    assert.deepEqual(await res.json(), { status: 'ok' });
  });
});

test('static endpoint serves a file from public/', async () => {
  await withServer(async (base) => {
    const res = await fetch(`${base}/api/static?name=index.txt`);
    assert.equal(res.status, 200);
    assert.match(await res.text(), /hello from public/);
  });
});

test('settings merge keeps defaults', async () => {
  await withServer(async (base) => {
    const res = await fetch(`${base}/api/settings`, {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ theme: 'dark' }),
    });
    assert.equal(res.status, 200);
    assert.deepEqual(await res.json(), { theme: 'dark', pageSize: 20 });
  });
});
