import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TpisaReportTemplateComponent } from './tpisa-report-template.component';

describe('TpisaReportTemplateComponent', () => {
  let component: TpisaReportTemplateComponent;
  let fixture: ComponentFixture<TpisaReportTemplateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TpisaReportTemplateComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TpisaReportTemplateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
