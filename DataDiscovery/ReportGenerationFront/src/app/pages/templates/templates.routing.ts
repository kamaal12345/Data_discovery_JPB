import { Route, RouterModule, Routes } from '@angular/router';
import { TemplatesComponent } from './templates.component';
import { PdfGenerationComponent } from './pdf-generation/pdf-generation.component';
import { NgModule } from '@angular/core';

export const routes: Routes = [
{
  path: '',
  component: TemplatesComponent,
  children: [
    { path: '', component: PdfGenerationComponent },
    { path: 'pdf', component: PdfGenerationComponent }
  ]
}

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TemplateRoutingModule {}
