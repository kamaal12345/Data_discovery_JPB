import { TestBed } from '@angular/core/testing';

import { VulnerabilitiesSummaryService } from './vulnerabilities-summary.service';

describe('VulnerabilitiesSummaryService', () => {
  let service: VulnerabilitiesSummaryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VulnerabilitiesSummaryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
