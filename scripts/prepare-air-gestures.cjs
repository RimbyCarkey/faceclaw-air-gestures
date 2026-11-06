const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');
const root = path.resolve(__dirname, '..');
function prepareAirGestures() {
 const source = path.join(root, 'vendor/air-gestures/app/src/main/java');
 if (!fs.existsSync(source)) throw new Error('Initialize submodules: git submodule update --init --recursive');
 const stage = path.join(root, 'native/air-gestures/build/upstream');
 fs.rmSync(stage, { recursive: true, force: true });
 fs.mkdirSync(path.join(stage, 'app/src/main'), { recursive: true });
 fs.cpSync(source, path.join(stage, 'app/src/main/java'), { recursive: true });
 const args = ['apply', '--directory', 'native/air-gestures/build/upstream', 'integrations/air-gestures/host.patch'];
 execFileSync('git', ['apply', '--check', ...args.slice(1)], { cwd: root, stdio: 'inherit' });
 execFileSync('git', args, { cwd: root, stdio: 'inherit' });
}
if (require.main === module) prepareAirGestures();
module.exports = { prepareAirGestures };
