import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { PropertiesFormComponent } from './properties-form/properties-form.component';
import { PropertiesListComponent } from './properties-list/properties-list.component';
import { PropertiesComponent } from './properties.component';

const routes: Routes = [
    {
        path: '',
        component: PropertiesComponent,
        children: [
            {
                path: '',
                redirectTo: 'properties-list',
                pathMatch: 'full'
            },
            {
                path: 'properties-list',
                component: PropertiesListComponent
            },
            {
                path: 'add-property',
                component: PropertiesFormComponent
            },
            {
                path: 'edit-property/:propertyId',
                component: PropertiesFormComponent
            }
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class PropertiesRoutingModule { }
