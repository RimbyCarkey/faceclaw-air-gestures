const { spawn } = require('node:child_process');
const { existsSync, mkdirSync, copyFileSync, readFileSync, rmSync } = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '..');
async function buildNativeBand() {
  const stage = path.join(root, 'native/air-gestures/plugin/platforms/android/faceclaw-air-gestures.aar');
  if (process.env.FACECLAW_NEURAL_BAND === '0') { rmSync(stage, { force: true }); return; }
  require('./prepare-air-gestures.cjs').prepareAirGestures();
  const jni = path.join(root, 'native/air-gestures/jniLibs/arm64-v8a/libairgestures_bridge.so');
  if (!existsSync(jni)) throw new Error('Build the pinned Air Gestures Rust JNI before the combined APK.');
  await new Promise((resolve, reject) => {
    const child = spawn(path.join(root, 'wear/gradlew'), ['-p', path.join(root, 'native/air-gestures'), 'assembleDebug', '--console=plain'], { cwd: root, stdio: 'inherit' });
    child.on('error', reject); child.on('exit', code => code === 0 ? resolve() : reject(new Error(`Combined band build failed (${code})`)));
  });
  const aar = path.join(root, 'native/air-gestures/build/outputs/aar/faceclaw-air-gestures-debug.aar');
  mkdirSync(path.dirname(stage), { recursive: true });
  if (!existsSync(stage) || !readFileSync(stage).equals(readFileSync(aar))) {
    copyFileSync(aar, stage);
    rmSync(path.join(root, 'platforms/android/app/src/main/assets/metadata'), { recursive: true, force: true });
  }
}
module.exports = { buildNativeBand };
