import { Injectable } from '@angular/core';
import { EMPTY, Observable } from 'rxjs';
import {
  HttpClient,
  HttpErrorResponse,
  HttpHeaders,
  HttpParams,
} from '@angular/common/http';
import { GlobalService } from './global-service.service';
import { environment } from '../../../environments/environment';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  constructor(
    private _http: HttpClient,
    private globalService: GlobalService,
    private tokenService: TokenService
  ) {}

  /**
   * this method will set the request header
   * */
  private setHeaders(): HttpHeaders {
    const headerConfig: any = {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    };

    const token = this.tokenService.getToken();
    if (token) {
      headerConfig['Authorization'] = `${token}`;
    }

    return new HttpHeaders(headerConfig);
  }

  get(path: string, httpParams?: HttpParams): Observable<any> {
    return this._http.get(`${environment.api_url}${path}`, {
      headers: this.setHeaders(),
      params: httpParams,
    });
  }

  post(path: string, body: Object = {}): Observable<any> {
    return this._http.post(
      `${environment.api_url}${path}`,
      JSON.stringify(body),
      { headers: this.setHeaders() }
    );
  }

  put(path: string, body: Object = {}): Observable<any> {
    return this._http.put(
      `${environment.api_url}${path}`,
      JSON.stringify(body),
      { headers: this.setHeaders() }
    );
  }

  delete(path: string, httpParams?: HttpParams): Observable<any> {
    return this._http.delete(`${environment.api_url}${path}`, {
      headers: this.setHeaders(),
      params: httpParams,
    });
  }

postWithCustomHeaders(path: string, body: Object = {}, headers: HttpHeaders): Observable<any> {
  return this._http.post(
    `${environment.api_url}${path}`,
    JSON.stringify(body),
    { headers }
  );
}


  getNotificationsUrl(): { url: string; headers: HttpHeaders } {
    const url = `${environment.api_url}`;
    const headers = this.setHeaders();
    return { url, headers };
  }

  /**
   * Special GET method to download files like Excel (returns Blob)
   * @param path - API endpoint path
   * @returns Observable<Blob>
   */
  downloadFile(path: string): Observable<Blob> {
    const headers = this.setHeaders();
    return this._http.get(`${environment.api_url}${path}`, {
      headers: headers,
      responseType: 'blob' as 'blob',
    });
  }

  public handleError(error: HttpErrorResponse) {
    if (error.error instanceof Error) {
      console.error('An error occurred:', error.error.message);
    } else {
      console.error(
        `Backend returned code ${error.status}, body was: ${error.error}`
      );
      if (error.status == 401) {
        this.globalService.showAlert('alert', 'Session expired !');
      }
    }
    return EMPTY;
  }
}
