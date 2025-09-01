import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JrmComponent } from './jrm.component';

describe('JrmComponent', () => {
  let component: JrmComponent;
  let fixture: ComponentFixture<JrmComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JrmComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(JrmComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
