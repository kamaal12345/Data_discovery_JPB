import { Login } from '../core/models/login';
import { SharedModule } from '../../shared/shared.module';
import { finalize, Subject, takeUntil } from 'rxjs';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { GlobalService } from '../core/services/global-service.service';
import { AuthenticationService } from '../core/services/authentication.service';
import { SHA256 } from 'crypto-js';
import { TokenService } from '../core/services/token.service';
import { SessionLogoutRequest } from '../core/models/SessionLogoutRequest';
import { NgxUiLoaderModule, NgxUiLoaderService } from 'ngx-ui-loader';
// import * as CryptoJS from 'crypto-js';
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent implements OnInit, OnDestroy {
  loginDetails: Login;
  sessionLogoutRequest: SessionLogoutRequest;
  isLoading: boolean = false;
  destroy$: Subject<boolean> = new Subject<boolean>();
  userForm: FormGroup = new FormGroup({});
  showError: boolean;
  errorMsg: string;
  showSessionLogout: boolean;
  remainingTime: number;
  previousToken: any;
  // secretKey = 'Ganesh@#.12345678910';

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private globalService: GlobalService,
    private authenticationService: AuthenticationService,
    private tokenService: TokenService,
    private spinner: NgxUiLoaderService
  ) {
    this.loginDetails = {} as Login;
    this.showError = false;
    this.showSessionLogout = false;
    this.errorMsg = '';
  }

  ngOnInit(): void {
    this.userForm = this.fb.group({
      username: [
        '',
        [
          Validators.required,
          Validators.minLength(4),
          Validators.maxLength(128),
        ],
      ],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  login(): void {
    this.showError = false;
    this.showSessionLogout = false;

    if (!this.userForm.valid) {
      this.globalService.showAlert(
        'Error',
        'Please enter all mandatory fields !'
      );
      return;
    }

    this.loginDetails.username = this.userForm.controls['username'].value;

    let password = this.userForm.controls['password'].value;
    if (password && password.trim() !== '') {
      this.loginDetails.password = SHA256(password).toString();
    } else {
      this.loginDetails.password = password;
    }
    this.isLoading = true;
    this.spinner.start();
    this.authenticationService
      .login(this.loginDetails)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isLoading = false;
          this.spinner.stop();
        })
      )
      .subscribe({
        next: (data) => {
          console.log(data);
          if (data) {
            this.globalService.setLoginDetails(this.loginDetails);
            this.globalService.redirectPostLogin();
          } else {
            this.showError = true;
            this.errorMsg = 'Incorrect Username or Password';
          }
        },
        error: (e) => {
          console.log(e);
          this.showError = true;
          let msg: string;
          if (e.error.errorCode == 801) {
            this.showSessionLogout = true;
            msg = e.error.errorMessage + " / Previous session not Closed Properly";
            // this.previousToken = e.error.previousToken;
          } else if (e.error.errorCode == 601) {
            msg = e.error.errorMessage;
          } else {
            msg = e?.error?.message
              ? e?.error?.message
              : 'Oops! There was an error while processing your request !';
          }
          msg = this.globalService.beautifyErrorMsg(msg);
          this.errorMsg = msg;
          // this.globalService.showAlert('Error', msg);
        },
        complete: () => console.info('complete'),
      });
  }

  // public encrypt(password: string): string {
  //   return CryptoJS.AES.encrypt(password, this.secretKey).toString();
  // }

  // public decrypt(passwordToDecrypt: string) {
  //     return CryptoJS.AES.decrypt(passwordToDecrypt, this.secretKey).toString(CryptoJS.enc.Utf8);
  // }

  // redis based logout
  // sessionlogout(): void {
  //   this.showError = false;
  //   this.showSessionLogout = false;

  //   const token = this.previousToken;
  //   this.tokenService.setToken(token); // Store token so apiService picks it up

  //   const body = {
  //     username: this.loginDetails.username
  //   };

  //   this.authenticationService.sessionLogout(body).subscribe({
  //     next: (resp: any) => {
  //       if (resp?.message === 'User logged out and token blacklisted') {
  //         this.globalService.showAlert('Success', 'Successfully logged out!');
  //         this.tokenService.clearToken();
  //         this.router.navigate(['/login']);
  //       } else {
  //         this.showError = true;
  //         this.errorMsg = 'Error logging out';
  //       }
  //     },
  //     error: (err) => {
  //       console.error(err);
  //       this.showError = true;
  //       this.errorMsg = 'Error logging out';
  //     }
  //   });
  // }

  sessionlogout(): void {
    this.showError = false;
    this.showSessionLogout = false;

    this.spinner.start();

    this.authenticationService
      .sessionLogout(this.loginDetails)
      .pipe(finalize(() => this.spinner.stop()))
      .subscribe(
        (resp: any) => {
          console.log(resp);
          if (resp) {
            this.globalService.showAlert(
              'Success',
              'Successfully logged out from all sessions!'
            );
            this.globalService.logout();
          } else {
            this.showError = true;
            this.errorMsg = 'Incorrect Username or Password';
          }
        },
        (error) => {
          console.error(error);
          this.showError = true;
          this.errorMsg = 'Error logging out';
        }
      );
  }

  dashboard() {
    localStorage.setItem('token', '123');
    this.router.navigate(['/jrm/dashboard']);
  }
}
