import { TestBed } from '@angular/core/testing';

import { ReportDetailsService } from './report-details.service';

describe('ReportDetailsService', () => {
  let service: ReportDetailsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ReportDetailsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
