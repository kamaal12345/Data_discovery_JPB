import { NgFor, NgIf, NgTemplateOutlet } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { NgbToastModule } from '@ng-bootstrap/ng-bootstrap';
import { ToastService } from '../../app/core/services/toast.service';

@Component({
  selector: 'app-toasts-container',
  templateUrl: './toasts-container.component.html',
  styleUrls: ['./toasts-container.component.css'],
  standalone: true,
  imports: [ NgbToastModule, NgIf, NgTemplateOutlet, NgFor ],
  host: { class: 'toast-container position-fixed top-0 end-0 p-3', style: 'z-index: 1200' }
})
export class ToastsContainerComponent implements OnInit {

  constructor(
    public toastService: ToastService
  ) { }

  ngOnInit(): void {
  }

  isTemplate(toast: any) {
    return toast.textOrTpl instanceof TemplateRef;
  }

}
