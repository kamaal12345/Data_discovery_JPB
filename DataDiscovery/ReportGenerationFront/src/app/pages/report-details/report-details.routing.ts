import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ReportDetailsAddComponent } from './report-details-add/report-details-add.component';
import { ReportDetailsListComponent } from './report-details-list/report-details-list.component';
import { ReportDetailsComponent } from './report-details.component';
import { VulnerabilitiesUploadComponent } from './vulnerabilities-upload/vulnerabilities-upload.component';

const routes: Routes = [
    {
        path: '',
        component: ReportDetailsComponent,
        children: [
            {
                path: '',
                redirectTo: 'list',
                pathMatch: 'full'
            },

            {
                path: 'list',
                component: ReportDetailsListComponent
            },

            {
                path: 'add',
                component: ReportDetailsAddComponent
            },

            {
                path: 'edit-report-details/:reportId',
                component: ReportDetailsAddComponent,
            },

            {
                path: 'vulner-upload',
                component: VulnerabilitiesUploadComponent
            },
        ],
    },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
})
export class ReportDetailsRoutingModule { }
