import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScanFilesDetailsComponent } from './scan-files-details.component';

describe('ScanFilesDetailsComponent', () => {
  let component: ScanFilesDetailsComponent;
  let fixture: ComponentFixture<ScanFilesDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScanFilesDetailsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ScanFilesDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
