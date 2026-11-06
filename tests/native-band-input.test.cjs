const { test } = require('node:test');
const assert = require('node:assert/strict');
const { loader } = require('./helpers/load-typescript.cjs');
const { BAND_INPUTS, bandInputPayload } = loader()('app/band/input.ts');
const { isBandInput, isWatchInput, isSpatialInput } = loader()('app/ui/gestures.ts');
test('native band events retain independent identity for every supported gesture', () => {
  BAND_INPUTS.forEach((type, code) => {
    const event = bandInputPayload(code);
    assert.equal(event.type, type); assert.equal(event.source, 'band');
    assert.equal(isBandInput(event), true); assert.equal(isWatchInput(event), false);
    assert.equal(isSpatialInput(event), true);
  });
});
test('malformed band codes cannot turn into a known gesture', () => {
  for (const code of [-1, 999, NaN]) assert.equal(bandInputPayload(code).type, 'unknown');
});
