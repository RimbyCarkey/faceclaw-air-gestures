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
const traceListeners = new Set<(trace: string) => void>();
const recentTrace: string[] = [];

export function recordBandTrace(message: string): void {
  recentTrace.push(message);
  if (recentTrace.length > 8) recentTrace.shift();
  const trace = recentTrace.join('\n');
  traceListeners.forEach(listener => listener(trace));
}

export function subscribeBandTrace(listener: (trace: string) => void): () => void {
  traceListeners.add(listener);
  listener(recentTrace.join('\n'));
  return () => traceListeners.delete(listener);
}

let listener: any;
export function startNativeBand(host: { target(): 'preview' | 'glasses' | null; input(value: BandInput): Promise<void>; keyboard(): void }): void {
  if (!nativeBandAvailable() || listener) return;
  let busy = false, lastAt = -Infinity;
  const allowed = new Set(['click', 'double-click', 'swipe-up', 'swipe-down', 'swipe-left', 'swipe-right', 'long-press', 'short-then-long-press']);
  listener = new dev.kb896.airgestures.FaceclawHost.Listener({ onInput: (raw: string) => {
    const input = String(raw);
    if (input === "mode-changed") { modeListeners.forEach(fn => fn()); return; }
    if (input !== 'keyboard' && !allowed.has(input)) return;
    const target = host.target();
    if (!target) {
      recordBandTrace(`${input}: blocked (no active unlocked display)`);
      return;
    }
    if (busy || Date.now() - lastAt < 80) {
      recordBandTrace(`${input}: skipped (input busy or repeated too quickly)`);
      return;
    }
    recordBandTrace(`${input}: received → ${target}`);
    lastAt = Date.now();
    if (input === 'keyboard') { host.keyboard(); return; }
    busy = true;
    void host.input(input as BandInput)
      .catch(() => recordBandTrace(`${input}: dispatch failed`))
      .finally(() => { busy = false; });
  } });
  nativeBand().setListener(listener);
  Application.on(Application.exitEvent, () => nativeBand().setListener(null));
}
