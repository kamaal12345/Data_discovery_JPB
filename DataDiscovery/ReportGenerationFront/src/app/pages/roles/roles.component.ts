import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RoleNamesEnum, RolesEnum } from '../../core/enums/Roles.enum';
import { LoginUser } from '../../core/models/LoginUser';
import { GlobalService } from '../../core/services/global-service.service';
import { SharedModule } from '../../../shared/shared.module';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './roles.component.html',
  styleUrl: './roles.component.css',
})
export class RolesComponent implements OnInit, OnDestroy {
  loginRoleType: number[];
  userDetails: LoginUser;
  roleTypes = RolesEnum;
  destroy$: Subject<boolean> = new Subject<boolean>();
  constructor(private globalService: GlobalService, private router: Router) {}

  ngOnInit(): void {
    this.globalService.userLoginStatus.subscribe((value) => {
      if (value) {
        this.userDetails = this.globalService.getLoginUserDetails();
        this.loginRoleType = this.userDetails.roleValue;
      }
    });
  }

  selectRole(role: number): void {
    this.globalService.setSelectedRole(role);
    this.globalService.setRoleSelectedStatus(true);
  }

  getDefaultRoleLogo(role: number): string {
    const defaultLogos = {
      [RolesEnum.SUPER_ADMIN]: 'assets/images/icons/Hr_logo_preview.png',
      [RolesEnum.ADMIN]: 'assets/images/icons/admin.svg',
      [RolesEnum.EMPLOYEE]: 'assets/images/icons/employee.svg',
    };
    return defaultLogos[role as RolesEnum];
  }

  getRoleAltText(role: number): string {
    const altTexts = {
      [RolesEnum.SUPER_ADMIN]: RoleNamesEnum.SUPER_ADMIN,
      [RolesEnum.ADMIN]: RoleNamesEnum.ADMIN,
      [RolesEnum.EMPLOYEE]: RoleNamesEnum.EMPLOYEE,
    };
    return altTexts[role as RolesEnum];
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
