'use strict';

const { exec } = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');
const express = require('express');
const _ = require('lodash');

const PUBLIC_DIR = path.join(__dirname, 'public');

function createApp() {
  const app = express();
  app.use(express.json());

  app.get('/health', (req, res) => {
    res.json({ status: 'ok' });
  });

  // VULN-E1: OS command injection - query parameter spliced into a shell command.
  app.get('/api/lookup', (req, res) => {
    const host = String(req.query.host || '');
    exec('nslookup ' + host, (err, stdout, stderr) => {
      if (err) {
        res.status(500).send(stderr);
        return;
      }
      res.type('text/plain').send(stdout);
    });
  });

  // VULN-E2: path traversal - file name from the request joined to a base dir without containment check.
  app.get('/api/static', (req, res) => {
    const name = String(req.query.name || 'index.txt');
    const target = path.join(PUBLIC_DIR, name);
    fs.readFile(target, 'utf8', (err, data) => {
      if (err) {
        res.status(404).send('not found');
        return;
      }
      res.type('text/plain').send(data);
    });
  });

  // VULN-E2b (round 2 variant): same as VULN-E2 but without String()/default wrapping.
  app.get('/api/static2', (req, res) => {
    const target = path.join(PUBLIC_DIR, req.query.name);
    fs.readFile(target, 'utf8', (err, data) => {
      if (err) {
        res.status(404).send('not found');
        return;
      }
      res.type('text/plain').send(data);
    });
  });

  // VULN-E2c (round 2 variant): plain string concatenation instead of path.join.
  app.get('/api/static3', (req, res) => {
    const name = String(req.query.name || 'index.txt');
    fs.readFile(PUBLIC_DIR + '/' + name, 'utf8', (err, data) => {
      if (err) {
        res.status(404).send('not found');
        return;
      }
      res.type('text/plain').send(data);
    });
  });

  // DEPBAIT-03 usage: lodash 4.17.15 (CVE-2020-8203 prototype pollution via merge, CVE-2021-23337 command injection via template).
  app.post('/api/settings', (req, res) => {
    const defaults = { theme: 'light', pageSize: 20 };
    const merged = _.merge({}, defaults, req.body || {});
    res.json(merged);
  });

  return app;
}

module.exports = { createApp, PUBLIC_DIR };
