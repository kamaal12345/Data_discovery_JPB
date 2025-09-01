import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { FooterComponent } from './layout/footer/footer.component';
import { HeaderComponent } from './layout/header/header.component';
import { SharedModule } from '../shared/shared.module';
import { CommonModule } from '@angular/common';
import { BrowserModule } from '@angular/platform-browser';
import { ToastsContainerComponent } from '../shared/toasts-container/toasts-container.component';
import { PdfGenerationComponent } from './pages/templates/pdf-generation/pdf-generation.component';
import { NgxUiLoaderModule } from 'ngx-ui-loader';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, 
    FooterComponent, 
    HeaderComponent,
    ToastsContainerComponent,
    PdfGenerationComponent,
    NgxUiLoaderModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit {
  title = 'ReportGeneration';

  constructor( private router: Router, ) {
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.clear();
    }
  }

  ngOnInit() {
    // this.router.navigate(['/report-details/list']);
  }
}
