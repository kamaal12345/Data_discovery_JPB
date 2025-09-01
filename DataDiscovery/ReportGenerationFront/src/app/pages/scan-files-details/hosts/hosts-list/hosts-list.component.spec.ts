import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HostsListComponent } from './hosts-list.component';

describe('HostsListComponent', () => {
  let component: HostsListComponent;
  let fixture: ComponentFixture<HostsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HostsListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(HostsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
