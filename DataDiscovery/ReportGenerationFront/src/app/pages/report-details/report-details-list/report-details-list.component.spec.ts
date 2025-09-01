import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportDetailsListComponent } from './report-details-list.component';

describe('ReportDetailsListComponent', () => {
  let component: ReportDetailsListComponent;
  let fixture: ComponentFixture<ReportDetailsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportDetailsListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ReportDetailsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
