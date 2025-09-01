import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JioAssessmentReportTemplateComponent } from './jio-assessment-report-template.component';

describe('JioAssessmentReportTemplateComponent', () => {
  let component: JioAssessmentReportTemplateComponent;
  let fixture: ComponentFixture<JioAssessmentReportTemplateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JioAssessmentReportTemplateComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(JioAssessmentReportTemplateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
