import { TestBed } from '@angular/core/testing';

import { FilesScanService } from './files-scan.service';

describe('FilesScanService', () => {
  let service: FilesScanService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FilesScanService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
