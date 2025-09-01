import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { FormGroup } from '@angular/forms';
import { DomSanitizer } from '@angular/platform-browser';
import { ToastService } from './toast.service';
import { Router } from '@angular/router';
import { LoginUser } from '../models/LoginUser';
import { RoleNamesEnum, RolesEnum } from '../enums/Roles.enum';
import { Login } from '../models/login';
import { TokenService } from './token.service';
import { HostCategory } from '../enums/HostCategory.enum';
import { HostCategoryNameEnum } from '../enums/HostCategoryName.enum';
@Injectable({
  providedIn: 'root',
})
export class GlobalService {
  private userLoginStatusSubject = new BehaviorSubject<boolean>(false);
  public userLoginStatus = this.userLoginStatusSubject.asObservable();

  private userRoleStatusSubject = new BehaviorSubject<boolean>(false);
  public userRoleSelectionStatus = this.userRoleStatusSubject.asObservable();

  userId: number;
  userDetails: LoginUser;
  roleType = RolesEnum;
  roleName = RoleNamesEnum;
  selectedRoleValue: number;
  loginDetails: Login;

  public IS_LOGGED_IN: boolean;
  public IS_ROLE_SELECTED: boolean;
  public IS_SUPER_ADMIN: boolean;
  public IS_ADMIN: boolean;
  public Is_EMPLOYEE: boolean;

  constructor(
    private sanitizer: DomSanitizer,
    private tokenService: TokenService,
    private toastService: ToastService,
    private router: Router
  ) {
    this.userDetails = {} as LoginUser;
    if (this.tokenService.getToken()) {
      this.setUserLoginStatus(true);
    }
  }

  setUserLoginStatus(loginStatus: boolean) {
    this.userLoginStatusSubject.next(loginStatus);
    this.IS_LOGGED_IN = loginStatus;
    if (loginStatus) {
      this.userDetails = this.tokenService.parseJwt();
    }
  }

  setRoleSelectedStatus(roleStatus: boolean) {
    this.userRoleStatusSubject.next(roleStatus);
    if (roleStatus) {
      this.IS_SUPER_ADMIN =
        this.getUserRoleType() === 'SUPER_ADMIN' ? true : false;
      this.IS_ADMIN = this.getUserRoleType() === 'ADMIN' ? true : false;
      this.Is_EMPLOYEE = this.getUserRoleType() === 'Employee' ? true : false;
    }
  }

  //   setRoleSelectedStatus(roleStatus: boolean) {
  //   this.userRoleStatusSubject.next(roleStatus);
  //   if (roleStatus) {
  //     const userRoleType = this.getUserRoleType();
  //     this.IS_SUPER_ADMIN = userRoleType === 'SUPER_ADMIN';
  //     this.IS_ADMIN = userRoleType === 'ADMIN';
  //     this.Is_EMPLOYEE = userRoleType === 'Employee';

  //     console.log('User role detected:', userRoleType);
  //     console.log('IS_SUPER_ADMIN:', this.IS_SUPER_ADMIN);
  //     console.log('IS_ADMIN:', this.IS_ADMIN);
  //     console.log('Is_EMPLOYEE:', this.Is_EMPLOYEE);
  //   } else {
  //     console.log('Role selection status set to false');
  //   }
  // }

  setLoginDetails(loginDetails: Login): Login {
    return (this.loginDetails = loginDetails);
  }

  getLoginDetails(): Login {
    return this.loginDetails;
  }

  getLoginUserDetails(): LoginUser {
    return this.userDetails;
  }

  setSelectedRole(role: number): void {
    this.selectedRoleValue = role;
  }

  getSelectedRole(): number {
    return this.selectedRoleValue;
  }

  getUserFullName(): string {
    return this.userDetails?.firstName + ' ' + this.userDetails?.lastName;
  }

  getUserName(): string {
    return this.userDetails.username;
  }

  getUserId(): number {
    return this.userDetails?.userId;
  }

  getUser_Gender() {
    return this.userDetails.gender;
  }

  getUser_Designation() {
    return this.userDetails.designation;
  }

