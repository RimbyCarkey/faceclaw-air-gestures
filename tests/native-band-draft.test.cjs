const { test } = require('node:test');
const assert = require('node:assert/strict');
const { loader } = require('./helpers/load-typescript.cjs');
test('band-opened draft is retained while its pinned destination is unavailable', () => {
  let available = false, closed = false;
  const sent = [], history = [];
  const { KeyboardInputLayer } = loader({}, {
    './input-dialog': { createInputDialogMenu: () => ({ items: [], setItems() {} }), paintInputDialog() {} },
  })('app/ui/shell/keyboard-input.ts');
  const draft = new KeyboardInputLayer({ actions: { requestRender() {} }, onClosed: () => closed = true,
    dismiss() {}, setShownOnGlasses() {}, visibility: 'phone', onVisibilityChanged() {}, history,
    sendTargets: [{ id: 'original', label: 'Confirm original destination', available: () => available, onSend: text => sent.push(text) }],
  });
  draft.setText('keep the draft'); draft.sendTo('original');
  assert.equal(closed, false); assert.deepEqual(sent, []); assert.deepEqual(history, []);
  assert.match(draft.problem, /Draft retained/);
  available = true; draft.sendTo('original');
  assert.equal(closed, true); assert.deepEqual(sent, ['keep the draft']);
});
