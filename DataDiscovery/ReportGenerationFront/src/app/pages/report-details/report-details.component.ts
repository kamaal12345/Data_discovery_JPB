import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ReportDetailsRoutingModule } from './report-details.routing';

@Component({
  selector: 'app-report-details',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './report-details.component.html',
  styleUrl: './report-details.component.css'
})
export class ReportDetailsComponent {

}
