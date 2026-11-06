#!/usr/bin/env python3
"""Preserve upstream components; adapt only the host application and entry points."""
from pathlib import Path
import xml.etree.ElementTree as ET
ROOT = Path(__file__).resolve().parents[1]
ANDROID = '{http://schemas.android.com/apk/res/android}'
ET.register_namespace('android', 'http://schemas.android.com/apk/res/android')
ET.register_namespace('tools', 'http://schemas.android.com/tools')
def adapt(source):
    tree = ET.parse(source)
    app = tree.getroot().find('application')
    if app is None: raise ValueError('Upstream manifest has no application')
    app.attrib.clear()
    for component in app:
        name = component.get(ANDROID + 'name', '')
        if name == '.MainActivity':
            component.set(ANDROID + 'exported', 'false')
            for intent in list(component.findall('intent-filter')):
                if any(category.get(ANDROID + 'name') == 'android.intent.category.LAUNCHER' for category in intent.findall('category')):
                    component.remove(intent)
        if name == '.MetaSignIn': component.set(ANDROID + 'process', ':band_enrollment')
    for item in tree.iter():
        for key, value in list(item.attrib.items()):
            item.set(key, value.replace('${appLabel}', 'Faceclaw band'))
    return tree
if __name__ == '__main__':
    output = ROOT / 'native/air-gestures/build/upstream/AndroidManifest.xml'
    output.parent.mkdir(parents=True, exist_ok=True)
    adapt(ROOT / 'vendor/air-gestures/app/src/main/AndroidManifest.xml').write(output, encoding='utf-8', xml_declaration=True)
