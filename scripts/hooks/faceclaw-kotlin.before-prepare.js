const { buildKotlin, checkAndroidRuntime } = require('../../scripts/kotlin-build.cjs');

const { buildNativeBand } = require('../../scripts/neural-band-build.cjs');

module.exports = async function (hookArgs) {
  // NativeScript parses hooks with an older JavaScript parser; avoid optional chaining.
  const { platform, release } = hookArgs.prepareData;
  if (platform === 'android') { checkAndroidRuntime(); await buildNativeBand(); }
  return buildKotlin(platform, release ? 'release' : 'debug');
};
