import { EventData, Observable, Page, View } from '@nativescript/core';
import { nativeBand, nativeBandAvailable, openBandManager, subscribeBandMode, subscribeBandTrace } from '../band/native';

class BandSettings extends Observable {
  private stopMode?: () => void;
  private stopTrace?: () => void;

  attach(): void {
    this.stopMode ??= subscribeBandMode(() => this.refreshMode());
    this.stopTrace ??= subscribeBandTrace(trace => {
      this.set('trace', trace || 'No Faceclaw gestures received yet.');
    });
    this.refreshMode();
  }

  detach(): void {
    this.stopMode?.();
    this.stopTrace?.();
    this.stopMode = undefined;
    this.stopTrace = undefined;
  }

  private refreshMode(): void {
    const mode = !nativeBandAvailable() ? 'Native band module unavailable.'
      : nativeBand().isGlassesMode() ? 'Glasses mode enabled.' : 'Phone mode restored.';
    this.set('mode', mode);
  }

  private run(action: () => void): void {
    this.set('message', '');
    try {
      action();
      this.refreshMode();
    } catch (error) {
      this.set('message', String(error));
    }
  }

  manage(): void { this.run(() => openBandManager()); }
  setup(): void { this.run(() => openBandManager(true)); }
  toggle(): void { this.run(() => nativeBand().toggleGlassesMode()); }
}

export function navigatingTo(args: EventData): void {
  const page = args.object as Page;
  page.bindingContext ??= new BandSettings();
  (page.bindingContext as BandSettings).attach();
}

export function navigatingFrom(args: EventData): void {
  const page = args.object as Page;
  (page.bindingContext as BandSettings | undefined)?.detach();
}

export function back(args: EventData): void { (args.object as View).page.frame.goBack(); }