  getUserRoleType(): string {
    const selectedRole = this.getSelectedRole();
    let roleType = '';
    if (this.userDetails.roleValue.includes(selectedRole)) {
      if (selectedRole === this.roleType.SUPER_ADMIN) {
        roleType = 'SUPER_ADMIN';
      } else if (selectedRole === this.roleType.ADMIN) {
        roleType = 'ADMIN';
      } else if (selectedRole === this.roleType.EMPLOYEE) {
        roleType = 'Employee';
      } else {
        roleType = '';
      }
    }
    return roleType;
  }

  //   getUserRoleType(): string {
  //   const selectedRole = this.getSelectedRole();
  //   let roleType = '';
  //   if (this.userDetails.roleValue.includes(selectedRole)) {
  //     if (selectedRole === this.roleType.SUPER_ADMIN) {
  //       roleType = 'SUPER_ADMIN';
  //     } else if (selectedRole === this.roleType.ADMIN) {
  //       roleType = 'ADMIN';
  //     } else if (selectedRole === this.roleType.EMPLOYEE) {
  //       roleType = 'Employee';
  //     } else {
  //       roleType = '';
  //     }
  //   }
  //   console.log('getUserRoleType called - Selected Role:', selectedRole, 'RoleType:', roleType);
  //   return roleType;
  // }

  getUserRoleArray(): string[] {
    const roleTypes: string[] = [];
    const roleMap = {
      [this.roleType.SUPER_ADMIN]: this.roleName.SUPER_ADMIN,
      [this.roleType.ADMIN]: this.roleName.ADMIN,
      [this.roleType.EMPLOYEE]: this.roleName.EMPLOYEE,
    };

    for (const [key, value] of Object.entries(roleMap)) {
      if (this.userDetails.roleValue.includes(Number(key))) {
        roleTypes.push(value);
      }
    }

    return roleTypes;
  }

  redirectPostLogin(): void {
    this.userDetails = this.tokenService.parseJwt(); // update from token
    const roles: number[] = this.userDetails?.roleValue || [];

    if (roles.length === 1 || roles.includes(this.roleType.SUPER_ADMIN)) {
      const selectedRole = roles.includes(this.roleType.SUPER_ADMIN)
        ? this.roleType.SUPER_ADMIN
        : roles[0];

      this.setSelectedRole(selectedRole);
      this.setRoleSelectedStatus(true);
      this.router.navigate(['/jrm/dashboard']);
    } else {
      this.router.navigate(['/roles']);
    }
  }

  // redirectPostLogin(): void {
  //   this.userDetails = this.tokenService.parseJwt(); // update from token
  //   const roles: number[] = this.userDetails?.roleValue || [];

  //   console.log('User roles after login:', roles);

  //   if (roles.length === 1 || roles.includes(this.roleType.SUPER_ADMIN)) {
  //     const selectedRole = roles.includes(this.roleType.SUPER_ADMIN)
  //       ? this.roleType.SUPER_ADMIN
  //       : roles[0];

  //     this.setSelectedRole(selectedRole);
  //     this.setRoleSelectedStatus(true);

  //     console.log('Redirecting with selected role:', selectedRole);

  //     this.router.navigate(['/jrm/dashboard']);
  //   } else {
  //     this.router.navigate(['/roles']);
  //   }
  // }

  logout(): void {
    this.tokenService.clearToken();
    this.tokenService.clearRefreshToken();
    this.setUserLoginStatus(false);
    this.router.navigate(['/login']);
  }

  getCategoryLabel(value: number): string {
    switch (value) {
      case HostCategory.DEV:
        return HostCategoryNameEnum.DEV;
      case HostCategory.TEST:
        return HostCategoryNameEnum.TEST;
      case HostCategory.PROD:
        return HostCategoryNameEnum.PROD;
      case HostCategory.DB:
        return HostCategoryNameEnum.DB;
      case HostCategory.INFRA:
        return HostCategoryNameEnum.INFRA;
      case HostCategory.JPB_PROD_DR:
        return HostCategoryNameEnum.JPB_PROD_DR;
      case HostCategory.JPB_PROD:
        return HostCategoryNameEnum.JPB_PROD;
      default:
        return 'Unknown';
    }
  }

