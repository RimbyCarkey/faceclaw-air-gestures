import { EventData, Observable, Page, View } from '@nativescript/core';
import { nativeBand, openBandManager } from '../band/native';
class BandSettings extends Observable {
  private run(action: () => void): void { try { action(); this.set('message', nativeBand().isGlassesMode() ? 'Glasses mode enabled.' : 'Phone mode restored.'); } catch (error) { this.set('message', String(error)); } }
  manage(): void { this.run(() => openBandManager()); }
  setup(): void { this.run(() => openBandManager(true)); }
  toggle(): void { this.run(() => nativeBand().toggleGlassesMode()); }
}
export function navigatingTo(args: EventData): void { (args.object as Page).bindingContext = new BandSettings(); }
export function back(args: EventData): void { (args.object as View).page.frame.goBack(); }
