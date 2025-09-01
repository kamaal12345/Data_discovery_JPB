import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ScanFilesDetailsRoutingModule } from './scan-files-details.routing';
import { HostsComponent } from './hosts/hosts.component';
import { HostsRoutingModule } from './hosts/hosts.routing';

@Component({
  selector: 'app-scan-files-details',
  standalone: true,
  imports: [RouterOutlet,],
  templateUrl: './scan-files-details.component.html',
  styleUrl: './scan-files-details.component.css'
})
export class ScanFilesDetailsComponent {

}
