import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NgxUiLoaderModule } from 'ngx-ui-loader';
import { HeaderComponent } from '../../layout/header/header.component';

@Component({
  selector: 'app-jrm',
  standalone: true,
  imports: [RouterOutlet, 
    HeaderComponent,
    NgxUiLoaderModule
  ],
  templateUrl: './jrm.component.html',
  styleUrl: './jrm.component.css'
})
export class JrmComponent {

}
