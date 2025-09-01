import { Injectable } from '@angular/core';
import { catchError, map, Observable } from 'rxjs';

import { ApiService } from './api.service';
import { GlobalService } from './global-service.service';
import { TokenService } from './token.service';
import { HttpHeaders } from '@angular/common/http';
import { Login } from '../models/login';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  constructor(
    private apiService: ApiService,
    private tokenService: TokenService,
    private globalService: GlobalService,
  ) { }

  login(body: any): Observable<any> {
    return this.apiService.post('/authenticate', body).pipe(
      map((res) => {
        if (res?.jwt) {
          this.tokenService.setToken(res.jwt);
          this.tokenService.setRefreshToken(res.refreshToken);
          this.globalService.setUserLoginStatus(true);
          return true;
        }
        this.tokenService.clearToken();
        this.tokenService.clearRefreshToken(); 
        this.globalService.setUserLoginStatus(false);
        return false;
      }));
      // catchError(err => this.apiService.handleError(err)));
  }

logout(body:Login): Observable<any> {
  const token = this.tokenService.getToken();
  const headers = new HttpHeaders({
    'Content-Type': 'application/json',
    Authorization: token || '',

    
  });

  return this.apiService.postWithCustomHeaders('/logout', body, headers);
}



  sessionLogout(body: any): Observable<any> {
    return this.apiService.post('/session-logout', body);
  }
}
