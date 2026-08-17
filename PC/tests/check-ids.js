/* Static check: every element id referenced in app.js must exist in index.html,
 * otherwise a null addEventListener call in bindControls would throw and stop
 * binding the remaining buttons (e.g. btn-save-here). */
'use strict';

const fs = require('node:fs');

const appJs = fs.readFileSync('F:/Projects/StringArtHelper/PC/src/renderer/app.js', 'utf8');
const html = fs.readFileSync('F:/Projects/StringArtHelper/PC/src/renderer/index.html', 'utf8');

const refs = new Set();
const re = /\$\('([a-zA-Z0-9_-]+)'\)|getElementById\('([a-zA-Z0-9_-]+)'\)/g;
let m;
while ((m = re.exec(appJs)) !== null) refs.add(m[1] || m[2]);

const defs = new Set();
const re2 = /id="([a-zA-Z0-9_-]+)"/g;
while ((m = re2.exec(html)) !== null) defs.add(m[1]);

const missing = [...refs].filter((id) => !defs.has(id)).sort();
console.log('referenced ids:', refs.size);
console.log('defined ids:', defs.size);
console.log('MISSING (referenced but not in HTML):', missing.length ? JSON.stringify(missing) : '(none)');
process.exit(missing.length ? 1 : 0);
