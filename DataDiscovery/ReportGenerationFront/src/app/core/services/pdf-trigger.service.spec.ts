import { TestBed } from '@angular/core/testing';

import { PdfTriggerService } from './pdf-trigger.service';

describe('PdfTriggerService', () => {
  let service: PdfTriggerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PdfTriggerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
