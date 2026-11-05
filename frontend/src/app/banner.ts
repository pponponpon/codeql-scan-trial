import { Component } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-banner',
  template: '<div class="banner" [innerHTML]="html"></div>',
})
export class Banner {
  protected html: SafeHtml = '';

  // VULN-A1b (round 2 variant): same bypassSecurityTrustHtml sink, but DomSanitizer obtained via constructor injection
  // instead of inject(); source is still a URL query parameter.
  constructor(private readonly sanitizer: DomSanitizer) {
    const banner = new URLSearchParams(window.location.search).get('banner2');
    if (banner !== null) {
      this.html = this.sanitizer.bypassSecurityTrustHtml(banner);
    }
  }
}
