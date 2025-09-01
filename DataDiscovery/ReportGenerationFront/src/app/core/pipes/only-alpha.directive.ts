import { Directive, Input, ElementRef, HostListener } from '@angular/core';

@Directive({
  selector: '[appOnlyAlpha]'
})
export class OnlyAlphaDirective {

  // <input type="text" appOnlyAlpha>

  inputElement: HTMLElement;
  @Input('appOnlyAlpha') appOnlyAlpha: boolean;

  constructor(private el: ElementRef) {
    this.inputElement = el.nativeElement;
  }

  @HostListener('input', ['$event']) onInputChange(event: any) {
    if (this.appOnlyAlpha == false) {
      
    } else {
      const initalValue = this.el.nativeElement.value;
      this.el.nativeElement.value = initalValue.replace(/[^a-zA-Z ]*/g, '');
      if ( initalValue !== this.el.nativeElement.value) {
        event.stopPropagation();
      }
    }
  }

  // @HostListener('paste', ['$event'])
  // onPaste(event: ClipboardEvent) {
  //   if (this.appOnlyAlpha == false) {
      
  //   } else {
  //     event.preventDefault();
  //     const pastedInput: string = event.clipboardData
  //       .getData('text/plain')
  //       .replace(/[^a-zA-Z ]*/g, '');
  //     document.execCommand('insertText', false, pastedInput);
  //   }
  // }
  
  // @HostListener('drop', ['$event'])
  // onDrop(event: DragEvent) {
  //   if (this.appOnlyAlpha == false) {
      
  //   } else {
  //     event.preventDefault();
  //     const textData = event.dataTransfer
  //       .getData('text').replace(/[^a-zA-Z ]*/g, '');
  //     this.inputElement.focus();
  //     document.execCommand('insertText', false, textData);
  //   }
  // }

}
