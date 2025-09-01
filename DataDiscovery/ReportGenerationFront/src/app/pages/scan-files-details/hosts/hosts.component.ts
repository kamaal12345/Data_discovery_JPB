import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-hosts',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './hosts.component.html',
  styleUrl: './hosts.component.css'
})
export class HostsComponent {

}
