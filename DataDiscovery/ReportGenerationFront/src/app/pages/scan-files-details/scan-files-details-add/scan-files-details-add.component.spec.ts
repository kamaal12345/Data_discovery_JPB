import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScanFilesDetailsAddComponent } from './scan-files-details-add.component';

describe('ScanFilesDetailsAddComponent', () => {
  let component: ScanFilesDetailsAddComponent;
  let fixture: ComponentFixture<ScanFilesDetailsAddComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScanFilesDetailsAddComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ScanFilesDetailsAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
