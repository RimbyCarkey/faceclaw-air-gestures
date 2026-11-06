const { test } = require('node:test');
const assert = require('node:assert/strict');
const { execFileSync } = require('node:child_process');
test('host manifest retains every upstream feature component and profile announcements', () => {
  // Module build consumes this same production generator.
  const result = execFileSync('python3', ['-c', `
import importlib.util, xml.etree.ElementTree as E
from pathlib import Path
spec=importlib.util.spec_from_file_location('manifest', 'scripts/air-gestures-manifest.py')
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
p=Path('vendor/air-gestures/app/src/main/AndroidManifest.xml')
if not p.exists(): raise SystemExit('Initialize recursive submodules to test native manifest parity')
original=E.parse(p).getroot().find('application')
new=m.adapt(p).getroot().find('application')
a=m.ANDROID
assert [(x.tag,x.get(a+'name')) for x in original] == [(x.tag,x.get(a+'name')) for x in new]
assert not new.attrib
by_name={x.get(a+'name'):x for x in new}
assert by_name['.ProfileReceiver'].get(a+'exported') == 'true'
assert by_name['.MainActivity'].get(a+'exported') == 'false'
assert by_name['.MetaSignIn'].get(a+'process') == ':band_enrollment'
print('upstream feature parity preserved')
`], { encoding: 'utf8' });
  assert.match(result, /feature parity preserved/);
});
