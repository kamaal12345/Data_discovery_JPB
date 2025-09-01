import { Routes } from '@angular/router';
import { ImageUploadComponent } from './pages/image-upload/image-upload.component';
import { LoginComponent } from './login/login.component';
import { AuthGuardService } from './core/services/auth-guard-service.service';
import { JrmComponent } from './pages/jrm/jrm.component';
import { RolesComponent } from './pages/roles/roles.component';
import { UserProfileComponent } from './pages/user/user-profile/user-profile.component';

export const routes: Routes = [
  { path: '', redirectTo: 'jrm/dashboard', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'roles', component: RolesComponent, canActivate: [AuthGuardService]},

  {
    path: 'jrm',
    component: JrmComponent,
    canActivate: [AuthGuardService],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'user-profile', component: UserProfileComponent },
      { path: 'dashboard', loadChildren: () => import('./pages/dashboard/dashboard.routing').then(m => m.DashBoardsRoutingModule) },
      { path: 'report-details', loadChildren: () => import('./pages/report-details/report-details.routing').then(m => m.ReportDetailsRoutingModule) },
      { path: 'template', loadChildren: () => import('./pages/templates/templates.routing').then(m => m.TemplateRoutingModule) },
      { path: 'scan-files-details', loadChildren: () => import('./pages/scan-files-details/scan-files-details.routing').then(m => m.ScanFilesDetailsRoutingModule) },
      { path: 'users', loadChildren: () => import('./pages/user/user.routing').then(m => m.UserRoutingModule) },
      { path: 'properties', loadChildren: () => import('./pages/properties/properties.routing').then(m => m.PropertiesRoutingModule ) },
      { path: 'image-upload', component: ImageUploadComponent },
      { path: 'edit-upload-image-details/:id', component: ImageUploadComponent },
    ],
  },
];
