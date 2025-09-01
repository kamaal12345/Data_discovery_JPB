import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScanFilesDetailsResultsComponent } from './scan-files-details-results.component';

describe('ScanFilesDetailsResultsComponent', () => {
  let component: ScanFilesDetailsResultsComponent;
  let fixture: ComponentFixture<ScanFilesDetailsResultsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScanFilesDetailsResultsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ScanFilesDetailsResultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
