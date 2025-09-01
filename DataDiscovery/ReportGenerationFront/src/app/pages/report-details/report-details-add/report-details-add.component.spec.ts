import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportDetailsAddComponent } from './report-details-add.component';

describe('ReportDetailsAddComponent', () => {
  let component: ReportDetailsAddComponent;
  let fixture: ComponentFixture<ReportDetailsAddComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportDetailsAddComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ReportDetailsAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