  showAlert(title: string, msg: string): void {
    // alert(msg);
    this.beautifyErrorMsg(msg);
    if (title === 'alert') {
      alert(msg);
    } else if (title === 'Success' || title === 'success') {
      this.toastService.show(msg, {
        classname: 'bg-success text-light',
        delay: 1000,
      });
    } else if (title === 'warning' || title === 'Warning') {
      this.toastService.show(msg, {
        classname: 'bg-warning text-light',
        delay: 3000,
      });
    } else if (title === 'Error' || title === 'error') {
      this.toastService.show(msg, {
        classname: 'bg-danger text-light',
        delay: 3000,
      });
    } else {
      this.toastService.show(msg);
    }
  }

  beautifyErrorMsg(msg: string): string {
    if (
      msg.includes('could not execute statement') ||
      msg.includes('org.hibernate.exception')
    ) {
      msg = 'Oops! There was an error while processing your request';
    }
    return msg;
  }

  getErrorMessage(
    form: FormGroup,
    fieldName: string,
    fieldLabel: string
  ): string {
    let msg = '';
    if (form.controls[fieldName].touched) {
      if (form.controls[fieldName].hasError('required')) {
        msg = fieldLabel + ' required';
      } else if (form.controls[fieldName].hasError('minlength')) {
        let errors: any = form.controls[fieldName]?.errors;
        msg =
          'Min length (' + errors['minlength']?.requiredLength + ') required';
      } else if (form.controls[fieldName].hasError('maxlength')) {
        let errors: any = form.controls[fieldName]?.errors;
        msg =
          'Max length (' + errors['maxlength']?.requiredLength + ') exceeded';
      } else if (form.controls[fieldName].hasError('min')) {
        let errors: any = form.controls[fieldName]?.errors;
        msg = fieldLabel + ' should be at least ' + errors['min']?.min +'MB';
      } else if (form.controls[fieldName].hasError('max')) {
        let errors: any = form.controls[fieldName]?.errors;
        msg = fieldLabel + ' should not exceed ' + errors['max']?.max +'MB';
      } else if (form.controls[fieldName].hasError('pattern')) {
        // msg = 'Please enter valid ' + fieldLabel + ' Pattern';
        msg = 'Please enter valid Pattern';
      } else if (form.controls[fieldName].hasError('passwordMismatch')) {
        msg = 'Password and Confirm Password Miss match';
      }
    }
    return msg;
  }

  /**
   * returns page limit list
   */
  getPageLimits() {
    let arr: number[];
    arr = [10, 25, 50, 100, 500, 1000];
    return arr;
  }

  /**
   * download file, with encrypted string
   * @param data - encrypted string
   * @param fileType - pdf, csv
   * @param name
   * @param linkType - url, encrypted string
   * call - this.globalService.downloadFile(linkOrData, 'pdf', fileName, 'url');
   */
  downloadFileCommon(
    data: Blob | string,
    fileType: string,
    name: string,
    linkType?: 'url' | 'blob'
  ): void {
    let linkSource: string;
    const fileName = `${name}.${fileType}`;

    if (linkType === 'url') {
      // When data is a direct URL
      linkSource = data as string;
      const downloadLink = document.createElement('a');
      downloadLink.href = linkSource;
      downloadLink.download = fileName;
      document.body.appendChild(downloadLink);
      downloadLink.click();
      document.body.removeChild(downloadLink);
    } else {
      // When data is Blob (from backend)
      const blob =
        data instanceof Blob
          ? data
          : new Blob([data], { type: 'application/octet-stream' });
      linkSource = window.URL.createObjectURL(blob);

      const downloadLink = document.createElement('a');
      downloadLink.href = linkSource;
      downloadLink.download = fileName;
      document.body.appendChild(downloadLink);
      downloadLink.click();

      // Cleanup
      document.body.removeChild(downloadLink);
      window.URL.revokeObjectURL(linkSource);
    }
  }
}
