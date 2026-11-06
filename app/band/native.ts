import { Application, Utils } from '@nativescript/core';
import type { BandInput } from './input';
declare const dev: any;
export function nativeBandAvailable(): boolean { return global.isAndroid && typeof dev !== 'undefined' && !!dev.kb896?.airgestures?.FaceclawHost; }
export function nativeBand(): any {
  if (!nativeBandAvailable()) throw new Error('Native neural-band module is unavailable.');
  return dev.kb896.airgestures.FaceclawHost.getInstance(Utils.android.getApplicationContext());
}
export function openBandManager(setup = false): void {
  const activity = Application.android.foregroundActivity || Application.android.startActivity;
  activity.startActivity(setup ? nativeBand().setupIntent() : nativeBand().managementIntent());
}
const modeListeners = new Set<() => void>();
export function subscribeBandMode(changed: () => void): () => void {
  modeListeners.add(changed); changed(); return () => modeListeners.delete(changed);
}
let listener: any;
export function startNativeBand(host: { ready(): boolean; input(value: BandInput): Promise<void>; keyboard(): void }): void {
  if (!nativeBandAvailable() || listener) return;
  let busy = false, lastAt = -Infinity;
  const allowed = new Set(['click', 'double-click', 'swipe-up', 'swipe-down', 'swipe-left', 'swipe-right', 'long-press', 'short-then-long-press']);
  listener = new dev.kb896.airgestures.FaceclawHost.Listener({ onInput: (raw: string) => {
    const input = String(raw);
    if (input === "mode-changed") { modeListeners.forEach(fn => fn()); return; }
    if (!host.ready() || busy || Date.now() - lastAt < 80 || (input !== 'keyboard' && !allowed.has(input))) return;
    lastAt = Date.now();
    if (input === 'keyboard') { host.keyboard(); return; }
    busy = true; void host.input(input as BandInput).catch(() => {}).finally(() => { busy = false; });
  } });
  nativeBand().setListener(listener);
  Application.on(Application.exitEvent, () => nativeBand().setListener(null));
}
