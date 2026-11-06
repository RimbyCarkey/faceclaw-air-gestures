import type { InputEventPayload } from '../ui/gestures';
export const BAND_INPUTS = ['click', 'double-click', 'swipe-up', 'swipe-down', 'swipe-left', 'swipe-right', 'long-press', 'long-press-release', 'short-then-long-press'] as const;
export type BandInput = typeof BAND_INPUTS[number];
export function bandInputPayload(code: number): InputEventPayload {
  const type = BAND_INPUTS[code];
  return type ? { type, source: 'band' } as InputEventPayload : { type: 'unknown', kind: 'band-gesture', eventSource: 5, eventType: code };
}
