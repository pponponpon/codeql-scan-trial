import { TestBed } from '@angular/core/testing';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render title', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('codeql-scan-trial frontend');
  });

  it('should render a posted message into the raw box', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    window.postMessage('hello-from-test', '*');
    await new Promise((resolve) => setTimeout(resolve, 50));
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.raw-box')?.textContent).toContain('hello-from-test');
  });
});
