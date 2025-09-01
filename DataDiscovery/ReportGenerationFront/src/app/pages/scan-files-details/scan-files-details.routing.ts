import { RouterModule, Routes } from "@angular/router";
import { ScanFilesDetailsComponent } from "./scan-files-details.component";
import { ScanFilesDetailsListComponent } from "./scan-files-details-list/scan-files-details-list.component";
import { ScanFilesDetailsAddComponent } from "./scan-files-details-add/scan-files-details-add.component";
import { NgModule } from "@angular/core";

const routes: Routes = [
    {
        path: '',
        component: ScanFilesDetailsComponent,
        children: [
            {
                path: '',
                redirectTo: 'files_list',
                pathMatch: 'full'
            },

            {
                path: 'files_list',
                component: ScanFilesDetailsListComponent
            },

            {
                path: 'files_scan',
                component: ScanFilesDetailsAddComponent
            },

            {   path: 'hosts-config', loadChildren: () => import('../scan-files-details/hosts/hosts.routing').then(m => m.HostsRoutingModule) },
        ],
    },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ScanFilesDetailsRoutingModule {}