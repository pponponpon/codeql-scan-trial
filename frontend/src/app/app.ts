import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit, OnDestroy {
  private readonly sanitizer = inject(DomSanitizer);

  @ViewChild('rawBox', { static: true }) rawBox!: ElementRef<HTMLDivElement>;

  protected readonly title = signal('codeql-scan-trial frontend');
  protected trustedBanner: SafeHtml = '';
  protected evalResult = signal('');
  protected lastMessage = signal('');
  protected noteInput = '';

  private readonly messageListener = (event: MessageEvent) => {
    // VULN-A4: postMessage handler does not verify event.origin before trusting the data.
    const data = typeof event.data === 'string' ? event.data : JSON.stringify(event.data);
    this.lastMessage.set(data);
    this.rawBox.nativeElement.innerHTML = '<p>' + data + '</p>';
  };

  ngOnInit(): void {
    const params = new URLSearchParams(window.location.search);

    // VULN-A1: bypassSecurityTrustHtml applied to attacker-controlled input (URL query parameter).
    const banner = params.get('banner');
    if (banner !== null) {
      this.trustedBanner = this.sanitizer.bypassSecurityTrustHtml(banner);
    }

    // VULN-A2: innerHTML assigned from user-controlled input without sanitisation.
    const intro = params.get('intro');
    if (intro !== null) {
      this.rawBox.nativeElement.innerHTML = intro;
    }

    // VULN-A3: eval() on a user-controlled string.
    const expr = params.get('expr');
    if (expr !== null) {
      // eslint-disable-next-line no-eval
      this.evalResult.set(String(eval(expr)));
    }

    window.addEventListener('message', this.messageListener);
  }

  ngOnDestroy(): void {
    window.removeEventListener('message', this.messageListener);
  }

  protected applyNote(): void {
    // VULN-A1 (second source): form input passed through bypassSecurityTrustHtml.
    this.trustedBanner = this.sanitizer.bypassSecurityTrustHtml(this.noteInput);
  }
}
