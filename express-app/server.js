'use strict';

// DEPBAIT-04 usage: minimist 1.2.5 (CVE-2021-44906 prototype pollution).
const minimist = require('minimist');
const { createApp } = require('./app');

const argv = minimist(process.argv.slice(2), { default: { port: 3000 } });
const app = createApp();
app.listen(argv.port, () => {
  console.log(`express trial app listening on port ${argv.port}`);
});
