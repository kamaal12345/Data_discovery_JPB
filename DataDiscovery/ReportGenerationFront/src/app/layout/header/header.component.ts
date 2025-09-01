import { Component, OnDestroy, OnInit } from '@angular/core';
import { SharedModule } from '../../../shared/shared.module';
import { GlobalService } from '../../core/services/global-service.service';
import { Router } from '@angular/router';
import { UsersService } from '../../core/services/users.service';
import { AuthenticationService } from '../../core/services/authentication.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { finalize, Subject, takeUntil } from 'rxjs';
import { Login } from '../../core/models/login';
import { RolesEnum } from '../../core/enums/Roles.enum';
import { LoginUser } from '../../core/models/LoginUser';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent implements OnInit, OnDestroy {
  userProfileImage: string;
  public destroy$: Subject<boolean>;
  loginDetails: Login;
  loginRoleType: number[];
  userDetails: LoginUser;
  roleTypes = RolesEnum;
  isLoading: boolean;
  showError: boolean;
  errorMsg: string;
  selectedRoleValue: number;

  constructor(
    public globalService: GlobalService,
    private router: Router,
    private usersService: UsersService,
    private authenticationService: AuthenticationService,
    private spinner: NgxUiLoaderService
  ) {
    this.destroy$ = new Subject<boolean>();
    this.loginDetails = {} as Login;
    this.loginDetails = globalService.getLoginDetails();
    this;
    this.isLoading = false;
    this.showError = false;
    this.selectedRoleValue = 0;
    this.errorMsg = '';
    this.userProfileImage = '';
  }

  ngOnInit(): void {
    this.selectedRoleValue = this.globalService.getSelectedRole();

    // Common method to set user info and image
    const initializeUserDetails = () => {
      this.userDetails = this.globalService.getLoginUserDetails();
      this.loginRoleType = this.userDetails.roleValue;
      this.getUserProfileImage(this.userDetails.userId);
    };

    // Subscribe to role selection changes
    this.globalService.userRoleSelectionStatus
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        if (value) {
          initializeUserDetails();
        }
      });

    // If already SUPER_ADMIN, initialize immediately
    if (this.selectedRoleValue === RolesEnum.SUPER_ADMIN) {
      initializeUserDetails();
    }
  }

  ngOnDestroy() {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  getUserProfileImage(userId: number) {
    this.usersService
      .getUserProfileImage(userId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {})
      )
      .subscribe({
        next: (data) => {
          if (data) {
            this.userProfileImage = data?.profileImg;
          }
        },
        error: (e) => console.error(e),
        complete: () => console.info('complete'),
      });
  }

  logout() {
    this.authenticationService.logout(this.loginDetails).subscribe({
      next: (resp: any) => {
        this.globalService.showAlert('Success', 'Logged Out Successfully!');
        this.globalService.logout();
      },
      error: (err) => {
        this.globalService.showAlert(
          'Error',
          'Logout failed. Please try again.'
        );
      },
    });
  }

  //   logout() {
  //   this.authenticationService.logout(this.loginDetails).subscribe(
  //     (resp: any) => {
  //       console.log(resp);
  //       this.globalService.showAlert('Success', 'Logged Out Successfully !');
  //       this.globalService.logout();
  //     }
  //   )
  // }

  userprofile() {
    this.router.navigate(['/jrm/user-profile']);
  }
}
