import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VulnerabilitiesUploadComponent } from './vulnerabilities-upload.component';

describe('VulnerabilitiesUploadComponent', () => {
  let component: VulnerabilitiesUploadComponent;
  let fixture: ComponentFixture<VulnerabilitiesUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VulnerabilitiesUploadComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(VulnerabilitiesUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
