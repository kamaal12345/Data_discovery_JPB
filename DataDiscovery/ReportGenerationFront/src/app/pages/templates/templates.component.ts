import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TemplateRoutingModule } from './templates.routing';

@Component({
  selector: 'app-templates',
  standalone: true,
  imports: [RouterOutlet,TemplateRoutingModule],
  templateUrl: './templates.component.html',
  styleUrl: './templates.component.css'
})
export class TemplatesComponent {

}
