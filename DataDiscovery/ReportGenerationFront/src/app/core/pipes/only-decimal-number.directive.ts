import { Directive, HostListener, ElementRef, Input } from '@angular/core';

@Directive({
  selector: '[appOnlyDecimalNumber]'
})
export class OnlyDecimalNumberDirective {
  // <input type="text" name="creditcard" id="creditcard_number"
  //   placeholder="000" maxlength="3"
  //   inputmode="numeric" pattern="[0-9]*" appOnlyDecimalNumber>
  private regex: RegExp = new RegExp(/^\d*\.?\d{0,2}$/g);
  private specialKeys = [
    'Backspace',
    'Delete',
    'Tab',
    'Escape',
    'Enter',
    'Home',
    'End',
    'ArrowLeft',
    'ArrowRight',
    'Clear',
    'Copy',
    'Paste'
  ];
  inputElement: HTMLElement;
  @Input() appOnlyDecimalNumber: number;

  constructor(private el: ElementRef) {
    this.inputElement = el.nativeElement;
  }

  @HostListener('keydown', ['$event'])
  onKeyDown(e: KeyboardEvent) {
    console.log(this.el.nativeElement.value);
    // Allow Backspace, tab, end, and home keys
    if (this.specialKeys.indexOf(e.key) !== -1) {
      return;
    }
    let current: string = this.el.nativeElement.value;
    const position = this.el.nativeElement.selectionStart;
    const next: string = [current.slice(0, position), e.key == 'Decimal' ? '.' : e.key, current.slice(position)].join('');
    if (next && !String(next).match(this.regex)) {
      e.preventDefault();
    }
  }
  // @HostListener('paste', ['$event'])
  // onPaste(event: ClipboardEvent) {
  //   event.preventDefault();
  //   const pastedInput: string = event.clipboardData
  //     .getData('text/plain')
  //     .replace(/^\d*\.?\d{0,2}$/g, '');
  //   document.execCommand('insertText', false, pastedInput);
  // }

  // @HostListener('drop', ['$event'])
  // onDrop(event: DragEvent) {
  //   event.preventDefault();
  //   const textData = event.dataTransfer
  //     .getData('text').replace(/^\d*\.?\d{0,2}$/g, '');
  //   this.inputElement.focus();
  //   document.execCommand('insertText', false, textData);
  // }
}
