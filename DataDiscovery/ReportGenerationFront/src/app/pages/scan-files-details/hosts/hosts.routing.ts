import { RouterModule, Routes } from "@angular/router";
import { NgModule } from "@angular/core";
import { HostsListComponent } from "./hosts-list/hosts-list.component";
import { HostsComponent } from "./hosts.component";
import { HostsAddComponent } from "./hosts-add/hosts-add.component";

const routes: Routes = [
    {
        path: '',
        component: HostsComponent,
        children: [
            {
                path: '',
                redirectTo: 'list',
                pathMatch: 'full'
            },

            {
                path: 'list',
                component: HostsListComponent
            },

            {
                path: 'add',
                component: HostsAddComponent
            },
        ],
    },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
})
export class HostsRoutingModule { }