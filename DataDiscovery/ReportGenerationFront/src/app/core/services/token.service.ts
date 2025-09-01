import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class TokenService {
  constructor() {}

  private isBrowser(): boolean {
    return typeof window !== 'undefined' && typeof localStorage !== 'undefined';
  }

  getToken(): string | null {
    if (this.isBrowser()) {
      return localStorage.getItem('token');
    }
    return null;
  }

  setToken(token: string): void {
    if (this.isBrowser()) {
      localStorage.setItem('token', token);
    }
  }

  clearToken(): void {
    if (this.isBrowser()) {
      localStorage.removeItem('token');
    }
  }

  setRefreshToken(token: string): void {
  if (this.isBrowser()) {
    localStorage.setItem('refreshToken', token);
  }
}

getRefreshToken(): string | null {
  if (this.isBrowser()) {
    return localStorage.getItem('refreshToken');
  }
  return null;
}

clearRefreshToken(): void {
  if (this.isBrowser()) {
    localStorage.removeItem('refreshToken');
  }
}


  b64DecodeUnicode(str: string): string {
    return decodeURIComponent(
      Array.prototype.map.call(atob(str), c =>
        '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
      ).join('')
    );
  }

  parseJwt(): any {
    const token = this.getToken();
    if (!token) return null;

    return JSON.parse(
      this.b64DecodeUnicode(
        token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
      )
    );
  }

  //   getToken(): string {
  //   let token: any = localStorage.getItem('token');
  //   return token;
  // }

  // setToken(token: string) {
  //   localStorage.setItem('token', token);
  // }

  // clearToken(): void {
  //   localStorage.removeItem('token');
  // }

  // b64DecodeUnicode(str: any) {
  //   return decodeURIComponent(
  //     Array.prototype.map.call(atob(str), c =>
  //       '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
  //     ).join(''))
  // }

  // parseJwt() {
  //   let parsedToken = JSON.parse(
  //     this.b64DecodeUnicode(
  //       this.getToken().split('.')[1].replace('-', '+').replace('_', '/')
  //     )
  //   );
  //   return parsedToken;
  // }
}
