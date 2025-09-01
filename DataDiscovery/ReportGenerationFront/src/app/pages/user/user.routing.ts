import { RouterModule, Routes } from "@angular/router";

import { NgModule } from "@angular/core";
import { UserComponent } from "./user.component";
import { UserListComponent } from "./user-list/user-list.component";
import { UserFormComponent } from "./user-form/user-form.component";

const routes: Routes = [
    {
        path: '',
        component: UserComponent,
        children: [
            {
                path: '',
                redirectTo: 'list',
                pathMatch: 'full'
            },

            {
                path: 'list',
                component: UserListComponent
            },

            {
                path: 'create',
                component: UserFormComponent
            },
            
            {
                path: ':userId',
                component: UserFormComponent
            }
        ],
    },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UserRoutingModule {}