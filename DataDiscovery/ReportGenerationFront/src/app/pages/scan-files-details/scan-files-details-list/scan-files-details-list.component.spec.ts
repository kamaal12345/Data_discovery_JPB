import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScanFilesDetailsListComponent } from './scan-files-details-list.component';

describe('ScanFilesDetailsListComponent', () => {
  let component: ScanFilesDetailsListComponent;
  let fixture: ComponentFixture<ScanFilesDetailsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScanFilesDetailsListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ScanFilesDetailsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
