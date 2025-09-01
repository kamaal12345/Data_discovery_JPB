import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

interface PdfTriggerData {
  reportId: number;
  action: 'download' | 'preview';
}

@Injectable({
  providedIn: 'root'
})
export class PdfTriggerService {
  private triggerPdfSubject = new Subject<PdfTriggerData>();
  triggerPdf$ = this.triggerPdfSubject.asObservable();

  triggerPdfGeneration(reportId: number, action: 'download' | 'preview') {
    this.triggerPdfSubject.next({ reportId, action });
  }
}
